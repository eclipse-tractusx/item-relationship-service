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
package org.eclipse.tractusx.irs.services;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.BatchOrder;
import org.eclipse.tractusx.irs.connector.batch.BatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.eclipse.tractusx.irs.connector.batch.JobProgress;
import org.eclipse.tractusx.irs.services.events.BatchOrderProcessingFinishedEvent;
import org.eclipse.tractusx.irs.services.events.BatchOrderRegisteredEvent;
import org.eclipse.tractusx.irs.services.events.BatchProcessingFinishedEvent;
import org.eclipse.tractusx.irs.services.timeouts.TimeoutSchedulerBatchProcessingService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TimeoutSchedulerBatchProcessingService timeoutScheduler;

    @Async
    @EventListener
    public void handleBatchOrderRegisteredEvent(final BatchOrderRegisteredEvent batchOrderRegisteredEvent) {
        log.info("Listener received BatchOrderRegisteredEvent with BatchOrderId: {}.",
                batchOrderRegisteredEvent.batchOrderId());
        batchOrderStore.find(batchOrderRegisteredEvent.batchOrderId())
                       .ifPresent(batchOrder -> batchStore.findAll()
                                                          .stream()
                                                          .filter(batch -> batch.getBatchOrderId()
                                                                                .equals(batchOrder.getBatchOrderId()))
                                                          .filter(batch -> batch.getBatchNumber().equals(1))
                                                          .findFirst()
                                                          .ifPresent(batch -> startBatch(batchOrder, batch)));
    }

    @Async
    @EventListener
    public void handleBatchProcessingFinishedEvent(final BatchProcessingFinishedEvent batchEvent) {
        log.info(
                "Listener received BatchProcessingFinishedEvent with BatchId: {}, BatchOrderId: {} and BatchNumber: {}",
                batchEvent.batchId(), batchEvent.batchOrderId(), batchEvent.batchNumber());
        batchOrderStore.find(batchEvent.batchOrderId()).ifPresent(batchOrder -> {
            final List<ProcessingState> batchStates = batchStore.findAll()
                                                                .stream()
                                                                .filter(batch -> batch.getBatchOrderId()
                                                                                      .equals(batchOrder.getBatchOrderId()))
                                                                .map(Batch::getBatchState)
                                                                .toList();
            final ProcessingState batchOrderState = calculateBatchOrderState(batchStates);
            batchOrder.setBatchOrderState(batchOrderState);
            batchOrderStore.save(batchOrder.getBatchOrderId(), batchOrder);
            if (ProcessingState.COMPLETED.equals(batchOrderState) || ProcessingState.ERROR.equals(batchOrderState)) {
                applicationEventPublisher.publishEvent(
                        new BatchOrderProcessingFinishedEvent(batchOrder.getBatchOrderId(),
                                batchOrder.getBatchOrderState(), batchOrder.getCallbackUrl()));
            } else {
                batchStore.findAll()
                          .stream()
                          .filter(batch -> batch.getBatchOrderId().equals(batchOrder.getBatchOrderId()))
                          .filter(batch -> batch.getBatchNumber().equals(batchEvent.batchNumber() + 1))
                          .findFirst()
                          .ifPresent(batch -> startBatch(batchOrder, batch));
            }
        });
    }

    private void startBatch(final BatchOrder batchOrder, final Batch batch) {
        // here we use only globalAssetId
        final List<JobProgress> createdJobIds = batch.getJobProgressList()
                                                     .stream()
                                                     .map(JobProgress::getIdentificationKey)
                                                     .map(identificationKey -> createRegisterJob(batchOrder, identificationKey))
                                                     .map(registerJob -> createJobProgress(
                                                             irsItemGraphQueryService.registerItemJob(registerJob,
                                                                     batch.getBatchId()),
                                                             registerJob.getKey()))
                                                     .toList();
        batch.setJobProgressList(createdJobIds);
        batch.setStartedOn(ZonedDateTime.now(ZoneOffset.UTC));
        batchStore.save(batch.getBatchId(), batch);
        timeoutScheduler.registerBatchTimeout(batch.getBatchId(), batchOrder.getTimeout());
        timeoutScheduler.registerJobsTimeout(createdJobIds.stream().map(JobProgress::getJobId).toList(),
                batchOrder.getJobTimeout());
    }

    private JobProgress createJobProgress(final JobHandle jobHandle, final PartChainIdentificationKey identificationKey) {
        return JobProgress.builder()
                          .jobId(jobHandle.getId())
                          .jobState(JobState.INITIAL)
                          .identificationKey(identificationKey)
                          .build();
    }

    private RegisterJob createRegisterJob(final BatchOrder batchOrder, final PartChainIdentificationKey identificationKey) {
        return RegisterJob.builder()
                          .key(identificationKey)
                          .bomLifecycle(batchOrder.getBomLifecycle())
                          .aspects(batchOrder.getAspects())
                          .depth(batchOrder.getDepth())
                          .direction(batchOrder.getDirection())
                          .collectAspects(batchOrder.getCollectAspects())
                          .lookupBPNs(batchOrder.getLookupBPNs())
                          .callbackUrl(batchOrder.getCallbackUrl())
                          .build();
    }

    private ProcessingState calculateBatchOrderState(final List<ProcessingState> stateList) {
        if (stateList.stream().anyMatch(ProcessingState.PROCESSING::equals)) {
            return ProcessingState.PROCESSING;
        } else if (stateList.stream().anyMatch(ProcessingState.ERROR::equals)) {
            return ProcessingState.ERROR;
        } else if (stateList.stream().anyMatch(ProcessingState.PARTIAL::equals)) {
            return ProcessingState.PARTIAL;
        } else if (stateList.stream().allMatch(ProcessingState.COMPLETED::equals)) {
            return ProcessingState.COMPLETED;
        } else {
            return ProcessingState.PARTIAL;
        }
    }

}
