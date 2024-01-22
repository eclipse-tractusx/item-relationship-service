/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.services.timeouts;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class TimeoutSchedulerBatchProcessingServiceTest {

    private final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    private final CancelBatchProcessingService cancelBatchProcessingService = mock(CancelBatchProcessingService.class);
    private final TimeoutSchedulerBatchProcessingService timeoutScheduler = new TimeoutSchedulerBatchProcessingService(scheduler, cancelBatchProcessingService);

    @Test
    void shouldCancelJobsFromList() {
        // given
        UUID firstJobId = UUID.randomUUID();
        UUID secondJobId = UUID.randomUUID();

        // when
        timeoutScheduler.registerJobsTimeout(List.of(firstJobId, secondJobId), 3);

        // then
        verify(scheduler).schedule(any((Runnable.class)), eq(3L), eq(TimeUnit.SECONDS));
    }

    @Test
    void shouldCancelJobsFromBatch() {
        // given
        UUID batchId = UUID.randomUUID();

        // when
        timeoutScheduler.registerBatchTimeout(batchId, 5);

        // then
        verify(scheduler).schedule(any((Runnable.class)), eq(5L), eq(TimeUnit.SECONDS));
    }
}