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
package org.eclipse.tractusx.irs.aaswrapper.registry.domain;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Decentral implementation of DigitalTwinRegistryService
 */
@Service
@ConditionalOnProperty(prefix = "digitalTwinRegistry", name = "type", havingValue = "decentral")
@RequiredArgsConstructor
@Slf4j
public class DecentralDigitalTwinRegistryService implements DigitalTwinRegistryService {

    private final DiscoveryFinderClient discoveryFinderClient;
    private final EndpointDataForConnectorsService endpointDataForConnectorsService;
    private final DecentralDigitalTwinRegistryClient decentralDigitalTwinRegistryClient;

    @Override
    public AssetAdministrationShellDescriptor getAAShellDescriptor(final DigitalTwinRegistryKey key) {
        log.info("Retrieved AAS Identification for DigitalTwinRegistryKey: {}", key);
        final DiscoveryFinderRequest onlyBpn = new DiscoveryFinderRequest(List.of("bpn"));
        final List<String> providedBpn = List.of(key.bpn());
        final List<DiscoveryEndpoint> discoveryEndpoints = discoveryFinderClient.findDiscoveryEndpoints(onlyBpn);
        final List<String> connectorEndpoints = discoveryEndpoints.stream()
                                                                  .map(discoveryEndpoint -> discoveryFinderClient.findConnectorEndpoints(
                                                                                                                         discoveryEndpoint.endpointAddress(),
                                                                                                                         providedBpn)
                                                                                                                 .stream()
                                                                                                                 .filter(edcDiscoveryResult -> edcDiscoveryResult.bpn()
                                                                                                                                                                 .equals(key.bpn()))
                                                                                                                 .map(EdcDiscoveryResult::connectorEndpoint)
                                                                                                                 .toList())
                                                                  .flatMap(List::stream)
                                                                  .flatMap(List::stream)
                                                                  .toList();
        // take first
        final EndpointDataReference endpointDataReference = endpointDataForConnectorsService.findEndpointDataForConnectors(
                connectorEndpoints).stream().findFirst().orElseThrow();
        final IdentifierKeyValuePair identifierKeyValuePair = IdentifierKeyValuePair.builder()
                                                                                    .key("globalAssetId")
                                                                                    .value(key.globalAssetId())
                                                                                    .build();
        final String aaShellIdentification = decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                                                                                       endpointDataReference, List.of(identifierKeyValuePair))
                                                                               .stream()
                                                                               .findFirst()
                                                                               .orElse(key.globalAssetId());
        log.info("Retrieved AAS Identification {} for globalAssetId {}", aaShellIdentification, key.globalAssetId());

        return decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(endpointDataReference,
                aaShellIdentification);

    }

}
