//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.job;

import lombok.RequiredArgsConstructor;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Manages storage of {@link MultiTransferJob} state in memory with no persistence.
 */
@RequiredArgsConstructor
@SuppressWarnings({"PMD.GuardLogStatement", "PMD.TooManyMethods"}) // Monitor doesn't offer guard statements
public class InMemoryJobStore implements JobStore {

    /**
     * The timeout in milliseconds to try to acquire locks.
     */
    private static final int TIMEOUT = 30_000;
    /**
     * Logger.
     */
    private final Monitor monitor;
    /**
     * A lock to synchronize access to the collection of stored jobs.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * The collection of stored jobs.
     */
    @SuppressWarnings("PMD.UseConcurrentHashMap") // externally synchronized
    private final Map<String, MultiTransferJob> jobsById = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MultiTransferJob> find(final String jobId) {
        return readLock(() -> Optional.ofNullable(jobsById.get(jobId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(final MultiTransferJob job) {
        writeLock(() -> {
            final var newJob = job.toBuilder().transitionInitial().build();
            jobsById.put(job.getJobId(), newJob);
            return null;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTransferProcess(final String jobId, final String processId) {
        modifyJob(jobId, (job) -> {
            final var newJob = job.toBuilder()
                    .transferProcessId(processId)
                    .transitionInProgress()
                    .build();
            return newJob;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void completeTransferProcess(final String jobId, final TransferProcess process) {
        modifyJob(jobId, (job) -> {
            final var remainingTransfers = job.getTransferProcessIds().stream().filter(id -> !id.equals(process.getId())).collect(Collectors.toList());
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void completeJob(final String jobId) {
        modifyJob(jobId, job -> job.toBuilder().transitionComplete().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markJobInError(final String jobId, final @Nullable String errorDetail) {
        modifyJob(jobId, job -> job.toBuilder().transitionError(errorDetail).build());
    }

    private void modifyJob(final String jobId, final UnaryOperator<MultiTransferJob> action) {
        writeLock(() -> {
            final var job = jobsById.get(jobId);
            if (job == null) {
                monitor.warning("Job not found: " + jobId);
            } else {
                jobsById.put(job.getJobId(), action.apply(job));
            }
            return null;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MultiTransferJob> findByProcessId(final String processId) {
        return jobsById.values().stream()
                .filter(j -> j.getTransferProcessIds().contains(processId))
                .findFirst();
    }

    private <T> T readLock(final Supplier<T> work) {
        try {
            if (!lock.readLock().tryLock(TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new EdcException("Timeout acquiring read lock");
            }
            try {
                return work.get();
            } finally {
                lock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EdcException(e);
        }
    }

    private <T> T writeLock(final Supplier<T> work) {
        try {
            if (!lock.writeLock().tryLock(TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new EdcException("Timeout acquiring write lock");
            }
            try {
                return work.get();
            } finally {
                lock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EdcException(e);
        }
    }
}
