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

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.RegisterBatchOrder;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.BatchOrder;
import org.eclipse.tractusx.irs.connector.batch.BatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreationBatchService {

    private final BatchOrderStore batchOrderStore;
    private final BatchStore batchStore;

    public UUID create(RegisterBatchOrder request) {
        // TODO: simple split - need to use strategy
        final UUID batchOrderId = UUID.randomUUID();
        final BatchOrder batchOrder = BatchOrder.builder()
                                                .batchOrderId(batchOrderId)
                                                .batchOrderState(ProcessingState.INITIALIZED)
                                                .bomLifecycle(request.getBomLifecycle())
                                                .aspects(request.getAspects())
                                                .depth(request.getDepth())
                                                .direction(request.getDirection())
                                                .collectAspects(request.isCollectAspects())
                                                .timeout(request.getTimeout())
                                                .jobTimeout(request.getJobTimeout())
                                                .callbackUrl(request.getCallbackUrl())
                                                .build();
        final UUID batchId = UUID.randomUUID();
        // TODO: will be a part of strategy
        final Batch batch = Batch.builder()
                                 .batchId(batchId)
                                 .batchOrderId(batchOrderId)
                                 .batchNumber(1)
                                 .batchState(ProcessingState.INITIALIZED)
                                 .globalAssetIds(request.getGlobalAssetIds().stream().toList())
                                 .build();

        batchOrderStore.save(batchOrderId, batchOrder);
        batchStore.save(batchId, batch);
        return batchOrderId;
    }

}
