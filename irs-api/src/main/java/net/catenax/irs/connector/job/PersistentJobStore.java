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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.util.JsonUtil;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

/**
 * Stores Job data using persistent blob storage.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PersistentJobStore implements JobStore {

    /**
     * The prefix for job IDs used as key in the blobstore
     */
    private static final String JOB_PREFIX = "job:";
    /**
     * The timeout in milliseconds to try to acquire locks.
     */
    private static final int TIMEOUT = 30_000;

    /**
     * A lock to synchronize access to the collection of stored jobs.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final BlobPersistence blobStore;

    private final JsonUtil json = new JsonUtil();

    @Override
    public Optional<MultiTransferJob> find(final String jobId) {
        return readLock(() -> {
            try {
                final byte[] blob = blobStore.getBlob(toBlobId(jobId));
                if (blob == null) {
                    return Optional.empty();
                }
                return Optional.of(toJob(blob));
            } catch (BlobPersistenceException e) {
                log.error("Error while trying to get job from blobstore", e);
                return Optional.empty();
            }
        });
    }

    private MultiTransferJob toJob(final byte[] blob) {
        return json.fromString(new String(blob, StandardCharsets.UTF_8), MultiTransferJob.class);
    }

    @Override
    public List<MultiTransferJob> findByStateAndCompletionDateOlderThan(final JobState jobState,
            final LocalDateTime localDateTime) {
        try {
            final Collection<byte[]> allBlobs = blobStore.findBlobByPrefix(JOB_PREFIX);
            return allBlobs.stream()
                           .map(this::toJob)
                           .filter(job -> job.getState().equals(jobState))
                           .filter(job -> job.getCompletionDate()
                                             .map(date -> date.isBefore(localDateTime))
                                             .orElse(false))
                           .collect(Collectors.toList());
        } catch (BlobPersistenceException e) {
            log.error("Cannot search for jobs in blobstore", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<MultiTransferJob> findByProcessId(final String processId) {
        try {
            final Collection<byte[]> allBlobs = blobStore.findBlobByPrefix(JOB_PREFIX);
            return allBlobs.stream()
                           .map(this::toJob)
                           .filter(j -> j.getTransferProcessIds().contains(processId))
                           .findFirst();
        } catch (BlobPersistenceException e) {
            log.error("Cannot search for jobs in blobstore", e);
            return Optional.empty();
        }
    }

    @Override
    public void create(final MultiTransferJob job) {
        writeLock(() -> {
            final MultiTransferJob initialJob = job.toBuilder().transitionInitial().build();
            final byte[] blob = toBlob(initialJob);
            try {
                blobStore.putBlob(toBlobId(job.getJobId()), blob);
            } catch (BlobPersistenceException e) {
                log.error("Cannot create job in BlobStore", e);
            }
            return initialJob;
        });
    }

    private byte[] toBlob(final MultiTransferJob job) {
        final String jobString = this.json.asString(job);
        log.info(jobString);
        return jobString.getBytes(StandardCharsets.UTF_8);
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
            final byte[] jobBlob;
            try {
                jobBlob = blobStore.getBlob(toBlobId(jobId));
                if (jobBlob == null) {
                    log.warn("Job not found: {}", jobId);
                } else {
                    final MultiTransferJob job = toJob(jobBlob);
                    blobStore.putBlob(toBlobId(job.getJobId()), toBlob(action.apply(job)));
                }
            } catch (BlobPersistenceException e) {
                throw new JobException(e);
            }
            return null;
        });
    }

    private String toBlobId(final String jobId) {
        return JOB_PREFIX + jobId;
    }

    @Override
    public MultiTransferJob deleteJob(final String jobId) {
        return writeLock(() -> {
            try {
                final byte[] blob = blobStore.getBlob(toBlobId(jobId));
                blobStore.delete(toBlobId(jobId));
                return toJob(blob);
            } catch (BlobPersistenceException e) {
                throw new JobException(e);
            }
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
