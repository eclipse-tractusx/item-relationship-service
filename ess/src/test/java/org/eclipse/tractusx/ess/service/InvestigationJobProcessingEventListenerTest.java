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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.tractusx.edc.EdcSubmodelFacade;
import org.eclipse.tractusx.edc.exceptions.EdcClientException;
import org.eclipse.tractusx.edc.model.notification.EdcNotification;
import org.eclipse.tractusx.ess.discovery.EdcDiscoveryFacade;
import org.eclipse.tractusx.ess.irs.IrsFacade;
import org.eclipse.tractusx.irs.common.JobProcessingFinishedEvent;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvestigationJobProcessingEventListenerTest {

    private final IrsFacade irsFacade = mock(IrsFacade.class);
    private final EdcDiscoveryFacade edcDiscoveryFacade = mock(EdcDiscoveryFacade.class);
    private final EdcSubmodelFacade edcSubmodelFacade = mock(EdcSubmodelFacade.class);
    private final BpnInvestigationJobCache bpnInvestigationJobCache = mock(BpnInvestigationJobCache.class);

    private final InvestigationJobProcessingEventListener jobProcessingEventListener = new InvestigationJobProcessingEventListener(
            irsFacade, edcDiscoveryFacade, edcSubmodelFacade, bpnInvestigationJobCache);

    private final UUID jobId = UUID.randomUUID();

    @BeforeEach
    void mockInit() {
        final Jobs jobs = Jobs.builder()
                              .job(Job.builder().id(jobId).build())
                              .shells(List.of(createShell(UUID.randomUUID().toString(), "bpn")))
                              .build();
        final BpnInvestigationJob bpnInvestigationJob = BpnInvestigationJob.create(jobs, List.of("BPNS000000000DDD"));

        when(bpnInvestigationJobCache.findByJobId(jobId)).thenReturn(Optional.of(bpnInvestigationJob));
        when(irsFacade.getIrsJob(jobId.toString())).thenReturn(jobs);
    }

    @Test
    void shouldSendEdcNotificationWhenJobCompleted() throws EdcClientException {
        // given
        final String edcBaseUrl = "http://edc-server-url.com";
        when(edcDiscoveryFacade.getEdcBaseUrl(anyString())).thenReturn(Optional.of(edcBaseUrl));
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId.toString(),
                JobState.COMPLETED.name(), "");

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verify(this.edcSubmodelFacade, times(1)).sendNotification(eq(edcBaseUrl), anyString(),
                any(EdcNotification.class));
        verify(this.bpnInvestigationJobCache, times(1)).store(eq(jobId), any(BpnInvestigationJob.class));
    }

    @Test
    void shouldStopProcessingIfOneOfEdcAddressIsNotDiscovered() throws EdcClientException {
        // given
        when(edcDiscoveryFacade.getEdcBaseUrl(anyString())).thenReturn(Optional.empty());
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId.toString(),
                JobState.COMPLETED.name(), "");

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verify(this.edcSubmodelFacade, times(0)).sendNotification(anyString(), anyString(), any(EdcNotification.class));
        verify(this.bpnInvestigationJobCache, times(1)).store(eq(jobId), any(BpnInvestigationJob.class));
    }

    private static AssetAdministrationShellDescriptor createShell(final String catenaXId, final String bpn) {
        return AssetAdministrationShellDescriptor.builder()
                                                 .globalAssetId(Reference.builder().value(List.of(catenaXId)).build())
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                                                 .key("manufacturerId")
                                                                                                 .value(bpn)
                                                                                                 .build()))
                                                 .build();
    }

}
