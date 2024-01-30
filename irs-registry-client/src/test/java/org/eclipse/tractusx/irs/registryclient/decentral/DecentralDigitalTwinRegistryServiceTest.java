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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.Shell;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.model.EDRAuthCode;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryKey;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class DecentralDigitalTwinRegistryServiceTest {

    private final ConnectorEndpointsService connectorEndpointsService = Mockito.mock(ConnectorEndpointsService.class);
    private final EndpointDataForConnectorsService endpointDataForConnectorsService = Mockito.mock(
            EndpointDataForConnectorsService.class);

    private final DecentralDigitalTwinRegistryClient decentralDigitalTwinRegistryClient = Mockito.mock(
            DecentralDigitalTwinRegistryClient.class);

    private final DecentralDigitalTwinRegistryService decentralDigitalTwinRegistryService = new DecentralDigitalTwinRegistryService(
            connectorEndpointsService, endpointDataForConnectorsService, decentralDigitalTwinRegistryClient);

    private static String createAuthCode(final Function<Instant, Instant> expirationModifier) {
        final var serializedEdrAuthCode = StringMapper.mapToString(
                EDRAuthCode.builder().exp(expirationModifier.apply(Instant.now()).getEpochSecond()).build());
        final var bytes = serializedEdrAuthCode.getBytes(StandardCharsets.UTF_8);
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    public static AssetAdministrationShellDescriptor shellDescriptor(
            final List<SubmodelDescriptor> submodelDescriptors) {
        return AssetAdministrationShellDescriptor.builder()
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                                                 .name("ManufacturerId")
                                                                                                 .value("BPNL00000003AYRE")
                                                                                                 .build()))
                                                 .submodelDescriptors(submodelDescriptors)
                                                 .build();
    }

    @Test
    void shouldReturnExpectedShell() throws RegistryServiceException {
        // given
        final DigitalTwinRegistryKey digitalTwinRegistryKey = new DigitalTwinRegistryKey(
                "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b", "bpn");
        final Shell expectedShell = new Shell("", shellDescriptor(Collections.emptyList()));
        EndpointDataReference endpointDataReference = EndpointDataReference.Builder.newInstance()
                                                                                   .endpoint("url.to.host")
                                                                                   .build();
        final LookupShellsResponse lookupShellsResponse = LookupShellsResponse.builder()
                                                                              .result(Collections.emptyList())
                                                                              .build();
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("address"));
        when(endpointDataForConnectorsService.findEndpointDataForConnectors(ArgumentMatchers.anyList())).thenReturn(
                endpointDataReference);
        when(decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(any(),
                ArgumentMatchers.anyList())).thenReturn(lookupShellsResponse);
        when(decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(any(), any())).thenReturn(
                expectedShell.payload());

        // when
        final Collection<Shell> actualShell = decentralDigitalTwinRegistryService.fetchShells(
                List.of(digitalTwinRegistryKey));

        // then
        Assertions.assertThat(actualShell).containsExactly(expectedShell);
    }

    @Test
    void shouldRenewEndpointDataReferenceForMultipleAssets() throws RegistryServiceException {
        // given
        final DigitalTwinRegistryKey digitalTwinRegistryKey = new DigitalTwinRegistryKey(
                "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b", "bpn");
        final Shell expectedShell = new Shell("", shellDescriptor(Collections.emptyList()));
        final var authCode = "test." + createAuthCode(exp -> exp.minus(1, ChronoUnit.DAYS));
        EndpointDataReference endpointDataReference = EndpointDataReference.Builder.newInstance()
                                                                                   .endpoint("url.to.host")
                                                                                   .authKey("test")
                                                                                   .authCode(authCode)
                                                                                   .build();
        EndpointDataReference renewedReference = EndpointDataReference.Builder.newInstance()
                                                                              .endpoint("url.to.host")
                                                                              .build();
        final LookupShellsResponse lookupShellsResponse = LookupShellsResponse.builder()
                                                                              .result(Collections.emptyList())
                                                                              .build();
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("address"));
        when(endpointDataForConnectorsService.findEndpointDataForConnectors(ArgumentMatchers.anyList())).thenReturn(
                endpointDataReference, renewedReference);
        when(decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(any(),
                ArgumentMatchers.anyList())).thenReturn(lookupShellsResponse);
        when(decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(any(), any())).thenReturn(
                expectedShell.payload());

        // when
        final Collection<Shell> actualShell = decentralDigitalTwinRegistryService.fetchShells(
                List.of(digitalTwinRegistryKey, digitalTwinRegistryKey));

        // then
        Assertions.assertThat(actualShell).containsExactly(expectedShell, expectedShell);

        verify(endpointDataForConnectorsService, times(2)).findEndpointDataForConnectors(anyList());
    }

    @Test
    void shouldNotRenewEndpointDataReferenceForMultipleAssets() throws RegistryServiceException {
        // given
        final DigitalTwinRegistryKey digitalTwinRegistryKey = new DigitalTwinRegistryKey(
                "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b", "bpn");
        final Shell expectedShell = new Shell("", shellDescriptor(Collections.emptyList()));
        final var authCode = "test." + createAuthCode(exp -> exp.plus(1, ChronoUnit.DAYS));
        EndpointDataReference endpointDataReference = EndpointDataReference.Builder.newInstance()
                                                                                   .endpoint("url.to.host")
                                                                                   .authKey("test")
                                                                                   .authCode(authCode)
                                                                                   .build();
        final LookupShellsResponse lookupShellsResponse = LookupShellsResponse.builder()
                                                                              .result(Collections.emptyList())
                                                                              .build();
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("address"));
        when(endpointDataForConnectorsService.findEndpointDataForConnectors(ArgumentMatchers.anyList())).thenReturn(
                endpointDataReference);
        when(decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(any(),
                ArgumentMatchers.anyList())).thenReturn(lookupShellsResponse);
        when(decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(any(), any())).thenReturn(
                expectedShell.payload());

        // when
        final Collection<Shell> actualShell = decentralDigitalTwinRegistryService.fetchShells(
                List.of(digitalTwinRegistryKey, digitalTwinRegistryKey, digitalTwinRegistryKey));

        // then
        Assertions.assertThat(actualShell).containsExactly(expectedShell, expectedShell, expectedShell);

        verify(endpointDataForConnectorsService, times(1)).findEndpointDataForConnectors(anyList());
    }

    @Test
    void shouldReturnExpectedGlobalAssetId() throws RegistryServiceException {
        // given
        final DigitalTwinRegistryKey digitalTwinRegistryKey = new DigitalTwinRegistryKey(
                "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b", "bpn");

        final String expectedGlobalAssetId = "urn:uuid:4132cd2b-cbe7-4881-a6b4-aaaaaaaaaaaa";
        final var expectedShell = new Shell("", shellDescriptor(Collections.emptyList()).toBuilder()
                                                                          .globalAssetId(expectedGlobalAssetId)
                                                                          .build());
        final var endpointDataReference = EndpointDataReference.Builder.newInstance().endpoint("url.to.host").build();
        final LookupShellsResponse lookupShellsResponse = LookupShellsResponse.builder()
                                                                              .result(List.of(
                                                                                digitalTwinRegistryKey.shellId()))
                                                                              .build();
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("address"));
        when(endpointDataForConnectorsService.findEndpointDataForConnectors(ArgumentMatchers.anyList())).thenReturn(
                endpointDataReference);
        when(decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(any(),
                ArgumentMatchers.anyList())).thenReturn(lookupShellsResponse);
        when(decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(any(), any())).thenReturn(
                expectedShell.payload());

        // when
        final Collection<Shell> shell = decentralDigitalTwinRegistryService.lookupShellsByBPN(
                digitalTwinRegistryKey.bpn());

        // then
        String actualGlobalAssetId = shell.stream().findFirst().map(Shell::payload).map(AssetAdministrationShellDescriptor::getGlobalAssetId).get();
        Assertions.assertThat(actualGlobalAssetId).isEqualTo(expectedGlobalAssetId);
    }

}
