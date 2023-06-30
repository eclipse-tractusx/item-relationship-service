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
package org.eclipse.tractusx.ess.discovery;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Connector Endpoints service to find connectors in Discovery Finder
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectorEndpointsService {

    private final EssDiscoveryFinderClient essDiscoveryFinderClient;

    public List<String> fetchConnectorEndpoints(final String bpn) {
        final DiscoveryFinderRequest onlyBpn = new DiscoveryFinderRequest(List.of("bpn"));
        final List<DiscoveryEndpoint> discoveryEndpoints = essDiscoveryFinderClient.findDiscoveryEndpoints(onlyBpn)
                                                                                   .endpoints();
        final List<String> providedBpn = List.of(bpn);
        return discoveryEndpoints.stream()
                                 .map(discoveryEndpoint -> essDiscoveryFinderClient.findConnectorEndpoints(
                                                                                        discoveryEndpoint.endpointAddress(),
                                                                                        providedBpn)
                                                                                   .stream()
                                                                                   .filter(edcDiscoveryResult -> edcDiscoveryResult.bpn()
                                                                                                                                .equals(bpn))
                                                                                   .map(EdcDiscoveryResult::connectorEndpoint)
                                                                                   .toList())
                                 .flatMap(List::stream)
                                 .flatMap(List::stream)
                                 .toList();
    }

}
