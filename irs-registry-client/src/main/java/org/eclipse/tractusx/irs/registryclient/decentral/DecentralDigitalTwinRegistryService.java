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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.Shell;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.edc.client.model.EDRAuthCode;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryKey;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;
import org.eclipse.tractusx.irs.registryclient.exceptions.ShellNotFoundException;
import org.jetbrains.annotations.NotNull;

/**
 * Decentral implementation of DigitalTwinRegistryService
 */
@RequiredArgsConstructor
@Slf4j
public class DecentralDigitalTwinRegistryService implements DigitalTwinRegistryService {

    private final ConnectorEndpointsService connectorEndpointsService;
    private final EndpointDataForConnectorsService endpointDataForConnectorsService;
    private final DecentralDigitalTwinRegistryClient decentralDigitalTwinRegistryClient;

    private static Stream<Map.Entry<String, List<DigitalTwinRegistryKey>>> groupKeysByBpn(
            final Collection<DigitalTwinRegistryKey> keys) {
        return keys.stream().collect(Collectors.groupingBy(DigitalTwinRegistryKey::bpn)).entrySet().stream();
    }

    @Override
    public Collection<Shell> fetchShells(final Collection<DigitalTwinRegistryKey> keys)
            throws RegistryServiceException {
        log.info("Fetching shell(s) for {} key(s)", keys.size());
        final var calledEndpoints = new HashSet<String>();
        final var collectedShells = groupKeysByBpn(keys).flatMap(
                entry -> fetchShellDescriptors(calledEndpoints, entry.getKey(), entry.getValue())).toList();
        if (collectedShells.isEmpty()) {
            throw new ShellNotFoundException("Unable to find any of the requested shells", calledEndpoints);
        } else {
            log.info("Found {} shell(s) for {} key(s)", collectedShells.size(), keys.size());
            return collectedShells;
        }
    }

    @NotNull
    private Stream<Shell> fetchShellDescriptors(final Set<String> calledEndpoints,
            final String bpn, final List<DigitalTwinRegistryKey> keys) {
        log.info("Fetching {} shells for bpn {}", keys.size(), bpn);
        final var connectorEndpoints = connectorEndpointsService.fetchConnectorEndpoints(bpn);
        calledEndpoints.addAll(connectorEndpoints);

        final List<Shell> descriptors = new ArrayList<>();

        EndpointDataReference endpointDataReference = null;

        for (final DigitalTwinRegistryKey key : keys) {
            endpointDataReference = renewIfNecessary(endpointDataReference, connectorEndpoints);
            descriptors.add(new Shell(contractNegotiationId(endpointDataReference.getAuthCode()),
                    fetchShellDescriptor(endpointDataReference, key)));
        }

        return descriptors.stream();
    }

    private EndpointDataReference renewIfNecessary(final EndpointDataReference endpointDataReference,
            final List<String> connectorEndpoints) {
        if (endpointDataReference == null || endpointDataReference.getAuthCode() == null) {
            return getEndpointDataReference(connectorEndpoints);
        } else {
            final var tokenExpirationInstant = extractTokenExpiration(endpointDataReference.getAuthCode());
            if (Instant.now().isAfter(tokenExpirationInstant)) {
                log.info("EndpointDataReference token has expired, getting a new one.");
                return getEndpointDataReference(connectorEndpoints);
            }
            return endpointDataReference;
        }
    }

    private String contractNegotiationId(final String token) {
        return Optional.ofNullable(token)
                       .map(EDRAuthCode::fromAuthCodeToken)
                       .map(EDRAuthCode::getCid)
                       .orElse("");
    }

    private Instant extractTokenExpiration(final String token) {
        return Instant.ofEpochSecond(EDRAuthCode.fromAuthCodeToken(token).getExp());
    }

    private AssetAdministrationShellDescriptor fetchShellDescriptor(final EndpointDataReference endpointDataReference,
            final DigitalTwinRegistryKey key) {
        log.info("Retrieving AAS Identification for DigitalTwinRegistryKey: {}", key);
        final String aaShellIdentification = mapToShellId(endpointDataReference, key.shellId());

        return decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(endpointDataReference,
                aaShellIdentification);
    }

    /**
     * This method takes the provided ID and maps it to the corresponding asset administration shell ID.
     * If the ID is already a shellId, the same ID will be returned.
     * If the ID is a globalAssetId, the corresponding shellId will be returned.
     *
     * @param endpointDataReference the reference to access the digital twin registry
     * @param key                   the ambiguous key (shellId or globalAssetId)
     * @return the shellId
     */
    @NotNull
    private String mapToShellId(final EndpointDataReference endpointDataReference, final String key) {
        final var identifierKeyValuePair = IdentifierKeyValuePair.builder().name("globalAssetId").value(key).build();
        final var aaShellIdentification = decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                endpointDataReference, List.of(identifierKeyValuePair)).getResult().stream().findFirst().orElse(key);

        if (key.equals(aaShellIdentification)) {
            log.info("Found shell with shellId {} in registry", aaShellIdentification);
        } else {
            log.info("Retrieved shellId {} for globalAssetId {}", aaShellIdentification, key);
        }
        return aaShellIdentification;
    }

    @NotNull
    private EndpointDataReference getEndpointDataReference(final List<String> connectorEndpoints) {
        return endpointDataForConnectorsService.findEndpointDataForConnectors(connectorEndpoints);
    }

    private Collection<String> lookupShellIds(final String bpn) {
        log.info("Looking up shell ids for bpn {}", bpn);
        final var connectorEndpoints = connectorEndpointsService.fetchConnectorEndpoints(bpn);
        final var endpointDataReference = getEndpointDataReference(connectorEndpoints);

        final var shellIds = decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                endpointDataReference,
                List.of(IdentifierKeyValuePair.builder().name("manufacturerId").value(bpn).build())).getResult();
        log.info("Found {} shell id(s) in total", shellIds.size());
        return shellIds;
    }

    @Override
    public Collection<DigitalTwinRegistryKey> lookupShellIdentifiers(final String bpn) {
        return lookupShellIds(bpn).stream().map(id -> new DigitalTwinRegistryKey(id, bpn)).toList();
    }

}
