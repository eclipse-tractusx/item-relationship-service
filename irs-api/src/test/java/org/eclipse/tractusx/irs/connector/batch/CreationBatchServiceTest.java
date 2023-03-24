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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.tractusx.irs.component.RegisterBatchOrder;
import org.eclipse.tractusx.irs.component.enums.BatchStrategy;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.services.CreationBatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreationBatchServiceTest {

    public static final String FIRST_GLOBAL_ASSET_ID = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0";
    public static final String SECOND_GLOBAL_ASSET_ID = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b1";
    private BatchOrderStore batchOrderStore;
    private BatchStore batchStore;
    private CreationBatchService service;

    @BeforeEach
    void beforeEach() {
        batchOrderStore = new InMemoryBatchOrderStore();
        batchStore = new InMemoryBatchStore();
        service = new CreationBatchService(batchOrderStore, batchStore);
    }

    @Test
    void shouldStoreBatchOrder() {
        // given
        final RegisterBatchOrder registerBatchOrder = exampleBatchRequest();

        // when
        final UUID batchOrderId = service.create(registerBatchOrder);

        // then
        assertThat(batchOrderStore.findAll()).hasSize(1);
        assertThat(batchStore.findAll()).hasSize(1);

        Batch actual = batchStore.findAll().stream().findFirst().orElseThrow();
        assertThat(actual.getGlobalAssetIds()).containsOnly(FIRST_GLOBAL_ASSET_ID, SECOND_GLOBAL_ASSET_ID);
    }

    @Test
    void shouldSplitGlobalAssetIdIntoBatches() {
        // given
        final List<String> globalAssetIds = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18" , "19" , "20");
        final int batchSize = 3;

        // when
        final List<Batch> batches = service.createBatches(globalAssetIds, batchSize, UUID.randomUUID());

        // then
        assertThat(batches).hasSize(7);
        assertThat(batches.get(0).getGlobalAssetIds()).containsExactly("1", "2", "3");
        assertThat(batches.get(6).getGlobalAssetIds()).containsExactly("19", "20");

    }

    private static RegisterBatchOrder exampleBatchRequest() {
        return RegisterBatchOrder.builder()
                                 .globalAssetIds(Set.of(FIRST_GLOBAL_ASSET_ID, SECOND_GLOBAL_ASSET_ID))
                                 .bomLifecycle(BomLifecycle.AS_PLANNED)
                                 .aspects(List.of("aspects"))
                                 .depth(1)
                                 .direction(Direction.DOWNWARD)
                                 .collectAspects(true)
                                 .timeout(1000)
                                 .jobTimeout(500)
                                 .batchStrategy(BatchStrategy.PRESERVE_JOB_ORDER)
                                 .callbackUrl("exampleUrl.com")
                                 .batchSize(10)
                                 .build();
    }

}