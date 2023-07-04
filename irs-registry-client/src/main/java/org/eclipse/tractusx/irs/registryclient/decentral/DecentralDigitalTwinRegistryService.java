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
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryKey;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryEndpoint;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClient;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderRequest;
import org.eclipse.tractusx.irs.registryclient.discovery.EdcDiscoveryResult;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;
import org.jetbrains.annotations.NotNull;

/**
 * Decentral implementation of DigitalTwinRegistryService
 */
@RequiredArgsConstructor
@Slf4j
public class DecentralDigitalTwinRegistryService implements DigitalTwinRegistryService {

    private final DiscoveryFinderClient discoveryFinderClient;
    private final EndpointDataForConnectorsService endpointDataForConnectorsService;
    private final DecentralDigitalTwinRegistryClient decentralDigitalTwinRegistryClient;

    @Override
    public Collection<AssetAdministrationShellDescriptor> fetchShells(final Collection<DigitalTwinRegistryKey> keys) {
        return keys.stream().map(key -> {
            log.info("Retrieved AAS Identification for DigitalTwinRegistryKey: {}", key);
            final EndpointDataReference endpointDataReference = getEndpointDataReference(key.bpn());
            final IdentifierKeyValuePair identifierKeyValuePair = IdentifierKeyValuePair.builder()
                                                                                        .key("globalAssetId")
                                                                                        .value(key.globalAssetId())
                                                                                        .build();
            final String aaShellIdentification = decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                                                                                           endpointDataReference, List.of(identifierKeyValuePair))
                                                                                   .stream()
                                                                                   .findFirst()
                                                                                   .orElse(key.globalAssetId());
            log.info("Retrieved AAS Identification {} for globalAssetId {}", aaShellIdentification,
                    key.globalAssetId());

            return decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(endpointDataReference,
                    aaShellIdentification);
        }).toList();
    }

    @NotNull
    private EndpointDataReference getEndpointDataReference(final String bpn) {
        final DiscoveryFinderRequest onlyBpn = new DiscoveryFinderRequest(List.of("bpn"));
        final List<String> providedBpn = List.of(bpn);
        final List<DiscoveryEndpoint> discoveryEndpoints = discoveryFinderClient.findDiscoveryEndpoints(onlyBpn)
                                                                                .endpoints();
        final List<String> connectorEndpoints = discoveryEndpoints.stream()
                                                                  .flatMap(
                                                                          discoveryEndpoint -> discoveryFinderClient.findConnectorEndpoints(
                                                                                                                            discoveryEndpoint.endpointAddress(),
                                                                                                                            providedBpn)
                                                                                                                    .stream()
                                                                                                                    .filter(edcDiscoveryResult -> edcDiscoveryResult.bpn()
                                                                                                                                                                    .equals(bpn))
                                                                                                                    .map(EdcDiscoveryResult::connectorEndpoint))
                                                                  .flatMap(List::stream)
                                                                  .toList();

        return endpointDataForConnectorsService.findEndpointDataForConnectors(connectorEndpoints);
    }

    @Override
    public Collection<DigitalTwinRegistryKey> lookupShells(final String bpn) throws RegistryServiceException {
        log.info("Looking up shells for bpn {}", bpn);
        final var endpointDataReference = getEndpointDataReference(bpn);
        final var shellIds = decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                endpointDataReference, List.of());
        log.info("Found {} shells in total", shellIds.size());
        return shellIds.stream().map(id -> new DigitalTwinRegistryKey(id, bpn)).toList();
    }

}
