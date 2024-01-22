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
package org.eclipse.tractusx.irs.registryclient.discovery;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

/**
 * Alternative discovery client which uses local data instead.
 */
@RequiredArgsConstructor
public class LocalDataDiscovery implements DiscoveryFinderClient {

    private final Map<String, String> bpnToEdcMapping;

    @Override
    public DiscoveryResponse findDiscoveryEndpoints(final DiscoveryFinderRequest request) {
        return new DiscoveryResponse(List.of(new DiscoveryEndpoint("local", "", "", "", "")));
    }

    @Override
    public List<EdcDiscoveryResult> findConnectorEndpoints(final String endpointAddress, final List<String> bpns) {
        return bpns.stream()
                   .filter(bpnToEdcMapping::containsKey)
                   .map(bpn -> toDiscoveryResult(bpn, bpnToEdcMapping.get(bpn)))
                   .toList();
    }

    private EdcDiscoveryResult toDiscoveryResult(final String bpn, final String endpoint) {
        return new EdcDiscoveryResult(bpn, List.of(endpoint));
    }

    public void registerMapping(final String bpn, final String endpoint) {
        bpnToEdcMapping.put(bpn, endpoint);
    }
}
