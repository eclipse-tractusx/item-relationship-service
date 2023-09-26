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
package org.eclipse.tractusx.irs.edc.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EdcDataPlaneClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EdcDataPlaneClient testee;

    @Test
    void shouldReturnValidData() {
        // arrange
        final var expectedData = "testdata";
        final EndpointDataReference dataRef = EndpointDataReference.Builder.newInstance()
                                                                           .authKey("testkey")
                                                                           .authCode("testcode")
                                                                           .endpoint("testEndpoint")
                                                                           .build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(
                ResponseEntity.of(Optional.of(expectedData)));

        // act
        final String result = testee.getData(dataRef, "");

        // assert
        assertThat(result).isEqualTo(expectedData);

    }

    @Test
    void shouldSendNotification() {
        // arrange
        final var expectedData = "testdata";
        final EndpointDataReference dataRef = EndpointDataReference.Builder.newInstance()
                                                                           .authKey("testkey")
                                                                           .authCode("testcode")
                                                                           .endpoint("testEndpoint")
                                                                           .build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(
                ResponseEntity.of(Optional.of(expectedData)));

        // act
        final EdcNotificationResponse result = testee.sendData(dataRef, "", EdcNotification.builder().build());

        // assert
        assertThat(result.deliveredSuccessfully()).isTrue();

    }
}