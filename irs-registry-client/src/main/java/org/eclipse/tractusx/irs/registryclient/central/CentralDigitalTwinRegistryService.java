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
package org.eclipse.tractusx.irs.registryclient.central;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryKey;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;

/**
 * Central implementation of DigitalTwinRegistryService
 */
@RequiredArgsConstructor
@Slf4j
public class CentralDigitalTwinRegistryService implements DigitalTwinRegistryService {

    private final DigitalTwinRegistryClient digitalTwinRegistryClient;

    @Override
    public Collection<AssetAdministrationShellDescriptor> fetchShells(final Collection<DigitalTwinRegistryKey> keys) {
        return keys.stream().map(key -> {
            final String aaShellIdentification = getAAShellIdentificationOrGlobalAssetId(key.shellId());
            log.info("Retrieved AAS Identification {} for globalAssetId {}", aaShellIdentification, key.shellId());

            return digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(aaShellIdentification);
        }).toList();
    }

    @Override
    public Collection<DigitalTwinRegistryKey> lookupShellIdentifiers(final String bpn) {
        log.info("Looking up shells for bpn {}", bpn);
        final var shellIds = digitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(List.of());
        log.info("Found {} shells in total", shellIds.size());
        return shellIds.stream().map(id -> new DigitalTwinRegistryKey(id, bpn)).toList();
    }

    private String getAAShellIdentificationOrGlobalAssetId(final String globalAssetId) {
        final IdentifierKeyValuePair identifierKeyValuePair = IdentifierKeyValuePair.builder()
                                                                                    .name("globalAssetId")
                                                                                    .value(globalAssetId)
                                                                                    .build();

        final List<String> allAssetAdministrationShellIdsByAssetLink = digitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                Collections.singletonList(identifierKeyValuePair));

        return allAssetAdministrationShellIdsByAssetLink.stream().findFirst().orElse(globalAssetId);
    }

}
