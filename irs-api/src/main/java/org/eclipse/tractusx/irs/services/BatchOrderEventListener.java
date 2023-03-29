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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.aaswrapper.job.JobProcessingFinishedEvent;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.BatchOrder;
import org.eclipse.tractusx.irs.connector.batch.BatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.eclipse.tractusx.irs.connector.batch.JobProgress;
import org.eclipse.tractusx.irs.services.events.BatchOrderRegisteredEvent;
import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * Event Listener to handle registration and processing of Batches and Batch Orders
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BatchOrderEventListener {

    private final BatchOrderStore batchOrderStore;
    private final BatchStore batchStore;
    private final IrsItemGraphQueryService irsItemGraphQueryService;

    @Async
    @EventListener
    public void handleBatchOrderRegisteredEvent(final BatchOrderRegisteredEvent batchOrderRegisteredEvent) {
        batchOrderStore.find(batchOrderRegisteredEvent.batchOrderId()).ifPresent(batchOrder -> {
            final Batch firstBatch = batchStore.findAll()
                                         .stream()
                                         .filter(batch -> batch.getBatchOrderId().equals(batchOrder.getBatchOrderId()))
                                         .filter(batch -> batch.getBatchNumber().equals(1))
                                         .findFirst()
                                         .orElseThrow();

            final List<JobProgress> createdJobIds = firstBatch.getJobProgressList()
                                                        .stream()
                                                        .map(JobProgress::getGlobalAssetId)
                                                        .map(globalAssetId -> createRegisterJob(batchOrder,
                                                                globalAssetId))
                                                        .map(registerJob -> createJobProgress(
                                                                irsItemGraphQueryService.registerItemJob(registerJob,
                                                                        firstBatch.getBatchId()),
                                                                registerJob.getGlobalAssetId()))
                                                        .collect(Collectors.toList());
            firstBatch.setJobProgressList(createdJobIds);
            firstBatch.setStartedOn(ZonedDateTime.now(ZoneOffset.UTC));
            batchStore.save(firstBatch.getBatchId(), firstBatch);
        });

    }

    private JobProgress createJobProgress(final JobHandle jobHandle, final String globalAssetId) {
        return JobProgress.builder()
                          .jobId(jobHandle.getId())
                          .jobState(JobState.INITIAL)
                          .globalAssetId(globalAssetId)
                          .build();
    }

    @Async
    @EventListener
    public void handleJobProcessingFinishedEvent(final JobProcessingFinishedEvent jobEvent) {
        jobEvent.batchId().ifPresent(batchId -> {
            batchStore.find(batchId).ifPresent(batch -> {
                batch.getJobProgressList().stream()
                     .filter(jobProgress -> jobProgress.getJobId().toString().equals(jobEvent.jobId()))
                        .forEach(jobProgress -> jobProgress.setJobState(jobEvent.jobState()));
                batchStore.save(batchId, batch);
            });
        });
    }

    private RegisterJob createRegisterJob(final BatchOrder batchOrder, final String globalAssetId) {
        return RegisterJob.builder()
                          .globalAssetId(globalAssetId)
                          .bomLifecycle(batchOrder.getBomLifecycle())
                          .aspects(batchOrder.getAspects())
                          .depth(batchOrder.getDepth())
                          .direction(batchOrder.getDirection())
                          .collectAspects(batchOrder.getCollectAspects())
                          .callbackUrl(batchOrder.getCallbackUrl())
                          .build();
    }

}
