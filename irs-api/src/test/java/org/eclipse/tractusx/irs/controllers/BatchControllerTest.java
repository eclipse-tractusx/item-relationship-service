/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.controllers;

import static org.eclipse.tractusx.irs.util.TestMother.registerBatchOrder;
import static org.eclipse.tractusx.irs.util.TestMother.registerBpnInvestigationBatchOrder;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.tractusx.irs.ControllerTest;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.component.BatchOrderResponse;
import org.eclipse.tractusx.irs.component.BatchResponse;
import org.eclipse.tractusx.irs.component.RegisterBatchOrder;
import org.eclipse.tractusx.irs.configuration.security.SecurityConfiguration;
import org.eclipse.tractusx.irs.services.CreationBatchService;
import org.eclipse.tractusx.irs.services.QueryBatchService;
import org.eclipse.tractusx.irs.services.timeouts.CancelBatchProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BatchController.class)
@Import(SecurityConfiguration.class)
class BatchControllerTest extends ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreationBatchService creationBatchService;

    @MockBean
    private QueryBatchService queryBatchService;

    @MockBean
    private CancelBatchProcessingService cancelBatchProcessingService;

    @Test
    void shouldReturnUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        when(authenticationService.getAuthentication(any(HttpServletRequest.class))).thenThrow(
                new BadCredentialsException("Wrong ApiKey"));

        this.mockMvc.perform(post("/irs/orders").contentType(MediaType.APPLICATION_JSON)
                                                .content(new ObjectMapper().writeValueAsString(registerBatchOrder(
                                                        "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b"))))
                    .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequestWhenGlobalAssetIdWithWrongFormat() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        this.mockMvc.perform(post("/irs/orders").contentType(MediaType.APPLICATION_JSON)
                                                .content(new ObjectMapper().writeValueAsString(
                                                        registerBatchOrder("MALFORMED_GLOBAL_ASSET"))))
                    .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBatchSizeNotMod10Compliant() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final RegisterBatchOrder registerBatchOrder = registerBatchOrder("MALFORMED_GLOBAL_ASSET");
        registerBatchOrder.setBatchSize(33);
        this.mockMvc.perform(post("/irs/orders").contentType(MediaType.APPLICATION_JSON)
                                                .content(new ObjectMapper().writeValueAsString(registerBatchOrder)))
                    .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRegisterRegularJobBatchOrder() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        this.mockMvc.perform(post("/irs/orders").contentType(MediaType.APPLICATION_JSON)
                                                .content(new ObjectMapper().writeValueAsString(registerBatchOrder(
                                                        "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b"))))
                    .andExpect(status().isCreated());
    }

    @Test
    void shouldRegisterEssJobBatchOrder() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        this.mockMvc.perform(post("/irs/ess/orders").contentType(MediaType.APPLICATION_JSON)
                                                    .content(new ObjectMapper().writeValueAsString(
                                                            registerBpnInvestigationBatchOrder(
                                                                    "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b"))))
                    .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnBatchOrder() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final UUID orderId = UUID.randomUUID();
        when(queryBatchService.findOrderById(orderId)).thenReturn(
                BatchOrderResponse.builder().orderId(orderId).build());

        this.mockMvc.perform(get("/irs/orders/" + orderId))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(orderId.toString())));
    }

    @Test
    void shouldReturnBatch() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final UUID orderId = UUID.randomUUID();
        final UUID batchId = UUID.randomUUID();
        when(queryBatchService.findBatchById(orderId, batchId)).thenReturn(
                BatchResponse.builder().batchId(batchId).orderId(orderId).build());

        this.mockMvc.perform(get("/irs/orders/" + orderId + "/batches/" + batchId))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(orderId.toString())))
                    .andExpect(content().string(containsString(batchId.toString())));
    }

    @Test
    void shouldCancelBatchOrder() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final UUID orderId = UUID.randomUUID();
        when(queryBatchService.findOrderById(orderId)).thenReturn(
                BatchOrderResponse.builder().orderId(orderId).build());

        this.mockMvc.perform(put("/irs/orders/" + orderId))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(orderId.toString())));
    }

    @Test
    void shouldReturnBadRequestWhenUuidMalformed() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final String orderId = "12345-1348-13-abcd-a-b-z-g-h";
        final String orderId2 = "zc311d29-5753-46d4-b32c-19b918ea93b0";
        final String orderId3 = "ac311d29-5753-4-d4-b32c-19b918ea93b0";

        this.mockMvc.perform(put("/irs/orders/" + orderId)).andExpect(status().isBadRequest());
        this.mockMvc.perform(put("/irs/orders/" + orderId2)).andExpect(status().isBadRequest());
        this.mockMvc.perform(put("/irs/orders/" + orderId3)).andExpect(status().isBadRequest());
    }
}
