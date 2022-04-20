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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
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
            final LocalDateTime localDateTime) {
        return readLock(() -> getAll().stream()
                                      .filter(hasState(jobState))
                                      .filter(isCompletionDateBefore(localDateTime))
                                      .collect(Collectors.toList()));
    }

    private Predicate<MultiTransferJob> hasState(final JobState jobState) {
        return job -> job.getState().equals(jobState);
    }

    private Predicate<MultiTransferJob> isCompletionDateBefore(final LocalDateTime localDateTime) {
        return job -> job.getCompletionDate().isPresent() && job.getCompletionDate().get().isBefore(localDateTime);
    }

    @Override
    public Optional<MultiTransferJob> findByProcessId(final String processId) {
        return getAll().stream().filter(j -> j.getTransferProcessIds().contains(processId)).findFirst();
    }

    @Override
    public void create(final MultiTransferJob job) {
        writeLock(() -> {
            final var newJob = job.toBuilder().transitionInitial().build();
            put(job.getJobId(), newJob);
            return null;
        });
    }

    @Override
    public void addTransferProcess(final String jobId, final String processId) {
        modifyJob(jobId, job -> job.toBuilder().transferProcessId(processId).transitionInProgress().build());
    }

    @Override
    public void completeTransferProcess(final String jobId, final TransferProcess process) {
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
                newJob.transitionTransfersFinished();
            }
            return newJob.build();
        });
    }

    @Override
    public void completeJob(final String jobId) {
        modifyJob(jobId, job -> job.toBuilder().transitionComplete().build());
    }

    @Override
    public void markJobInError(final String jobId, @Nullable final String errorDetail) {
        modifyJob(jobId, job -> job.toBuilder().transitionError(errorDetail).build());
    }

    @Override
    public Optional<MultiTransferJob> deleteJob(final String jobId) {
        return writeLock(() -> remove(jobId));
    }

    private void modifyJob(final String jobId, final UnaryOperator<MultiTransferJob> action) {
        writeLock(() -> {
            final var job = get(jobId);
            if (job.isEmpty()) {
                log.warn("Job not found: {}", jobId);
            } else {
                final MultiTransferJob multiTransferJob = job.get();
                put(multiTransferJob.getJobId(), action.apply(multiTransferJob));
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
            throw new JobException(e);
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
            throw new JobException(e);
        }
    }
}
