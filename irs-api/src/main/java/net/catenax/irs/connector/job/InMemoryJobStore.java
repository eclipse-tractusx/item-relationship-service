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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages storage of {@link MultiTransferJob} state in memory with no persistence.
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.PreserveStackTrace" })

public class InMemoryJobStore extends BaseJobStore {

    /**
     * The timeout in milliseconds to try to acquire locks.
     */
    private static final int TIMEOUT = 30_000;

    /**
     * A lock to synchronize access to the collection of stored jobs.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * The collection of stored jobs.
     */
    @SuppressWarnings("PMD.UseConcurrentHashMap") // externally synchronized
    private final Map<String, MultiTransferJob> jobsById = new HashMap<>();

    @Override
    protected Optional<MultiTransferJob> get(final String jobId) {
        return Optional.ofNullable(jobsById.get(jobId));
    }

    @Override

    protected Collection<MultiTransferJob> getAll() {
        return jobsById.values();
    }

    @Override
    protected void put(final String jobId, final MultiTransferJob job) {
        jobsById.put(jobId, job);
    }

    @Override

    protected Optional<MultiTransferJob> remove(final String jobId) {
        return Optional.ofNullable(jobsById.remove(jobId));
    }

}
