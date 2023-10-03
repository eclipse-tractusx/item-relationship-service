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
package org.eclipse.tractusx.irs.ess.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.ess.service.EdcRegistration.ASSET_ID_REQUEST_RECURSIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.tractusx.irs.common.JobProcessingFinishedEvent;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.connector.job.JobStore;
import org.eclipse.tractusx.irs.connector.job.MultiTransferJob;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.InvestigationNotificationContent;
import org.eclipse.tractusx.irs.edc.client.model.notification.NotificationContent;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvestigationJobProcessingEventListenerTest {

    private final IrsItemGraphQueryService irsItemGraphQueryService = mock(IrsItemGraphQueryService.class);
    private final EdcSubmodelFacade edcSubmodelFacade = mock(EdcSubmodelFacade.class);
    private final BpnInvestigationJobCache bpnInvestigationJobCache = Mockito.mock(BpnInvestigationJobCache.class);
    private final JobStore jobStore = Mockito.mock(JobStore.class);
    private final EssRecursiveNotificationHandler recursiveNotificationHandler = Mockito.mock(
            EssRecursiveNotificationHandler.class);
    private final UUID jobId = UUID.randomUUID();
    private final UUID recursiveJobId = UUID.randomUUID();

    private final ConnectorEndpointsService connectorEndpointsService = mock(ConnectorEndpointsService.class);

    private final InvestigationJobProcessingEventListener jobProcessingEventListener = new InvestigationJobProcessingEventListener(
            irsItemGraphQueryService, connectorEndpointsService, edcSubmodelFacade, bpnInvestigationJobCache, jobStore,
            "", "", List.of(), recursiveNotificationHandler);

    @Captor
    ArgumentCaptor<EdcNotification<NotificationContent>> edcNotificationCaptor;

    private static AssetAdministrationShellDescriptor createShell(final String catenaXId, final String bpn) {
        return AssetAdministrationShellDescriptor.builder()
                                                 .globalAssetId(catenaXId)
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                                                 .name("manufacturerId")
                                                                                                 .value(bpn)
                                                                                                 .build()))
                                                 .build();
    }

    private static Relationship createRelationship(final String lifecycle, final String bpn, final String parentId,
            final String childId) {
        return Relationship.builder()
                           .aspectType(lifecycle)
                           .bpn(bpn)
                           .catenaXId(GlobalAssetIdentification.of(parentId))
                           .linkedItem(
                                   LinkedItem.builder().childCatenaXId(GlobalAssetIdentification.of(childId)).build())
                           .build();
    }

    @BeforeEach
    void mockInit() {
        createMockForJobIdAndShell(jobId, "bpn", List.of(createRelationship("SingleLevelBomAsPlanned", "BPN123",
                "urn:uuid:52207a60-e541-4bea-8ec4-3172f09e6dbb", "urn:uuid:86f69643-3b90-4e34-90bf-789edcf40e7e")));
    }

    @Test
    void shouldSendEdcNotificationWhenJobCompleted() throws EdcClientException {
        // given
        final String edcBaseUrl = "http://edc-server-url.com";
        when(connectorEndpointsService.fetchConnectorEndpoints(anyString())).thenReturn(List.of(edcBaseUrl));
        when(edcSubmodelFacade.sendNotification(anyString(), anyString(), any(EdcNotification.class))).thenReturn(
                () -> true);
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId.toString(),
                JobState.COMPLETED.name(), "", Optional.empty());

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verify(this.edcSubmodelFacade, times(1)).sendNotification(eq(edcBaseUrl), anyString(),
                any(EdcNotification.class));
        verify(this.bpnInvestigationJobCache, times(1)).store(eq(jobId), any(BpnInvestigationJob.class));
    }

    @Test
    void shouldStopProcessingIfNoRelationshipContainsBPN() throws EdcClientException {
        // given
        createMockForJobIdAndShell(jobId, "bpn",
                List.of(createRelationship("asPlanned", null, "testParent", "testChild")));
        final String edcBaseUrl = "http://edc-server-url.com";
        when(connectorEndpointsService.fetchConnectorEndpoints(anyString())).thenReturn(List.of(edcBaseUrl));
        when(edcSubmodelFacade.sendNotification(anyString(), anyString(), any(EdcNotification.class))).thenReturn(
                () -> true);
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId.toString(),
                JobState.COMPLETED.name(), "", Optional.empty());

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verify(this.recursiveNotificationHandler, times(1)).handleNotification(any(), eq(SupplyChainImpacted.UNKNOWN));
        verify(this.bpnInvestigationJobCache, times(1)).store(eq(jobId), any(BpnInvestigationJob.class));
    }

    @Test
    void shouldHandleCaseWhenRelationshipDoesNotContainBPN() throws EdcClientException {
        // given
        createMockForJobIdAndShell(jobId, "bpn",
                List.of(createRelationship("asPlanned", "BPN1", "parentId1", "childId1"),
                        createRelationship("asPlanned", null, "parentId2", "childId2")));
        final String edcBaseUrl = "http://edc-server-url.com";
        when(connectorEndpointsService.fetchConnectorEndpoints(anyString())).thenReturn(List.of(edcBaseUrl));
        when(edcSubmodelFacade.sendNotification(anyString(), anyString(), any(EdcNotification.class))).thenReturn(
                () -> true);
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId.toString(),
                JobState.COMPLETED.name(), "", Optional.empty());

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verify(this.edcSubmodelFacade, times(1)).sendNotification(eq(edcBaseUrl), anyString(),
                any(EdcNotification.class));
        verify(this.bpnInvestigationJobCache, times(1)).store(eq(jobId), any(BpnInvestigationJob.class));
    }

    @Test
    void shouldTriggerCorrectNotificationOnNextLevel() throws EdcClientException {
        // given
        createMockForJobIdAndShell(jobId, "bpn",
                List.of(createRelationship("asPlanned", "BPN1", "parentId1", "childId1")));
        final String edcBaseUrl = "http://edc-server-url.com";
        when(edcSubmodelFacade.sendNotification(anyString(), anyString(), any(EdcNotification.class))).thenReturn(
                () -> true);
        when(connectorEndpointsService.fetchConnectorEndpoints(anyString())).thenReturn(List.of(edcBaseUrl));
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId.toString(),
                JobState.COMPLETED.name(), "", Optional.empty());

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verify(this.edcSubmodelFacade, times(1)).sendNotification(eq(edcBaseUrl), eq("notify-request-asset-recursive"),
                edcNotificationCaptor.capture());
        assertThat(edcNotificationCaptor.getValue().getHeader().getNotificationType()).isEqualTo(
                "ess-supplier-request");
        final InvestigationNotificationContent content = (InvestigationNotificationContent) edcNotificationCaptor.getValue()
                                                                                                                 .getContent();
        assertThat(content.getIncidentBPNSs()).containsAll(List.of("BPNS000000000DDD"));
        assertThat(content.getConcernedCatenaXIds()).containsAll(List.of("childId1"));
        verify(this.bpnInvestigationJobCache, times(1)).store(eq(jobId), any(BpnInvestigationJob.class));
    }

    @Test
    void shouldStopProcessingIfNoEdcAddressIsDiscovered() throws EdcClientException {
        // given
        when(connectorEndpointsService.fetchConnectorEndpoints(anyString())).thenReturn(Collections.emptyList());
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId.toString(),
                JobState.COMPLETED.name(), "", Optional.empty());

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verify(this.edcSubmodelFacade, times(0)).sendNotification(anyString(), anyString(), any(EdcNotification.class));
        verify(this.bpnInvestigationJobCache, times(1)).store(eq(jobId), any(BpnInvestigationJob.class));
    }

    @Test
    void shouldSendCallbackIfNoMoreRelationshipsAreFound() throws EdcClientException {
        // given
        createMockForJobIdAndShell(jobId, "bpn", List.of());
        when(connectorEndpointsService.fetchConnectorEndpoints(anyString())).thenReturn(Collections.emptyList());
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId.toString(),
                JobState.COMPLETED.name(), "", Optional.empty());

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verify(this.edcSubmodelFacade, times(0)).sendNotification(anyString(), anyString(), any(EdcNotification.class));
        verify(this.bpnInvestigationJobCache, times(1)).store(eq(jobId), any(BpnInvestigationJob.class));
        verify(this.recursiveNotificationHandler, times(1)).handleNotification(any(), eq(SupplyChainImpacted.NO));
    }

    @Test
    void shouldStopProcessingIfOneOfEdcAddressesIsNotDiscovered() throws EdcClientException {
        // given
        createMockForJobIdAndShells(jobId, List.of("BPN123", "BPN456"));
        final String edcBaseUrl = "http://edc-server-url.com";
        when(connectorEndpointsService.fetchConnectorEndpoints("BPN123")).thenReturn(Collections.emptyList());
        when(connectorEndpointsService.fetchConnectorEndpoints("BPN456")).thenReturn(List.of(edcBaseUrl));
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId.toString(),
                JobState.COMPLETED.name(), "", Optional.empty());

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verify(this.edcSubmodelFacade, times(0)).sendNotification(anyString(), anyString(), any(EdcNotification.class));
        verify(this.bpnInvestigationJobCache, times(1)).store(eq(jobId), any(BpnInvestigationJob.class));
    }

    @Test
    void shouldSendEdcRecursiveNotificationWhenJobCompleted() throws EdcClientException {
        // given
        createMockForJobIdAndShell(recursiveJobId, "BPN000RECURSIVE",
                List.of(createRelationship("SingleLevelBomAsPlanned", "BPN123",
                        "urn:uuid:52207a60-e541-4bea-8ec4-3172f09e6dbb",
                        "urn:uuid:86f69643-3b90-4e34-90bf-789edcf40e7e")));
        final String edcBaseUrl = "http://edc-server-url.com";
        when(connectorEndpointsService.fetchConnectorEndpoints(anyString())).thenReturn(List.of(edcBaseUrl));
        when(edcSubmodelFacade.sendNotification(anyString(), anyString(), any(EdcNotification.class))).thenReturn(
                () -> true);
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(
                recursiveJobId.toString(), JobState.COMPLETED.name(), "", Optional.empty());

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verify(this.edcSubmodelFacade, times(1)).sendNotification(eq(edcBaseUrl), eq(ASSET_ID_REQUEST_RECURSIVE),
                edcNotificationCaptor.capture());
        assertThat(edcNotificationCaptor.getValue().getHeader().getNotificationType()).isEqualTo(
                "ess-supplier-request");
        verify(this.bpnInvestigationJobCache, times(1)).store(eq(recursiveJobId), any(BpnInvestigationJob.class));
    }

    @Test
    void shouldSendEdcRecursiveNotificationWithMultipleIncidentBPNSs() throws EdcClientException {
        // given
        createMockForJobIdAndShell(jobId, "bpn",
                List.of(createRelationship("asPlanned", "BPN1", "parentId1", "childId1")), List.of("BPN1", "BPN2"));
        final String edcBaseUrl = "http://edc-server-url.com";
        when(edcSubmodelFacade.sendNotification(anyString(), anyString(), any(EdcNotification.class))).thenReturn(
                () -> true);
        when(connectorEndpointsService.fetchConnectorEndpoints(anyString())).thenReturn(List.of(edcBaseUrl));
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId.toString(),
                JobState.COMPLETED.name(), "", Optional.empty());

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verify(this.edcSubmodelFacade, times(1)).sendNotification(eq(edcBaseUrl), eq("notify-request-asset-recursive"),
                edcNotificationCaptor.capture());
        final InvestigationNotificationContent content = (InvestigationNotificationContent) edcNotificationCaptor.getValue()
                                                                                                                 .getContent();
        assertThat(edcNotificationCaptor.getValue().getHeader().getNotificationType()).isEqualTo(
                "ess-supplier-request");
        assertThat(content.getIncidentBPNSs()).containsAll(List.of("BPN1", "BPN2"));
        assertThat(content.getConcernedCatenaXIds()).containsAll(List.of("childId1"));
        verify(this.bpnInvestigationJobCache, times(1)).store(eq(jobId), any(BpnInvestigationJob.class));
        verify(this.edcSubmodelFacade, times(1)).sendNotification(any(), any(), any(EdcNotification.class));
    }

    private void createMockForJobIdAndShell(final UUID mockedJobId, final String mockedShell,
            final List<Relationship> relationships, final List<String> incindentBPNSs) {
        final String partAsPlannedRaw = """
                {
                  "validityPeriod": {
                    "validFrom": "2019-04-04T03:19:03.000Z",
                    "validTo": "2124-12-29T10:25:12.000Z"
                  },
                  "catenaXId": "urn:uuid:0733946c-59c6-41ae-9570-cb43a6e4c79e",
                  "partTypeInformation": {
                    "manufacturerPartId": "ZX-55",
                    "classification": "product",
                    "nameAtManufacturer": "Vehicle Model A"
                  }
                }
                """;
        final String partSiteInformationAsPlannedRaw = """
                {
                  "catenaXId": "urn:uuid:0733946c-59c6-41ae-9570-cb43a6e4c79e",
                  "sites": [
                    {
                      "functionValidUntil": "2025-02-08T04:30:48.000Z",
                      "function": "production",
                      "functionValidFrom": "2019-08-21T02:10:36.000Z",
                      "catenaXSiteId": "BPNS000004711DMY"
                    }
                  ]
                }
                """;
        final Submodel partAsPlanned = Submodel.from("test1", "urn:bamm:io.catenax.part_as_planned:1.0.1#PartAsPlanned",
                StringMapper.mapFromString(partAsPlannedRaw, Map.class));
        final Submodel partSiteInformationAsPlanned = Submodel.from("test2",
                "urn:bamm:io.catenax.part_site_information_as_planned:1.0.0#PartSiteInformationAsPlanned",
                StringMapper.mapFromString(partSiteInformationAsPlannedRaw, Map.class));
        final Jobs jobs = Jobs.builder()
                              .job(Job.builder()
                                      .id(mockedJobId)
                                      .globalAssetId(GlobalAssetIdentification.of("dummyGlobalAssetId"))
                                      .build())
                              .relationships(relationships)
                              .shells(List.of(createShell(UUID.randomUUID().toString(), mockedShell)))
                              .submodels(List.of(partAsPlanned, partSiteInformationAsPlanned))
                              .build();
        final BpnInvestigationJob bpnInvestigationJob = BpnInvestigationJob.create(jobs, incindentBPNSs);

        when(bpnInvestigationJobCache.findByJobId(mockedJobId)).thenReturn(Optional.of(bpnInvestigationJob));
        when(jobStore.find(eq(mockedJobId.toString()))).thenReturn(Optional.of(MultiTransferJob.builder().job(jobs.getJob()).build()));
        when(irsItemGraphQueryService.getJobForJobId(any(MultiTransferJob.class), eq(false))).thenReturn(jobs);
    }

    private void createMockForJobIdAndShell(final UUID mockedJobId, final String mockedShell,
            final List<Relationship> relationships) {
        createMockForJobIdAndShell(mockedJobId, mockedShell, relationships, List.of("BPNS000000000DDD"));
    }

    private void createMockForJobIdAndShells(final UUID mockedJobId, final List<String> bpns) {
        final Jobs jobs = Jobs.builder()
                              .job(Job.builder()
                                      .id(mockedJobId)
                                      .globalAssetId(GlobalAssetIdentification.of("dummyGlobalAssetId"))
                                      .build())
                              .shells(bpns.stream().map(bpn -> createShell(UUID.randomUUID().toString(), bpn)).toList())
                              .build();
        final BpnInvestigationJob bpnInvestigationJob = BpnInvestigationJob.create(jobs,
                List.of("BPNS000000000DDD"));

        when(bpnInvestigationJobCache.findByJobId(mockedJobId)).thenReturn(Optional.of(bpnInvestigationJob));
        when(jobStore.find(eq(mockedJobId.toString()))).thenReturn(Optional.of(MultiTransferJob.builder().job(jobs.getJob()).build()));
        when(irsItemGraphQueryService.getJobForJobId(any(MultiTransferJob.class), eq(false))).thenReturn(jobs);
    }

}
