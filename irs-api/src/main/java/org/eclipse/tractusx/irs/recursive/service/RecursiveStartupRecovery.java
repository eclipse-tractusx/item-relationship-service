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

import java.util.concurrent.Executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Resumes open recursive jobs once after startup, so a pod restart does not leave accepted jobs
 * idling until their deadline. Runs on the recursive job executor to keep startup fast; a failing
 * recovery only logs - the timeout sweeper remains the safety net.
 */
@Slf4j
@RequiredArgsConstructor
public class RecursiveStartupRecovery {

    private final RecursiveJobService jobService;
    private final Executor recursiveJobExecutor;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverAfterRestart() {
        recursiveJobExecutor.execute(() -> {
            try {
                final int resumed = jobService.recoverOpenJobs();
                if (resumed > 0) {
                    log.info("Resumed {} recursive job(s) after restart", resumed);
                }
            } catch (final RuntimeException e) {
                log.warn("Recursive restart recovery failed: causeType={}", e.getClass().getName());
            }
        });
    }
}
