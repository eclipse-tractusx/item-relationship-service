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
package org.eclipse.tractusx.irs.controllers;

import static org.eclipse.tractusx.irs.util.TestMother.registerBatchOrder;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.component.BatchOrderResponse;
import org.eclipse.tractusx.irs.component.BatchResponse;
import org.eclipse.tractusx.irs.component.RegisterBatchOrder;
import org.eclipse.tractusx.irs.configuration.SecurityConfiguration;
import org.eclipse.tractusx.irs.common.auth.AuthorizationService;
import org.eclipse.tractusx.irs.services.CreationBatchService;
import org.eclipse.tractusx.irs.services.QueryBatchService;
import org.eclipse.tractusx.irs.services.timeouts.CancelBatchProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BatchController.class)
@Import(SecurityConfiguration.class)
class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreationBatchService creationBatchService;

    @MockBean
    private QueryBatchService queryBatchService;

    @MockBean
    private CancelBatchProcessingService cancelBatchProcessingService;

    @MockBean(name = "authorizationService")
    private AuthorizationService authorizationService;

    @Test
    void shouldReturnUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        this.mockMvc.perform(post("/irs/orders").contentType(MediaType.APPLICATION_JSON)
                                                .content(new ObjectMapper().writeValueAsString(
                                                        registerBatchOrder("urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b"))))
                    .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldReturnBadRequestWhenGlobalAssetIdWithWrongFormat() throws Exception {
        when(authorizationService.verifyBpn()).thenReturn(Boolean.TRUE);

        this.mockMvc.perform(post("/irs/orders").contentType(MediaType.APPLICATION_JSON)
                                                .content(new ObjectMapper().writeValueAsString(
                                                        registerBatchOrder("MALFORMED_GLOBAL_ASSET"))))
                    .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldReturnBadRequestWhenBatchSizeNotMod10Compliant() throws Exception {
        when(authorizationService.verifyBpn()).thenReturn(Boolean.TRUE);

        final RegisterBatchOrder registerBatchOrder = registerBatchOrder("MALFORMED_GLOBAL_ASSET");
        registerBatchOrder.setBatchSize(33);
        this.mockMvc.perform(post("/irs/orders").contentType(MediaType.APPLICATION_JSON)
                                                .content(new ObjectMapper().writeValueAsString(registerBatchOrder)))
                    .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldRegisterBatchOrder() throws Exception {
        when(authorizationService.verifyBpn()).thenReturn(Boolean.TRUE);

        this.mockMvc.perform(post("/irs/orders").contentType(MediaType.APPLICATION_JSON)
                                                .content(new ObjectMapper().writeValueAsString(
                                                        registerBatchOrder("urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b"))))
                    .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldReturnBatchOrder() throws Exception {
        when(authorizationService.verifyBpn()).thenReturn(Boolean.TRUE);

        final UUID orderId = UUID.randomUUID();
        when(queryBatchService.findOrderById(orderId)).thenReturn(BatchOrderResponse.builder().orderId(orderId).build());

        this.mockMvc.perform(get("/irs/orders/" + orderId))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(orderId.toString())));
    }

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldReturnBatch() throws Exception {
        final UUID orderId = UUID.randomUUID();
        final UUID batchId = UUID.randomUUID();
        when(authorizationService.verifyBpn()).thenReturn(Boolean.TRUE);
        when(queryBatchService.findBatchById(orderId, batchId)).thenReturn(
                BatchResponse.builder().batchId(batchId).orderId(orderId).build());

        this.mockMvc.perform(get("/irs/orders/" + orderId + "/batches/" + batchId))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(orderId.toString())))
                    .andExpect(content().string(containsString(batchId.toString())));
    }

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldCancelBatchOrder() throws Exception {
        when(authorizationService.verifyBpn()).thenReturn(Boolean.TRUE);

        final UUID orderId = UUID.randomUUID();
        when(queryBatchService.findOrderById(orderId)).thenReturn(BatchOrderResponse.builder().orderId(orderId).build());

        this.mockMvc.perform(put("/irs/orders/" + orderId))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(orderId.toString())));
    }
}
