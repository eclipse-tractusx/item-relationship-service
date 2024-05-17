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
import static org.eclipse.tractusx.irs.edc.client.policy.ConstraintConstants.ACTIVE_MEMBERSHIP;
import static org.eclipse.tractusx.irs.edc.client.policy.ConstraintConstants.FRAMEWORK_AGREEMENT_TRACEABILITY_ACTIVE;
import static org.eclipse.tractusx.irs.edc.client.policy.ConstraintConstants.PURPOSE_ID_3_1_TRACE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.tractusx.irs.edc.client.policy.Constraints;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyType;
import org.eclipse.tractusx.irs.policystore.models.CreatePoliciesResponse;
import org.eclipse.tractusx.irs.policystore.models.CreatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.models.PolicyResponse;
import org.eclipse.tractusx.irs.policystore.models.UpdatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.services.PolicyStoreService;
import org.eclipse.tractusx.irs.policystore.testutil.PolicyStoreTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PolicyStoreControllerTest {

    public static final String REGISTER_POLICY_EXAMPLE_PAYLOAD = """
            {
                "@context": {
                    "odrl": "http://www.w3.org/ns/odrl/2/"
                },
                "@id": "e917f5f-8dac-49ac-8d10-5b4d254d2b48",
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
            """;

    private PolicyStoreController testee;

    @Mock
    private PolicyStoreService policyStoreServiceMock;

    @BeforeEach
    void setUp() {
        testee = new PolicyStoreController(policyStoreServiceMock, mock(HttpServletRequest.class));
    }

    @Nested
    class RegisterPolicyTests {

        @Test
        void registerAllowedPolicy() {
            // arrange
            final OffsetDateTime now = OffsetDateTime.now();
            final JsonObject jsonObject = PolicyStoreTestUtil.toJsonObject(REGISTER_POLICY_EXAMPLE_PAYLOAD);

            // act
            final CreatePolicyRequest request = new CreatePolicyRequest(now.plusMinutes(1), null,
                    jsonObject.get("policy").asJsonObject());
            when(policyStoreServiceMock.registerPolicy(request)).thenReturn(Policy.builder().policyId("dummy").build());
            final CreatePoliciesResponse createPoliciesResponse = testee.registerAllowedPolicy(request);

            // assert
            verify(policyStoreServiceMock).registerPolicy(request);
            assertThat(createPoliciesResponse.policyId()).isEqualTo("dummy");
        }
    }

    @Nested
    class GetPolicyTests {

        @Test
        void getPolicies() {
            // arrange
            final String policyId = randomPolicyId();
            final List<Policy> policies = List.of(Policy.builder()
                                                        .policyId(policyId)
                                                        .createdOn(OffsetDateTime.now())
                                                        .validUntil(OffsetDateTime.now())
                                                        .permissions(createPermissions())
                                                        .build());
            final String bpn = "bpn1";
            when(policyStoreServiceMock.getPolicies(List.of(bpn))).thenReturn(Map.of(bpn, policies));

            // act
            final Map<String, List<PolicyResponse>> returnedPolicies = testee.getPolicies(List.of(bpn));

            // assert
            assertThat(returnedPolicies.get(bpn)).isEqualTo(
                    policies.stream().map(PolicyResponse::fromPolicy).collect(Collectors.toList()));
        }

        @Test
        void getAllPolicies() {
            // arrange
            final String policyId = randomPolicyId();
            final List<Policy> policies = List.of(Policy.builder()
                                                        .policyId(policyId)
                                                        .createdOn(OffsetDateTime.now())
                                                        .validUntil(OffsetDateTime.now())
                                                        .permissions(createPermissions())
                                                        .build());
            final String bpn = "bpn1";
            when(policyStoreServiceMock.getPolicies(null)).thenReturn(Map.of(bpn, policies));

            // act
            final Map<String, List<PolicyResponse>> returnedPolicies = testee.getPolicies(null);

            // assert
            assertThat(returnedPolicies).containsEntry(bpn,
                    policies.stream().map(PolicyResponse::fromPolicy).collect(Collectors.toList()));
        }
    }

    @Nested
    class DeletePolicyTests {

        @Test
        void deleteAllowedPolicy() {
            // act
            testee.deleteAllowedPolicy("testId");

            // assert
            verify(policyStoreServiceMock).deletePolicy("testId");
        }
    }

    @Nested
    class UpdatePolicyTests {

        @Test
        void updateAllowedPolicy() {
            // arrange
            final String policyId = "policyId";
            final UpdatePolicyRequest request = new UpdatePolicyRequest(OffsetDateTime.now(), List.of("bpn"),
                    List.of(policyId));

            // act
            testee.updateAllowedPolicy(request);

            // assert
            verify(policyStoreServiceMock).updatePolicies(request);
        }

        /*
         There are no null and empty tests for the list of business partner numbers and the list of policy IDs
         because this is done via jakarta validation.
        */

    }

    private List<Permission> createPermissions() {
        return List.of(new Permission(PolicyType.USE, createConstraints()),
                new Permission(PolicyType.ACCESS, createConstraints()));
    }

    private Constraints createConstraints() {
        return new Constraints(Collections.emptyList(),
                List.of(ACTIVE_MEMBERSHIP, FRAMEWORK_AGREEMENT_TRACEABILITY_ACTIVE, PURPOSE_ID_3_1_TRACE));
    }

    private static String randomPolicyId() {
        return UUID.randomUUID().toString();
    }

}