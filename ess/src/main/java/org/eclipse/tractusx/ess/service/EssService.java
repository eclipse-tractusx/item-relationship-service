/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.ess.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.edc.model.notification.EdcNotification;
import org.eclipse.tractusx.ess.irs.IrsFacade;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.springframework.stereotype.Service;

/**
 * Business logic for investigation if part is in supply chain/part chain
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EssService {

    private final IrsFacade irsFacade;
    private final BpnInvestigationJobCache bpnInvestigationJobCache = new InMemoryBpnInvestigationJobCache();

    public JobHandle startIrsJob(final RegisterBpnInvestigationJob request) {
        final JobHandle jobHandle = irsFacade.startIrsJob(request.getGlobalAssetId(), request.getBomLifecycle());

        final UUID createdJobId = jobHandle.getId();
        final Jobs createdJob = irsFacade.getIrsJob(createdJobId.toString());
        bpnInvestigationJobCache.store(createdJobId, BpnInvestigationJob.create(createdJob, request.getIncidentBpns()));

        return jobHandle;
    }

    public Jobs getIrsJob(final String jobId) {
        return bpnInvestigationJobCache.findByJobId(UUID.fromString(jobId)).map(BpnInvestigationJob::getJobSnapshot).orElseThrow(/* should result with 404 not found */);
    }

    public void handleNotificationCallback(final EdcNotification notification) {
        // TODO: find correct investigation job
        final BpnInvestigationJob investigationJob = bpnInvestigationJobCache.findAll().stream().findAny().get();
        // TODO check parsing :)
        final String notificationResult = (String) notification.getContent().get("result");
        final SupplyChainImpacted supplyChainImpacted = SupplyChainImpacted.valueOf(notificationResult);

        investigationJob.update(investigationJob.getJobSnapshot(), supplyChainImpacted);
    }
}
