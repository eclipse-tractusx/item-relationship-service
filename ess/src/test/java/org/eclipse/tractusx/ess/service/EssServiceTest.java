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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.ess.discovery.EdcDiscoveryFacade;
import org.eclipse.tractusx.ess.irs.IrsFacade;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.junit.jupiter.api.Test;

public class EssServiceTest {

    private final IrsFacade irsFacade = mock(IrsFacade.class);
    private final EdcDiscoveryFacade edcDiscoveryFacade = mock(EdcDiscoveryFacade.class);

    private final EssService essService = new EssService(irsFacade, edcDiscoveryFacade);

    @Test
    void shouldReturnJobWithSubmodelsContainingOnlySupplyChainSubmodel() {
        final UUID jobId = UUID.randomUUID();
        final GlobalAssetIdentification globalAssetId = GlobalAssetIdentification.of(UUID.randomUUID().toString());
        final Jobs expectedResponse = Jobs.builder()
                                          .job(Job.builder()
                                                  .state(JobState.COMPLETED)
                                                  .globalAssetId(globalAssetId)
                                                  .build())
                                          .submodels(new ArrayList<>())
                                          .shells(new ArrayList<>())
                                          .build();

        given(irsFacade.getIrsJob(jobId.toString())).willReturn(expectedResponse);

        final Jobs jobs = essService.getIrsJob(jobId.toString());

        assertThat(jobs).isNotNull();
        assertThat(jobs.getSubmodels()).hasSize(1);
        assertThat(jobs.getSubmodels().get(0).getPayload()).containsKey("supplyChainImpacted");
    }

    @Test
    void shouldReturnCreatedJobId() {
        final String globalAssetId = UUID.randomUUID().toString();
        final List<String> bpns = List.of("BPNS000000000DDD");
        RegisterBpnInvestigationJob request = RegisterBpnInvestigationJob.builder()
                                                                         .globalAssetId(globalAssetId)
                                                                         .incidentBpns(bpns)
                                                                         .build();

        when(irsFacade.startIrsJob(eq(globalAssetId), any())).thenReturn(
                JobHandle.builder().id(UUID.randomUUID()).build());

        final JobHandle jobHandle = essService.startIrsJob(request);

        assertThat(jobHandle).isNotNull();
        assertThat(jobHandle.getId()).isNotNull();
    }

}
