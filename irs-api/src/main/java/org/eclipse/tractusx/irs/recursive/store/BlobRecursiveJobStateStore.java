/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.store;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;
import org.eclipse.tractusx.irs.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MinIO / S3-backed implementation of {@link RecursiveJobStateStore}.
 *
 * <h3>Blob key scheme</h3>
 * <pre>
 *   recursive-job:{jobId}                 -> JSON of RecursiveJobState
 *   recursive-msg-in:{messageId}          -> child jobId for duplicate incoming REQUESTs
 *   recursive-msg-out:{messageId}         -> parent jobId for incoming RESPONSE correlation
 * </pre>
 *
 * <p>All recursive data lives in the same bucket as regular IRS jobs
 * ({@code irs-jobs} by default) but with distinct key prefixes,
 * so no additional MinIO configuration is needed.</p>
 *
 * <h3>Crash recovery</h3>
 * <p>After a pod restart, all job states and messageId->jobId mappings are
 * read back from blob storage on the first access. No in-memory warmup needed.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class BlobRecursiveJobStateStore implements RecursiveJobStateStore {

    /** Prefix for job state blobs. */
    /* package */ static final String JOB_PREFIX = "recursive-job:";

    /** Prefix for incoming child request messageId -> child jobId mapping blobs. */
    /* package */ static final String INCOMING_REQUEST_MSG_PREFIX = "recursive-msg-in:";

    /** Prefix for outgoing child request messageId -> parent jobId mapping blobs. */
    /* package */ static final String CHILD_REQUEST_MSG_PREFIX = "recursive-msg-out:";

    private final BlobPersistence blobPersistence;
    private final JsonUtil jsonUtil;

    @Override
    public void save(final RecursiveJobState state) {
        try {
            final String json = jsonUtil.asString(state);
            blobPersistence.putBlob(JOB_PREFIX + state.getJobId(),
                    json.getBytes(StandardCharsets.UTF_8));
            if (log.isDebugEnabled()) {
                log.debug("Saved recursive job state: jobId={}, phase={}",
                        RecursiveLogValue.of(state.getJobId().toString()), state.getState());
            }
        } catch (final BlobPersistenceException | JsonParseException e) {
            throw new RecursiveStoreException("Failed to save recursive job", e);
        }
    }

    @Override
    public Optional<RecursiveJobState> findById(final UUID jobId) {
        try {
            return blobPersistence.getBlob(JOB_PREFIX + jobId)
                    .map(bytes -> jsonUtil.fromString(new String(bytes, StandardCharsets.UTF_8),
                            RecursiveJobState.class));
        } catch (final BlobPersistenceException | JsonParseException e) {
            throw new RecursiveStoreException("Failed to read recursive job", e);
        }
    }

    @Override
    public Optional<UUID> findJobIdByIncomingRequestMessageId(final String messageId) {
        return findMessageMapping(INCOMING_REQUEST_MSG_PREFIX, messageId);
    }

    @Override
    public void registerIncomingRequestMessageId(final String messageId, final UUID jobId) {
        registerMessageMapping(INCOMING_REQUEST_MSG_PREFIX, messageId, jobId);
    }

    @Override
    public Optional<UUID> findJobIdByChildRequestMessageId(final String messageId) {
        return findMessageMapping(CHILD_REQUEST_MSG_PREFIX, messageId);
    }

    @Override
    public void registerChildRequestMessageId(final String messageId, final UUID jobId) {
        registerMessageMapping(CHILD_REQUEST_MSG_PREFIX, messageId, jobId);
    }

    private Optional<UUID> findMessageMapping(final String prefix, final String messageId) {
        if (messageId == null) {
            return Optional.empty();
        }
        try {
            return blobPersistence.getBlob(prefix + messageId)
                    .map(bytes -> UUID.fromString(new String(bytes, StandardCharsets.UTF_8).trim()));
        } catch (final BlobPersistenceException | IllegalArgumentException e) {
            throw new RecursiveStoreException("Failed to read recursive message mapping", e);
        }
    }

    private void registerMessageMapping(final String prefix, final String messageId, final UUID jobId) {
        if (messageId == null) {
            return;
        }
        try {
            blobPersistence.putBlob(prefix + messageId, jobId.toString().getBytes(StandardCharsets.UTF_8));
        } catch (final BlobPersistenceException e) {
            throw new RecursiveStoreException("Failed to register recursive message mapping", e);
        }
    }

    @Override
    public List<RecursiveJobState> findAll() {
        try {
            final Collection<byte[]> blobs = RecursiveBlobStoreAccess.findBlobByPrefix(blobPersistence, JOB_PREFIX);
            return blobs.stream()
                    .map(bytes -> jsonUtil.fromString(new String(bytes, StandardCharsets.UTF_8),
                            RecursiveJobState.class))
                    .toList();
        } catch (final BlobPersistenceException | JsonParseException e) {
            throw new RecursiveStoreException("Failed to list recursive jobs", e);
        }
    }
}
