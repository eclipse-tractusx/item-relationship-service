/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
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

import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.tractusx.irs.registryclient.TestMother.endpointDataReference;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.github.resilience4j.core.functions.Either;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.eclipse.tractusx.irs.common.util.concurrent.ResultFinder;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.Shell;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryKey;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;
import org.eclipse.tractusx.irs.registryclient.exceptions.ShellNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DecentralDigitalTwinRegistryServiceTest {

    private final ConnectorEndpointsService connectorEndpointsService = mock(ConnectorEndpointsService.class);
    private final EndpointDataForConnectorsService endpointDataForConnectorsService = mock(
            EndpointDataForConnectorsService.class);

    private final DecentralDigitalTwinRegistryClient decentralDigitalTwinRegistryClient = mock(
            DecentralDigitalTwinRegistryClient.class);

    private final DecentralDigitalTwinRegistryService sut = new DecentralDigitalTwinRegistryService(
            connectorEndpointsService, endpointDataForConnectorsService, decentralDigitalTwinRegistryClient,
            new EdcConfiguration());

    public static AssetAdministrationShellDescriptor shellDescriptor(
            final List<SubmodelDescriptor> submodelDescriptors) {

        final var specificAssetIds = List.of(
                IdentifierKeyValuePair.builder().name("ManufacturerId").value("BPNL00000003AYRE").build());

        return AssetAdministrationShellDescriptor.builder()
                                                 .specificAssetIds(specificAssetIds)
                                                 .submodelDescriptors(submodelDescriptors)
                                                 .build();
    }

    @Nested
    @DisplayName("fetchShells")
    class FetchShellsTests {

        @Test
        void shouldReturnExpectedShell() throws RegistryServiceException {
            // given
            final var shellId = "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b";
            final var digitalTwinRegistryKey = new DigitalTwinRegistryKey(shellId, "bpn");
            final var expectedShell = shellDescriptor(emptyList());
            final var endpointDataReference = endpointDataReference("url.to.host");
            final var lookupShellsResponse = LookupShellsResponse.builder().result(List.of(shellId)).build();

            when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("address"));

            final var endpointDataRefFutures = List.of(completedFuture(endpointDataReference));
            when(endpointDataForConnectorsService.createFindEndpointDataForConnectorsFutures(anyList(),
                    any())).thenReturn(endpointDataRefFutures);

            when(decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(any(),
                    any(IdentifierKeyValuePair.class))).thenReturn(lookupShellsResponse);
            when(decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(any(), any())).thenReturn(
                    expectedShell);

            // when
            final var actualShell = sut.fetchShells(List.of(digitalTwinRegistryKey))
                                       .stream()
                                       .map(Either::getOrNull)
                                       .map(Shell::payload);

            // then
            assertThat(actualShell).containsExactly(expectedShell);
        }

        @Test
        void whenInterruptedExceptionOccurs() throws ExecutionException, InterruptedException, TimeoutException {

            // given
            simulateResultFinderInterrupted();

            final var lookupShellsResponse = LookupShellsResponse.builder().result(emptyList()).build();

            final List<String> connectorEndpoints = List.of("address1", "address2");
            when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(connectorEndpoints);

            final var dataRefFutures = List.of( //
                    completedFuture(endpointDataReference("url.to.host1")), //
                    completedFuture(endpointDataReference("url.to.host2")));
            when(endpointDataForConnectorsService.createFindEndpointDataForConnectorsFutures(connectorEndpoints,
                    "bpn")).thenReturn(dataRefFutures);

            when(decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(any(),
                    any(IdentifierKeyValuePair.class))).thenReturn(lookupShellsResponse);
            when(decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(any(), any())).thenReturn(
                    shellDescriptor(emptyList()));

            // when
            final ThrowingCallable call = () -> sut.fetchShells(
                    List.of(new DigitalTwinRegistryKey("dummyShellId", "dummyBpn")));

            // then
            assertThatThrownBy(call).isInstanceOf(ShellNotFoundException.class)
                                    .hasMessage("Unable to find any of the requested shells")
                                    .hasSuppressedException(new InterruptedException("interrupted"))
                                    .satisfies(e -> assertThat(
                                            ((ShellNotFoundException) e).getCalledEndpoints()).containsExactlyInAnyOrder(
                                            "address1", "address2"));

            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        }

        @Test
        void whenExecutionExceptionOccurs() {

            // given
            simulateGetFastestResultFailedFuture();

            final var lookupShellsResponse = LookupShellsResponse.builder().result(emptyList()).build();

            final List<String> connectorEndpoints = List.of("address");
            when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(connectorEndpoints);

            final var dataRefFutures = List.of(completedFuture(endpointDataReference("contractId", "url.to.host")));
            when(endpointDataForConnectorsService.createFindEndpointDataForConnectorsFutures(connectorEndpoints,
                    "bpn")).thenReturn(dataRefFutures);

            when(decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(any(),
                    any(IdentifierKeyValuePair.class))).thenReturn(lookupShellsResponse);
            when(decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(any(), any())).thenReturn(
                    shellDescriptor(emptyList()));

            // when
            final var bpn = "dummyBpn";
            final ThrowingCallable call = () -> sut.fetchShells(
                    List.of(new DigitalTwinRegistryKey("dummyShellId", bpn)));

            // then
            assertThatThrownBy(call).isInstanceOf(ShellNotFoundException.class)
                                    .hasMessageContaining("Unable to find any of the requested shells");

        }

        @Test
        void shouldThrowShellNotFoundException_ifNoDigitalTwinRegistryKeysGiven() {
            assertThatThrownBy(() -> sut.fetchShells(emptyList())).isInstanceOf(ShellNotFoundException.class);
        }

    }

    private void simulateGetFastestResultFailedFuture() {
        final ResultFinder resultFinderMock = mock(ResultFinder.class);
        when(resultFinderMock.getFastestResult(any())).thenReturn(
                CompletableFuture.failedFuture(new IllegalStateException("some illegal state")));
        sut.setResultFinder(resultFinderMock);
    }

    private void simulateResultFinderInterrupted() throws InterruptedException, ExecutionException, TimeoutException {
        final ResultFinder resultFinderMock = mock(ResultFinder.class);
        final CompletableFuture completableFutureMock = mock(CompletableFuture.class);
        when(completableFutureMock.get(anyLong(), any(TimeUnit.class))).thenThrow(
                new InterruptedException("interrupted"));
        when(resultFinderMock.getFastestResult(any())).thenReturn(completableFutureMock);
        sut.setResultFinder(resultFinderMock);
    }

    @Nested
    @DisplayName("lookupGlobalAssetIds")
    class LookupGlobalAssetIdsTests {

        @Test
        void shouldReturnTheExpectedGlobalAssetId() throws RegistryServiceException {
            // given
            final var digitalTwinRegistryKey = new DigitalTwinRegistryKey(
                    "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b", "bpn");

            final var expectedGlobalAssetId = "urn:uuid:4132cd2b-cbe7-4881-a6b4-aaaaaaaaaaaa";
            final var expectedShell = shellDescriptor(emptyList()).toBuilder()
                                                                  .globalAssetId(expectedGlobalAssetId)
                                                                  .build();
            final var dataRefFutures = List.of(
                    completedFuture(endpointDataReference("contractAgreementId", "url.to.host")));
            final var lookupShellsResponse = LookupShellsResponse.builder()
                                                                 .result(List.of(digitalTwinRegistryKey.shellId()))
                                                                 .build();
            when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("address"));
            when(endpointDataForConnectorsService.createFindEndpointDataForConnectorsFutures(anyList(),
                    any())).thenReturn(dataRefFutures);
            when(decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(any(),
                    any(IdentifierKeyValuePair.class))).thenReturn(lookupShellsResponse);
            when(decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(any(), any())).thenReturn(
                    expectedShell);

            // when
            final var assetAdministrationShellDescriptors = sut.lookupShellsByBPN(digitalTwinRegistryKey.bpn());

            String actualGlobalAssetId = assetAdministrationShellDescriptors.stream()
                                                                            .findFirst()
                                                                            .map(Either::getOrNull)
                                                                            .map(Shell::payload)
                                                                            .map(AssetAdministrationShellDescriptor::getGlobalAssetId)
                                                                            .get();
            // then
            assertThat(actualGlobalAssetId).isEqualTo(expectedGlobalAssetId);
        }

        @Test
        void whenInterruptedExceptionOccurs() throws ExecutionException, InterruptedException, TimeoutException {
            // given
            simulateResultFinderInterrupted();

            // when
            final ThrowingCallable call = () -> sut.lookupShellsByBPN("dummyBpn");

            // then
            assertThatThrownBy(call).isInstanceOf(RegistryServiceException.class)
                                    .hasMessageContaining(
                                            "InterruptedException occurred while looking up shell ids for bpn")
                                    .hasMessageContaining("dummyBpn");
        }

        @Test
        void whenExecutionExceptionOccurs() {
            // given
            simulateGetFastestResultFailedFuture();

            // when
            final var bpn = "dummyBpn";
            final ThrowingCallable call = () -> sut.lookupShellsByBPN(bpn);

            // then
            assertThatThrownBy(call).isInstanceOf(RegistryServiceException.class)
                                    .hasMessageContaining("Exception occurred while looking up shell ids for bpn")
                                    .hasMessageContaining("'" + bpn + "'");
        }
    }


    @Nested
    @DisplayName("lookupAasIdentificator")
    class LookupAasIdentificatorTests {

        @Test
        void shouldReturnExpectedGlobalAssetId() throws RegistryServiceException {
            // given
            final var digitalTwinRegistryKey = new DigitalTwinRegistryKey(
                    "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b", "bpn");

            final var expectedGlobalAssetId = "urn:uuid:4132cd2b-cbe7-4881-a6b4-aaaaaaaaaaaa";
            final var expectedShell = shellDescriptor(emptyList()).toBuilder()
                                                                  .globalAssetId(expectedGlobalAssetId)
                                                                  .build();
            final var dataRefFutures = List.of(
                    completedFuture(endpointDataReference("contractAgreementId", "url.to.host")));
            final var lookupShellsResponse = LookupShellsResponse.builder()
                                                                 .result(List.of(digitalTwinRegistryKey.shellId()))
                                                                 .build();

            when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("address"));
            when(endpointDataForConnectorsService.createFindEndpointDataForConnectorsFutures(anyList(),
                    any())).thenReturn(dataRefFutures);
            when(decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(any(),
                    any(IdentifierKeyValuePair.class))).thenReturn(lookupShellsResponse);
            when(decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(any(), any())).thenReturn(
                    expectedShell);

            PartChainIdentificationKey partChainIdentificationKey = PartChainIdentificationKey.builder()
                                                                                              .identifier("urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b")
                                                                                              .bpn("bpn")
                                                                                              .build();

            // when
            final var assetAdministrationShellDescriptors = sut.fetchShell(partChainIdentificationKey);

            String actualGlobalAssetId = assetAdministrationShellDescriptors
                    .map(Shell::payload)
                    .map(AssetAdministrationShellDescriptor::getGlobalAssetId)
                    .get();

            // then
            assertThat(actualGlobalAssetId).isEqualTo(expectedGlobalAssetId);
        }

        @Test
        void shouldReturnExpectedGlobalAssetId_noAasIdPassed() throws RegistryServiceException {
            // given
            final var digitalTwinRegistryKey = new DigitalTwinRegistryKey(
                    "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b", "bpn");

            final var expectedGlobalAssetId = "urn:uuid:4132cd2b-cbe7-4881-a6b4-aaaaaaaaaaaa";
            final var expectedShell = shellDescriptor(emptyList()).toBuilder()
                                                                  .globalAssetId(expectedGlobalAssetId)
                                                                  .build();
            final var dataRefFutures = List.of(
                    completedFuture(endpointDataReference("contractAgreementId", "url.to.host")));
            final var lookupShellsResponse = LookupShellsResponse.builder()
                                                                 .result(List.of(digitalTwinRegistryKey.shellId()))
                                                                 .build();

            when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("address"));
            when(endpointDataForConnectorsService.createFindEndpointDataForConnectorsFutures(anyList(),
                    any())).thenReturn(dataRefFutures);
            when(decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(any(),
                    any(IdentifierKeyValuePair.class))).thenReturn(lookupShellsResponse);
            when(decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(any(), any())).thenReturn(
                    expectedShell);

            PartChainIdentificationKey partChainIdentificationKey = PartChainIdentificationKey.builder()
                                                                                              .globalAssetId("urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b")
                                                                                              .bpn("bpn")
                                                                                              .build();

            // when
            final var assetAdministrationShellDescriptors = sut.fetchShell(partChainIdentificationKey);

            String actualGlobalAssetId = assetAdministrationShellDescriptors
                    .map(Shell::payload)
                    .map(AssetAdministrationShellDescriptor::getGlobalAssetId)
                    .get();

            // then
            assertThat(actualGlobalAssetId).isEqualTo(expectedGlobalAssetId);
        }
    }

}