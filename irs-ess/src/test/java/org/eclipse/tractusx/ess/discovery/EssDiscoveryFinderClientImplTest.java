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
package org.eclipse.tractusx.ess.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EssDiscoveryFinderClientImplTest {
    @Mock
    private RestTemplate restTemplate;

    @Test
    void shouldReturnEmptyListWhenDiscoveryReturnsNull() {
        // Arrange
        final String discoveryFinderUrl = "https://discovery.mock/api/administration/connectors/discovery/search";
        final String providerUrl = "https://discovery.mock/api/administration/connectors/discovery";
        final List<String> bpns = List.of("BPN123");
        final EssDiscoveryFinderClientImpl discoveryFinderClient = new EssDiscoveryFinderClientImpl(discoveryFinderUrl,
                restTemplate);
        when(restTemplate.postForObject(providerUrl, bpns, EdcDiscoveryResult[].class)).thenReturn(null);

        // Act
        final List<EdcDiscoveryResult> connectorEndpoints = discoveryFinderClient.findConnectorEndpoints(providerUrl,
                bpns);

        // Assert
        assertThat(connectorEndpoints).isNotNull().isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenDiscoveryReturnsEmpty() {
        // Arrange
        final String discoveryFinderUrl = "https://discovery.mock/api/administration/connectors/discovery/search";
        final String providerUrl = "https://discovery.mock/api/administration/connectors/discovery";
        final List<String> bpns = List.of("BPN123");
        final EssDiscoveryFinderClientImpl discoveryFinderClient = new EssDiscoveryFinderClientImpl(discoveryFinderUrl,
                restTemplate);
        final EdcDiscoveryResult[] discoveryResponse = { new EdcDiscoveryResult("BPN123",
                List.of("https://provider.edc"))
        };

        when(restTemplate.postForObject(providerUrl, bpns, EdcDiscoveryResult[].class)).thenReturn(discoveryResponse);

        // Act
        final List<EdcDiscoveryResult> connectorEndpoints = discoveryFinderClient.findConnectorEndpoints(providerUrl,
                bpns);

        // Assert
        assertThat(connectorEndpoints).isNotNull().hasSize(1);
    }
}