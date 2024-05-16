/*
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
 */

package org.eclipse.tractusx.irs.edc.client.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.objectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.core.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonObjectToCatalogTransformerTest {

    private EdcTransformer edcTransformer;

    @BeforeEach
    void setUp() {
        final TitaniumJsonLd jsonLd = new TitaniumJsonLd(new ConsoleMonitor());
        jsonLd.registerNamespace("odrl", "http://www.w3.org/ns/odrl/2/");
        jsonLd.registerNamespace("dct", "http://purl.org/dc/terms/");
        jsonLd.registerNamespace("tx", "https://w3id.org/tractusx/v0.0.1/ns/");
        jsonLd.registerNamespace("edc", "https://w3id.org/edc/v0.0.1/ns/");
        jsonLd.registerNamespace("dcat", "https://www.w3.org/ns/dcat/");
        jsonLd.registerNamespace("dspace", "https://w3id.org/dspace/v0.8/");

        ObjectMapper objectMapper = objectMapper();
        edcTransformer = new EdcTransformer(objectMapper, jsonLd, new TypeTransformerRegistryImpl());
    }

    @Test
    void shouldTransformJsonObjectToPolicyCorrectly() {
        // Arrange
        final String catalogJson = getCatalogJson();

        // Act
        final Catalog catalog = edcTransformer.transformCatalog(catalogJson, StandardCharsets.UTF_8);

        // Assert
        assertThat(catalog).isNotNull();
        assertThat(catalog.getId()).isEqualTo("16d542da-7a7d-4419-b5e9-38e92a4e1395");
        assertThat(catalog.getDatasets()).hasSize(2);
        assertThat(catalog.getDatasets().get(0).getDistributions()).hasSize(3);
        assertThat(catalog.getDatasets().get(0).getOffers()).hasSize(1);
        assertThat(catalog.getDatasets().get(0).getOffers()).containsKey(
                "ZDU1MTA0YTUtOGJiZi00YjRhLTgzMTItZTQ4MzY5ZTlkNWIw:cmVnaXN0cnktYXNzZXQ=:YTA3ZjFkYjAtOGE4MS00ZWZkLTlmMGYtMmExZmNmMWI3ZmE3");
        assertThat(catalog.getParticipantId()).isNull();
        assertThat(catalog.getProperties()).hasSize(2);
        assertThat(catalog.getProperties()).containsAllEntriesOf(
                Map.of("https://w3id.org/dspace/v0.8/participantId", "BPNL00000001CRHK",
                        "https://w3id.org/edc/v0.0.1/ns/participantId", "BPNL00000001CRHK"));
        assertThat(catalog.getDataServices()).hasSize(1);
        assertThat(catalog.getDataServices().get(0).getEndpointUrl()).isEqualTo("https://irs-test2-cp.dev.demo.catena-x.net/api/v1/dsp");
    }

    private static @NotNull String getCatalogJson() {
        return """
                {
                	"@id": "16d542da-7a7d-4419-b5e9-38e92a4e1395",
                	"@type": "dcat:Catalog",
                	"dspace:participantId": "BPNL00000001CRHK",
                	"dcat:dataset": [
                		{
                			"@id": "registry-asset",
                			"@type": "dcat:Dataset",
                			"odrl:hasPolicy": {
                				"@id": "ZDU1MTA0YTUtOGJiZi00YjRhLTgzMTItZTQ4MzY5ZTlkNWIw:cmVnaXN0cnktYXNzZXQ=:YTA3ZjFkYjAtOGE4MS00ZWZkLTlmMGYtMmExZmNmMWI3ZmE3",
                				"@type": "odrl:Offer",
                				"odrl:permission": {
                					"odrl:action": {
                						"odrl:type": "use"
                					},
                					"odrl:constraint": {
                						"odrl:or": {
                							"odrl:leftOperand": "PURPOSE",
                							"odrl:operator": {
                								"@id": "odrl:eq"
                							},
                							"odrl:rightOperand": "ID 3.1 Trace"
                						}
                					}
                				},
                				"odrl:prohibition": [],
                				"odrl:obligation": []
                			},
                			"dcat:distribution": [
                				{
                					"@type": "dcat:Distribution",
                					"dct:format": {
                						"@id": "AzureStorage-PUSH"
                					},
                					"dcat:accessService": {
                						"@id": "7d021194-7e36-43bf-ba3e-ed59675e4576",
                						"@type": "dcat:DataService",
                						"dct:terms": "connector",
                						"dct:endpointUrl": "https://irs-test2-cp.dev.demo.catena-x.net/api/v1/dsp"
                					}
                				},
                				{
                					"@type": "dcat:Distribution",
                					"dct:format": {
                						"@id": "HttpData-PULL"
                					},
                					"dcat:accessService": {
                						"@id": "7d021194-7e36-43bf-ba3e-ed59675e4576",
                						"@type": "dcat:DataService",
                						"dct:terms": "connector",
                						"dct:endpointUrl": "https://irs-test2-cp.dev.demo.catena-x.net/api/v1/dsp"
                					}
                				},
                				{
                					"@type": "dcat:Distribution",
                					"dct:format": {
                						"@id": "AmazonS3-PUSH"
                					},
                					"dcat:accessService": {
                						"@id": "7d021194-7e36-43bf-ba3e-ed59675e4576",
                						"@type": "dcat:DataService",
                						"dct:terms": "connector",
                						"dct:endpointUrl": "https://irs-test2-cp.dev.demo.catena-x.net/api/v1/dsp"
                					}
                				}
                			],
                			"type": "data.core.digitalTwinRegistry",
                			"description": "Digital Twin Registry Endpoint of IRS DEV",
                			"id": "registry-asset"
                		},
                		{
                			"@id": "urn:uuid:ea32f6f7-c884-4bfd-af4a-778666a1fffb",
                			"@type": "dcat:Dataset",
                			"odrl:hasPolicy": {
                				"@id": "YTYxMjJlNTUtZWY2Ni00MWM4LWFjMjgtZDA5ODhlMmQ2YzRi:dXJuOnV1aWQ6ZWEzMmY2ZjctYzg4NC00YmZkLWFmNGEtNzc4NjY2YTFmZmZi:NGFmMjU0YTktNTg0OS00YmIwLTg2YTEtYTJjMDJjZTE0YTEy",
                				"@type": "odrl:Offer",
                				"odrl:permission": {
                					"odrl:action": {
                						"odrl:type": "use"
                					},
                					"odrl:constraint": {
                						"odrl:or": {
                							"odrl:leftOperand": "PURPOSE",
                							"odrl:operator": {
                								"@id": "odrl:eq"
                							},
                							"odrl:rightOperand": "ID 3.1 Trace"
                						}
                					}
                				},
                				"odrl:prohibition": [],
                				"odrl:obligation": []
                			},
                			"dcat:distribution": [
                				{
                					"@type": "dcat:Distribution",
                					"dct:format": {
                						"@id": "AzureStorage-PUSH"
                					},
                					"dcat:accessService": {
                						"@id": "7d021194-7e36-43bf-ba3e-ed59675e4576",
                						"@type": "dcat:DataService",
                						"dct:terms": "connector",
                						"dct:endpointUrl": "https://irs-test2-cp.dev.demo.catena-x.net/api/v1/dsp"
                					}
                				},
                				{
                					"@type": "dcat:Distribution",
                					"dct:format": {
                						"@id": "HttpData-PULL"
                					},
                					"dcat:accessService": {
                						"@id": "7d021194-7e36-43bf-ba3e-ed59675e4576",
                						"@type": "dcat:DataService",
                						"dct:terms": "connector",
                						"dct:endpointUrl": "https://irs-test2-cp.dev.demo.catena-x.net/api/v1/dsp"
                					}
                				},
                				{
                					"@type": "dcat:Distribution",
                					"dct:format": {
                						"@id": "AmazonS3-PUSH"
                					},
                					"dcat:accessService": {
                						"@id": "7d021194-7e36-43bf-ba3e-ed59675e4576",
                						"@type": "dcat:DataService",
                						"dct:terms": "connector",
                						"dct:endpointUrl": "https://irs-test2-cp.dev.demo.catena-x.net/api/v1/dsp"
                					}
                				}
                			],
                			"description": "IRS EDC Test Asset",
                			"id": "urn:uuid:ea32f6f7-c884-4bfd-af4a-778666a1fffb"
                		}
                	],
                	"dcat:service": {
                		"@id": "7d021194-7e36-43bf-ba3e-ed59675e4576",
                		"@type": "dcat:DataService",
                		"dct:terms": "connector",
                		"dct:endpointUrl": "https://irs-test2-cp.dev.demo.catena-x.net/api/v1/dsp"
                	},
                	"participantId": "BPNL00000001CRHK",
                	"@context": {
                		"@vocab": "https://w3id.org/edc/v0.0.1/ns/",
                		"edc": "https://w3id.org/edc/v0.0.1/ns/",
                		"tx": "https://w3id.org/tractusx/v0.0.1/ns/",
                		"tx-auth": "https://w3id.org/tractusx/auth/",
                		"cx-policy": "https://w3id.org/catenax/policy/",
                		"dcat": "http://www.w3.org/ns/dcat#",
                		"dct": "http://purl.org/dc/terms/",
                		"odrl": "http://www.w3.org/ns/odrl/2/",
                		"dspace": "https://w3id.org/dspace/v0.8/"
                	}
                }
                """;
    }
}