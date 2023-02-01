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

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.ess.bpn.validation.BPNIncidentValidation;
import org.eclipse.tractusx.ess.discovery.EdcDiscoveryFacade;
import org.eclipse.tractusx.ess.irs.IrsFacade;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.springframework.stereotype.Service;

/**
 * Business logic for investigation if part is in supply chain/part chain
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EssService {

    private final IrsFacade irsFacade;
    private final EdcDiscoveryFacade edcDiscoveryFacade;
    private final IncidentBpnCache incidentBpnCache = new InMemoryIncidentBpnCache();

    public JobHandle startIrsJob(final RegisterBpnInvestigationJob request) {
        final JobHandle jobHandle = irsFacade.startIrsJob(request.getGlobalAssetId(), request.getBomLifecycle());

        incidentBpnCache.store(jobHandle.getId(), request.getIncidentBpns());

        return jobHandle;
    }

    public Jobs getIrsJob(final String jobId) {
        final Jobs irsJob = irsFacade.getIrsJob(jobId);

        SupplyChainImpacted supplyChainImpacted = SupplyChainImpacted.UNKNOWN;

        //        TODO:
        //        Implement business logic
        //        1. Discover EDC Address
        //        2. ESSIncidentRequest supplier-request stuff
        if (isJobProcessingFinished(irsJob)) {
            log.info("Job is completed. Starting SupplyChainImpacted processing for job {}.", irsJob.getJob().getId());
            supplyChainImpacted = BPNIncidentValidation.jobContainsIncidentBPNs(irsJob,
                    incidentBpnCache.findByJobId(UUID.fromString(jobId)));
        }

        return extendJobWithSupplyChainSubmodel(irsJob, supplyChainImpacted);
    }

    private boolean isJobProcessingFinished(final Jobs irsJob) {
        return irsJob.getJob().getState().equals(JobState.COMPLETED);
    }

    private Jobs extendJobWithSupplyChainSubmodel(final Jobs completedIrsJob,
            final SupplyChainImpacted supplyChainImpacted) {
        final Submodel supplyChainImpactedSubmodel = Submodel.builder()
                                                             .payload(
                                                                     Map.of("supplyChainImpacted", supplyChainImpacted))
                                                             .build();

        return completedIrsJob.toBuilder().submodels(Collections.singletonList(supplyChainImpactedSubmodel)).build();
    }
}
