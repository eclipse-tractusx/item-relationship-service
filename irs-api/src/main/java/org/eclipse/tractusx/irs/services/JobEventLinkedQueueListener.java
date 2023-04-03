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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.aaswrapper.job.JobProcessingFinishedEvent;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.BatchOrder;
import org.eclipse.tractusx.irs.connector.batch.BatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.eclipse.tractusx.irs.connector.batch.JobProgress;
import org.eclipse.tractusx.irs.services.events.BatchProcessingFinishedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobEventLinkedQueueListener {

    private final Map<UUID, LimitedJobEventQueue> queueMap = new ConcurrentHashMap<>();

    private final BatchOrderStore batchOrderStore;
    private final BatchStore batchStore;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void addQueueForBatch(final UUID batchId, final Integer size) {
        queueMap.put(batchId, new LimitedJobEventQueue(new ConcurrentLinkedQueue<>(), size));
    }

    @Async
    @EventListener
    public void handleJobProcessingFinishedEvent(final JobProcessingFinishedEvent jobEvent) {
        log.info("Listener received JobProcessingFinishedEvent with JobId: {}, JobState: {} and BatchId: {}",
                jobEvent.jobId(), jobEvent.jobState(), jobEvent.batchId());

        jobEvent.batchId().ifPresent(batchId -> Optional.ofNullable(queueMap.get(batchId)).ifPresent(queue -> {
            queue.linkedQueue.add(jobEvent);
            checkSize(batchId, queue);
        }));
    }

    private void checkSize(final UUID batchId, final LimitedJobEventQueue queue) {
        if (queue.hasReachLimit()) {
            log.info("BatchId: {} reached size to update status and check state.", batchId);
            batchStore.find(batchId).ifPresent(batch -> {
                final List<JobProgress> progressList = batch.getJobProgressList();
                queue.linkedQueue.forEach(event -> progressList.stream()
                                               .filter(jobProgress -> jobProgress.getJobId().toString().equals(event.jobId()))
                                               .findFirst()
                                               .ifPresent(jobProgress -> jobProgress.setJobState(event.jobState())));
                final ProcessingState processingState = calculateProcessingState(progressList);
                log.info("BatchId: {} reached {} state.", batchId, processingState);

                batch.setBatchState(processingState);
                batch.setJobProgressList(progressList);

                batchStore.save(batchId, batch);
                queueMap.remove(batchId);

                if (ProcessingState.COMPLETE.equals(processingState) || ProcessingState.ERROR.equals(processingState)) {
                    log.info("BatchId: {} finished processing.", batchId);
                    processingCompleted(batch, processingState);
                }
            });
        }
    }

    private void processingCompleted(final Batch batch, final ProcessingState processingState) {
        batch.setCompletedOn(ZonedDateTime.now());
        final Optional<BatchOrder> batchOrder = batchOrderStore.find(batch.getBatchOrderId());

        final String callbackUrl = batchOrder.map(BatchOrder::getCallbackUrl).orElse("");
        final ProcessingState orderState = batchOrder.map(BatchOrder::getBatchOrderState).orElse(null);

        applicationEventPublisher.publishEvent(
                new BatchProcessingFinishedEvent(batch.getBatchOrderId(), batch.getBatchId(), orderState,
                        processingState, batch.getBatchNumber(), callbackUrl));
    }

    private ProcessingState calculateProcessingState(final List<JobProgress> progressList) {
        if (progressList.stream().anyMatch(jobProgress -> JobState.RUNNING.equals(jobProgress.getJobState()))) {
            return ProcessingState.PROCESSING;
        } else if (progressList.stream().anyMatch(jobProgress -> JobState.ERROR.equals(jobProgress.getJobState()))) {
            return ProcessingState.PARTIAL;
        } else if (progressList.stream()
                               .allMatch(jobProgress -> JobState.COMPLETED.equals(jobProgress.getJobState()))) {
            return ProcessingState.COMPLETE;
        } else {
            return ProcessingState.PARTIAL;
        }
    }

    record LimitedJobEventQueue(ConcurrentLinkedQueue<JobProcessingFinishedEvent> linkedQueue, Integer limit) {
        private boolean hasReachLimit() {
            return linkedQueue.size() == limit;
        }
    }

}
