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

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.util.TestMother.shellDescriptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.junit.jupiter.api.Test;

class DecentralDigitalTwinRegistryServiceTest {

    private final DiscoveryFinderClient discoveryFinderClient = mock(DiscoveryFinderClient.class);
    private final EDCConnectorsForAASService edcConnectorsForAASService = mock(EDCConnectorsForAASService.class);

    private final DecentralDigitalTwinRegistryService decentralDigitalTwinRegistryService = new DecentralDigitalTwinRegistryService(discoveryFinderClient, edcConnectorsForAASService);

    @Test
    void shouldReturnExpectedShell() {
        // given
        final DigitalTwinRegistryKey digitalTwinRegistryKey = new DigitalTwinRegistryKey("urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b", "bpn");
        final AssetAdministrationShellDescriptor expectedShell = shellDescriptor(Collections.emptyList());
        when(discoveryFinderClient.findDiscoveryEndpoints(any(DiscoveryFinderRequest.class))).thenReturn(
                Collections.singletonList(new DiscoveryEndpoint("type", "desc", "address", "doc", "resId")));
        when(edcConnectorsForAASService.findAASinConnectors(anyList())).thenReturn(expectedShell);

        // when
        final AssetAdministrationShellDescriptor actualShell = decentralDigitalTwinRegistryService.getAAShellDescriptor(
                digitalTwinRegistryKey);

        // then
        assertThat(actualShell).isEqualTo(expectedShell);
    }
}
