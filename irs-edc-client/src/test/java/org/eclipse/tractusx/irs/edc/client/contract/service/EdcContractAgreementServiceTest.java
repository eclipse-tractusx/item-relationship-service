/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.edc.client.contract.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.contract.model.EdcContractAgreementNegotiationResponse;
import org.eclipse.tractusx.irs.edc.client.contract.model.EdcContractAgreementsResponse;
import org.eclipse.tractusx.irs.edc.client.contract.model.exception.ContractAgreementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EdcContractAgreementServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Spy
    private EdcConfiguration edcConfiguration;

    private EdcContractAgreementService edcContractAgreementService;

    @BeforeEach
    void setUp() {
        edcConfiguration.getControlplane().setEndpoint(new EdcConfiguration.ControlplaneConfig.EndpointConfig());
        edcConfiguration.getControlplane()
                        .getEndpoint()
                        .setData("https://irs-consumer-controlplane.dev.demo.net/data/management");
        edcConfiguration.getControlplane().getEndpoint().setContractAgreements("/v2/contractagreements");
        this.edcContractAgreementService = new EdcContractAgreementService(edcConfiguration, restTemplate);
    }

    @Test
    void shouldReturnContractAgreements() throws ContractAgreementException {
        //GIVEN
        List<String> contractAgreementIds = List.of("contractAgreementId");

        final EdcContractAgreementsResponse[] edcContractAgreementsResponse = new EdcContractAgreementsResponse[1];
        edcContractAgreementsResponse[0] = EdcContractAgreementsResponse.builder().contractAgreementId("id")
                                                                        .assetId("assetId")
                                                                        .consumerId("consumerId")
                                                                        .providerId("providerId")
                                                                        .build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(),
                eq(EdcContractAgreementsResponse[].class))).thenReturn(
                ResponseEntity.ok(edcContractAgreementsResponse));

        //WHEN
        final List<EdcContractAgreementsResponse> contractAgreements = edcContractAgreementService.getContractAgreements(
                contractAgreementIds);

        //THEN
        Mockito.verify(restTemplate)
               .exchange(
                       eq("https://irs-consumer-controlplane.dev.demo.net/data/management/v2/contractagreements/request"),
                       any(), any(), eq(EdcContractAgreementsResponse[].class));
        assertThat(contractAgreements).isNotNull();
    }

    @Test
    void shouldThrowContractAgreementExceptionWhenResponseBodyIsEmtpy() {
        //GIVEN
        List<String> contractAgreementIds = List.of("contractAgreementId");

        when(restTemplate.exchange(anyString(), any(), any(), eq(EdcContractAgreementsResponse[].class))).thenReturn(
                ResponseEntity.ok(new EdcContractAgreementsResponse[0]));

        //WHEN
        final ContractAgreementException contractAgreementException = assertThrows(ContractAgreementException.class,
                () -> edcContractAgreementService.getContractAgreements(contractAgreementIds));

        //THEN
        Mockito.verify(restTemplate)
               .exchange(
                       eq("https://irs-consumer-controlplane.dev.demo.net/data/management/v2/contractagreements/request"),
                       any(), any(), eq(EdcContractAgreementsResponse[].class));
        assertThat(contractAgreementException.getMessage()).startsWith("Empty message body on edc response:");
    }

    @Test
    void shouldReturnContractAgreementNegotiation() {
        //GIVEN
        String contractAgreementId = "contractAgreementId";

        final EdcContractAgreementNegotiationResponse contractAgreementNegotiationMock = EdcContractAgreementNegotiationResponse.builder()
                                                                                                                                .correlationId(
                                                                                                                                        "id")
                                                                                                                                .counterPartyId(
                                                                                                                                        "")
                                                                                                                                .counterPartyAddress(
                                                                                                                                        "")
                                                                                                                                .protocol(
                                                                                                                                        "")
                                                                                                                                .build();
        when(restTemplate.exchange(anyString(), any(), any(),
                eq(EdcContractAgreementNegotiationResponse.class))).thenReturn(
                ResponseEntity.ok(contractAgreementNegotiationMock));

        //WHEN
        final EdcContractAgreementNegotiationResponse edcContractAgreementNegotiationResponse = edcContractAgreementService.getContractAgreementNegotiation(
                contractAgreementId);

        //THEN
        Mockito.verify(restTemplate)
               .exchange(
                       eq("https://irs-consumer-controlplane.dev.demo.net/data/management/v2/contractagreements/contractAgreementId/negotiation"),
                       any(), any(), eq(EdcContractAgreementNegotiationResponse.class));
        assertThat(edcContractAgreementNegotiationResponse).isNotNull();
    }
}