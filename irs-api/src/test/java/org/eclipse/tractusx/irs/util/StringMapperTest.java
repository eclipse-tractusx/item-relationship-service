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
package org.eclipse.tractusx.irs.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.tractusx.irs.component.Bpn;
import org.eclipse.tractusx.irs.component.Description;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.policy.Constraint;
import org.eclipse.tractusx.irs.edc.client.policy.Constraints;
import org.eclipse.tractusx.irs.edc.client.policy.OperatorType;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyType;
import org.junit.jupiter.api.Test;

class StringMapperTest {

    @Test
    void mapToString() {
        final var serialized = StringMapper.mapToString(Bpn.withManufacturerId("test"));

        assertThat(serialized).isEqualTo("{\"manufacturerId\":\"test\",\"manufacturerName\":null}");
    }

    @Test
    void shouldThrowParseExceptionWhenMappingToString() {
        final var object = new Object();
        assertThatThrownBy(() -> StringMapper.mapToString(object)).isInstanceOf(JsonParseException.class);
    }

    @Test
    void mapFromString() {
        final var bpn = StringMapper.mapFromString("{\"manufacturerId\":\"test\",\"manufacturerName\":null}",
                Bpn.class);

        assertThat(bpn.getManufacturerId()).isEqualTo("test");
    }

    @Test
    void shouldThrowParseExceptionWhenMappingFromString() {
        assertThatThrownBy(() -> StringMapper.mapFromString("test", Description.class)).isInstanceOf(
                JsonParseException.class);
    }

    @Test
    void shouldMapFromStringUsingTypeReference() {

        // ARRANGE
        final TypeReference<List<Policy>> listOfPoliciesType = new TypeReference<>() {
        };

        final String originalJson = """
                [{
                    "policyId": "default-trace-policy",
                    "createdOn": "2024-07-17T16:15:14.12345678Z",
                    "validUntil": "9999-01-01T00:00:00.00000000Z",
                    "permissions": [
                        {
                            "action": "use",
                            "constraint": {
                                "and": [
                                    {
                                        "leftOperand": "https://w3id.org/catenax/policy/FrameworkAgreement",
                                        "operator": {
                                            "@id": "eq"
                                        },
                                        "rightOperand": "traceability:1.0"
                                    },
                                    {
                                        "leftOperand": "https://w3id.org/catenax/policy/UsagePurpose",
                                        "operator": {
                                            "@id": "eq"
                                        },
                                        "rightOperand": "cx.core.industrycore:1"
                                    }
                                ]
                            }
                        }
                    ]
                }]
                """;

        // ACT
        // convert back andConstraints forth to facilitate comparison
        final List<Policy> listOfPolicies = StringMapper.mapFromString(originalJson, listOfPoliciesType);
        final String backToString = StringMapper.mapToString(listOfPolicies);
        final List<Policy> backToObj = StringMapper.mapFromString(backToString, listOfPoliciesType);

        // ASSERT
        {
            assertThat(listOfPolicies).hasSize(1);
            assertThat(backToObj).hasSize(1);
            assertThat(backToObj).usingRecursiveComparison().isEqualTo(listOfPolicies);

            final Policy policy = listOfPolicies.get(0);
            assertThat(policy.getPolicyId()).isEqualTo("default-trace-policy");
            assertThat(policy.getValidUntil()).isEqualTo(OffsetDateTime.parse("9999-01-01T00:00:00.00000000Z"));
            assertThat(policy.getCreatedOn()).isEqualTo(OffsetDateTime.parse("2024-07-17T16:15:14.12345678Z"));
            assertThat(policy.getPermissions()).hasSize(1);

            final Permission permission = policy.getPermissions().get(0);
            assertThat(permission.getAction()).isEqualTo(PolicyType.USE);

            final Constraints constraints = permission.getConstraint();
            final List<Constraint> andConstraints = constraints.getAnd();
            assertThat(andConstraints).hasSize(2);
            {
                final Constraint constraint = andConstraints.get(0);
                assertThat(constraint.getLeftOperand()).isEqualTo("https://w3id.org/catenax/policy/FrameworkAgreement");
                assertThat(constraint.getOperator().getOperatorType()).isEqualTo(OperatorType.EQ);
                assertThat(constraint.getRightOperand()).isEqualTo("traceability:1.0");
            }
            {
                final Constraint constraint = andConstraints.get(1);
                assertThat(constraint.getLeftOperand()).isEqualTo("https://w3id.org/catenax/policy/UsagePurpose");
                assertThat(constraint.getOperator().getOperatorType()).isEqualTo(OperatorType.EQ);
                assertThat(constraint.getRightOperand()).isEqualTo("cx.core.industrycore:1");
            }
        }
    }

}