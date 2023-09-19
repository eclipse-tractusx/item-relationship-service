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

import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PersistentBatchOrderStoreTest {

    private final UUID BATCH_ORDER_ID = UUID.randomUUID();
    private final BlobPersistence blobStore = Mockito.mock(BlobPersistence.class);
    private final JsonUtil json = new JsonUtil();
    private final PersistentBatchOrderStore store = new PersistentBatchOrderStore(blobStore);

    @Test
    void shouldSaveBatchOrder() throws BlobPersistenceException {
        // given
        final BatchOrder batchOrder = BatchOrder.builder().batchOrderId(BATCH_ORDER_ID).batchOrderState(ProcessingState.INITIALIZED).build();
        // when
        store.save(BATCH_ORDER_ID, batchOrder);
        // then
        verify(blobStore).putBlob(eq("order:" + BATCH_ORDER_ID), any());
    }

    @Test
    void shouldHandleBlobPersistenceExceptionOnSave() throws BlobPersistenceException {
        // given
        final BatchOrder batchOrder = BatchOrder.builder().batchOrderId(BATCH_ORDER_ID).batchOrderState(ProcessingState.INITIALIZED).build();
        willThrow(new BlobPersistenceException("message", new Exception()))
                .given(blobStore).putBlob(eq("order:" + BATCH_ORDER_ID), any());
        // when + then
        assertDoesNotThrow(() -> store.save(BATCH_ORDER_ID, batchOrder));
    }

    @Test
    void shouldGetBatchOrderById() throws BlobPersistenceException {
        // given
        final BatchOrder expected = BatchOrder.builder().batchOrderId(BATCH_ORDER_ID).batchOrderState(ProcessingState.INITIALIZED).build();
        given(blobStore.getBlob("order:" + BATCH_ORDER_ID)).willReturn(Optional.of(toBlob(expected)));
        // when
        final Optional<BatchOrder> actual = store.find(BATCH_ORDER_ID);
        // then
        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyWhenBlobPersistenceExceptionOnFindBy() throws BlobPersistenceException {
        // given
        willThrow(new BlobPersistenceException("message", new Exception())).given(blobStore).getBlob(eq("order:" + BATCH_ORDER_ID));
        // when
        final Optional<BatchOrder> actual = store.find(BATCH_ORDER_ID);
        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindAllBatchOrdersInStore() throws BlobPersistenceException {
        // given
        final BatchOrder firstOrder = BatchOrder.builder().batchOrderId(BATCH_ORDER_ID).batchOrderState(ProcessingState.INITIALIZED).build();
        final BatchOrder secondOrder = BatchOrder.builder().batchOrderId(UUID.randomUUID()).batchOrderState(ProcessingState.INITIALIZED).build();
        final BatchOrder thirdOrder = BatchOrder.builder().batchOrderId(UUID.randomUUID()).batchOrderState(ProcessingState.INITIALIZED).build();
        given(blobStore.findBlobByPrefix("order:")).willReturn(List.of(
                toBlob(firstOrder),
                toBlob(secondOrder),
                toBlob(thirdOrder)
        ));
        // when
        final List<BatchOrder> actual = store.findAll();
        // then
        assertThat(actual).hasSize(3);
        assertThat(actual.get(0))
                .usingRecursiveComparison()
                .isEqualTo(firstOrder);
    }

    @Test
    void shouldReturnEmptyListWhenBlobPersistenceExceptionOnFindAll() throws BlobPersistenceException {
        // given
        willThrow(new BlobPersistenceException("message", new Exception())).given(blobStore).findBlobByPrefix("order:");
        // when
        final List<BatchOrder> actual = store.findAll();
        // then
        assertThat(actual).isEmpty();
    }

    private byte[] toBlob(final BatchOrder batchOrder) {
        final String batchOrderString = this.json.asString(batchOrder);
        return batchOrderString.getBytes(StandardCharsets.UTF_8);
    }

}