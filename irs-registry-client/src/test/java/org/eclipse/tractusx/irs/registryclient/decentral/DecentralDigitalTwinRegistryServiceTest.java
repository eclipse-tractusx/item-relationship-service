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
package org.eclipse.tractusx.irs.registryclient.decentral;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryKey;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryEndpoint;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClient;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderRequest;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryResponse;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class DecentralDigitalTwinRegistryServiceTest {

    private final DiscoveryFinderClient discoveryFinderClient = Mockito.mock(DiscoveryFinderClient.class);
    private final EndpointDataForConnectorsService endpointDataForConnectorsService = Mockito.mock(
            EndpointDataForConnectorsService.class);

    private final DecentralDigitalTwinRegistryClient decentralDigitalTwinRegistryClient = Mockito.mock(
            DecentralDigitalTwinRegistryClient.class);

    private final DecentralDigitalTwinRegistryService decentralDigitalTwinRegistryService = new DecentralDigitalTwinRegistryService(
            discoveryFinderClient, endpointDataForConnectorsService, decentralDigitalTwinRegistryClient);

    @Test
    void shouldReturnExpectedShell() throws RegistryServiceException {
        // given
        final DigitalTwinRegistryKey digitalTwinRegistryKey = new DigitalTwinRegistryKey(
                "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b", "bpn");
        final AssetAdministrationShellDescriptor expectedShell = shellDescriptor(Collections.emptyList());
        EndpointDataReference endpointDataReference = EndpointDataReference.Builder.newInstance()
                                                                                   .endpoint("url.to.host")
                                                                                   .build();
        final List<DiscoveryEndpoint> discoveryEndpoints = List.of(
                new DiscoveryEndpoint("type", "desc", "address", "doc", "resId"));
        Mockito.when(discoveryFinderClient.findDiscoveryEndpoints(ArgumentMatchers.any(DiscoveryFinderRequest.class)))
               .thenReturn(new DiscoveryResponse(discoveryEndpoints));
        Mockito.when(endpointDataForConnectorsService.findEndpointDataForConnectors(ArgumentMatchers.anyList()))
               .thenReturn(endpointDataReference);
        Mockito.when(
                decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(ArgumentMatchers.any(),
                        ArgumentMatchers.anyList())).thenReturn(Collections.emptyList());
        Mockito.when(decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(ArgumentMatchers.any(),
                ArgumentMatchers.any())).thenReturn(expectedShell);

        // when
        final Collection<AssetAdministrationShellDescriptor> actualShell = decentralDigitalTwinRegistryService.fetchShells(
                List.of(digitalTwinRegistryKey));

        // then
        Assertions.assertThat(actualShell).containsExactly(expectedShell);
    }

    public static AssetAdministrationShellDescriptor shellDescriptor(
            final List<SubmodelDescriptor> submodelDescriptors) {
        return AssetAdministrationShellDescriptor.builder()
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                                                 .key("ManufacturerId")
                                                                                                 .value("BPNL00000003AYRE")
                                                                                                 .build()))
                                                 .submodelDescriptors(submodelDescriptors)
                                                 .build();
    }

}
