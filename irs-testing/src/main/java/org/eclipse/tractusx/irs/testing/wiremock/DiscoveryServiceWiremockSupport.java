/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.testing.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.responseWithStatus;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.tomakehurst.wiremock.client.MappingBuilder;

/**
 * WireMock configurations and requests used for testing the Discovery Service flow.
 */
public final class DiscoveryServiceWiremockSupport {
    public static final String CONTROLPLANE_PUBLIC_URL = "https://test.edc.io";
    public static final String EDC_DISCOVERY_PATH = "/edcDiscovery";
    public static final String TEST_BPN = "BPNL00000000TEST";
    public static final String DISCOVERY_FINDER_PATH = "/discoveryFinder";
    public static final String DISCOVERY_HOST = "http://discovery.finder";
    public static final String EDC_DISCOVERY_URL = DISCOVERY_HOST + EDC_DISCOVERY_PATH;
    public static final String DISCOVERY_FINDER_URL = DISCOVERY_HOST + DISCOVERY_FINDER_PATH;
    public static final int STATUS_CODE_OK = 200;
    public static final int STATUS_CODE_NOT_FOUND = 404;

    private DiscoveryServiceWiremockSupport() {
    }

    public static MappingBuilder postEdcDiscovery200() {
        return postEdcDiscovery200(List.of(CONTROLPLANE_PUBLIC_URL));
    }

    public static MappingBuilder postEdcDiscovery200(final String... edcUrls) {
        return postEdcDiscovery200(Arrays.asList(edcUrls));
    }

    public static MappingBuilder postEdcDiscovery200(final List<String> edcUrls) {
        return postEdcDiscovery200(TEST_BPN, edcUrls);
    }

    public static MappingBuilder postEdcDiscovery200Empty() {
        return post(urlPathEqualTo(EDC_DISCOVERY_PATH)).willReturn(responseWithStatus(STATUS_CODE_OK).withBody("[]"));
    }

    public static MappingBuilder postEdcDiscoveryEmpty200() {
        return postEdcDiscovery200(TEST_BPN, List.of());
    }

    public static MappingBuilder postEdcDiscovery200(final String bpn, final List<String> edcUrls) {
        return post(urlPathEqualTo(EDC_DISCOVERY_PATH)).willReturn(
                responseWithStatus(STATUS_CODE_OK).withBody(edcDiscoveryResponse(bpn, edcUrls)));
    }

    public static String edcDiscoveryResponse(final String bpn, final List<String> connectorEndpoints) {
        return """
                [
                  {
                    "bpn": "%s",
                    "connectorEndpoint": [
                      %s
                    ]
                  }
                ]
                """.formatted(bpn, String.join(",\n", connectorEndpoints.stream().map(s -> "\"" + s + "\"").toList()));
    }

    public static MappingBuilder postDiscoveryFinder200() {
        return post(urlPathEqualTo(DISCOVERY_FINDER_PATH)).willReturn(
                responseWithStatus(STATUS_CODE_OK).withBody(discoveryFinderResponse(EDC_DISCOVERY_URL)));
    }

    public static MappingBuilder postDiscoveryFinder200(final String... edcUrls) {
        return post(urlPathEqualTo(DISCOVERY_FINDER_PATH)).willReturn(
                responseWithStatus(STATUS_CODE_OK).withBody(discoveryFinderResponse(edcUrls)));
    }

    public static String discoveryFinderResponse(final String... discoveryFinderUrls) {

        final String endpoints = Arrays.stream(discoveryFinderUrls)
                                       .map(endpointAddress -> {
                                           final String resourceId = UUID.randomUUID().toString();
                                           return """
                                                        {
                                                          "type": "bpn",
                                                          "description": "Service to discover EDC to a particular BPN",
                                                          "endpointAddress": "%s",
                                                          "documentation": "http://.../swagger/index.html",
                                                          "resourceId": "%s"
                                                        }
                                                   """.formatted(endpointAddress, resourceId);
                                       })
                                       .collect(Collectors.joining(","));

        return """
                {
                   "endpoints": [
                     %s
                   ]
                 }
                """.formatted(endpoints);
    }

    public static MappingBuilder postDiscoveryFinder404() {
        return post(urlPathEqualTo(DISCOVERY_FINDER_PATH)).willReturn(responseWithStatus(STATUS_CODE_NOT_FOUND));
    }

    public static MappingBuilder postEdcDiscovery404() {
        return post(urlPathEqualTo(EDC_DISCOVERY_PATH)).willReturn(responseWithStatus(STATUS_CODE_NOT_FOUND));
    }
}
