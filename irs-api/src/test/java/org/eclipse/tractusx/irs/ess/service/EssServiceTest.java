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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.common.auth.SecurityHelperService;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.connector.job.JobStore;
import org.eclipse.tractusx.irs.connector.job.MultiTransferJob;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationHeader;
import org.eclipse.tractusx.irs.edc.client.model.notification.ResponseNotificationContent;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class EssServiceTest {

    private final IrsItemGraphQueryService irsItemGraphQueryService = mock(IrsItemGraphQueryService.class);

    private final SecurityHelperService securityHelperService = mock(SecurityHelperService.class);

    private final BpnInvestigationJobCache bpnInvestigationJobCache = new InMemoryBpnInvestigationJobCache();
    private final JobStore jobStore = mock(JobStore.class);
    private final EssRecursiveNotificationHandler recursiveNotificationHandler = Mockito.mock(
            EssRecursiveNotificationHandler.class);
    private final EssService essService = new EssService(irsItemGraphQueryService, securityHelperService, bpnInvestigationJobCache,
            jobStore, recursiveNotificationHandler);

    @Test
    void shouldSuccessfullyStartJobAndReturnWithExtendedSubmodelList() {
        final String globalAssetId = UUID.randomUUID().toString();
        final List<String> bpns = List.of("BPNS000000000DDD");
        final UUID createdJobId = UUID.randomUUID();
        final var key = PartChainIdentificationKey.builder()
                                                  .globalAssetId(globalAssetId)
                                                  .bpn("BPNS0000000000DD")
                                                  .build();
        final RegisterBpnInvestigationJob request = RegisterBpnInvestigationJob.builder()
                                                                               .key(key)
                                                                               .incidentBPNSs(bpns)
                                                                               .build();
        final Jobs expectedResponse = Jobs.builder()
                                          .job(Job.builder()
                                                  .state(JobState.COMPLETED)
                                                  .id(createdJobId)
                                                  .owner(IrsRoles.VIEW_IRS)
                                                  .globalAssetId(GlobalAssetIdentification.of(globalAssetId))
                                                  .build())
                                          .submodels(new ArrayList<>())
                                          .shells(new ArrayList<>())
                                          .build();

        when(irsItemGraphQueryService.registerItemJob(any(RegisterJob.class), any(), any())).thenReturn(JobHandle.builder().id(createdJobId).build());
        when(jobStore.find(eq(createdJobId.toString()))).thenReturn(Optional.of(MultiTransferJob.builder().job(expectedResponse.getJob()).build()));
        when(irsItemGraphQueryService.getJobForJobId(any(MultiTransferJob.class), eq(true))).thenReturn(expectedResponse);
        when(securityHelperService.isAdmin()).thenReturn(true);

        final JobHandle jobHandle = essService.startIrsJob(request);
        final Jobs jobs = essService.getIrsJob(jobHandle.getId().toString());

        assertThat(jobHandle).isNotNull();
        assertThat(jobHandle.getId()).isNotNull();
        assertThat(jobs).isNotNull();
        assertThat(bpnInvestigationJobCache.findAll()).hasSize(1);
    }

    @Test
    void shouldUpdateJobSnapshotIfNotificationFound() {
        final String notificationId = UUID.randomUUID().toString();
        final String notificationId2 = UUID.randomUUID().toString();
        final UUID jobId = UUID.randomUUID();
        final String owner = securityHelperService.getClientIdClaim();

        final ResponseNotificationContent resultNo = ResponseNotificationContent.builder().result("No").build();
        final EdcNotificationHeader header1 = EdcNotificationHeader.builder()
                                                                   .notificationId(notificationId)
                                                                   .originalNotificationId(notificationId)
                                                                   .build();
        final EdcNotification<ResponseNotificationContent> edcNotification = EdcNotification.<ResponseNotificationContent>builder()
                                                                                            .header(header1)
                                                                                            .content(resultNo)
                                                                                            .build();
        final ResponseNotificationContent resultYes = ResponseNotificationContent.builder().result("Yes").build();
        final EdcNotificationHeader header2 = EdcNotificationHeader.builder()
                                                                   .notificationId(notificationId2)
                                                                   .originalNotificationId(notificationId2)
                                                                   .build();
        final EdcNotification<ResponseNotificationContent> edcNotification2 = EdcNotification.<ResponseNotificationContent>builder()
                                                                                             .header(header2)
                                                                                             .content(resultYes)
                                                                                             .build();

        final BpnInvestigationJob bpnInvestigationJob = BpnInvestigationJob.create(
                                                                                   Jobs.builder().job(Job.builder().id(jobId).owner(owner).build()).build(), new ArrayList<>())
                                                                           .withNotifications(List.of(notificationId,
                                                                                   notificationId2));
        bpnInvestigationJobCache.store(jobId, bpnInvestigationJob);

        assertDoesNotThrow(() -> essService.handleNotificationCallback(edcNotification));
        assertThat(bpnInvestigationJobCache.findAll()).hasSize(1);

        final BpnInvestigationJob job = bpnInvestigationJobCache.findAll().get(0);
        final String supplyChainImpacted = (String) job.getJobSnapshot()
                                                       .getSubmodels()
                                                       .get(0)
                                                       .getPayload()
                                                       .get("supplyChainImpacted");
        assertThat(supplyChainImpacted).isEqualTo("No");
        assertThat(job.getState()).isEqualTo(JobState.RUNNING);

        assertDoesNotThrow(() -> essService.handleNotificationCallback(edcNotification2));
        assertThat(bpnInvestigationJobCache.findAll()).hasSize(1);
        final BpnInvestigationJob job2 = bpnInvestigationJobCache.findAll().get(0);
        assertThat(job2.getJobSnapshot().getSubmodels()).hasSize(1);
        final String supplyChainImpacted2 = (String) job.getJobSnapshot()
                                                        .getSubmodels()
                                                        .get(0)
                                                        .getPayload()
                                                        .get("supplyChainImpacted");
        assertThat(supplyChainImpacted2).isEqualTo("Yes");
        assertThat(job.getState()).isEqualTo(JobState.COMPLETED);

    }

    @Test
    void shouldKeepJobInRunningIfNotificationIsOpen() {
        final String notificationId = UUID.randomUUID().toString();
        final UUID jobId = UUID.randomUUID();
        final String owner = securityHelperService.getClientIdClaim();
        when(securityHelperService.isAdmin()).thenReturn(true);

        final BpnInvestigationJob bpnInvestigationJob = BpnInvestigationJob.create(Jobs.builder().job(Job.builder().id(jobId).owner(owner).build()).build(),
                                                                                   new ArrayList<>())
                                                                           .withNotifications(Collections.singletonList(notificationId));
        bpnInvestigationJobCache.store(jobId, bpnInvestigationJob);

        assertThat(bpnInvestigationJobCache.findAll()).hasSize(1);
        final Jobs byJobId = essService.getIrsJob(jobId.toString());
        assertThat(byJobId.getJob().getState()).isEqualTo(JobState.RUNNING);
    }

    @Test
    void shouldThrowResponseStatusExceptionWhenIdDoesntExists() {
        final String jobIdNotExisting = UUID.randomUUID().toString();

        assertThrows(ResponseStatusException.class, () -> essService.getIrsJob(jobIdNotExisting));
    }

    @Test
    void shouldCompleteJobIfAllNotificationsSentWereAnswered() {
        // Arrange
        final String notificationId = UUID.randomUUID().toString();
        final String owner = securityHelperService.getClientIdClaim();
        when(securityHelperService.isAdmin()).thenReturn(true);

        final UUID jobId = UUID.randomUUID();
        final Jobs jobSnapshot = job(jobId, owner);
        final BpnInvestigationJob bpnInvestigationJob = BpnInvestigationJob.create(jobSnapshot, null)
                                                                           .withAnsweredNotification(notificationId)
                                                                           .withNotifications(List.of());
        bpnInvestigationJobCache.store(jobId, bpnInvestigationJob);

        // Act
        final Jobs byJobId = essService.getIrsJob(jobId.toString());

        // Assert
        assertThat(bpnInvestigationJobCache.findAll()).hasSize(1);
        assertThat(byJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);
    }

    @Test
    void shouldCompleteJobIfFinalNotificationWasReceived() {
        // Arrange
        final String owner = securityHelperService.getClientIdClaim();
        when(securityHelperService.isAdmin()).thenReturn(true);

        final UUID jobId = UUID.randomUUID();
        final Jobs jobSnapshot = job(jobId, owner);
        final String notificationId = UUID.randomUUID().toString();
        final BpnInvestigationJob bpnInvestigationJob = BpnInvestigationJob.create(jobSnapshot, null)
                                                                           .update(jobSnapshot, SupplyChainImpacted.NO)
                                                                           .withNotifications(List.of(notificationId));
        bpnInvestigationJobCache.store(jobId, bpnInvestigationJob);
        final ResponseNotificationContent resultNo = ResponseNotificationContent.builder().result("No").build();
        final EdcNotificationHeader header1 = EdcNotificationHeader.builder()
                                                                   .notificationId(notificationId)
                                                                   .originalNotificationId(notificationId)
                                                                   .build();
        final EdcNotification<ResponseNotificationContent> edcNotification2 = EdcNotification.<ResponseNotificationContent>builder()
                                                                                             .header(header1)
                                                                                             .content(resultNo)
                                                                                             .build();

        // Act
        final Jobs jobBeforeNotificationReceive = essService.getIrsJob(jobId.toString());
        essService.handleNotificationCallback(edcNotification2);
        final Jobs jobAfterNotificationReceive = essService.getIrsJob(jobId.toString());

        // Assert
        assertThat(bpnInvestigationJobCache.findAll()).hasSize(1);
        assertThat(jobBeforeNotificationReceive.getJob().getState()).isEqualTo(JobState.RUNNING);
        assertThat(jobAfterNotificationReceive.getJob().getState()).isEqualTo(JobState.COMPLETED);
    }

    private Jobs job(final UUID jobId, final String owner) {
        final Job job = Job.builder().id(jobId).owner(owner).build();
        return Jobs.builder().job(job).build();
    }
}
