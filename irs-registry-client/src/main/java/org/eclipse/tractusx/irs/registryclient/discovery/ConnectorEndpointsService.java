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
package org.eclipse.tractusx.irs.registryclient.discovery;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Connector Endpoints service to find connectors in Discovery Finder
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class ConnectorEndpointsService {
    private static final String DSP_PATH = "/api/v1/dsp";
    private final DiscoveryFinderClient discoveryFinderClient;
    private static final String CONNECTOR_ENDPOINT_SERVICE_CACHE_NAME = "connector_endpoint_service_cache";
    private final String discoveryType;

    /**
     * Get EDCs for BPN.
     *
     * @param bpn the BPN
     * @return list of EDC URLs
     */
    @Cacheable(CONNECTOR_ENDPOINT_SERVICE_CACHE_NAME)
    public List<String> fetchConnectorEndpoints(final String bpn) {

        if (StringUtils.isBlank(bpn)) {
            log.warn("BPN was null, cannot search for any connector endpoints. Returning empty list.");
            return List.of();
        }

        log.info("Requesting connector endpoints for BPN {}", bpn);
        final var onlyBpn = new DiscoveryFinderRequest(List.of(discoveryType));
        final var discoveryEndpoints = discoveryFinderClient.findDiscoveryEndpoints(onlyBpn).endpoints();
        final var endpoints = discoveryEndpoints.stream()
                                                .flatMap(
                                                        discoveryEndpoint -> discoveryFinderClient.findConnectorEndpoints(
                                                                                                          discoveryEndpoint.endpointAddress(), List.of(bpn))
                                                                                                  .stream()
                                                                                                  .filter(edcDiscoveryResult -> edcDiscoveryResult.bpn()
                                                                                                                                                  .equals(bpn))
                                                                                                  .map(EdcDiscoveryResult::connectorEndpoint))
                                                .flatMap(List::stream)
                                                .map(ConnectorEndpointsService::ensureStandardDspEndpoint)
                                                .toList();
        final var endPointsWithoutDuplicates = removeDuplicates(endpoints);

        log.info("Discovered the following endpoints for BPN '{}': '{}'", bpn,
                String.join(", ", endPointsWithoutDuplicates));
        return endPointsWithoutDuplicates;
    }

    private static List<String> removeDuplicates(final List<String> urls) {
        final Set<String> uniqueUrls = new LinkedHashSet<>(urls);
        return new ArrayList<>(uniqueUrls);
    }

    private static String ensureStandardDspEndpoint(final String url) {
        String processedUrl = url;
        if (!processedUrl.endsWith(DSP_PATH)) {
            if (processedUrl.endsWith("/")) {
                processedUrl = processedUrl.substring(0, processedUrl.length() - 1);
            }
            processedUrl += DSP_PATH;
        }
        return processedUrl;
    }

    @CacheEvict(value = CONNECTOR_ENDPOINT_SERVICE_CACHE_NAME, allEntries = true)
    @Scheduled(fixedRateString = "${irs-edc-client.connectorEndpointService.cacheTTL}")
    public void evictCachesValues() {
        log.debug("Clearing \"{}\" cache.", CONNECTOR_ENDPOINT_SERVICE_CACHE_NAME);
    }
}
