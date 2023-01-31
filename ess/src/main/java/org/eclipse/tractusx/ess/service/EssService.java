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

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.ess.discovery.EdcDiscoveryFacade;
import org.eclipse.tractusx.ess.irs.IrsFacade;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
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

    public JobHandle startIrsJob(final String globalAssetId, final BomLifecycle bomLifecycle) {
        return irsFacade.startIrsJob(globalAssetId, bomLifecycle);
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
        }

        extendSubmodelsWithSupplyChain(irsJob, supplyChainImpacted);

        return irsJob;
    }

    private boolean isJobProcessingFinished(final Jobs irsJob) {
        return irsJob.getJob().getState().equals(JobState.COMPLETED);
    }

    private void extendSubmodelsWithSupplyChain(final Jobs completedIrsJob, final SupplyChainImpacted supplyChainImpacted) {
        completedIrsJob.getSubmodels().add(
                Submodel.builder()
                        .payload(new ObjectMapper().convertValue(supplyChainImpacted, Map.class))
                        .build()
        );
    }
}
