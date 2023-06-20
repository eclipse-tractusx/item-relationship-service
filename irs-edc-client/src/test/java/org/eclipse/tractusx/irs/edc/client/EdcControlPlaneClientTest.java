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
package org.eclipse.tractusx.irs.edc.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationId;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationRequest;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessId;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessRequest;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EdcControlPlaneClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private final AsyncPollingService pollingService = new AsyncPollingService(Clock.systemUTC(),
            Executors.newSingleThreadScheduledExecutor());
    @Spy
    private final EdcConfiguration config = new EdcConfiguration();
    @InjectMocks
    private EdcControlPlaneClient testee;

    @BeforeEach
    void setUp() {
        config.setControlplane(new EdcConfiguration.ControlplaneConfig());
        config.getControlplane().setEndpoint(new EdcConfiguration.ControlplaneConfig.EndpointConfig());
        config.getControlplane().getEndpoint().setData("https://irs-consumer-controlplane.dev.demo.catena-x.net/data");
        config.getControlplane().setRequestTtl(Duration.ofMinutes(10));
        config.getControlplane().setApiKey(new EdcConfiguration.ControlplaneConfig.ApiKeyConfig());
        config.getControlplane().setCatalogPageSize(10);

        config.setSubmodel(new EdcConfiguration.SubmodelConfig());
        config.getSubmodel().setPath("/submodel");
        config.getSubmodel().setUrnPrefix("/urn");
        config.getSubmodel().setRequestTtl(Duration.ofMinutes(10));
    }

    @Test
    void shouldReturnValidCatalog() {
        // arrange
        final var catalog = mock(Catalog.class);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(Catalog.class))).thenReturn(ResponseEntity.of(Optional.of(catalog)));

        // act
        final var result = testee.getCatalog("test", 0);

        // assert
        assertThat(result).isEqualTo(catalog);
    }

    @Test
    void shouldReturnValidCatalogUsingFilters() {
        // arrange
        final var catalog = mock(Catalog.class);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(Catalog.class))).thenReturn(ResponseEntity.of(Optional.of(catalog)));

        // act
        final var result = testee.getCatalogWithFilter("test", "asset:prop:type", "data.core.digitalTwinRegistry");

        // assert
        assertThat(result).isEqualTo(catalog);
    }

    @Test
    void shouldReturnValidNegotiationId() {
        // arrange
        final var negotiationId = NegotiationId.builder().value("test").build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(NegotiationId.class)))
                .thenReturn(ResponseEntity.of(Optional.of(negotiationId)));
        final NegotiationRequest request = NegotiationRequest.builder().build();

        // act
        final var result = testee.startNegotiations(request);

        // assert
        Assertions.assertThat(result).isEqualTo(negotiationId);
    }

    @Test
    void shouldReturnConfirmedNegotiationResult() throws Exception {
        // arrange
        final var negotiationId = NegotiationId.builder().value("test").build();
        final var negotiationResult = NegotiationResponse.builder()
                                                         .contractAgreementId("testContractId")
                                                         .state("CONFIRMED")
                                                         .build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(NegotiationResponse.class)))
                .thenReturn(ResponseEntity.of(Optional.of(negotiationResult)));

        // act
        final var result = testee.getNegotiationResult(negotiationId);
        final NegotiationResponse response = result.get(5, TimeUnit.SECONDS);

        // assert
        assertThat(response).isEqualTo(negotiationResult);
    }

    @Test
    void shouldReturnValidTransferProcessId() {
        // arrange
        final var processId = TransferProcessId.builder().value("test").build();
        final var request = TransferProcessRequest.builder().requestId("testRequest").build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(TransferProcessId.class)))
                .thenReturn(ResponseEntity.of(Optional.of(processId)));

        // act
        final var result = testee.startTransferProcess(request);

        // assert
        Assertions.assertThat(result).isEqualTo(processId);
    }

    @Test
    void shouldReturnCompletedTransferProcessResult() throws Exception {
        // arrange
        final var processId = TransferProcessId.builder().value("test").build();
        final var response = TransferProcessResponse.builder().responseId("testResponse").state("COMPLETED").build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(TransferProcessResponse.class)))
                .thenReturn(ResponseEntity.of(Optional.of(response)));

        // act
        final var result = testee.getTransferProcess(processId);
        final TransferProcessResponse transferProcessResponse = result.get(5, TimeUnit.SECONDS);

        // assert
        assertThat(transferProcessResponse).isEqualTo(response);
    }
}