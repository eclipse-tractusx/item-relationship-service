/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.recursive.config.RecursiveProperties;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Periodically closes recursive jobs that are waiting for child responses past their deadline.
 */
@Slf4j
@RequiredArgsConstructor
public class RecursiveTimeoutMonitor implements SchedulingConfigurer {

    private final RecursiveJobService jobService;
    private final RecursiveProperties recursiveProperties;

    public void checkTimeouts() {
        final int processedJobs = jobService.processExpiredJobs();
        if (processedJobs > 0) {
            log.info("Completed {} recursive job(s) with expired deadlines", processedJobs);
        }
    }

    @Override
    public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addFixedDelayTask(this::checkTimeouts,
                recursiveProperties.getTimeout().getTimeoutCheckInterval().toMillis());
    }
}
