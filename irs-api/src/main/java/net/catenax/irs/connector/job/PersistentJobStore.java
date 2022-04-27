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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.util.JsonUtil;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Stores Job data using persistent blob storage.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PersistentJobStore extends BaseJobStore {

    /**
     * The prefix for job IDs used as key in the blobstore
     */
    private static final String JOB_PREFIX = "job:";

    private final BlobPersistence blobStore;

    private final JsonUtil json = new JsonUtil();

    @Override
    protected Optional<MultiTransferJob> get(final String jobId) {
        try {
            return blobStore.getBlob(toBlobId(jobId)).map(this::toJob);
        } catch (BlobPersistenceException e) {
            log.error("Error while trying to get job from blobstore", e);
            return Optional.empty();
        }
    }

    @Override
    protected Collection<MultiTransferJob> getAll() {
        try {
            final Collection<byte[]> allBlobs = blobStore.findBlobByPrefix(JOB_PREFIX);
            return allBlobs.stream().map(this::toJob).collect(Collectors.toList());
        } catch (BlobPersistenceException e) {
            log.error("Cannot search for jobs in blobstore", e);
            return Collections.emptyList();
        }
    }

    @Override
    protected void put(final String jobId, final MultiTransferJob job) {
        final byte[] blob = toBlob(job);
        try {
            blobStore.putBlob(toBlobId(jobId), blob);
        } catch (BlobPersistenceException e) {
            log.error("Cannot create job in BlobStore", e);
        }
    }

    @Override
    protected Optional<MultiTransferJob> remove(final String jobId) {
        try {
            final Optional<byte[]> blob = blobStore.getBlob(toBlobId(jobId));
            blobStore.delete(toBlobId(jobId));
            return blob.map(this::toJob);
        } catch (BlobPersistenceException e) {
            throw new JobException("Blob persistence error", e);
        }
    }

    private MultiTransferJob toJob(final byte[] blob) {
        return json.fromString(new String(blob, StandardCharsets.UTF_8), MultiTransferJob.class);
    }

    private byte[] toBlob(final MultiTransferJob job) {
        final String jobString = this.json.asString(job);
        return jobString.getBytes(StandardCharsets.UTF_8);
    }

    private String toBlobId(final String jobId) {
        return JOB_PREFIX + jobId;
    }

}
