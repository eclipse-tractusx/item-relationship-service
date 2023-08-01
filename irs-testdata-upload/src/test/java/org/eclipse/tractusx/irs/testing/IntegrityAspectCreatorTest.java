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
package org.eclipse.tractusx.irs.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.testing.KeyUtils.loadPrivateKey;
import static org.eclipse.tractusx.irs.testing.KeyUtils.loadPublicKey;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.eclipse.tractusx.irs.testing.models.IntegrityAspect;
import org.eclipse.tractusx.irs.testing.models.IntegrityReference;
import org.eclipse.tractusx.irs.testing.models.TestdataContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntegrityAspectCreatorTest {

    private IntegrityAspectCreator integrityAspectCreator;
    private String testdata;
    private ObjectMapper objectMapper;

    private static Map<String, Object> getTwin(final String catenaXId, final List<Map<String, Object>> container) {
        for (final Map<String, Object> digitalTwin : container) {
            final String twinCatenaXId = (String) digitalTwin.get("catenaXId");
            if (catenaXId.equals(twinCatenaXId)) {
                return digitalTwin;
            }
        }
        return Map.of();
    }

    private static String getTestdata() {
        return """
                {
                  "policies": {},
                  "https://catenax.io/schema/TestDataContainer/1.0.0": [
                    {
                      "urn:bamm:io.catenax.vehicle.product_description:1.0.0#ProductDescription": [
                        {
                          "bodyVariant": "Sedan",
                          "catenaXId": "urn:uuid:6ce41c0b-c84a-46b0-b4c4-78fce958663f"
                        }
                      ],
                      "urn:bamm:io.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt": [
                        {
                          "catenaXId": "urn:uuid:6ce41c0b-c84a-46b0-b4c4-78fce958663f",
                          "childItems": [
                            {
                              "catenaXId": "urn:uuid:5e8910cc-cd76-4863-9fd4-4ddaadd32c89",
                              "quantity": {
                                "quantityNumber": 2.5,
                                "measurementUnit": "unit:litre"
                              },
                              "businessPartner": "BPNL00000003AVTH",
                              "createdOn": "2022-02-03T14:48:54.709Z",
                              "lastModifiedOn": "2022-02-03T14:48:54.709Z"
                            }
                          ]
                        }
                      ],
                      "catenaXId": "urn:uuid:6ce41c0b-c84a-46b0-b4c4-78fce958663f",
                      "urn:bamm:io.catenax.certificate_of_destruction:1.0.0#CertificateOfDestruction": [
                        {
                          "catenaXId": "urn:uuid:6ce41c0b-c84a-46b0-b4c4-78fce958663f",
                          "dismantlingDate": "2022-07-11T08:38:46.739Z"
                        }
                      ],
                      "bpnl": "BPNL00000003AZQP",
                      "urn:bamm:io.catenax.serial_part:1.0.1#SerialPart": [
                        {
                          "manufacturingInformation": {
                            "date": "2020-01-03T13:51:32.000Z",
                            "country": "DEU"
                          },
                          "catenaXId": "urn:uuid:6ce41c0b-c84a-46b0-b4c4-78fce958663f"
                        }
                      ],
                      "urn:bamm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling": [
                        {
                          "component": [
                            {
                              "materialName": "Iron",
                              "recycledContent": 45,
                              "materialClass": "1.1",
                              "quantity": {
                                "unit": "unit:kilogram",
                                "value": 327.6
                              },
                              "aggregateState": "solid",
                              "materialAbbreviation": "IR334"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "urn:bamm:io.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt": [
                        {
                          "catenaXId": "urn:uuid:5e8910cc-cd76-4863-9fd4-4ddaadd32c89",
                          "childItems": [
                            {
                              "catenaXId": "urn:uuid:f41dae33-1eab-48c5-89d7-f36f71bb4e5a",
                              "quantity": {
                                "quantityNumber": 2.5,
                                "measurementUnit": "unit:litre"
                              },
                              "businessPartner": "BPNL00000003AYRE",
                              "createdOn": "2022-02-03T14:48:54.709Z",
                              "lastModifiedOn": "2022-02-03T14:48:54.709Z"
                            }
                          ]
                        }
                      ],
                      "catenaXId": "urn:uuid:5e8910cc-cd76-4863-9fd4-4ddaadd32c89",
                      "bpnl": "BPNL00000003CSGV",
                      "urn:bamm:io.catenax.single_level_usage_as_built:1.0.1#SingleLevelUsageAsBuilt": [
                        {
                          "parentParts": [
                            {
                              "parentCatenaXId": "urn:uuid:6ce41c0b-c84a-46b0-b4c4-78fce958663f"
                            }
                          ],
                          "catenaXId": "urn:uuid:5e8910cc-cd76-4863-9fd4-4ddaadd32c89"
                        }
                      ],
                      "urn:bamm:io.catenax.serial_part:1.0.1#SerialPart": [
                        {
                          "manufacturingInformation": {
                            "date": "2022-02-04T14:48:54",
                            "country": "DEU"
                          },
                          "catenaXId": "urn:uuid:5e8910cc-cd76-4863-9fd4-4ddaadd32c89"
                        }
                      ]
                    },
                    {
                      "catenaXId": "urn:uuid:f41dae33-1eab-48c5-89d7-f36f71bb4e5a",
                      "bpnl": "BPNL00000000BJTL",
                      "urn:bamm:io.catenax.single_level_usage_as_built:1.0.1#SingleLevelUsageAsBuilt": [
                        {
                          "parentParts": [
                            {
                              "parentCatenaXId": "urn:uuid:5e8910cc-cd76-4863-9fd4-4ddaadd32c89"
                            }
                          ],
                          "catenaXId": "urn:uuid:f41dae33-1eab-48c5-89d7-f36f71bb4e5a"
                        }
                      ],
                      "urn:bamm:io.catenax.serial_part:1.0.1#SerialPart": [
                        {
                          "manufacturingInformation": {
                            "date": "2022-02-04T14:48:54",
                            "country": "DEU"
                          },
                          "catenaXId": "urn:uuid:f41dae33-1eab-48c5-89d7-f36f71bb4e5a"
                        }
                      ]
                    }
                  ]
                }
                """;
    }

    @BeforeEach
    void setUp() throws Exception {
        testdata = getTestdata();
        objectMapper = new ObjectMapper();
        final Path pathPrivate = Paths.get(
                Objects.requireNonNull(getClass().getClassLoader().getResource("priv-key.pem")).toURI());
        AsymmetricKeyParameter privateKey = loadPrivateKey(new FileInputStream(pathPrivate.toFile()));

        final Path pathPub = Paths.get(
                Objects.requireNonNull(getClass().getClassLoader().getResource("pub-key.pem")).toURI());
        AsymmetricKeyParameter publicKey = loadPublicKey(new FileInputStream(pathPub.toFile()));
        final IntegritySigner integritySigner = new IntegritySigner(privateKey, publicKey);
        integrityAspectCreator = new IntegrityAspectCreator(integritySigner);
    }

    @Test
    void shouldEnrichTestdata() throws IOException {
        // Act
        final String result = integrityAspectCreator.enrichTestdata(testdata);

        // Assert
        assertThat(result).contains("urn:bamm:io.catenax.data_integrity:1.0.0#DataIntegrity")
                          .contains("references")
                          .contains("hash");

        final TestdataContainer testdataContainer = objectMapper.readValue(result, TestdataContainer.class);

        final String catenaXId = "urn:uuid:6ce41c0b-c84a-46b0-b4c4-78fce958663f";
        final List<Map<String, Object>> container = testdataContainer.getContainer();
        final Map<String, Object> twin = getTwin(catenaXId, container);
        assertThat(twin).containsKey("urn:bamm:io.catenax.data_integrity:1.0.0#DataIntegrity");
        final String dI = objectMapper.writeValueAsString(
                twin.get("urn:bamm:io.catenax.data_integrity:1.0.0#DataIntegrity"));
        final IntegrityAspect[] integrityAspects = objectMapper.readValue(dI, IntegrityAspect[].class);
        assertThat(integrityAspects).hasSize(1);
        final IntegrityAspect integrityAspect = integrityAspects[0];
        assertThat(integrityAspect.childParts()).hasSize(1);
        final IntegrityReference integrityReference = integrityAspect.childParts()
                                                                     .stream()
                                                                     .findFirst()
                                                                     .get()
                                                                     .references()
                                                                     .get(0);
        assertThat(integrityReference.semanticModelUrn()).isEqualTo(
                "urn:bamm:io.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt");
        assertThat(integrityReference.hash()).isEqualTo(
                "53863105c26c6100e245025ffdfc79780fec881e6bdfbbe4ef7652522814483c");
    }

    @Test
    void name() throws JsonProcessingException {
        String test = """
                {
                        "urn:bamm:io.catenax.serial_part:1.0.1#SerialPart": [
                            {
                                "manufacturingInformation": {
                                  "date": "2022-02-04T14:48:54",
                                  "country": "DEU"
                                },
                                "catenaXId": "urn:uuid:5e8910cc-cd76-4863-9fd4-4ddaadd32c89"
                            }
                        ]
                        }
                """;
        final Map<String, Object> map = objectMapper.readValue(test, Map.class);
        final Map.Entry<String, Object> aspectModelEntry = map.entrySet().stream().findFirst().orElseThrow();
        final IntegrityReference integrityReference = integrityAspectCreator.createIntegrityReference(aspectModelEntry);
        assertThat(integrityReference.hash()).isEqualTo(
                "033b8d149850ae5f1841207abf03dd9ec89e995530e359222862095223a84a30");
        assertThat(integrityReference.semanticModelUrn()).isEqualTo("urn:bamm:io.catenax.serial_part:1.0.1#SerialPart");
        assertThat(integrityReference.signature()).isEqualTo(
                "0f1fb21d4650ff6914d43419488d4fb6a37afc462b62fb6f805f1be9234070b8a00dd87f1db557f312d9211a2e766cb744947c4ada0236ec792dad0bd488325b920281d12a1898e800f4b9552e55512206bde03b65ef2855e2bb00798eb8839702e761d8de0f025ef2b86d5e87cef6ede516bff90a3c7400644c64e35861d883d88d75c002a09687a43d2348cfd8e10c39d35a22e077dde66407ad3ab08d6d725c834355f318ddfade1487228795c5d0e7470273e92fb1daae847b3b721b4d5205ce257ab3fd3f0b525f7a008be3f420e628c1d0f4bdd963e375fc3042c757c1f03b74fbd2ae2ffb8b24eb1b876689e53c914bb6cb2cb8bd118a13565c3d9747");

    }
}