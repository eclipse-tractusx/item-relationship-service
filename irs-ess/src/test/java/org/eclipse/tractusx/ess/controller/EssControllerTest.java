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
package org.eclipse.tractusx.ess.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.ess.service.EssService;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EssController.class)
class EssControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EssService essService;

    private final String path = "/ess/bpn/investigations";
    private final String globalAssetId = "urn:uuid:d3c0bf85-d44f-47c5-990d-fec8a36065c6";
    private final String bpn = "BPNS000000000DDD";

    @Test
    @WithMockUser(authorities = "view_irs")
    void shouldRegisterBpnInvestigationForValidRequest() throws Exception {
        when(essService.startIrsJob(any(RegisterBpnInvestigationJob.class))).thenReturn(
                JobHandle.builder().id(UUID.randomUUID()).build());

        this.mockMvc.perform(post(path).with(csrf())
                                       .contentType(MediaType.APPLICATION_JSON)
                                       .content(new ObjectMapper().writeValueAsString(
                                               reqBody(globalAssetId, List.of(bpn))))).andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void shouldGetJob() throws Exception {
        final String jobId = UUID.randomUUID().toString();
        when(essService.getIrsJob(jobId)).thenReturn(Jobs.builder().build());

        this.mockMvc.perform(get(path + "/" + jobId)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void shouldReturnBadRequestForWrongGlobalAssetId() throws Exception {
        this.mockMvc.perform(post(path).with(csrf())
                                       .contentType(MediaType.APPLICATION_JSON)
                                       .content(new ObjectMapper().writeValueAsString(
                                               reqBody("wrongGlobalAssetId", List.of(bpn)))))
                    .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void shouldReturnBadRequestForWrongBpn() throws Exception {
        this.mockMvc.perform(post(path).with(csrf())
                                       .contentType(MediaType.APPLICATION_JSON)
                                       .content(new ObjectMapper().writeValueAsString(
                                               reqBody(globalAssetId, List.of(bpn, "WRONG_BPN")))))
                    .andExpect(status().isBadRequest());
    }

    RegisterBpnInvestigationJob reqBody(final String globalAssetId, final List<String> bpns) {
        return RegisterBpnInvestigationJob.builder()
                                          .key(PartChainIdentificationKey.builder()
                                                                         .globalAssetId(globalAssetId)
                                                                         .bpn(bpn)
                                                                         .build())
                                          .incidentBpns(bpns)
                                          .build();
    }

}