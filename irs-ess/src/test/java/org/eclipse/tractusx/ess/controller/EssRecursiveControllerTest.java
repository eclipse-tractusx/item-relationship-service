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
package org.eclipse.tractusx.ess.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.ess.service.EssRecursiveService;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationHeader;
import org.eclipse.tractusx.irs.edc.client.model.notification.InvestigationNotificationContent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EssRecursiveController.class)
class EssRecursiveControllerTest {

    private final String path = "/ess/notification/receive-recursive";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EssRecursiveService essRecursiveService;

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldHandleRecursiveBpnInvestigationByNotification() throws Exception {

        this.mockMvc.perform(post(path).with(csrf())
                                       .contentType(MediaType.APPLICATION_JSON)
                                       .content(new ObjectMapper().writeValueAsString(prepareNotification())))
                    .andExpect(status().isCreated());
    }

    private EdcNotification<InvestigationNotificationContent> prepareNotification() {
        final EdcNotificationHeader header = EdcNotificationHeader.builder()
                                                                  .notificationId("notification-id")
                                                                  .senderEdc("senderEdc")
                                                                  .senderBpn("senderBpn")
                                                                  .recipientBpn("recipientBpn")
                                                                  .replyAssetId("ess-response-asset")
                                                                  .replyAssetSubPath("")
                                                                  .notificationType("ess-supplier-request")
                                                                  .build();

        return EdcNotification.<InvestigationNotificationContent>builder()
                              .header(header)
                              .content(InvestigationNotificationContent.builder()
                                                                       .concernedCatenaXIds(List.of("cat1", "cat2"))
                                                                       .incidentBpn("BPNS000000000BBB")
                                                                       .build())
                              .build();
    }

}