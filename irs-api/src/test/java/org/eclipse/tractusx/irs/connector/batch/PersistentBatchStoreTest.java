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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PersistentBatchStoreTest {

    private final UUID BATCH_ID = UUID.randomUUID();
    private final BlobPersistence blobStore = Mockito.mock(BlobPersistence.class);
    private final JsonUtil json = new JsonUtil();
    private final PersistentBatchStore store = new PersistentBatchStore(blobStore);

    @Test
    void shouldSaveBatch() throws BlobPersistenceException {
        // given
        final Batch batch = Batch.builder().batchId(BATCH_ID).batchState(ProcessingState.PARTIAL).build();
        // when
        store.save(BATCH_ID, batch);
        // then
        verify(blobStore).putBlob(eq("batch:" + BATCH_ID), any());
    }

    @Test
    void shouldHandleBlobPersistenceExceptionOnSave() throws BlobPersistenceException {
        // given
        final Batch batch = Batch.builder().batchId(BATCH_ID).batchState(ProcessingState.PARTIAL).build();
        willThrow(new BlobPersistenceException("message", new Exception()))
                .given(blobStore).putBlob(eq("batch:" + BATCH_ID), any());
        // when + then
        assertDoesNotThrow(() -> store.save(BATCH_ID, batch));
    }

    @Test
    void shouldGetBatchById() throws BlobPersistenceException {
        // given
        final Batch expected = Batch.builder().batchId(BATCH_ID).batchState(ProcessingState.PARTIAL).build();
        given(blobStore.getBlob("batch:" + BATCH_ID)).willReturn(Optional.of(toBlob(expected)));
        // when
        final Optional<Batch> actual = store.find(BATCH_ID);
        // then
        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyWhenBlobPersistenceExceptionOnFindBy() throws BlobPersistenceException {
        // given
        willThrow(new BlobPersistenceException("message", new Exception())).given(blobStore).getBlob(eq("batch:" + BATCH_ID));
        // when
        final Optional<Batch> actual = store.find(BATCH_ID);
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindAllBatchesInStore() throws BlobPersistenceException {
        // given
        final Batch firstBatch = Batch.builder().batchId(BATCH_ID).batchState(ProcessingState.PARTIAL).build();
        final Batch secondBatch = Batch.builder().batchId(UUID.randomUUID()).batchState(ProcessingState.PARTIAL).build();
        final Batch thirdBatch = Batch.builder().batchId(UUID.randomUUID()).batchState(ProcessingState.PARTIAL).build();
        given(blobStore.findBlobByPrefix("batch:")).willReturn(List.of(
                toBlob(firstBatch),
                toBlob(secondBatch),
                toBlob(thirdBatch)
        ));
        // when
        final List<Batch> actual = store.findAll();
        // then
        assertThat(actual).hasSize(3);
        assertThat(actual.get(0))
                .usingRecursiveComparison()
                .isEqualTo(firstBatch);
    }

    @Test
    void shouldReturnEmptyListWhenBlobPersistenceExceptionOnFindAll() throws BlobPersistenceException {
        // given
        willThrow(new BlobPersistenceException("message", new Exception())).given(blobStore).findBlobByPrefix("batch:");
        // when
        final List<Batch> actual = store.findAll();
        // then
        assertThat(actual).isEmpty();
    }

    private byte[] toBlob(final Batch batch) {
        final String batchString = this.json.asString(batch);
        return batchString.getBytes(StandardCharsets.UTF_8);
    }

}