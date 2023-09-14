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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Connector Endpoints service to find connectors in Discovery Finder
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class ConnectorEndpointsService {

    private final DiscoveryFinderClient discoveryFinderClient;

    public List<String> fetchConnectorEndpoints(final String bpn) {
        if (StringUtils.isBlank(bpn)) {
            log.warn("BPN was null, cannot search for any connector endpoints. Returning empty list.");
            return List.of();
        }

        log.info("Requesting connector endpoints for BPN {}", bpn);
        final DiscoveryFinderRequest onlyBpn = new DiscoveryFinderRequest(List.of("bpn"));
        final List<DiscoveryEndpoint> discoveryEndpoints = discoveryFinderClient.findDiscoveryEndpoints(onlyBpn)
                                                                                .endpoints();
        final List<String> providedBpn = List.of(bpn);
        final var endpoints = discoveryEndpoints.stream()
                                                .flatMap(
                                                        discoveryEndpoint -> discoveryFinderClient.findConnectorEndpoints(
                                                                                                          discoveryEndpoint.endpointAddress(), providedBpn)
                                                                                                  .stream()
                                                                                                  .filter(edcDiscoveryResult -> edcDiscoveryResult.bpn()
                                                                                                                                                  .equals(bpn))
                                                                                                  .map(EdcDiscoveryResult::connectorEndpoint))
                                                .flatMap(List::stream)
                                                .toList();
        log.info("Discovered the following endpoints for BPN '{}': '{}'", bpn, String.join(", ", endpoints));
        return endpoints;
    }

}
