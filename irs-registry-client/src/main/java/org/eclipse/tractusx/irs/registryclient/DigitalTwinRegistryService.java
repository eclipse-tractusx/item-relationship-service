/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.registryclient;

import java.util.Collection;

import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;

/**
 * Public API Service for digital twin registry domain
 */
public interface DigitalTwinRegistryService {

    /**
     * Retrieves the shell details of all asset administration shells for a given BPN.
     *
     * @param bpn the BPN to retrieve the shells for
     * @return the collection of asset administration shells
     */
    default Collection<AssetAdministrationShellDescriptor> lookupShellsByBPN(final String bpn) throws RegistryServiceException {
        return fetchShells(lookupShellIdentifiers(bpn)).stream().toList();
    }

    /**
     * Retrieves all registered shell identifiers for a given BPN.
     *
     * @param bpn the BPN to retrieve the shells for
     * @return the collection of shell identifiers
     * @deprecated replaced by {@link DigitalTwinRegistryService#lookupShellIdentifiers(String)}
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    default Collection<DigitalTwinRegistryKey> lookupShells(final String bpn) throws RegistryServiceException {
        return lookupShellIdentifiers(bpn);
    }

    /**
     * Retrieves all registered shell identifiers for a given BPN.
     *
     * @param bpn the BPN to retrieve the shells for
     * @return the collection of shell identifiers
     */
    Collection<DigitalTwinRegistryKey> lookupShellIdentifiers(String bpn) throws RegistryServiceException;

    /**
     * Retrieves the shell details for the given identifiers.
     *
     * @param identifiers the shell identifiers
     * @return the shell descriptors
     */
    Collection<AssetAdministrationShellDescriptor> fetchShells(Collection<DigitalTwinRegistryKey> identifiers)
            throws RegistryServiceException;
}
