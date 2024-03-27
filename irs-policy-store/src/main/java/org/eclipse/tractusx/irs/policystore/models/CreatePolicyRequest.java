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
package org.eclipse.tractusx.irs.policystore.models;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.json.JsonObject;
import jakarta.validation.constraints.NotNull;

/**
 * Object for API to create policy
 */
@SuppressWarnings("FileTabCharacter")
@Schema(description = "Request to add a policy")
public record CreatePolicyRequest(

        @NotNull //
        @Schema(description = "Timestamp after which the policy will no longer be accepted in negotiations.",
                example = "2025-12-12T23:59:59.999Z") //
        OffsetDateTime validUntil, //

        @Schema(description = """
                The business partner number (BPN) for which the policy should be registered.
                This parameter is optional.
                If not set the policy is registered for each existing BPN.
                """, example = "BPNL00000123ABCD") //
        String businessPartnerNumber,

        @NotNull //
        @Schema(description = "The policy payload.", example = CreatePolicyRequest.EXAMPLE_PAYLOAD) //
        JsonObject payload) {

    @SuppressWarnings("java:S2479")
    // this value is used by open-api to show example policies
    // \u0009 character is required for this value to be correctly shown in open-api
    public static final String EXAMPLE_PAYLOAD = """
            {
                "@context": {
                    "odrl": "http://www.w3.org/ns/odrl/2/"
                },
                "@id": "policy-id",
                "@type": "PolicyDefinitionRequestDto",
                "policy": {
                    "@type": "Policy",
                    "odrl:permission": [
                        {
                            "odrl:action": "USE",
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
            """;
}
