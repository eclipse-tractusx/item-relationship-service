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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.configuration.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BatchController.class)
@Import(SecurityConfiguration.class)
class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        this.mockMvc.perform(post("/irs/orders").contentType(MediaType.APPLICATION_JSON)
                                                .content(new ObjectMapper().writeValueAsString(
                                                        registerBatchOrder("urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b"))))
                    .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void shouldReturnBadRequestWhenGlobalAssetIdWithWrongFormat() throws Exception {
        this.mockMvc.perform(post("/irs/orders").contentType(MediaType.APPLICATION_JSON)
                                                .content(new ObjectMapper().writeValueAsString(
                                                        registerBatchOrder("MALFORMED_GLOBAL_ASSET"))))
                    .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void shouldRegisterBatchOrder() throws Exception {
        this.mockMvc.perform(post("/irs/orders").contentType(MediaType.APPLICATION_JSON)
                                                .content(new ObjectMapper().writeValueAsString(
                                                        registerBatchOrder("urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b"))))
                    .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void shouldReturnBatchOrder() throws Exception {
        final UUID orderId = UUID.randomUUID();

        this.mockMvc.perform(get("/irs/orders/" + orderId))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(orderId.toString())));
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void shouldReturnBatch() throws Exception {
        final UUID orderId = UUID.randomUUID();
        final UUID batchId = UUID.randomUUID();

        this.mockMvc.perform(get("/irs/orders/" + orderId + "/batches/" + batchId))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(orderId.toString())))
                    .andExpect(content().string(containsString(batchId.toString())));
    }
}
