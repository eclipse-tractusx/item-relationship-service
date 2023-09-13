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
package org.eclipse.tractusx.ess.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationHeader;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EssRecursiveServiceTest {

    private final EssService essService = mock(EssService.class);
    private final RelatedInvestigationJobsCache relatedInvestigationJobsCache = new InMemoryRelatedInvestigationJobsCache();
    private final String localBpn = "BPNS000000000AAA";
    private final EdcNotificationSender edcNotificationSender = mock(EdcNotificationSender.class);

    private final EssRecursiveService essRecursiveService = new EssRecursiveService(essService, relatedInvestigationJobsCache, localBpn, edcNotificationSender);

    @Captor
    ArgumentCaptor<SupplyChainImpacted> supplyChainCaptor;

    @Test
    void shouldResponseWithoutRecursiveWhenLocalBpnIsPartOfIncident() {
        // given
        EdcNotification edcNotification = EdcNotification.builder().content(Map.of("incidentBpn", localBpn)).build();
        Mockito.doNothing().when(edcNotificationSender).sendEdcNotification(any(), supplyChainCaptor.capture());

        // when
        essRecursiveService.handleNotification(edcNotification);

        // then
        assertThat(supplyChainCaptor.getValue()).isEqualTo(SupplyChainImpacted.YES);
    }

    @Test
    void shouldStartBpnInvestigationsJobForEachCatenaXId() {
        // given
        UUID uuid = UUID.randomUUID();

        EdcNotification edcNotification = EdcNotification.builder()
                .header(EdcNotificationHeader.builder().notificationId("notification-id").build())
                                                         .content(
                Map.of("incidentBpn", "BPNS000000000BBB",
                "concernedCatenaXIds", List.of("cat1", "cat2"))).build();
        Mockito.when(essService.startIrsJob(any())).thenReturn(JobHandle.builder().id(uuid).build());

        // when
        essRecursiveService.handleNotification(edcNotification);

        // then
        verify(essService, times(2)).startIrsJob(any());
        assertThat(relatedInvestigationJobsCache.findByRecursiveRelatedJobId(uuid)).isPresent();

    }
}