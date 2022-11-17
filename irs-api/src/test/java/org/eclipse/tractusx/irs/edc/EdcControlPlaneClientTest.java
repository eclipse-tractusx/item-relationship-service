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
package org.eclipse.tractusx.irs.edc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.dataspaceconnector.spi.types.domain.catalog.Catalog;
import org.eclipse.tractusx.irs.edc.model.NegotiationId;
import org.eclipse.tractusx.irs.edc.model.NegotiationRequest;
import org.eclipse.tractusx.irs.edc.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.model.TransferProcessId;
import org.eclipse.tractusx.irs.edc.model.TransferProcessRequest;
import org.eclipse.tractusx.irs.edc.model.TransferProcessResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EdcControlPlaneClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EdcControlPlaneClient testee;



    @Test
    void shouldReturnValidCatalog() {
        // arrange
        final var catalog = mock(Catalog.class);
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(Catalog.class), any(Object.class))).thenReturn(
                ResponseEntity.of(Optional.of(catalog)));

        // act
        final var result = testee.getCatalog("test");

        // assert
        assertThat(result).isEqualTo(catalog);
    }

    @Test
    void shouldReturnValidNegotiationId() {
        // arrange
        final var negotiationId = NegotiationId.builder().id("test").build();
        when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(NegotiationId.class),
                any(Object.class))).thenReturn(ResponseEntity.of(Optional.of(negotiationId)));
        final NegotiationRequest request = NegotiationRequest.builder().build();

        // act
        final var result = testee.startNegotiations(request);

        // assert
        assertThat(result).isEqualTo(negotiationId);
    }

    @Test
    void shouldReturnConfirmedNegotiationResult() {
        // arrange
        final var negotiationId = NegotiationId.builder().id("test").build();
        final var negotiationResult = NegotiationResponse.builder()
                                                         .contractAgreementId("testContractId")
                                                         .state("CONFIRMED")
                                                         .build();
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(NegotiationResponse.class),
                any(Object.class))).thenReturn(ResponseEntity.of(Optional.of(negotiationResult)));

        // act
        final var result = testee.getNegotiationResult(negotiationId);

        // assert
        assertThat(result).isEqualTo(negotiationResult);
    }

    @Test
    void shouldReturnValidTransferProcessId() {
        // arrange
        final var processId = TransferProcessId.builder().id("test").build();
        final var request = TransferProcessRequest.builder().id("testRequest").build();
        when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(TransferProcessId.class),
                any(Object.class))).thenReturn(ResponseEntity.of(Optional.of(processId)));

        // act
        final var result = testee.startTransferProcess(request);

        // assert
        assertThat(result).isEqualTo(processId);
    }

    @Test
    void shouldReturnCompletedTransferProcessResult() {
        // arrange
        final var processId = TransferProcessId.builder().id("test").build();
        final var response = TransferProcessResponse.builder().id("testResponse").state("COMPLETED").build();
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(TransferProcessResponse.class),
                any(Object.class))).thenReturn(ResponseEntity.of(Optional.of(response)));

        // act
        final var result = testee.getTransferProcess(processId);

        // assert
        assertThat(result).isEqualTo(response);
    }
}