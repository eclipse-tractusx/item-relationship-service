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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.eclipse.tractusx.irs.component.BatchOrderResponse;
import org.eclipse.tractusx.irs.services.QueryBatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryBatchServiceTest {

    private BatchOrderStore batchOrderStore;
    private BatchStore batchStore;
    private QueryBatchService service;

    @BeforeEach
    void beforeEach() {
        batchOrderStore = new InMemoryBatchOrderStore();
        batchStore = new InMemoryBatchStore();
        service = new QueryBatchService(batchOrderStore, batchStore);
    }

    @Test
    void shouldFindBatchOrder() {
        // given
        final UUID batchOrderId = UUID.randomUUID();
        batchOrderStore.save(batchOrderId, createBatchOrder(batchOrderId));

        final UUID batchId = UUID.randomUUID();
        batchStore.save(batchId, createBatch(batchId, batchOrderId));

        // when
        final BatchOrderResponse response = service.findById(batchOrderId);

        // then
        assertThat(response.getOrderId()).isEqualTo(batchOrderId);
        assertThat(response.getBatches()).hasSize(1);

    }

    private BatchOrder createBatchOrder(final UUID batchOrderId) {
        return BatchOrder.builder().build();
    }

    private Batch createBatch(final UUID batchId, final UUID batchOrderId) {
        return Batch.builder().batchId(batchId).batchOrderId(batchOrderId).build();
    }

}