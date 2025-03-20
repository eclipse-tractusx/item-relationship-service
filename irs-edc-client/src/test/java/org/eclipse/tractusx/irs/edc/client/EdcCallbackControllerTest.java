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
package org.eclipse.tractusx.irs.edc.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.storage.ContractNegotiationIdStorage;
import org.eclipse.tractusx.irs.edc.client.storage.EndpointDataReferenceStorage;
import org.junit.jupiter.api.Test;

class EdcCallbackControllerTest {

    private final EndpointDataReferenceStorage storage = new EndpointDataReferenceStorage(Duration.ofMinutes(1));
    private final ContractNegotiationIdStorage contractNegotiationIdStorage = new ContractNegotiationIdStorage(Duration.of(1, ChronoUnit.MINUTES));
    private final EdcCallbackController testee = new EdcCallbackController(storage, contractNegotiationIdStorage);

    @Test
    void shouldStoreAgreementId() {
        // arrange
        final String ref = """
                {
                    "id": "bc916834-61b8-4754-b3e2-1eb041d253c2",
                    "at": 1714645750814,
                    "payload": {
                        "assetId": "urn:uuid:df3aa078-567a-4b39-afa1-c92f32e6eaad",
                        "contractId": "testContractId",
                        "dataAddress": {
                            "properties": {
                                "process_id": "testid",
                                "https://w3id.org/edc/v0.0.1/ns/endpoint": "test",
                                "asset_id": "urn:uuid:df3aa078-567a-4b39-afa1-c92f32e6eaad",
                                "agreement_id": "testContractId",
                                "https://w3id.org/edc/v0.0.1/ns/authorization": "testToken"
                            }
                        }
                    }
                }
                """;
        final String expectedId = "testid";
        final String expectedContractId = "testContractId";
        final String expectedEndpoint = "test";
        final String expectedAuthKey = "Authorization";
        final String expectedAuthCode = "testToken";

        // act
        testee.receiveEdcCallback(ref);

        // assert
        final var result = storage.get("testContractId");
        assertThat(result).isNotNull().isPresent();
        assertThat(result.get().getId()).isEqualTo(expectedId);
        assertThat(result.get().getContractId()).isEqualTo(expectedContractId);
        assertThat(result.get().getEndpoint()).isEqualTo(expectedEndpoint);
        assertThat(result.get().getAuthKey()).isEqualTo(expectedAuthKey);
        assertThat(result.get().getAuthCode()).isEqualTo(expectedAuthCode);
    }

