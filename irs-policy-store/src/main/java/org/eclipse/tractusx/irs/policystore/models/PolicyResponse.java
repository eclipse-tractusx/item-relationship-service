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
package org.eclipse.tractusx.irs.policystore.models;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;

/**
 * Policy representation for policies response
 */
@Builder
@Schema(example = PolicyResponse.EXAMPLE_PAYLOAD)
public record PolicyResponse(OffsetDateTime validUntil, Payload payload,
                             @JsonInclude(JsonInclude.Include.NON_NULL) String bpn) {

    public static final String BPN_TO_POLICY_MAP_EXAMPLE = """
              {
                  "BPNL1234567890AB": [
                      {
                          "validUntil": "2025-12-12T23:59:59.999Z",
                          "payload": {
                              "@context": {
                                  "odrl": "http://www.w3.org/ns/odrl/2/"
                              },
                              "@id": "e917f5f-8dac-49ac-8d10-5b4d254d2b48",
                              "policy": {
                                  "policyId": "e917f5f-8dac-49ac-8d10-5b4d254d2b48",
                                  "createdOn": "2024-03-28T03:34:42.9454448Z",
                                  "validUntil": "2025-12-12T23:59:59.999Z",
                                  "permissions": [
                                      {
                                          "action": "use",
                                          "constraint": {
                                              "and": [
                                                  {
                                                      "leftOperand": "Membership",
                                                      "operator": {
                                                          "@id": "eq"
                                                      },
                                                      "rightOperand": "active"
                                                  },
                                                  {
                                                      "leftOperand": "PURPOSE",
                                                      "operator": {
                                                          "@id": "eq"
                                                      },
                                                      "rightOperand": "ID 3.1 Trace"
                                                  }
                                              ],
                                              "or": null
                                          }
                                      }
                                  ]
                              }
                          }
                      }
                  ],
                  "BPNA1234567890DF": []
              }
            """;

    @SuppressWarnings({ "FileTabCharacter",
                        "java:S2479"
    })
    // required to show example of policies correctly in open-api
    public static final String EXAMPLE_PAYLOAD = """
            [
               {
                	"validUntil": "2025-12-12T23:59:59.999Z",
                	"payload": {
                		"@context": {
                			"odrl": "http://www.w3.org/ns/odrl/2/"
                		},
                		"@id": "policy-id1",
                		"policy": {
                			"odrl:permission": [
                				{
                					"odrl:action": "use",
                					"odrl:constraint": {
                						"odrl:and": [
                							{
                								"odrl:leftOperand": "Membership",
                								"odrl:operator": {
                									"@id": "odrl:eq"
                								},
                								"odrl:rightOperand": "active"
                							},
                							{
                								"odrl:leftOperand": "PURPOSE",
                								"odrl:operator": {
                									"@id": "odrl:eq"
                								},
                								"odrl:rightOperand": "ID 3.1 Trace"
                							}
                						]
                					}
                				}
                			]
                		}
                	}
                },
                {
                	"validUntil": "2025-12-31T23:59:59.999Z",
                	"payload": {
                		"@context": {
                			"odrl": "http://www.w3.org/ns/odrl/2/"
                		},
                		"@id": "policy-id2",
                		"policy": {
                			...
                		}
                	}
                }
             ]
            """;

    public static PolicyResponse fromPolicy(final Policy policy) {
        return PolicyResponse.builder()
                             .validUntil(policy.getValidUntil())
                             .payload(Payload.builder()
                                             .policyId(policy.getPolicyId())
                                             .context(Context.getDefault())
                                             .policy(policy)
                                             .build())
                             .build();
    }

    public static PolicyResponse fromPolicyWithBpn(final PolicyWithBpn policyWithBpn) {
        return PolicyResponse.builder()
                             .validUntil(policyWithBpn.policy().getValidUntil())
                             .payload(Payload.builder()
                                             .policyId(policyWithBpn.policy().getPolicyId())
                                             .context(Context.getDefault())
                                             .policy(policyWithBpn.policy())
                                             .build())
                             .bpn(policyWithBpn.bpn())
                             .build();
    }

}
