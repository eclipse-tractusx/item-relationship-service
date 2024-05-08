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

import java.io.StringReader;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.eclipse.edc.core.transform.TransformerContextImpl;
import org.eclipse.edc.core.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.tractusx.irs.edc.client.policy.Constraint;
import org.eclipse.tractusx.irs.edc.client.policy.OperatorType;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonObjectToIrsPolicyTransformerTest {

    public static final String EXAMPLE_PAYLOAD = """
            {
                "@context": {
                    "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
                    "edc": "https://w3id.org/edc/v0.0.1/ns/",
                    "tx": "https://w3id.org/tractusx/v0.0.1/ns/",
                    "tx-auth": "https://w3id.org/tractusx/auth/",
                    "cx-policy": "https://w3id.org/catenax/policy/",
                    "odrl": "http://www.w3.org/ns/odrl/2/"
                },
               "@id": "e917f5f-8dac-49ac-8d10-5b4d254d2b48",
                "policy": {
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

    private JsonObjectToIrsPolicyTransformer jsonObjectToIrsPolicyTransformer;
    private TransformerContext transformerContext;

    @BeforeEach
    public void setUp() {
        jsonObjectToIrsPolicyTransformer = new JsonObjectToIrsPolicyTransformer(new ObjectMapper());
        transformerContext = new TransformerContextImpl(new TypeTransformerRegistryImpl());
    }

    @Test
    void shouldTransformJsonObjectToPolicyCorrectly() {
        // given
        JsonReader jsonReader = Json.createReader(new StringReader(EXAMPLE_PAYLOAD));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        // when
        final Policy transformed = jsonObjectToIrsPolicyTransformer.transform(jsonObject, transformerContext);

        // then
        assertThat(transformed.getPolicyId()).isEqualTo("e917f5f-8dac-49ac-8d10-5b4d254d2b48");
        final Permission permission = transformed.getPermissions().get(0);
        assertThat(permission.getAction()).isEqualTo(PolicyType.USE);
        final List<Constraint> and = permission.getConstraint().getAnd();
        final Constraint and1 = and.get(0);
        final Constraint and2 = and.get(1);

        assertThat(and1.getLeftOperand()).isEqualTo("Membership");
        assertThat(and1.getRightOperand()).isEqualTo("active");
        assertThat(and1.getOperator().getOperatorType()).isEqualTo(OperatorType.EQ);

        assertThat(and2.getLeftOperand()).isEqualTo("PURPOSE");
        assertThat(and2.getRightOperand()).isEqualTo("ID 3.1 Trace");
        assertThat(and2.getOperator().getOperatorType()).isEqualTo(OperatorType.EQ);
    }
}