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
package org.eclipse.tractusx.irs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.irs.common.auth.SecurityHelperService;
import org.eclipse.tractusx.irs.component.BatchOrderResponse;
import org.eclipse.tractusx.irs.component.BatchResponse;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.BatchOrder;
import org.eclipse.tractusx.irs.connector.batch.BatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.eclipse.tractusx.irs.connector.batch.InMemoryBatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.InMemoryBatchStore;
import org.eclipse.tractusx.irs.connector.batch.JobProgress;
import org.eclipse.tractusx.irs.connector.job.InMemoryJobStore;
import org.eclipse.tractusx.irs.connector.job.JobStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryBatchServiceTest {

    private BatchOrderStore batchOrderStore;
    private BatchStore batchStore;
    private JobStore jobStore;

    private final SecurityHelperService securityHelperService = mock(SecurityHelperService.class);
    private QueryBatchService service;

    private static final String owner = "TestUser";

    @BeforeEach
    void beforeEach() {
        batchOrderStore = new InMemoryBatchOrderStore();
        batchStore = new InMemoryBatchStore();
        jobStore = new InMemoryJobStore();

        service = new QueryBatchService(batchOrderStore, batchStore, jobStore, securityHelperService);
    }

    @Test
    void shouldFindBatchOrder() {
        // given
        final UUID batchOrderId = UUID.randomUUID();
        batchOrderStore.save(batchOrderId, createBatchOrder(batchOrderId));
        when(securityHelperService.getClientIdForViewIrs()).thenReturn(owner);

        final UUID batchId = UUID.randomUUID();
        batchStore.save(batchId, createBatch(batchId, batchOrderId));

        // when
        final BatchOrderResponse response = service.findOrderById(batchOrderId);

        // then
        assertThat(response.getOrderId()).isEqualTo(batchOrderId);
        assertThat(response.getBatches()).hasSize(1);
    }

    @Test
    void shouldFindBatchOrderWithAdminRole() {
        // given
        final UUID batchOrderId = UUID.randomUUID();
        batchOrderStore.save(batchOrderId, createBatchOrder(batchOrderId));
        when(securityHelperService.isAdmin()).thenReturn(true);

        final UUID batchId = UUID.randomUUID();
        batchStore.save(batchId, createBatch(batchId, batchOrderId));

        // when
        final BatchOrderResponse response = service.findOrderById(batchOrderId);

        // then
        assertThat(response.getOrderId()).isEqualTo(batchOrderId);
        assertThat(response.getBatches()).hasSize(1);
    }

    @Test
    void shouldFindBatch() {
        // given
        final UUID batchOrderId = UUID.randomUUID();

        final UUID batchId = UUID.randomUUID();
        final UUID secondBatchId = UUID.randomUUID();
        batchStore.save(batchId, createBatch(batchId, batchOrderId));
        batchStore.save(secondBatchId, createBatch(secondBatchId, batchOrderId));

        when(securityHelperService.getClientIdForViewIrs()).thenReturn(owner);

        // when
        final BatchResponse response = service.findBatchById(batchOrderId, batchId);

        // then
        assertThat(response.getOrderId()).isEqualTo(batchOrderId);
        assertThat(response.getBatchId()).isEqualTo(batchId);
        assertThat(response.getJobsInBatchChecksum()).isEqualTo(3);
        assertThat(response.getTotalJobs()).isEqualTo(6);
    }

    @Test
    void shouldFindBatchWithAdminRole() {
        // given
        final UUID batchOrderId = UUID.randomUUID();
        final UUID batchId = UUID.randomUUID();
        batchStore.save(batchId, createBatch(batchId, batchOrderId));

        when(securityHelperService.isAdmin()).thenReturn(true);

        // when
        final BatchResponse response = service.findBatchById(batchOrderId, batchId);

        // then
        assertThat(response.getOrderId()).isEqualTo(batchOrderId);
        assertThat(response.getBatchId()).isEqualTo(batchId);
    }

    private BatchOrder createBatchOrder(final UUID batchOrderId) {
        return BatchOrder.builder().batchOrderId(batchOrderId).owner(owner).build();
    }

    private Batch createBatch(final UUID batchId, final UUID batchOrderId) {
        return Batch.builder().batchId(batchId).batchOrderId(batchOrderId).owner(owner)
                    .jobProgressList(List.of(
                            JobProgress.builder().jobId(UUID.randomUUID()).build(),
                            JobProgress.builder().jobId(UUID.randomUUID()).build(),
                            JobProgress.builder().jobId(UUID.randomUUID()).build()
                    )).build();
    }

}