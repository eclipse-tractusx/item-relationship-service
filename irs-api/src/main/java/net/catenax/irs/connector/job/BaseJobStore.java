//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.job;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.enums.JobState;
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

    protected abstract Collection<MultiTransferJob> getAll();

    protected abstract void put(String jobId, MultiTransferJob job);

    protected abstract Optional<MultiTransferJob> remove(String jobId);

    @Override
    public Optional<MultiTransferJob> find(final String jobId) {
        return readLock(() -> get(jobId));
    }

    @Override
    public List<MultiTransferJob> findByStateAndCompletionDateOlderThan(final JobState jobState,
            final ZonedDateTime dateTime) {
        return readLock(() -> getAll().stream()
                                      .filter(hasState(jobState))
                                      .filter(isCompletionDateBefore(dateTime))
                                      .collect(Collectors.toList()));
    }

    private Predicate<MultiTransferJob> hasState(final JobState jobState) {
        return job -> job.getJob().getJobState().equals(jobState);
    }

    private Predicate<MultiTransferJob> isCompletionDateBefore(final ZonedDateTime localDateTime) {
        return job -> {
            final ZonedDateTime completed = job.getJob().getJobCompleted();
            return completed != null && completed.isBefore(localDateTime);
        };
    }

    @Override
    public Optional<MultiTransferJob> findByProcessId(final String processId) {
        return getAll().stream().filter(j -> j.getTransferProcessIds().contains(processId)).findFirst();
    }

    @Override
    public void create(final MultiTransferJob job) {
        writeLock(() -> {
            final var newJob = job.toBuilder().transitionInitial().build();
            log.info("Adding new job into jobstore: {}", newJob);
            put(job.getJobIdString(), newJob);
            return null;
        });
    }

    @Override
    public void addTransferProcess(final String jobId, final String processId) {
        log.info("Adding transfer process {} to job {}", processId, jobId);
        modifyJob(jobId, job -> job.toBuilder().transferProcessId(processId).transitionInProgress().build());
    }

    @Override
    public List<MultiTransferJob> findAll() {
        return readLock(() -> getAll().stream().collect(Collectors.toList()));
    }

    @Override
    public void completeTransferProcess(final String jobId, final TransferProcess process) {
        log.info("Completing transfer process {} for job {}", process.getId(), jobId);
        modifyJob(jobId, job -> {
            final var remainingTransfers = job.getTransferProcessIds()
                                              .stream()
                                              .filter(id -> !id.equals(process.getId()))
                                              .collect(Collectors.toList());
            final var newJob = job.toBuilder()
                                  .clearTransferProcessIds()
                                  .transferProcessIds(remainingTransfers)
                                  .completedTransfer(process);
            if (remainingTransfers.isEmpty()) {
                log.info("Job {} has no remaining transfers, transitioning to TRANSFERS_FINISHED", jobId);
                newJob.transitionTransfersFinished();
            } else {
                log.info("Job {} has {} remaining transfers, cannot finish it: {}", jobId, remainingTransfers.size(),
                        newJob.build());
            }
            return newJob.build();
        });
    }

    @Override
    public void completeJob(final String jobId, final Consumer<MultiTransferJob> completionAction) {
        log.info("Completing job {}", jobId);
        modifyJob(jobId, job -> {
            final JobState jobState = job.getJob().getJobState();
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
        return readLock(() -> getAll().stream().filter(hasState(jobStates)).collect(Collectors.toList()));
    }

    private Predicate<MultiTransferJob> hasState(final List<JobState> jobStates) {
        return job -> jobStates.contains(job.getJob().getJobState());
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

    private <T> T readLock(final Supplier<T> work) {
        try {
            if (!lock.readLock().tryLock(TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new JobException("Timeout acquiring read lock");
            }
            try {
                return work.get();
            } finally {
                lock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JobException("Job Interrupted", e);
        }
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
