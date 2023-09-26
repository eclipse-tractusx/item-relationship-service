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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class EdcDiscoveryFacadeTest {

    private final EdcDiscoveryMockConfig edcDiscoveryMockConfig = mock(EdcDiscoveryMockConfig.class);
    private final EdcDiscoveryFacade edcDiscoveryFacade = new EdcDiscoveryFacade(
            new EdcDiscoveryClientLocalStub(edcDiscoveryMockConfig));

    @Test
    void shouldReturnEdcBaseUrl() {
        // Arrange
        final String bpn = "BPNS000000000DDD";
        final String url = "http://edc-url";
        when(edcDiscoveryMockConfig.getMockEdcAddress()).thenReturn(Map.of(bpn, List.of(url)));

        // Act
        final Optional<String> edcBaseUrl = edcDiscoveryFacade.getEdcBaseUrl(bpn);

        // Assert
        assertThat(edcBaseUrl).isNotEmpty().contains(url);
    }

    @Test
    void shouldReturnEdcBaseUrls() {
        // Arrange
        final String bpn = "BPNS000000000DDD";
        final String url1 = "http://edc-url1";
        final String url2 = "http://edc-url2";
        when(edcDiscoveryMockConfig.getMockEdcAddress()).thenReturn(Map.of(bpn, List.of(url1, url2)));

        // Act
        final Optional<String> edcBaseUrl = edcDiscoveryFacade.getEdcBaseUrl(bpn);

        // Assert
        assertThat(edcBaseUrl).isNotEmpty().contains(url1);
    }

    @Test
    void shouldReturnResponseWithEmptyConnectorEndpointList() {
        // Arrange
        when(edcDiscoveryMockConfig.getMockEdcAddress()).thenReturn(Map.of());

        // Act
        final Optional<String> edcBaseUrl = edcDiscoveryFacade.getEdcBaseUrl("not_existing");

        // Assert
        assertThat(edcBaseUrl).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenClientReturnsNull() {
        // Arrange
        final EdcDiscoveryClientLocalStub clientMock = mock(EdcDiscoveryClientLocalStub.class);
        final EdcDiscoveryFacade edcDiscoveryFacade = new EdcDiscoveryFacade(clientMock);
        when(clientMock.getEdcBaseUrl(anyString())).thenReturn(null);

        // Act
        final Optional<String> edcBaseUrl = edcDiscoveryFacade.getEdcBaseUrl("not_existing");

        // Assert
        assertThat(edcBaseUrl).isEmpty();
    }

}
