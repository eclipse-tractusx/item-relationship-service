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
package org.eclipse.tractusx.ess.notification.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationHeader;
import org.eclipse.tractusx.ess.discovery.EdcDiscoveryMockConfig;
import org.eclipse.tractusx.ess.service.EssService;
import org.eclipse.tractusx.ess.service.SupplyChainImpacted;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MockedNotificationReceiverEndpointTest {

    @InjectMocks
    private MockedNotificationReceiverEndpoint testee;

    @Mock
    private EdcSubmodelFacade edcSubmodelFacade;

    @Mock
    private EdcDiscoveryMockConfig edcDiscoveryMockConfig;

    @Mock
    private EssService essService;

    @Captor
    private ArgumentCaptor<RegisterBpnInvestigationJob> jobArgumentCaptor;

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldReceiveNotificationAndSendMockedNotificationResult() throws Exception {
        final String bpn = "BPN1";
        when(edcDiscoveryMockConfig.getMockEdcResult()).thenReturn(Map.of(bpn, SupplyChainImpacted.YES));
        when(edcSubmodelFacade.sendNotification(anyString(), anyString(), any(EdcNotification.class))).thenReturn(() -> true);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        testee.receiveNotification(EdcNotification.builder().header(validHeader()).content(Map.of("incidentBpn", bpn)).build());

        verify(edcSubmodelFacade).sendNotification(anyString(), anyString(), any(EdcNotification.class));
    }

    @Test
    void shouldReturnBadRequestIfNotificationHeaderBodyNotValid() {
        final EdcNotification request = EdcNotification.builder().header(EdcNotificationHeader.builder().build()).content(Map.of()).build();

        assertThrows(ResponseStatusException.class,
                () -> testee.receiveNotification(request));
    }

    @Test
    void shouldReturnBadRequestIfIncidentBpnNotInRequestBody() {
        final EdcNotification request = EdcNotification.builder().header(validHeader()).content(Map.of()).build();

        assertThrows(ResponseStatusException.class,
                () -> testee.receiveNotification(request));
    }

    @Test
    void shouldReturnBadRequestIfIncidentBpnNotInMockedMapResult() {
        final EdcNotification request = EdcNotification.builder().header(validHeader()).content(Map.of("incidentBpn", "BPN")).build();

        assertThrows(ResponseStatusException.class,
                () -> testee.receiveNotification(request));
    }

    EdcNotificationHeader validHeader() {
        return EdcNotificationHeader.builder()
                                      .notificationId(UUID.randomUUID().toString())
                                      .senderEdc("senderEdc")
                                      .senderBpn("senderBpn")
                                      .recipientBpn("recipientBpn")
                                      .replyAssetId("ess-response-asset")
                                      .replyAssetSubPath("")
                                      .notificationType("ess-supplier-request")
                                      .build();
    }
}