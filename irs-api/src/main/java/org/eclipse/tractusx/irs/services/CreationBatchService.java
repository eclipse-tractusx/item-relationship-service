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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.IrsApplication;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterBatchOrder;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.configuration.IrsConfiguration;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.BatchOrder;
import org.eclipse.tractusx.irs.connector.batch.BatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.eclipse.tractusx.irs.connector.batch.JobProgress;
import org.eclipse.tractusx.irs.services.events.BatchOrderRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CreationBatchService {

    private final BatchOrderStore batchOrderStore;
    private final BatchStore batchStore;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JobEventLinkedQueueListener jobEventLinkedQueueListener;
    private final IrsConfiguration irsConfiguration;

    public UUID create(final RegisterBatchOrder request) {
        final UUID batchOrderId = UUID.randomUUID();
        final BatchOrder batchOrder = BatchOrder.builder()
                                                .batchOrderId(batchOrderId)
                                                .batchOrderState(ProcessingState.INITIALIZED)
                                                .bomLifecycle(request.getBomLifecycle())
                                                .aspects(request.getAspects())
                                                .depth(request.getDepth())
                                                .direction(request.getDirection())
                                                .collectAspects(request.isCollectAspects())
                                                .lookupBPNs(request.isLookupBPNs())
                                                .timeout(request.getTimeout())
                                                .jobTimeout(request.getJobTimeout())
                                                .callbackUrl(request.getCallbackUrl())
                                                .build();

        // need to use whole key
        final List<Batch> batches = createBatches(request.getKeys().stream().toList(),
                request.getBatchSize(), batchOrderId);
        batchOrderStore.save(batchOrderId, batchOrder);
        batches.forEach(batch -> {
            batchStore.save(batch.getBatchId(), batch);
            jobEventLinkedQueueListener.addQueueForBatch(batch.getBatchId(), batch.getJobProgressList().size());
        });
        applicationEventPublisher.publishEvent(new BatchOrderRegisteredEvent(batchOrderId));
        return batchOrderId;
    }

    public List<Batch> createBatches(final List<PartChainIdentificationKey> keys, final int batchSize, final UUID batchOrderId) {
        final List<List<PartChainIdentificationKey>> globalAssetIdsBatches = Lists.partition(keys, batchSize);

        final AtomicInteger batchNumber = new AtomicInteger(1);

        return globalAssetIdsBatches.stream().map(batch -> {
            final UUID batchId = UUID.randomUUID();
            return Batch.builder()
                        .batchId(batchId)
                        .batchOrderId(batchOrderId)
                        .batchNumber(batchNumber.getAndIncrement())
                        .batchTotal(globalAssetIdsBatches.size())
                        .batchUrl(buildBatchUrl(batchOrderId, batchId))
                        .batchState(ProcessingState.INITIALIZED)
                        .jobProgressList(batch.stream()
                                              .map(identificationKey -> JobProgress.builder()
                                                                               .identificationKey(identificationKey)
                                                                               .jobState(JobState.UNSAVED)
                                                                               .build())
                                              .toList())
                        .build();
        }).toList();
    }

    private String buildBatchUrl(final UUID batchOrderId, final UUID batchId) {
        return String.join("/", irsConfiguration.getApiUrl().toString(), IrsApplication.API_PREFIX, "orders",
                batchOrderId.toString(), "batches", batchId.toString());
    }

}
