/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.connector.batch;

import static org.eclipse.tractusx.irs.configuration.JobConfiguration.JOB_BLOB_PERSISTENCE;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Manages storage of {@link BatchOrder} using persistent blob storage.
 */
@Service
@Slf4j
public class PersistentBatchOrderStore implements BatchOrderStore {

    private static final String BATCH_ORDER_PREFIX = "order:";

    private final JsonUtil json = new JsonUtil();


    private final BlobPersistence blobStore;

    public PersistentBatchOrderStore(@Qualifier(JOB_BLOB_PERSISTENCE) final BlobPersistence blobStore) {
        this.blobStore = blobStore;
    }

    @Override
    public void save(final UUID batchOrderId, final BatchOrder batchOrder) {
        final byte[] blob = toBlob(batchOrder);
        try {
            blobStore.putBlob(toBlobId(batchOrderId.toString()), blob);
        } catch (BlobPersistenceException e) {
            log.error("Cannot create Batch Order in BlobStore", e);
        }
    }

    @Override
    public Optional<BatchOrder> find(final UUID batchOrderId) {
        try {
            return blobStore.getBlob(toBlobId(batchOrderId.toString())).flatMap(this::toBatchOrder);
        } catch (BlobPersistenceException e) {
            log.error("Error while trying to get Batch Order from blobstore", e);
            return Optional.empty();
        }
    }

    @Override
    public List<BatchOrder> findAll() {
        try {
            final Collection<byte[]> allBlobs = blobStore.findBlobByPrefix(BATCH_ORDER_PREFIX);
            return allBlobs.stream().map(this::toBatchOrder).flatMap(Optional::stream).toList();
        } catch (BlobPersistenceException e) {
            log.error("Cannot search for Batch Order in blobstore", e);
            return Collections.emptyList();
        }
    }

    private Optional<BatchOrder> toBatchOrder(final byte[] blob) {
        try {
            return Optional.of(json.fromString(new String(blob, StandardCharsets.UTF_8), BatchOrder.class));
        } catch (JsonParseException exception) {
            log.warn("Stored Batch Order could not be parsed to Batch Order object.");
            return Optional.empty();
        }
    }

    private byte[] toBlob(final BatchOrder batchOrder) {
        final String batchOrderString = this.json.asString(batchOrder);
        return batchOrderString.getBytes(StandardCharsets.UTF_8);
    }

    private String toBlobId(final String batchOrderId) {
        return BATCH_ORDER_PREFIX + batchOrderId;
    }
}
