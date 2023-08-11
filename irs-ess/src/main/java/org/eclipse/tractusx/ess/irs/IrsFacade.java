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
package org.eclipse.tractusx.ess.irs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for IRS domain
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IrsFacade {

    private final IrsClient irsClient;

    public JobHandle startIrsJob(final PartChainIdentificationKey key, final BomLifecycle bomLifecycle) {
        final JobHandle response = irsClient.startJob(IrsRequest.bpnInvestigations(key, bomLifecycle));
        log.info("Registered IRS job with jobId: {}", response.getId());
        return response;
    }

    public Jobs getIrsJob(final String jobId) {
        final Jobs response = irsClient.getJobDetails(jobId);
        log.info("Retrieved IRS job with jobId: {}", response.getJob().getId());
        return response;
    }

}
