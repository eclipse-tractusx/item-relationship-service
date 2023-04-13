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
package org.eclipse.tractusx.irs.services.timeouts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TimeoutSchedulerBatchProcessingServiceTest {

    private CancelBatchProcessingService cancelBatchProcessingService;
    private TimeoutSchedulerBatchProcessingService timeoutScheduler;

    @BeforeEach
    void beforeEach() {
        cancelBatchProcessingService = mock(CancelBatchProcessingService.class);
        timeoutScheduler = new TimeoutSchedulerBatchProcessingService(
                Executors.newSingleThreadScheduledExecutor(), cancelBatchProcessingService);

    }

    @Test
    void shouldCancelJobsFromList() {
        // given
        UUID firstJobId = UUID.randomUUID();
        UUID secondJobId = UUID.randomUUID();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);

        // when
        timeoutScheduler.registerJobsTimeout(List.of(firstJobId, secondJobId), 0);

        // then
        verify(cancelBatchProcessingService).cancelNotFinishedJobs(listArgumentCaptor.capture());
        assertThat(listArgumentCaptor.getValue()).containsExactly(firstJobId, secondJobId);
    }

    @Test
    void shouldCancelJobsFromBatch() {
        // given
        UUID batchId = UUID.randomUUID();

        ArgumentCaptor<UUID> batchIdCaptor = ArgumentCaptor.forClass(UUID.class);

        // when
        timeoutScheduler.registerBatchTimeout(batchId, 0);

        // then
        verify(cancelBatchProcessingService).cancelNotFinishedJobsInBatch(batchIdCaptor.capture());
        assertThat(batchIdCaptor.getValue()).isEqualTo(batchId);
    }
}