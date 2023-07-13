/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.connector.batch;

import static org.eclipse.tractusx.irs.configuration.JobConfiguration.JOB_BLOB_PERSISTENCE;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Manages storage of {@link Batch} using persistent blob storage.
 */
@Service
@Slf4j
public class PersistentBatchStore implements BatchStore {

    private static final String BATCH_PREFIX = "batch:";

    private final JsonUtil json = new JsonUtil();

    private final BlobPersistence blobStore;

    public PersistentBatchStore(@Qualifier(JOB_BLOB_PERSISTENCE) final BlobPersistence blobStore) {
        this.blobStore = blobStore;
    }

    @Override
    public void save(final UUID batchId, final Batch batch) {
        final byte[] blob = toBlob(batch);
        try {
            blobStore.putBlob(toBlobId(batchId.toString()), blob);
        } catch (BlobPersistenceException e) {
            log.error("Cannot create Batch in BlobStore", e);
        }
    }

    @Override
    public Optional<Batch> find(final UUID batchId) {
        try {
            return blobStore.getBlob(toBlobId(batchId.toString())).flatMap(this::toBatch);
        } catch (BlobPersistenceException e) {
            log.error("Error while trying to Batch from blobstore", e);
            return Optional.empty();
        }
    }

    @Override
    public List<Batch> findAll() {
        try {
            final Collection<byte[]> allBlobs = blobStore.findBlobByPrefix(BATCH_PREFIX);
            return allBlobs.stream().map(this::toBatch).flatMap(Optional::stream).toList();
        } catch (BlobPersistenceException e) {
            log.error("Cannot search for Batch in blobstore", e);
            return Collections.emptyList();
        }
    }

    private Optional<Batch> toBatch(final byte[] blob) {
        try {
            return Optional.of(json.fromString(new String(blob, StandardCharsets.UTF_8), Batch.class));
        } catch (JsonParseException exception) {
            log.warn("Stored Batch could not be parsed to Batch object.");
            return Optional.empty();
        }
    }

    private byte[] toBlob(final Batch batch) {
        final String batchString = this.json.asString(batch);
        return batchString.getBytes(StandardCharsets.UTF_8);
    }

    private String toBlobId(final String batchId) {
        return BATCH_PREFIX + batchId;
    }
}
