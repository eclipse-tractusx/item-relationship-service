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
package org.eclipse.tractusx.irs.controllers;

import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithoutDepthAndAspect;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.client.HttpServerErrorException.InternalServerError;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.configuration.SecurityConfiguration;
import org.eclipse.tractusx.irs.services.AuthorizationService;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.eclipse.tractusx.irs.services.SemanticHubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@WebMvcTest(IrsController.class)
@Import(SecurityConfiguration.class)
class IrsExceptionHandlerTest {

    @MockBean
    private IrsItemGraphQueryService service;
    @MockBean
    private SemanticHubService semanticHubService;
    @MockBean(name = "authorizationService")
    private AuthorizationService authorizationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void handleAll() throws Exception {
        when(service.registerItemJob(any())).thenThrow(InternalServerError.class);
        when(authorizationService.verifyBpn()).thenReturn(Boolean.TRUE);

        this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(
                                                      registerJobWithoutDepthAndAspect())))
                    .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldReturn500WhenGetSemanticModelsFails() throws Exception {
        when(semanticHubService.getAllAspectModels()).thenThrow(InternalServerError.class);
        when(authorizationService.verifyBpn()).thenReturn(Boolean.TRUE);

        this.mockMvc.perform(get("/irs/aspectmodels").contentType(MediaType.APPLICATION_JSON)
                                                     .content(new ObjectMapper().writeValueAsString(
                                                             registerJobWithoutDepthAndAspect())))
                    .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldReturn400WhenProvidingBadInput() throws Exception {
        when(semanticHubService.getAllAspectModels()).thenThrow(IllegalArgumentException.class);
        when(authorizationService.verifyBpn()).thenReturn(Boolean.TRUE);

        this.mockMvc.perform(get("/irs/aspectmodels").contentType(MediaType.APPLICATION_JSON)
                                                     .content(new ObjectMapper().writeValueAsString(
                                                             registerJobWithoutDepthAndAspect())))
                    .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldReturn400WhenCatchingIllegalStateException() throws Exception {
        when(semanticHubService.getAllAspectModels()).thenThrow(IllegalStateException.class);
        when(authorizationService.verifyBpn()).thenReturn(Boolean.TRUE);

        this.mockMvc.perform(get("/irs/aspectmodels").contentType(MediaType.APPLICATION_JSON)
                                                     .content(new ObjectMapper().writeValueAsString(
                                                             registerJobWithoutDepthAndAspect())))
                    .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldReturn400WhenCatchingMethodArgumentTypeMismatchException() throws Exception {
        when(semanticHubService.getAllAspectModels()).thenThrow(MethodArgumentTypeMismatchException.class);
        when(authorizationService.verifyBpn()).thenReturn(Boolean.TRUE);

        this.mockMvc.perform(get("/irs/aspectmodels").contentType(MediaType.APPLICATION_JSON)
                                                     .content(new ObjectMapper().writeValueAsString(
                                                             registerJobWithoutDepthAndAspect())))
                    .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = IrsRoles.VIEW_IRS)
    void shouldReturn403WhenRightsAreMissing() throws Exception {
        when(semanticHubService.getAllAspectModels()).thenThrow(AccessDeniedException.class);

        this.mockMvc.perform(get("/irs/aspectmodels").contentType(MediaType.APPLICATION_JSON)
                                                     .content(new ObjectMapper().writeValueAsString(
                                                             registerJobWithoutDepthAndAspect())))
                    .andExpect(status().isForbidden());
    }
}