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
package org.eclipse.tractusx.ess.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class EdcDiscoveryClientImplTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final EdcDiscoveryClient edcDiscoveryClient = new EdcDiscoveryClientImpl(restTemplate,
            "url/api/administration/connectors/discovery");

    @Test
    void shouldCallExternalServiceOnceAndGetEdcAddressResponse() {
        // Arrange
        final String bpn = "BPNS000000000DDD";
        final EdcAddressResponse[] mockResponse = new EdcAddressResponse[] { EdcAddressResponse.builder()
                                                                                               .bpn(bpn)
                                                                                               .connectorEndpoint(
                                                                                                       Collections.singletonList(
                                                                                                               "http://edc-address.com")).build()
        };
        doReturn(mockResponse).when(restTemplate).postForObject(any(), any(), eq(EdcAddressResponse[].class));

        // Act
        final List<EdcAddressResponse> edcAddressResponse = List.of(edcDiscoveryClient.getEdcBaseUrl(bpn));

        // Assert
        assertThat(edcAddressResponse).isNotNull().hasSize(1);
        assertThat(edcAddressResponse.get(0).getBpn()).isEqualTo(bpn);
        assertThat(edcAddressResponse.get(0).getConnectorEndpoint()).isNotEmpty();
        verify(this.restTemplate, times(1)).postForObject(any(), any(), eq(EdcAddressResponse[].class));
    }

    @Test
    void shouldCallExternalServiceWithTwoEndpointsOnceAndGetEdcAddressResponse() {
        // Arrange
        final String bpn = "BPNS000000000DDD";
        final EdcAddressResponse[] mockResponse = new EdcAddressResponse[] { EdcAddressResponse.builder()
                                                                                               .bpn(bpn)
                                                                                               .connectorEndpoint(
                                                                                                       List.of("http://edc-address.com",
                                                                                                               "http://edc-address1.com")).build()
        };
        doReturn(mockResponse).when(restTemplate).postForObject(any(), any(), eq(EdcAddressResponse[].class));

        // Act
        final List<EdcAddressResponse> edcAddressResponse = List.of(edcDiscoveryClient.getEdcBaseUrl(bpn));

        // Assert
        assertThat(edcAddressResponse).isNotNull().hasSize(1);
        assertThat(edcAddressResponse.get(0).getBpn()).isEqualTo(bpn);
        assertThat(edcAddressResponse.get(0).getConnectorEndpoint()).hasSize(2);
        assertThat(edcAddressResponse.get(0).getConnectorEndpoint().get(0)).isEqualTo("http://edc-address.com");
        assertThat(edcAddressResponse.get(0).getConnectorEndpoint().get(1)).isEqualTo("http://edc-address1.com");
        verify(this.restTemplate, times(1)).postForObject(any(), any(), eq(EdcAddressResponse[].class));
    }

    @Test
    void shouldReturnNullWhenRestTemplateReturnsNull() {
        // Arrange
        final String bpn = "BPNS000000000DDD";
        doReturn(null).when(restTemplate).postForObject(any(), any(), eq(EdcAddressResponse[].class));

        // Act
        final EdcAddressResponse[] edcBaseUrl = edcDiscoveryClient.getEdcBaseUrl(bpn);

        // Assert
        assertThat(edcBaseUrl).isNull();
        verify(this.restTemplate, times(1)).postForObject(any(), any(), eq(EdcAddressResponse[].class));
    }
}
