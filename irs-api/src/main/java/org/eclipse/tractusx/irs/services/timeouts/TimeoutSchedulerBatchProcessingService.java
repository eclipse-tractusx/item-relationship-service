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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Schedule timeouts in Batch Process.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TimeoutSchedulerBatchProcessingService {

    private final ScheduledExecutorService scheduler;
    private final CancelBatchProcessingService cancelBatchProcessingService;

    public void registerJobsTimeout(final List<UUID> jobIds, final Integer timeoutInSeconds) {
        log.info("Register job timeout {} seconds for jobIds: {}", timeoutInSeconds, jobIds);
        scheduler.schedule(() -> cancelBatchProcessingService.cancelNotFinishedJobs(jobIds), timeoutInSeconds, TimeUnit.SECONDS);
    }

    public void registerBatchTimeout(final UUID batchId, final Integer timeoutInSeconds) {
        log.info("Register batch timeout {} seconds for batchId: {}", timeoutInSeconds, batchId);
        scheduler.schedule(() -> cancelBatchProcessingService.cancelNotFinishedJobsInBatch(batchId), timeoutInSeconds, TimeUnit.SECONDS);
    }

}
