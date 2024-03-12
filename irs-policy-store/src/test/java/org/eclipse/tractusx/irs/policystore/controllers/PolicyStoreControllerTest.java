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
package org.eclipse.tractusx.irs.policystore.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.eclipse.edc.core.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.tractusx.irs.edc.client.policy.Constraint;
import org.eclipse.tractusx.irs.edc.client.policy.Constraints;
import org.eclipse.tractusx.irs.edc.client.policy.Operator;
import org.eclipse.tractusx.irs.edc.client.policy.OperatorType;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyType;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcTransformer;
import org.eclipse.tractusx.irs.policystore.models.CreatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.models.PolicyResponse;
import org.eclipse.tractusx.irs.policystore.models.UpdatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.services.PolicyStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyStoreControllerTest {

    public static final String EXAMPLE_PAYLOAD = """
            {
             	"validUntil": "2025-12-12T23:59:59.999Z",
             	"payload": {
             		"@context": {
             			"odrl": "http://www.w3.org/ns/odrl/2/"
             		},
             		"@id": "policy-id",
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
             }
            """;

    private PolicyStoreController testee;
    private final TitaniumJsonLd titaniumJsonLd = new TitaniumJsonLd(new ConsoleMonitor());
    private final EdcTransformer edcTransformer = new EdcTransformer(new com.fasterxml.jackson.databind.ObjectMapper(),
            titaniumJsonLd, new TypeTransformerRegistryImpl());

    @Mock
    private PolicyStoreService service;

    @BeforeEach
    void setUp() {
        testee = new PolicyStoreController(service, edcTransformer);
    }

    @Test
    void registerAllowedPolicy() {
        // arrange
        final OffsetDateTime now = OffsetDateTime.now();
        JsonReader jsonReader = Json.createReader(new StringReader(EXAMPLE_PAYLOAD));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        // act
        testee.registerAllowedPolicy(
                new CreatePolicyRequest(now.plusMinutes(1), null, List.of(jsonObject.get("payload").asJsonObject())));

        // assert
        verify(service).registerPolicy(any(), eq(null));
    }

    @Test
    void getPolicies() {
        // arrange
        final List<Policy> policies = List.of(
                new Policy("testId", OffsetDateTime.now(), OffsetDateTime.now(), createPermissions()));
        when(service.getStoredPolicies(List.of("bpn1"))).thenReturn(policies);

        // act
        final List<PolicyResponse> returnedPolicies = testee.getPolicies(new String[]{"bpn1"});

        // assert
        assertThat(returnedPolicies).isEqualTo(
                policies.stream().map(PolicyResponse::fromPolicy).collect(Collectors.toList()));
    }

    @Test
    void getAllPolicies() {
        // arrange
        final List<Policy> policies = List.of(
                new Policy("testId", OffsetDateTime.now(), OffsetDateTime.now(), createPermissions()));
        when(service.getAllStoredPolicies()).thenReturn(Map.of("bpn", policies));

        // act
        final Map<String, List<PolicyResponse>> returnedPolicies = testee.getAllPolicies();

        // assert
        assertThat(returnedPolicies.get("bpn")).isEqualTo(
                policies.stream().map(PolicyResponse::fromPolicy).collect(Collectors.toList()));
    }

    @Test
    void deleteAllowedPolicy() {
        // act
        testee.deleteAllowedPolicy("testId");

        // assert
        verify(service).deletePolicy("testId");
    }

    @Test
    void updateAllowedPolicy() {
        // arrange
        final String policyId = "policyId";
        final UpdatePolicyRequest request = new UpdatePolicyRequest(OffsetDateTime.now(), List.of("bpn"),List.of(policyId));

        // act
        testee.updateAllowedPolicy(request);

        // assert
        verify(service).updatePolicy(policyId, request);
    }

    private List<Permission> createPermissions() {
        return List.of(new Permission(PolicyType.USE, createConstraints()),
                new Permission(PolicyType.ACCESS, createConstraints()));
    }

    private Constraints createConstraints() {
        return new Constraints(Collections.emptyList(),
                List.of(new Constraint("Membership", new Operator(OperatorType.EQ), "active"),
                        new Constraint("FrameworkAgreement.traceability", new Operator(OperatorType.EQ), "active"),
                        new Constraint("PURPOSE", new Operator(OperatorType.EQ), "ID 3.1 Trace")));
    }
}