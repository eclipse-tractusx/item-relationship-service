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

import java.util.List;

import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;

/**
 * Digital Twin Registry Rest Client
 */
public interface DigitalTwinRegistryClient {

    /**
     * @param aasIdentifier The Asset Administration Shell’s unique id
     * @return Returns a specific Asset Administration Shell Descriptor
     */
    AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(String aasIdentifier);

    /**
     * Returns a list of Asset Administration Shell ids based on Asset identifier key-value-pairs.
     * Only the Shell ids are returned when all provided key-value pairs match.
     *
     * @param assetIds The key-value-pair of an Asset identifier
     * @return urn uuid string list
     */
    List<String> getAllAssetAdministrationShellIdsByAssetLink(List<IdentifierKeyValuePair> assetIds);

}

