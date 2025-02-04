/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.connector.job;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all JobStores, implementing the Job transition logic and handling locking.
 */
@Slf4j
@SuppressWarnings("PMD.TooManyMethods")
public abstract class BaseJobStore implements JobStore {

    /**
     * The timeout in milliseconds to try to acquire locks.
     */
    private static final int TIMEOUT = 30_000;

    /**
     * A lock to synchronize access to the collection of stored jobs.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected abstract Optional<MultiTransferJob> get(String jobId);

    protected abstract Optional<MultiTransferJob> getByProcessId(String processId);

    protected abstract Collection<MultiTransferJob> getAll();

    protected abstract void put(String jobId, MultiTransferJob job);

    protected abstract Optional<MultiTransferJob> remove(String jobId);

    @Override
    public Optional<MultiTransferJob> find(final String jobId) {
        return get(jobId);
    }

    @Override
    public List<MultiTransferJob> findByStateAndCompletionDateOlderThan(final JobState jobState,
            final ZonedDateTime dateTime) {
        return getAll().stream().filter(hasState(jobState)).filter(isCompletionDateBefore(dateTime)).toList();
    }

    private Predicate<MultiTransferJob> hasState(final JobState jobState) {
        return job -> job.getJob().getState().equals(jobState);
    }

    private Predicate<MultiTransferJob> isCompletionDateBefore(final ZonedDateTime localDateTime) {
        return job -> {
            final ZonedDateTime completed = job.getJob().getCompletedOn();
            return completed != null && completed.isBefore(localDateTime);
        };
    }

    @Override
    public Optional<MultiTransferJob> findByProcessId(final String processId) {
        return getByProcessId(processId);
    }

    @Override
    public void create(final MultiTransferJob job) {
        final var newJob = job.toBuilder().transitionInitial().build();
        log.info("Adding new job into jobstore: {}", newJob);
        put(job.getJobIdString(), newJob);
    }

    @Override
    public void addTransferProcess(final String jobId, final String processId) {
        log.info("Adding transfer process {} to job {}", processId, jobId);
        modifyJob(jobId, job -> job.toBuilder().transferProcessId(processId).transitionInProgress().build());
    }

    @Override
    public List<MultiTransferJob> findAll() {
        return new ArrayList<>(getAll());
    }

    @Override
    public void completeTransferProcess(final String jobId, final TransferProcess process) {
        log.info("Completing transfer process {} for job {}", process.getId(), jobId);
        modifyJob(jobId, job -> {
            final var remainingTransfers = job.getTransferProcessIds()
                                              .stream()
                                              .filter(id -> !id.equals(process.getId()))
                                              .toList();
            final var newJob = job.toBuilder()
                                  .clearTransferProcessIds()
                                  .transferProcessIds(remainingTransfers)
                                  .completedTransfer(process);
            if (remainingTransfers.isEmpty()) {
                log.info("Job {} has no remaining transfers, transitioning to TRANSFERS_FINISHED", jobId);
                newJob.transitionTransfersFinished();
            } else {
                log.info("Job {} has {} remaining transfers, cannot finish it.", jobId, remainingTransfers.size());
            }
            return newJob.build();
        });
    }

    @Override
    public void completeJob(final String jobId, final Consumer<MultiTransferJob> completionAction) {
        log.info("Completing job {}", jobId);
        modifyJob(jobId, job -> {
            final JobState jobState = job.getJob().getState();
            if (jobState == JobState.TRANSFERS_FINISHED || jobState == JobState.INITIAL) {
                completionAction.accept(job);
                return job.toBuilder().transitionComplete().build();
            } else {
                log.info("Job is in state {}, cannot complete it.", jobState);
                return job;
            }
        });
    }

    @Override
    public void markJobInError(final String jobId, @Nullable final String errorDetail,
            final String exceptionClassName) {
        modifyJob(jobId, job -> job.toBuilder().transitionError(errorDetail, exceptionClassName).build());
    }

    @Override
    public List<MultiTransferJob> findByStates(final List<JobState> jobStates) {
        return getAll().stream().filter(hasState(jobStates)).toList();
    }

    private Predicate<MultiTransferJob> hasState(final List<JobState> jobStates) {
        return job -> jobStates.contains(job.getJob().getState());
    }

    @Override
    public Optional<MultiTransferJob> deleteJob(final String jobId) {
        return writeLock(() -> remove(jobId));
    }

    @Override
    public Optional<MultiTransferJob> cancelJob(final String jobId) {
        modifyJob(jobId, job -> job.toBuilder().transitionCancel().build());

        return this.get(jobId);
    }

    private void modifyJob(final String jobId, final UnaryOperator<MultiTransferJob> action) {
        writeLock(() -> {
            final var job = get(jobId);
            if (job.isEmpty()) {
                log.warn("Job not found: {}", jobId);
            } else {
                final MultiTransferJob multiTransferJob = job.get();
                put(multiTransferJob.getJobIdString(), action.apply(multiTransferJob));
            }
            return null;
        });
    }

    private <T> T writeLock(final Supplier<T> work) {
        try {
            if (!lock.writeLock().tryLock(TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new JobException("Timeout acquiring write lock");
            }
            try {
                return work.get();
            } finally {
                lock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JobException("Job Interrupted", e);
        }
    }
}
