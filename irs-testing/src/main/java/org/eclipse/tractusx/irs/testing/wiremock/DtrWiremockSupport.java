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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.responseWithStatus;

import java.util.List;

import com.github.tomakehurst.wiremock.client.MappingBuilder;

/**
 * WireMock configurations and requests used for testing the decentralized DigitalTwinRegistry flow.
 */
public final class DtrWiremockSupport {
    public static final String DATAPLANE_URL = "http://dataplane.test";
    public static final String DATAPLANE_PUBLIC_PATH = "/api/public";
    public static final String DATAPLANE_PUBLIC_URL = DATAPLANE_URL + DATAPLANE_PUBLIC_PATH;

    public static final String CREATE_SHELL_DESCRIPTOR_PATH = "/shell-descriptors";
    public static final String SHELL_DESCRIPTORS_PATH = "/shell-descriptors/";
    public static final String PUBLIC_SHELL_DESCRIPTORS_PATH = DATAPLANE_PUBLIC_PATH + SHELL_DESCRIPTORS_PATH;
    public static final String SHELL_DESCRIPTORS_TEMPLATE = SHELL_DESCRIPTORS_PATH + "{aasIdentifier}";
    public static final String LOOKUP_SHELLS_PATH = "/lookup/shells";
    public static final String PUBLIC_LOOKUP_SHELLS_PATH = DATAPLANE_PUBLIC_PATH + LOOKUP_SHELLS_PATH;
    public static final String LOOKUP_SHELLS_TEMPLATE = LOOKUP_SHELLS_PATH + "?assetIds={assetIds}";
    public static final int STATUS_CODE_OK = 200;
    public static final int STATUS_CODE_NOT_FOUND = 404;

    private DtrWiremockSupport() {
    }

    public static MappingBuilder getShellDescriptor200() {
        return getShellDescriptor200(SHELL_DESCRIPTORS_PATH + ".*");
    }

    public static MappingBuilder getShellDescriptor200(final String urlRegex) {
        final String materialForRecycling = submodelDescriptor(DATAPLANE_PUBLIC_URL,
                "urn:uuid:19b0338f-6d03-4198-b3b8-5c43f8958d60", DiscoveryServiceWiremockSupport.CONTROLPLANE_PUBLIC_URL,
                "MaterialForRecycling", "urn:uuid:cf06d5d5-e3f8-4bd4-bfcf-81815310701f",
                "urn:bamm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling");

        final String batch = submodelDescriptor(DATAPLANE_PUBLIC_URL, "urn:uuid:234edd2f-0223-47c7-9fe4-3984ab14c4f9",
                DiscoveryServiceWiremockSupport.CONTROLPLANE_PUBLIC_URL, "Batch",
                "urn:uuid:f53db6ef-7a58-4326-9169-0ae198b85dbf", "urn:samm:io.catenax.batch:2.0.0#Batch");

        final String singleLevelBomAsBuilt = submodelDescriptor(DATAPLANE_PUBLIC_URL,
                "urn:uuid:234edd2f-0223-47c7-9fe4-3984ab14c4f9", DiscoveryServiceWiremockSupport.CONTROLPLANE_PUBLIC_URL,
                "SingleLevelBomAsBuilt", "urn:uuid:0e413809-966b-4107-aae5-aeb28bcdaadf",
                "urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt");

        final List<String> submodelDescriptors = List.of(batch, singleLevelBomAsBuilt, materialForRecycling);
        final List<String> specificAssetIds = List.of(specificAssetId("manufacturerId", "BPNL00000003B0Q0"),
                specificAssetId("batchId", "BID12345678"));
        return get(urlPathMatching(urlRegex)).willReturn(responseWithStatus(STATUS_CODE_OK).withBody(
                assetAdministrationShellResponse(submodelDescriptors, "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf5",
                        "EngineeringPlastics", "urn:uuid:9ce43b21-75e3-4cea-b13e-9a34f4f6822a", specificAssetIds)));
    }

    @SuppressWarnings("PMD.UseObjectForClearerAPI") // used only for testing
    public static MappingBuilder getShellDescriptor200(final String urlRegex, final String bpn, final List<String> submodelDescriptors,
            final String globalAssetId, final String shellId, final String idShort) {
        final List<String> specificAssetIds = List.of(specificAssetId("manufacturerId", bpn));
        return get(urlPathMatching(urlRegex)).willReturn(responseWithStatus(STATUS_CODE_OK).withBody(
                assetAdministrationShellResponse(submodelDescriptors, globalAssetId, idShort, shellId, specificAssetIds)));
    }

    public static String assetAdministrationShellResponse(final List<String> submodelDescriptors,
            final String globalAssetId, final String idShort, final String shellId,
            final List<String> specificAssetIds) {
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

    @SuppressWarnings("PMD.UseObjectForClearerAPI") // used only for testing
    public static String submodelDescriptor(final String dataplaneUrl, final String assetId, final String dspEndpoint,
            final String idShort, final String submodelDescriptorId, final String semanticId) {
        final String href = dataplaneUrl + "/" + submodelDescriptorId;
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

    public static MappingBuilder getLookupShells200() {
        return getLookupShells200(LOOKUP_SHELLS_PATH);
    }

    public static MappingBuilder getLookupShells200(final String lookupShellsPath) {
        return get(urlPathEqualTo(lookupShellsPath)).willReturn(responseWithStatus(STATUS_CODE_OK).withBody(
                lookupShellsResponse(List.of("urn:uuid:21f7ebea-fa8a-410c-a656-bd9082e67dcf"))));
    }

    public static MappingBuilder getLookupShells200(final String lookupShellsPath, final List<String> shellIds) {
        return get(urlPathEqualTo(lookupShellsPath)).willReturn(responseWithStatus(STATUS_CODE_OK).withBody(
                lookupShellsResponse(shellIds)));
    }

    public static MappingBuilder getLookupShells200Empty() {
        return get(urlPathMatching(LOOKUP_SHELLS_PATH + ".*")).willReturn(
                responseWithStatus(STATUS_CODE_OK).withBody(lookupShellsResponse(List.of())));
    }

    public static String lookupShellsResponse(final List<String> shellIds) {
        return """
                {
                   "paging_metadata": {},
                   "result": [
                       %s
                   ]
                }
                """.formatted(String.join(",\n", shellIds.stream().map(s -> "\"" + s + "\"").toList()));
    }

    public static MappingBuilder getLookupShells404() {
        return get(urlPathEqualTo(LOOKUP_SHELLS_PATH)).willReturn(responseWithStatus(STATUS_CODE_NOT_FOUND));
    }

    public static MappingBuilder getShellDescriptor404() {
        return get(urlPathMatching(SHELL_DESCRIPTORS_PATH + ".*")).willReturn(
                responseWithStatus(STATUS_CODE_NOT_FOUND));
    }
}
