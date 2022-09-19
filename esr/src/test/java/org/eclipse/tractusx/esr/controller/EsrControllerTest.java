/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.esr.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.validation.ConstraintViolationException;

import org.eclipse.tractusx.esr.controller.model.BomLifecycle;
import org.eclipse.tractusx.esr.controller.model.CertificateType;
import org.eclipse.tractusx.esr.service.EsrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EsrController.class)
class EsrControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EsrService esrService;

    private final String globalAssetId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
    private final BomLifecycle bomLifecycle = BomLifecycle.AS_BUILT;
    private final CertificateType certificateType = CertificateType.ISO14001;

    @Test
    @WithMockUser
    void shouldGetEsrCertificateStatisticsForValidPath() throws Exception {
        final String path = "/esr/esr-statistics/" + globalAssetId + "/" + bomLifecycle.getName() + "/" + certificateType.name() + "/submodel";

        this.mockMvc.perform(get(path))
                    .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForWrongGlobalAssetId() {
        final String path = "/esr/esr-statistics/wrongGlobalAssetId/" + bomLifecycle.getName() + "/" + certificateType.name() + "/submodel";

        // MockMvc is not a real servlet environment, therefore it does not redirect
        // error responses to ErrorController, which produces error response.
        assertThatThrownBy(() -> mockMvc.perform(get(path)))
                .hasCauseInstanceOf(ConstraintViolationException.class).hasMessageContaining("globalAssetId");
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForWrongBomLifecycle() throws Exception {
        final String path = "/esr/esr-statistics/" + globalAssetId + "/notValidBomLifecycle/" + certificateType.name() + "/submodel";

        this.mockMvc.perform(get(path))
                    .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForWrongCertificateType() throws Exception {
        final String path = "/esr/esr-statistics/" + globalAssetId + "/" + bomLifecycle.getName() + "/wrongCertificateType/submodel";

        this.mockMvc.perform(get(path))
                    .andExpect(status().isBadRequest());
    }

}