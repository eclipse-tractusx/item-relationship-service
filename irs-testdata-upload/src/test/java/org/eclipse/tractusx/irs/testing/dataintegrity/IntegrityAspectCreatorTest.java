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
package org.eclipse.tractusx.irs.testing.dataintegrity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.eclipse.tractusx.irs.testing.dataintegrity.models.IntegrityAspect;
import org.eclipse.tractusx.irs.testing.dataintegrity.models.IntegrityReference;
import org.eclipse.tractusx.irs.testing.dataintegrity.models.TestdataContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntegrityAspectCreatorTest {

    private IntegrityAspectCreator integrityAspectCreator;
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
                      "bpnl": "BPNL00000003AZQP"
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
    void setUp() {
        objectMapper = new ObjectMapper();

        final SecureRandom random = new SecureRandom();
        final BigInteger e = BigInteger.valueOf(0x11);
        final RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
        rsaKeyPairGenerator.init(new RSAKeyGenerationParameters(e, random, 1024, 100));

        final AsymmetricCipherKeyPair asymmetricCipherKeyPair = rsaKeyPairGenerator.generateKeyPair();
        final AsymmetricKeyParameter privateKey = asymmetricCipherKeyPair.getPrivate();
        final AsymmetricKeyParameter publicKey = asymmetricCipherKeyPair.getPublic();

        final IntegritySigner integritySigner = new IntegritySigner(privateKey, publicKey);
        integrityAspectCreator = new IntegrityAspectCreator(integritySigner);
    }

    @Test
    void shouldCreateIntegrityAspectRecursively() throws IOException {
        // Act
        final String result = integrityAspectCreator.enrichTestdata(getTestdata());

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
        final Optional<IntegrityReference> integrityReference = integrityAspect.childParts()
                                                                               .stream()
                                                                               .filter(integrityChildPart -> integrityChildPart.catenaXId()
                                                                                                                               .equals("urn:uuid:5e8910cc-cd76-4863-9fd4-4ddaadd32c89"))
                                                                               .findFirst()
                                                                               .orElseThrow()
                                                                               .references()
                                                                               .stream()
                                                                               .filter(reference -> reference.semanticModelUrn()
                                                                                                             .equals("urn:bamm:io.catenax.serial_part:1.0.1#SerialPart"))
                                                                               .findFirst();
        assertThat(integrityReference).isPresent();
        assertThat(integrityReference.get().hash()).isEqualTo(
                "033b8d149850ae5f1841207abf03dd9ec89e995530e359222862095223a84a30");
    }

    @Test
    void shouldHashAndSignModels() throws JsonProcessingException {
        // Arrange
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

        // Act
        final IntegrityReference integrityReference = integrityAspectCreator.createIntegrityReference(aspectModelEntry);

        // Assert
        assertThat(integrityReference.hash()).isEqualTo(
                "033b8d149850ae5f1841207abf03dd9ec89e995530e359222862095223a84a30");
        assertThat(integrityReference.semanticModelUrn()).isEqualTo("urn:bamm:io.catenax.serial_part:1.0.1#SerialPart");
        assertThat(integrityReference.signature()).isNotBlank();

    }
}