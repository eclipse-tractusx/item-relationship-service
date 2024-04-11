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
import static org.eclipse.tractusx.irs.registryclient.TestMother.endpointDataReference;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.DISCOVERY_FINDER_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.DISCOVERY_FINDER_URL;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.EDC_DISCOVERY_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.TEST_BPN;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.postDiscoveryFinder200;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.postDiscoveryFinder404;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.postEdcDiscovery200;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.postEdcDiscovery404;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.LOOKUP_SHELLS_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.LOOKUP_SHELLS_TEMPLATE;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.SHELL_DESCRIPTORS_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.SHELL_DESCRIPTORS_TEMPLATE;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.getLookupShells200;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.getLookupShells200Empty;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.getLookupShells404;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.getShellDescriptor200;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.getShellDescriptor404;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.restTemplateProxy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.resilience4j.core.functions.Either;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.Shell;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryKey;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClientImpl;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;
import org.eclipse.tractusx.irs.registryclient.exceptions.ShellNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.web.client.RestTemplate;

@WireMockTest
class DecentralDigitalTwinRegistryServiceWiremockTest {
    private static final String PROXY_SERVER_HOST = "127.0.0.1";
    private final EdcEndpointReferenceRetriever edcEndpointReferenceRetrieverMock = mock(
            EdcEndpointReferenceRetriever.class);
    private DecentralDigitalTwinRegistryService decentralDigitalTwinRegistryService;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMockRuntimeInfo) {
        final RestTemplate restTemplate = restTemplateProxy(PROXY_SERVER_HOST, wireMockRuntimeInfo.getHttpPort());

        final var discoveryFinderClient = new DiscoveryFinderClientImpl(DISCOVERY_FINDER_URL, restTemplate);
        final var connectorEndpointsService = new ConnectorEndpointsService(discoveryFinderClient);
        final var endpointDataForConnectorsService = new EndpointDataForConnectorsService(
                edcEndpointReferenceRetrieverMock);
        final var decentralDigitalTwinRegistryClient = new DecentralDigitalTwinRegistryClient(restTemplate,
                SHELL_DESCRIPTORS_TEMPLATE, LOOKUP_SHELLS_TEMPLATE);
        decentralDigitalTwinRegistryService = new DecentralDigitalTwinRegistryService(connectorEndpointsService,
                endpointDataForConnectorsService, decentralDigitalTwinRegistryClient, new EdcConfiguration());
    }

    @Nested
    class FetchShellsTests {
        @Test
        void shouldDiscoverEDCAndRequestRegistry() throws RegistryServiceException, EdcRetrieverException {
            // Arrange
            givenThat(postDiscoveryFinder200());
            givenThat(postEdcDiscovery200());
            givenThat(getLookupShells200());
            givenThat(getShellDescriptor200());

            final var endpointDataReference = endpointDataReference("assetId");
            when(edcEndpointReferenceRetrieverMock.getEndpointReferencesForAsset(any(), any(), any(),
                    any())).thenReturn(List.of(CompletableFuture.completedFuture(endpointDataReference)));

            // Act
            final Collection<Either<Exception, Shell>> shells = decentralDigitalTwinRegistryService.fetchShells(
                    List.of(new DigitalTwinRegistryKey("testId", TEST_BPN)));

            // Assert
            assertThat(shells).hasSize(1);
            assertThat(shells.stream().findFirst().get().getOrNull().payload().getSubmodelDescriptors()).hasSize(3);
            verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
            verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
            verify(exactly(1), getRequestedFor(urlPathEqualTo(LOOKUP_SHELLS_PATH)));
            verify(exactly(1), getRequestedFor(urlPathMatching(SHELL_DESCRIPTORS_PATH + ".*")));
        }

        @Test
        void shouldThrowInCaseOfDiscoveryError() {
            // Arrange
            givenThat(postDiscoveryFinder404());
            final List<DigitalTwinRegistryKey> testId = List.of(new DigitalTwinRegistryKey("testId", TEST_BPN));

            // Act & Assert
            assertThatThrownBy(() -> decentralDigitalTwinRegistryService.fetchShells(testId)).isInstanceOf(
                    ShellNotFoundException.class);
            verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
        }

        @Test
        void shouldThrowInCaseOfEdcDiscoveryError() {
            // Arrange
            givenThat(postDiscoveryFinder200());
            givenThat(postEdcDiscovery404());
            final List<DigitalTwinRegistryKey> testId = List.of(new DigitalTwinRegistryKey("testId", TEST_BPN));

            // Act & Assert
            assertThatThrownBy(() -> decentralDigitalTwinRegistryService.fetchShells(testId)).isInstanceOf(
                    ShellNotFoundException.class);
            verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
            verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
        }

        @Test
        void shouldThrowInCaseOfLookupShellsError() throws EdcRetrieverException {
            // Arrange
            givenThat(postDiscoveryFinder200());
            givenThat(postEdcDiscovery200());

            final var endpointDataReference = endpointDataReference("assetId");
            when(edcEndpointReferenceRetrieverMock.getEndpointReferencesForAsset(any(), any(), any(),
                    any())).thenReturn(List.of(CompletableFuture.completedFuture(endpointDataReference)));

            givenThat(getLookupShells404());
            final List<DigitalTwinRegistryKey> testId = List.of(new DigitalTwinRegistryKey("testId", TEST_BPN));

            // Act & Assert
            assertThatThrownBy(() -> decentralDigitalTwinRegistryService.fetchShells(testId)).isInstanceOf(
                    ShellNotFoundException.class);
            verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
            verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
            verify(exactly(1), getRequestedFor(urlPathEqualTo(LOOKUP_SHELLS_PATH)));
        }

        @Test
        void shouldThrowInCaseOfShellDescriptorsError() throws EdcRetrieverException {
            // Arrange
            givenThat(postDiscoveryFinder200());
            givenThat(postEdcDiscovery200());

            final var endpointDataReference = endpointDataReference("assetId");
            when(edcEndpointReferenceRetrieverMock.getEndpointReferencesForAsset(any(), any(), any(),
                    any())).thenReturn(List.of(CompletableFuture.completedFuture(endpointDataReference)));

            givenThat(getLookupShells200());
            givenThat(getShellDescriptor404());
            final List<DigitalTwinRegistryKey> testId = List.of(new DigitalTwinRegistryKey("testId", TEST_BPN));

            // Act & Assert
            assertThatThrownBy(() -> decentralDigitalTwinRegistryService.fetchShells(testId)).isInstanceOf(
                    ShellNotFoundException.class);
            verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
            verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
            verify(exactly(1), getRequestedFor(urlPathEqualTo(LOOKUP_SHELLS_PATH)));
            verify(exactly(1), getRequestedFor(urlPathMatching(SHELL_DESCRIPTORS_PATH + ".*")));
        }

        @Test
        void shouldThrowExceptionOnEmptyShells() throws EdcRetrieverException {
            // Arrange
            givenThat(postDiscoveryFinder200());
            givenThat(postEdcDiscovery200());

            final var endpointDataReference = endpointDataReference("assetId");
            when(edcEndpointReferenceRetrieverMock.getEndpointReferencesForAsset(any(), any(), any(),
                    any())).thenReturn(List.of(CompletableFuture.completedFuture(endpointDataReference)));

            givenThat(getLookupShells200Empty());
            givenThat(getShellDescriptor404());
            final List<DigitalTwinRegistryKey> testId = List.of(new DigitalTwinRegistryKey("testId", TEST_BPN));

            // Act & Assert
            assertThatThrownBy(() -> decentralDigitalTwinRegistryService.fetchShells(testId)).isInstanceOf(
                    ShellNotFoundException.class);
            verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
            verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
            verify(exactly(1), getRequestedFor(urlPathEqualTo(LOOKUP_SHELLS_PATH)));
            verify(exactly(1), getRequestedFor(urlPathMatching(SHELL_DESCRIPTORS_PATH + ".*")));
        }
    }

    @Nested
    class LookupShellIdentifiersTests {

        @Test
        void lookupShellIdentifiers_oneEDC_oneDTR() throws RegistryServiceException, EdcRetrieverException {
            // Arrange
            givenThat(postDiscoveryFinder200());
            final List<String> edcUrls = List.of("https://test.edc.io");
            givenThat(postEdcDiscovery200(TEST_BPN, edcUrls));
            givenThat(getLookupShells200());

            // simulate endpoint data reference
            final var endpointDataReference = endpointDataReference("assetId");
            when(edcEndpointReferenceRetrieverMock.getEndpointReferencesForAsset(any(), any(), any(),
                    any())).thenReturn(List.of(CompletableFuture.completedFuture(endpointDataReference)));

            // Act
            final Collection<DigitalTwinRegistryKey> digitalTwinRegistryKeys = decentralDigitalTwinRegistryService.lookupShellIdentifiers(
                    TEST_BPN);

            // Assert
            assertThat(digitalTwinRegistryKeys).hasSize(1);
            assertThat(digitalTwinRegistryKeys.stream().findFirst().get().shellId()).isEqualTo(
                    "urn:uuid:21f7ebea-fa8a-410c-a656-bd9082e67dcf");
            verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
            verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
            verify(exactly(edcUrls.size()), getRequestedFor(urlPathEqualTo(LOOKUP_SHELLS_PATH)));
        }

        @ParameterizedTest(name = "{0}")
        @ArgumentsSource(NoOrFailedEndpointDataReferenceProvider.class)
        void lookupShellIdentifiers_multipleEDCs_oneDTR(String title,
                List<CompletableFuture<EndpointDataReference>> endpointDataReferenceForAssetFutures)
                throws RegistryServiceException, EdcRetrieverException {
            // Arrange
            givenThat(postDiscoveryFinder200());
            final String edc1Url = "https://test.edc1.io";
            final String edc2Url = "https://test.edc2.io";
            final List<String> edcUrls = List.of(edc1Url, edc2Url);
            givenThat(postEdcDiscovery200(TEST_BPN, edcUrls));
            givenThat(getLookupShells200());

            // simulate endpoint data reference
            final var endpointDataReference = endpointDataReference("assetId");
            when(edcEndpointReferenceRetrieverMock.getEndpointReferencesForAsset(eq(edc1Url), any(), any(),
                    any())).thenReturn(List.of(CompletableFuture.completedFuture(endpointDataReference)));
            when(edcEndpointReferenceRetrieverMock.getEndpointReferencesForAsset(eq(edc2Url), any(), any(),
                    any())).thenReturn(endpointDataReferenceForAssetFutures);

            // Act
            final Collection<DigitalTwinRegistryKey> digitalTwinRegistryKeys = decentralDigitalTwinRegistryService.lookupShellIdentifiers(
                    TEST_BPN);

            // Assert
            assertThat(digitalTwinRegistryKeys).hasSize(1);
            assertThat(digitalTwinRegistryKeys.stream().findFirst().get().shellId()).isEqualTo(
                    "urn:uuid:21f7ebea-fa8a-410c-a656-bd9082e67dcf");
            verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
            verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
            // because just one DTR
            verify(exactly(1), getRequestedFor(urlPathEqualTo(LOOKUP_SHELLS_PATH)));
        }

        public static class NoOrFailedEndpointDataReferenceProvider implements ArgumentsProvider {
            @Override
            public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
                return Stream.of(
                        // failed future
                        Arguments.of("given failed future", List.of(CompletableFuture.failedFuture(
                                new EdcRetrieverException(new EdcClientException(new RuntimeException("test")))))),
                        // no result
                        Arguments.of("given no result", Collections.emptyList()));
            }
        }

        @Test
        @Disabled("See GH Issue https://github.com/eclipse-tractusx/item-relationship-service/issues/431")
        void lookupShellIdentifiers_multipleEDCs_multipleDTRs() throws RegistryServiceException, EdcRetrieverException {
            // Arrange
            givenThat(postDiscoveryFinder200());
            final String edc1Url = "https://test.edc1.io";
            final String edc2Url = "https://test.edc2.io";
            final List<String> edcUrls = List.of(edc1Url, edc2Url);
            givenThat(postEdcDiscovery200(TEST_BPN, edcUrls));
            givenThat(getLookupShells200());

            // simulate endpoint data reference
            final var endpointDataReference1 = endpointDataReference("dtr1-assetId");
            final var endpointDataReference2 = endpointDataReference("dtr2-assetId");
            when(edcEndpointReferenceRetrieverMock.getEndpointReferencesForAsset(eq(edc1Url), any(), any(),
                    any())).thenReturn(List.of(CompletableFuture.completedFuture(endpointDataReference1)));
            when(edcEndpointReferenceRetrieverMock.getEndpointReferencesForAsset(eq(edc2Url), any(), any(),
                    any())).thenReturn(List.of(CompletableFuture.completedFuture(endpointDataReference2)));

            // Act & Assert
            final Collection<DigitalTwinRegistryKey> digitalTwinRegistryKeys = decentralDigitalTwinRegistryService.lookupShellIdentifiers(
                    TEST_BPN);

            // Assert
            assertThat(digitalTwinRegistryKeys).hasSize(1);
            assertThat(digitalTwinRegistryKeys.stream().findFirst().get().shellId()).isEqualTo(
                    "urn:uuid:21f7ebea-fa8a-410c-a656-bd9082e67dcf");
            verify(exactly(1), postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
            verify(exactly(1), postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
            // multiple DTR (one per EDC)
            verify(exactly(edcUrls.size()), getRequestedFor(urlPathEqualTo(LOOKUP_SHELLS_PATH)));
        }
    }
}