    @Test
    void shouldDoNothingWhenEDRTokenIsInvalid() {
        // arrange
        final String ref = """
                {
                    "id": "bc916834-61b8-4754-b3e2-1eb041d253c2",
                    "at": 1714645750814,
                    "payload": {
                        "assetId": "urn:uuid:df3aa078-567a-4b39-afa1-c92f32e6eaad",
                        "contractId": "testContractId"
                    }
                }
                """;

        // act
        testee.receiveEdcCallback(ref);

        // assert
        final var result = storage.get("testContractId");
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void shouldDeserializeCallbackEDR() {
        // arrange
        final String callbackEDR = """
                {
                    "id": "b4d5d2f6-9da4-4236-864f-2803aeda1f56",
                    "at": 1714647576205,
                    "payload": {
                        "transferProcessId": "2a02c181-7bb4-4521-aeb2-867adb829556",
                        "callbackAddresses": [
                            {
                                "uri": "http://callback.url",
                                "events": [
                                    "transfer.process.started"
                                ],
                                "transactional": false,
                                "authKey": null,
                                "authCodeId": null
                            }
                        ],
                        "assetId": "urn:uuid:df3aa078-567a-4b39-afa1-c92f32e6eaad",
                        "type": "CONSUMER",
                        "contractId": "e6a5704f-fdba-4ebd-975e-f650af8a70a8",
                        "dataAddress": {
                            "properties": {
                                "process_id": "ca06c205-71d6-4a0f-97a8-835189fa9856",
                                "participant_id": "BPNL000000012345",
                                "asset_id": "urn:uuid:df3aa078-567a-4b39-afa1-c92f32e6eaad",
                                "https://w3id.org/edc/v0.0.1/ns/endpointType": "https://w3id.org/idsa/v4.1/HTTP",
                                "https://w3id.org/tractusx/auth/refreshEndpoint": "http://dataplane.url/api/public/token",
                                "https://w3id.org/tractusx/auth/audience": "did:web:issuer:did:BPNL000000012345",
                                "agreement_id": "e6a5704f-fdba-4ebd-975e-f650af8a70a8",
                                "flow_type": "PULL",
                                "https://w3id.org/edc/v0.0.1/ns/type": "https://w3id.org/idsa/v4.1/HTTP",
                                "https://w3id.org/edc/v0.0.1/ns/endpoint": "http://dataplane.url/api/public",
                                "https://w3id.org/tractusx/auth/refreshToken": "testRefreshToken",
                                "https://w3id.org/tractusx/auth/expiresIn": "300",
                                "https://w3id.org/edc/v0.0.1/ns/authorization": "testJWT",
                                "https://w3id.org/tractusx/auth/refreshAudience": "did:web:issuer:did:BPNL000000012345"
                            }
                        }
                    },
                    "type": "TransferProcessStarted"
                }
                """;

        // act
        testee.receiveEdcCallback(callbackEDR);

        // assert
        final Optional<EndpointDataReference> actualEdr = storage.get("e6a5704f-fdba-4ebd-975e-f650af8a70a8");
        assertThat(actualEdr).isNotNull().isPresent();
        assertThat(actualEdr.get().getAuthCode()).isEqualTo("testJWT");
        assertThat(actualEdr.get().getAuthKey()).isEqualTo("Authorization");
        assertThat(actualEdr.get().getContractId()).isEqualTo("e6a5704f-fdba-4ebd-975e-f650af8a70a8");
    }

    @Test
    void shouldStoreNegotiationId() {
        // arrange
        final String callbackNegotiation = """
                {
                  "id": "c87cbd8a-363a-41eb-a9f5-214da3a08bdc",
                  "at": 1732284831946,
                  "payload": {
                    "contractNegotiationId": "negotiationId",
                    "counterPartyAddress": "https://tracexb-provider-edc-edc.enablement.integration.cofinity-x.com/api/v1/dsp",
                    "counterPartyId": "BPNL000000012345",
                    "callbackAddresses": [
                      {
                        "uri": "https://callback.url/edr",
                        "events": [
                          "transfer.process.started"
                        ],
                        "transactional": false,
                        "authKey": null,
                        "authCodeId": null
                      },
                      {
                        "uri": "https://callback.url/negotiation",
                        "events": [
                          "contract.negotiation.finalized"
                        ],
                        "transactional": false,
                        "authKey": null,
                        "authCodeId": null
                      },
                      {
                        "uri": "local://adapter",
                        "events": [
                          "contract.negotiation",
                          "transfer.process"
                        ],
                        "transactional": true,
                        "authKey": null,
                        "authCodeId": null
                      }
                    ],
                    "contractOffers": [
                      {
                        "id": "ZmNiZTVmMDgtOTUzNi00OWM2LTgyYzMtODYwYzcxNGE0M2Ex:dXJuOnV1aWQ6YjA4ZjU0ZTAtOTJjYS00Y2E1LTkxMTUtNzUzMWMzYTQ3YmJj:OTcxNmVkYmQtZDc2OS00Nzc5LTgwZGItMTFkODRlNmEzYTJh",
                        "policy": {
                          "permissions": [
                            {
                              "edctype": "dataspaceconnector:permission",
                              "action": {
                                "type": "use",
                                "includedIn": null,
                                "constraint": null
                              },
                              "constraints": [
                                {
                                  "edctype": "AndConstraint",
                                  "constraints": [
                                    {
                                      "edctype": "AtomicConstraint",
                                      "leftExpression": {
                                        "edctype": "dataspaceconnector:literalexpression",
                                        "value": "https://w3id.org/catenax/policy/FrameworkAgreement"
                                      },
                                      "rightExpression": {
                                        "edctype": "dataspaceconnector:literalexpression",
                                        "value": "traceability:1.0"
                                      },
                                      "operator": "EQ"
                                    },
                                    {
                                      "edctype": "AtomicConstraint",
                                      "leftExpression": {
                                        "edctype": "dataspaceconnector:literalexpression",
                                        "value": "https://w3id.org/catenax/policy/UsagePurpose"
                                      },
                                      "rightExpression": {
                                        "edctype": "dataspaceconnector:literalexpression",
                                        "value": "cx.core.industrycore:1"
                                      },
                                      "operator": "EQ"
                                    }
                                  ]
                                }
                              ],
                              "duties": []
                            }
                          ],
                          "prohibitions": [],
                          "obligations": [],
                          "extensibleProperties": {},
                          "inheritsFrom": null,
                          "assigner": "BPNL000000012345",
                          "assignee": null,
                          "target": "urn:uuid:b08f54e0-92ca-4ca5-9115-7531c3a47bbc",
                          "@type": {
                            "@policytype": "offer"
                          }
                        },
                        "assetId": "urn:uuid:b08f54e0-92ca-4ca5-9115-7531c3a47bbc"
                      }
                    ],
                    "protocol": "dataspace-protocol-http",
                    "contractAgreement": {
                      "id": "contractAgreementId",
                      "providerId": "BPNL000000012345",
                      "consumerId": "BPNL000000067890",
                      "contractSigningDate": 1732284827,
                      "assetId": "urn:uuid:b08f54e0-92ca-4ca5-9115-7531c3a47bbc",
                      "policy": {
                        "permissions": [
                          {
                            "edctype": "dataspaceconnector:permission",
                            "action": {
                              "type": "use",
                              "includedIn": null,
                              "constraint": null
                            },
                            "constraints": [
                              {
                                "edctype": "AndConstraint",
                                "constraints": [
                                  {
                                    "edctype": "AtomicConstraint",
                                    "leftExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "https://w3id.org/catenax/policy/FrameworkAgreement"
                                    },
                                    "rightExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "traceability:1.0"
                                    },
                                    "operator": "EQ"
                                  },
                                  {
                                    "edctype": "AtomicConstraint",
                                    "leftExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "https://w3id.org/catenax/policy/UsagePurpose"
                                    },
                                    "rightExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "cx.core.industrycore:1"
                                    },
                                    "operator": "EQ"
                                  }
                                ]
                              }
                            ],
                            "duties": []
                          }
                        ],
                        "prohibitions": [],
                        "obligations": [],
                        "extensibleProperties": {},
                        "inheritsFrom": null,
                        "assigner": "BPNL000000012345",
                        "assignee": "BPNL000000067890",
                        "target": "urn:uuid:b08f54e0-92ca-4ca5-9115-7531c3a47bbc",
                        "@type": {
                          "@policytype": "contract"
                        }
                      }
                    },
                    "lastContractOffer": {
                      "id": "ZmNiZTVmMDgtOTUzNi00OWM2LTgyYzMtODYwYzcxNGE0M2Ex:dXJuOnV1aWQ6YjA4ZjU0ZTAtOTJjYS00Y2E1LTkxMTUtNzUzMWMzYTQ3YmJj:OTcxNmVkYmQtZDc2OS00Nzc5LTgwZGItMTFkODRlNmEzYTJh",
                      "policy": {
                        "permissions": [
                          {
                            "edctype": "dataspaceconnector:permission",
                            "action": {
                              "type": "use",
                              "includedIn": null,
                              "constraint": null
                            },
                            "constraints": [
                              {
                                "edctype": "AndConstraint",
                                "constraints": [
                                  {
                                    "edctype": "AtomicConstraint",
                                    "leftExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "https://w3id.org/catenax/policy/FrameworkAgreement"
                                    },
                                    "rightExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "traceability:1.0"
                                    },
                                    "operator": "EQ"
                                  },
                                  {
                                    "edctype": "AtomicConstraint",
                                    "leftExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "https://w3id.org/catenax/policy/UsagePurpose"
                                    },
                                    "rightExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "cx.core.industrycore:1"
                                    },
                                    "operator": "EQ"
                                  }
                                ]
                              }
                            ],
                            "duties": []
                          }
                        ],
                        "prohibitions": [],
                        "obligations": [],
                        "extensibleProperties": {},
                        "inheritsFrom": null,
                        "assigner": "BPNL000000012345",
                        "assignee": null,
                        "target": "urn:uuid:b08f54e0-92ca-4ca5-9115-7531c3a47bbc",
                        "@type": {
                          "@policytype": "offer"
                        }
                      },
                      "assetId": "urn:uuid:b08f54e0-92ca-4ca5-9115-7531c3a47bbc"
                    }
                  },
                  "type": "ContractNegotiationFinalized"
                }
                """;
        final Optional<String> expectedContractAgreementId = Optional.of("contractAgreementId");

        // act
        testee.receiveNegotiationsCallback(callbackNegotiation);

        // assert
        final var result = contractNegotiationIdStorage.get("negotiationId");
        assertThat(result).isEqualTo(expectedContractAgreementId);
    }
}