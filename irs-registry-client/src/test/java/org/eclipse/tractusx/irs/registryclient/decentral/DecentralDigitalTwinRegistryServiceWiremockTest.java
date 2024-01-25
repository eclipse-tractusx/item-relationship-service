/********************************************************************************
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
package org.eclipse.tractusx.irs.registryclient.decentral;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.DISCOVERY_FINDER_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.DISCOVERY_FINDER_URL;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.EDC_DISCOVERY_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.TEST_BPN;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.postDiscoveryFinder200;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.postDiscoveryFinder404;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.postEdcDiscovery200;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.postEdcDiscovery404;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.DATAPLANE_URL;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.LOOKUP_SHELLS_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.LOOKUP_SHELLS_TEMPLATE;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.SHELL_DESCRIPTORS_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.SHELL_DESCRIPTORS_TEMPLATE;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.getLookupShells200;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.getLookupShells200Empty;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.getLookupShells404;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.getShellDescriptor200;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.getShellDescriptor404;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.restTemplateProxy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryKey;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClientImpl;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@WireMockTest
class DecentralDigitalTwinRegistryServiceWiremockTest {
    private static final String PROXY_SERVER_HOST = "127.0.0.1";
    private final EdcEndpointReferenceRetriever edcSubmodelFacadeMock = mock(EdcEndpointReferenceRetriever.class);
    private DecentralDigitalTwinRegistryService decentralDigitalTwinRegistryService;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMockRuntimeInfo) throws EdcRetrieverException {
        final RestTemplate restTemplate = restTemplateProxy(PROXY_SERVER_HOST, wireMockRuntimeInfo.getHttpPort());

        final var discoveryFinderClient = new DiscoveryFinderClientImpl(DISCOVERY_FINDER_URL, restTemplate);
        final var connectorEndpointsService = new ConnectorEndpointsService(discoveryFinderClient);
        final var endpointDataForConnectorsService = new EndpointDataForConnectorsService(edcSubmodelFacadeMock);
        final var decentralDigitalTwinRegistryClient = new DecentralDigitalTwinRegistryClient(restTemplate,
                SHELL_DESCRIPTORS_TEMPLATE, LOOKUP_SHELLS_TEMPLATE);
        decentralDigitalTwinRegistryService = new DecentralDigitalTwinRegistryService(connectorEndpointsService,
                endpointDataForConnectorsService, decentralDigitalTwinRegistryClient);
        final var endpointDataReference = EndpointDataReference.Builder.newInstance()
                                                                       .endpoint(DATAPLANE_URL)
                                                                       .authCode("TEST")
                                                                       .authKey("X-API-KEY")
                                                                       .properties(Map.of())
                                                                       .build();
        when(edcSubmodelFacadeMock.getEndpointReferenceForAsset(any(), any(), any())).thenReturn(endpointDataReference);
    }

    @Test
    void shouldDiscoverEDCAndRequestRegistry() throws RegistryServiceException {
        // Arrange
        givenThat(postDiscoveryFinder200());
        givenThat(postEdcDiscovery200());
        givenThat(getLookupShells200());
        givenThat(getShellDescriptor200());

        // Act
        final Collection<AssetAdministrationShellDescriptor> assetAdministrationShellDescriptors = decentralDigitalTwinRegistryService.fetchShells(
                List.of(new DigitalTwinRegistryKey("testId", TEST_BPN)));

        // Assert
        assertThat(assetAdministrationShellDescriptors).hasSize(1);
        assertThat(assetAdministrationShellDescriptors.stream().findFirst().get().getSubmodelDescriptors()).hasSize(3);
        verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
        verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
        verify(exactly(1), getRequestedFor(urlPathEqualTo(LOOKUP_SHELLS_PATH)));
        verify(exactly(1), getRequestedFor(urlPathMatching(SHELL_DESCRIPTORS_PATH + ".*")));
    }

    @Test
    void shouldThrowHttpClientExceptionInCaseOfDiscoveryError() {
        // Arrange
        givenThat(postDiscoveryFinder404());
        final List<DigitalTwinRegistryKey> testId = List.of(new DigitalTwinRegistryKey("testId", TEST_BPN));

        // Act & Assert
        // TODO fix implementation to not throw HttpClientErrorException$NotFound
        assertThatThrownBy(() -> decentralDigitalTwinRegistryService.fetchShells(testId)).isInstanceOf(
                HttpClientErrorException.class);
        verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
    }

    @Test
    void shouldThrowHttpClientExceptionInCaseOfEdcDiscoveryError() {
        // Arrange
        givenThat(postDiscoveryFinder200());
        givenThat(postEdcDiscovery404());
        final List<DigitalTwinRegistryKey> testId = List.of(new DigitalTwinRegistryKey("testId", TEST_BPN));

        // Act & Assert
        // TODO fix implementation to not throw HttpClientErrorException$NotFound
        assertThatThrownBy(() -> decentralDigitalTwinRegistryService.fetchShells(testId)).isInstanceOf(
                HttpClientErrorException.class);
        verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
        verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
    }

    @Test
    void shouldThrowHttpClientExceptionInCaseOfLookupShellsError() {
        // Arrange
        givenThat(postDiscoveryFinder200());
        givenThat(postEdcDiscovery200());
        givenThat(getLookupShells404());
        final List<DigitalTwinRegistryKey> testId = List.of(new DigitalTwinRegistryKey("testId", TEST_BPN));

        // Act & Assert
        // TODO fix implementation to not throw HttpClientErrorException$NotFound
        assertThatThrownBy(() -> decentralDigitalTwinRegistryService.fetchShells(testId)).isInstanceOf(
                HttpClientErrorException.class);
        verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
        verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
        verify(exactly(1), getRequestedFor(urlPathEqualTo(LOOKUP_SHELLS_PATH)));
    }

    @Test
    void shouldThrowHttpClientExceptionInCaseOfShellDescriptorsError() {
        // Arrange
        givenThat(postDiscoveryFinder200());
        givenThat(postEdcDiscovery200());
        givenThat(getLookupShells200());
        givenThat(getShellDescriptor404());
        final List<DigitalTwinRegistryKey> testId = List.of(new DigitalTwinRegistryKey("testId", TEST_BPN));

        // Act & Assert
        // TODO fix implementation to not throw HttpClientErrorException$NotFound
        assertThatThrownBy(() -> decentralDigitalTwinRegistryService.fetchShells(testId)).isInstanceOf(
                HttpClientErrorException.class);
        verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
        verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
        verify(exactly(1), getRequestedFor(urlPathEqualTo(LOOKUP_SHELLS_PATH)));
        verify(exactly(1), getRequestedFor(urlPathMatching(SHELL_DESCRIPTORS_PATH + ".*")));
    }

    @Test
    void shouldThrowExceptionOnEmptyShells() {
        // Arrange
        givenThat(postDiscoveryFinder200());
        givenThat(postEdcDiscovery200());
        givenThat(getLookupShells200Empty());
        givenThat(getShellDescriptor404());
        final List<DigitalTwinRegistryKey> testId = List.of(new DigitalTwinRegistryKey("testId", TEST_BPN));

        // Act & Assert
        // TODO fix implementation to not throw HttpClientErrorException$NotFound
        assertThatThrownBy(() -> decentralDigitalTwinRegistryService.fetchShells(testId)).isInstanceOf(
                HttpClientErrorException.class);
        verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
        verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
        verify(exactly(1), getRequestedFor(urlPathEqualTo(LOOKUP_SHELLS_PATH)));
        verify(exactly(1), getRequestedFor(urlPathMatching(SHELL_DESCRIPTORS_PATH + ".*")));
    }
}
