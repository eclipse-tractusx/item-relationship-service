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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationHeader;
import org.eclipse.tractusx.irs.edc.client.model.notification.InvestigationNotificationContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EssRecursiveNotificationHandlerTest {

    private final RelatedInvestigationJobsCache relatedInvestigationJobsCache = new InMemoryRelatedInvestigationJobsCache();
    private final BpnInvestigationJobCache bpnInvestigationJobCache = new InMemoryBpnInvestigationJobCache();
    private final EdcNotificationSender edcNotificationSender = Mockito.mock(EdcNotificationSender.class);
    private final BpnInvestigationJob currentBpnInvestigationJob = Mockito.mock(BpnInvestigationJob.class);
    private final BpnInvestigationJob pastBpnInvestigationJob = Mockito.mock(BpnInvestigationJob.class);
    private final EssRecursiveNotificationHandler cut = new EssRecursiveNotificationHandler(relatedInvestigationJobsCache, bpnInvestigationJobCache, edcNotificationSender);

    private final UUID jobId = UUID.randomUUID();

    @Test
    void shouldDoNothingWhenThereIsNoInvestigationJob() {
        // when
        cut.handleNotification(UUID.randomUUID(), SupplyChainImpacted.UNKNOWN, "bpn",
                0);

        // then
        verifyNoInteractions(edcNotificationSender);
    }

    @Test
    void shouldReplyWhenJobIsPresentAndSupplyChainIsImpacted() {
        // given
        final int hops = 0;
        relatedInvestigationJobsCache.store("notification-id", createRelatedJobsWith(List.of(jobId)));

        // when
        cut.handleNotification(jobId, SupplyChainImpacted.YES, "bpn", 0);

        // then
        verify(edcNotificationSender).sendEdcNotification(any(), eq(SupplyChainImpacted.YES), eq(hops), eq("bpn"));
    }

    @Test
    void shouldReplyOnlyWhenAllJobsAreCompleted() {
        // given
        final UUID anotherJobId = UUID.randomUUID();
        final int hops = 0;
        final String bpn = "bpn";
        relatedInvestigationJobsCache.store("notification-id", createRelatedJobsWith(List.of(jobId, anotherJobId)));
        bpnInvestigationJobCache.store(jobId, currentBpnInvestigationJob);
        bpnInvestigationJobCache.store(anotherJobId, pastBpnInvestigationJob);

        when(currentBpnInvestigationJob.getSupplyChainImpacted()).thenReturn(Optional.of(SupplyChainImpacted.NO));
        when(pastBpnInvestigationJob.getSupplyChainImpacted()).thenReturn(Optional.of(SupplyChainImpacted.NO));

        // when
        cut.handleNotification(jobId, SupplyChainImpacted.NO, bpn, 0);

        // then
        verify(edcNotificationSender, times(1)).sendEdcNotification(any(), eq(SupplyChainImpacted.NO), eq(hops), eq(bpn));
    }

    private RelatedInvestigationJobs createRelatedJobsWith(List<UUID> uuids) {
        return new RelatedInvestigationJobs(EdcNotification.<InvestigationNotificationContent>builder()
                                                           .header(EdcNotificationHeader.builder()
                                                                                        .notificationId(
                                                                                                "notification-id")
                                                                                        .build())
                                                           .build(), uuids);
    }

}