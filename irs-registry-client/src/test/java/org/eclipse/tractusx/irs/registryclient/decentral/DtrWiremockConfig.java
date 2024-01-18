/********************************************************************************
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import java.util.List;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

public class DtrWiremockConfig {
    public static final String SHELL_DESCRIPTORS_PATH = "/shell-descriptors/";
    public static final String SHELL_DESCRIPTORS_TEMPLATE = SHELL_DESCRIPTORS_PATH + "{aasIdentifier}";
    public static final String LOOKUP_SHELLS_PATH = "/lookup/shells";
    public static final String LOOKUP_SHELLS_TEMPLATE = LOOKUP_SHELLS_PATH + "?assetIds={assetIds}";
    public static final String DISCOVERY_FINDER_PATH = "/discoveryFinder";
    public static final String EDC_DISCOVERY_PATH = "/edcDiscovery";
    public static final String TEST_BPN = "BPNL00000000TEST";
    public static final String DISCOVERY_HOST = "http://discovery.finder";
    public static final String DATAPLANE_URL = "http://dataplane.test";
    public static final String DISCOVERY_FINDER_URL = DISCOVERY_HOST + DISCOVERY_FINDER_PATH;

    static ResponseDefinitionBuilder responseWithStatus(final int statusCode) {
        return aResponse().withStatus(statusCode).withHeader("Content-Type", "application/json;charset=UTF-8");
    }

    static MappingBuilder getShellDescriptor200() {
        final String materialForRecycling = submodel("https://dataplane.test/api/public/data/",
                "urn:uuid:19b0338f-6d03-4198-b3b8-5c43f8958d60", "https://controlplane.test", "MaterialForRecycling",
                "urn:uuid:cf06d5d5-e3f8-4bd4-bfcf-81815310701f",
                "urn:bamm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling");

        final String batch = submodel("https://dataplane.test/api/public/data/",
                "urn:uuid:234edd2f-0223-47c7-9fe4-3984ab14c4f9", "https://controlplane.test", "Batch",
                "urn:uuid:f53db6ef-7a58-4326-9169-0ae198b85dbf", "urn:samm:io.catenax.batch:2.0.0#Batch");

        final String singleLevelUsageAsBuilt = submodel("https://dataplane.test/api/public/data/",
                "urn:uuid:f8196d6a-1664-4531-bdee-f15dbb1daf26", "https://controlplane.test", "SingleLevelUsageAsBuilt",
                "urn:uuid:e2899f43-eca8-4aec-b877-4a69691f0487",
                "urn:bamm:io.catenax.single_level_usage_as_built:2.0.0#SingleLevelUsageAsBuilt");

        final List<String> submodelDescriptors = List.of(materialForRecycling, batch, singleLevelUsageAsBuilt);
        final List<String> specificAssetIds = List.of(specificAssetId("manufacturerId", "BPNL00000003B0Q0"),
                specificAssetId("batchId", "BID12345678"));
        return get(urlPathMatching(SHELL_DESCRIPTORS_PATH + ".*")).willReturn(responseWithStatus(200).withBody(
                assetAdministrationShell(submodelDescriptors, "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf5",
                        "EngineeringPlastics", "urn:uuid:9ce43b21-75e3-4cea-b13e-9a34f4f6822a", specificAssetIds)));
    }

    public static String assetAdministrationShell(final List<String> submodelDescriptors, final String globalAssetId,
            final String idShort, final String shellId, final List<String> specificAssetIds) {
        return """
                {
                  "description": [],
                  "displayName": [],
                  "globalAssetId": "%s",
                  "idShort": "%s",
                  "id": "%s",
                  "specificAssetIds": [
                    %s
                  ],
                  "submodelDescriptors": [
                    %s
                  ]
                }
                """.formatted(globalAssetId, idShort, shellId, String.join(",\n", specificAssetIds),
                String.join(",\n", submodelDescriptors));
    }

    public static String specificAssetId(final String key, final String value) {
        return """
                {
                  "supplementalSemanticIds": [],
                  "name": "%s",
                  "value": "%s",
                  "externalSubjectId": {
                    "type": "ExternalReference",
                    "keys": [
                      {
                        "type": "GlobalReference",
                        "value": "BPNL00000001CRHK"
                      }
                    ]
                  }
                }
                """.formatted(key, value);
    }

    public static String submodel(final String dataplaneUrl, final String assetId, final String dspEndpoint,
            final String idShort, final String submodelDescriptorId, final String semanticId) {
        final String href = dataplaneUrl + submodelDescriptorId;
        return """
                {
                  "endpoints": [
                    {
                      "interface": "SUBMODEL-3.0",
                      "protocolInformation": {
                        "href": "%s",
                        "endpointProtocol": "HTTP",
                        "endpointProtocolVersion": [
                          "1.1"
                        ],
                        "subprotocol": "DSP",
                        "subprotocolBody": "id=%s;dspEndpoint=%s",
                        "subprotocolBodyEncoding": "plain",
                        "securityAttributes": [
                          {
                            "type": "NONE",
                            "key": "NONE",
                            "value": "NONE"
                          }
                        ]
                      }
                    }
                  ],
                  "idShort": "%s",
                  "id": "%s",
                  "semanticId": {
                    "type": "ExternalReference",
                    "keys": [
                      {
                        "type": "GlobalReference",
                        "value": "%s"
                      }
                    ]
                  },
                  "supplementalSemanticId": [],
                  "description": [],
                  "displayName": []
                }
                """.formatted(href, assetId, dspEndpoint, idShort, submodelDescriptorId, semanticId);
    }

    static MappingBuilder postEdcDiscovery200() {
        return post(urlPathEqualTo(EDC_DISCOVERY_PATH)).willReturn(
                responseWithStatus(200).withBody(edcDiscovery("BPNL00000000TEST", List.of("http://edc.test/edc"))));
    }

    public static String edcDiscovery(final String bpn, final List<String> connectorEndpoints) {
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

    static MappingBuilder postDiscoveryFinder200() {
        return post(urlPathEqualTo(DISCOVERY_FINDER_PATH)).willReturn(
                responseWithStatus(200).withBody(discoveryFinder("http://discovery.finder/edcDiscovery")));
    }

    public static String discoveryFinder(final String discoveryFinderUrl) {
        return """
                {
                   "endpoints": [
                     {
                       "type": "bpn",
                       "description": "Service to discover EDC to a particular BPN",
                       "endpointAddress": "%s",
                       "documentation": "http://.../swagger/index.html",
                       "resourceId": "316417cd-0fb5-4daf-8dfa-8f68125923f1"
                     }
                   ]
                 }
                """.formatted(discoveryFinderUrl);
    }

    private static String discoveryFinderEmtpy() {
        return """
                {
                   "endpoints": [
                   ]
                 }
                """;
    }

    static MappingBuilder postDiscoveryFinder404() {
        return post(urlPathEqualTo(DISCOVERY_FINDER_PATH)).willReturn(responseWithStatus(404));
    }

    static MappingBuilder postEdcDiscovery404() {
        return post(urlPathEqualTo(EDC_DISCOVERY_PATH)).willReturn(responseWithStatus(404));
    }

    static MappingBuilder getLookupShells200() {
        return get(urlPathEqualTo(LOOKUP_SHELLS_PATH)).willReturn(responseWithStatus(200).withBody(
                lookupShells(List.of("urn:uuid:21f7ebea-fa8a-410c-a656-bd9082e67dcf"))));
    }

    static MappingBuilder getLookupShells200Empty() {
        return get(urlPathMatching(LOOKUP_SHELLS_PATH + ".*")).willReturn(
                responseWithStatus(200).withBody(lookupShells(List.of())));
    }

    public static String lookupShells(final List<String> shellIds) {
        return """
                {
                   "paging_metadata": {},
                   "result": [
                       %s
                   ]
                }
                """.formatted(String.join(",\n", shellIds.stream().map(s -> "\"" + s + "\"").toList()));
    }

    static MappingBuilder getLookupShells404() {
        return get(urlPathEqualTo(LOOKUP_SHELLS_PATH)).willReturn(responseWithStatus(404));
    }

    static MappingBuilder getShellDescriptor404() {
        return get(urlPathMatching(SHELL_DESCRIPTORS_PATH + ".*")).willReturn(responseWithStatus(404));
    }
}