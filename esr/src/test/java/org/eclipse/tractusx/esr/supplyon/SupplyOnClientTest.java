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
package org.eclipse.tractusx.esr.supplyon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.net.URI;

import org.eclipse.tractusx.esr.controller.model.CertificateType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class SupplyOnClientTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final SupplyOnClient supplyOnClient = new SupplyOnClientClientImpl(restTemplate, "http://local", "subKey");

    @Test
    void shouldReturnEsrCertificateWithValidState() {
        // given
        final EsrCertificate expectedResponse = EsrCertificate.builder().certificateState(CertificateState.VALID).build();
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(EsrCertificate.class)))
                .willReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // when
        final EsrCertificate esrCertificate = supplyOnClient.getESRCertificate("BPNL00000003AYRE", "BPNL00000003XXX", CertificateType.ISO14001.name());

        // then
        assertThat(esrCertificate).isEqualTo(expectedResponse);
    }

}
