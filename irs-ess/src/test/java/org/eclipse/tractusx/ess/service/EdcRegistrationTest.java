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
package org.eclipse.tractusx.ess.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EdcRegistrationTest {

    private EdcRegistration testee;

    @Mock
    private RestTemplate restTemplate;
    private String edcUrl;
    private String managementPath;

    @BeforeEach
    void setUp() {
        edcUrl = "http://localhost";
        managementPath = "/path";
        testee = new EdcRegistration(restTemplate, edcUrl, "", "", "", managementPath);
    }

    @Test
    void shouldRegisterAssets_whenGetRequestReturnsNoResults() {
        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(),
                Mockito.eq(String.class))).thenReturn(ResponseEntity.ok().body("[]"));

        testee.registerEdcAsset();

        verify(restTemplate, times(3)).exchange(Mockito.eq(edcUrl + managementPath + "/assets/request"),
                Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class));
        verify(restTemplate, times(3)).exchange(Mockito.eq(edcUrl + managementPath + "/assets"),
                Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class));
        verify(restTemplate, times(3)).exchange(Mockito.eq(edcUrl + managementPath + "/policydefinitions"),
                Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class));
        verify(restTemplate, times(3)).exchange(Mockito.eq(edcUrl + managementPath + "/contractdefinitions"),
                Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class));
    }

    @Test
    void shouldNotRegisterAssets_whenGetRequestReturnsResults() {
        when(restTemplate.exchange(Mockito.eq(edcUrl + managementPath + "/assets/request"),
                Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class))).thenReturn(
                ResponseEntity.ok().body("[{}]"));

        testee.registerEdcAsset();

        verify(restTemplate, times(3)).exchange(Mockito.eq(edcUrl + managementPath + "/assets/request"),
                Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class));
        verify(restTemplate, never()).exchange(Mockito.eq(edcUrl + managementPath + "/assets"),
                Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class));
        verify(restTemplate, never()).exchange(Mockito.eq(edcUrl + managementPath + "/policydefinitions"),
                Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class));
        verify(restTemplate, never()).exchange(Mockito.eq(edcUrl + managementPath + "/contractdefinitions"),
                Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class));
    }

}