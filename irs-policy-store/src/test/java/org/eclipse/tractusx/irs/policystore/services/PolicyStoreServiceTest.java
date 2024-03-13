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
package org.eclipse.tractusx.irs.policystore.services;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.irs.edc.client.policy.Constraint;
import org.eclipse.tractusx.irs.edc.client.policy.Constraints;
import org.eclipse.tractusx.irs.edc.client.policy.Operator;
import org.eclipse.tractusx.irs.edc.client.policy.OperatorType;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyType;
import org.eclipse.tractusx.irs.policystore.config.DefaultAcceptedPoliciesConfig;
import org.eclipse.tractusx.irs.policystore.exceptions.PolicyStoreException;
import org.eclipse.tractusx.irs.policystore.models.UpdatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.persistence.PolicyPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PolicyStoreServiceTest {

    private static final String BPN = "testBpn";
    private static final String EXAMPLE_ALLOWED_NAME = "ID 3.0 Trace";
    private static final String EXAMPLE_ACCEPTED_LEFT_OPERAND = "PURPOSE";
    private final Clock clock = Clock.systemUTC();
    @Captor
    ArgumentCaptor<Policy> policyCaptor;
    private PolicyStoreService testee;
    @Mock
    private PolicyPersistence persistence;

    @BeforeEach
    void setUp() {
        final DefaultAcceptedPoliciesConfig defaultAcceptedPoliciesConfig = new DefaultAcceptedPoliciesConfig();
        defaultAcceptedPoliciesConfig.setAcceptedPolicies(List.of());
        testee = new PolicyStoreService(defaultAcceptedPoliciesConfig, persistence, clock);
    }

    @Test
    void registerPolicy() {
        // arrange
        final OffsetDateTime now = OffsetDateTime.now(clock);
        final Policy policy = new Policy("testId", now, now.plusMinutes(1), emptyList());

        // act
        testee.registerPolicy(policy, List.of(BPN));

        // assert
        verify(persistence).save(eq(List.of(BPN)), any());
    }

    @Test
    void registerPolicyWithPermission() {
        // arrange
        final OffsetDateTime now = OffsetDateTime.now(clock);
        final Policy policy = new Policy("testId", now, now.plusMinutes(1), createPermissions());

        // act
        testee.registerPolicy(policy, List.of(BPN));

        // assert
        verify(persistence).save(eq(List.of(BPN)), policyCaptor.capture());

        assertThat(policyCaptor.getValue()).isNotNull();
        List<Permission> permissionList = policyCaptor.getValue().getPermissions();
        assertThat(permissionList).hasSize(2);
        assertThat(permissionList.get(0)).usingRecursiveComparison().isEqualTo(createPermissions().get(0));
        assertThat(permissionList.get(1)).usingRecursiveComparison().isEqualTo(createPermissions().get(1));
    }

    @Test
    void registerPolicyShouldThrowResponseStatusException() {
        // act
        final String policyId = "testId";
        final OffsetDateTime now = OffsetDateTime.now(clock);
        final Policy policy = new Policy(policyId, now, now.plusMinutes(1), createPermissions());
        doThrow(new PolicyStoreException("")).when(persistence).save(any(), any());

        // assert
        assertThrows(ResponseStatusException.class, () -> testee.registerPolicy(policy,
                List.of(BPN)));
    }

    @Test
    void getStoredPolicies() {
        // arrange
        final List<Policy> policies = List.of(createPolicy("test1"), createPolicy("test2"), createPolicy("test3"));
        when(persistence.readAll(BPN)).thenReturn(policies);

        // act
        final var storedPolicies = testee.getStoredPolicies(List.of(BPN));

        // assert
        assertThat(storedPolicies).hasSize(3);
    }

    @Test
    void getDefaultStoredPoliciesWhenEmpty() {
        // arrange
        final DefaultAcceptedPoliciesConfig.AcceptedPolicy acceptedPolicy1 = new DefaultAcceptedPoliciesConfig.AcceptedPolicy(
                EXAMPLE_ACCEPTED_LEFT_OPERAND, "eq", EXAMPLE_ALLOWED_NAME);
        final DefaultAcceptedPoliciesConfig.AcceptedPolicy acceptedPolicy2 = new DefaultAcceptedPoliciesConfig.AcceptedPolicy(
                EXAMPLE_ACCEPTED_LEFT_OPERAND, "eq", EXAMPLE_ALLOWED_NAME);
        final DefaultAcceptedPoliciesConfig defaultAcceptedPoliciesConfig = new DefaultAcceptedPoliciesConfig();
        defaultAcceptedPoliciesConfig.setAcceptedPolicies(List.of(acceptedPolicy1, acceptedPolicy2));
        testee = new PolicyStoreService(defaultAcceptedPoliciesConfig, persistence, clock);

        // act
        final var defaultPolicies = testee.getStoredPolicies(List.of(BPN));

        // assert
        assertThat(defaultPolicies).hasSize(1);
        final List<Permission> permissionList = defaultPolicies.get(0).getPermissions();
        assertThat(permissionList).hasSize(1);
        final Constraints constraints = permissionList.get(0).getConstraint();
        assertThat(constraints.getOr()).hasSize(2);
        assertThat(constraints.getAnd()).hasSize(2);
    }

    private Policy createPolicy(final String policyId) {
        return new Policy(policyId, OffsetDateTime.now(clock), OffsetDateTime.now(clock).plusDays(1), emptyList());
    }

    private List<Permission> createPermissions() {
        return List.of(new Permission(PolicyType.USE, createConstraints()),
                new Permission(PolicyType.ACCESS, createConstraints()));
    }

    private Constraints createConstraints() {
        return new Constraints(Collections.emptyList(),
                List.of(new Constraint("Membership", new Operator(OperatorType.EQ), "active"),
                        new Constraint("FrameworkAgreement.traceability", new Operator(OperatorType.EQ), "active"),
                        new Constraint(EXAMPLE_ACCEPTED_LEFT_OPERAND, new Operator(OperatorType.EQ), "ID 3.1 Trace")));
    }

    @Test
    void deletePolicy() {
        // arrange
        when(persistence.readAll()).thenReturn(Map.of(BPN, List.of(new Policy("testId", null, null, null))));

        // act
        testee.deletePolicy("testId");

        // assert
        verify(persistence).delete(BPN, "testId");
    }

    @Test
    void shouldThrowException() {

    }

    @Test
    void deletePolicyShouldThrowResponseStatusException() {
        // act
        final String policyId = "testId";
        when(persistence.readAll()).thenReturn(Map.of(BPN, List.of(new Policy("testId", null, null, null))));
        doThrow(new PolicyStoreException("")).when(persistence).delete(BPN, policyId);

        // assert
        assertThrows(ResponseStatusException.class, () -> testee.deletePolicy(policyId));
    }

    @Test
    void whenRegisterPolicyWithMissingPermissionsShouldThrowException() {
        // arrange
        final Policy policy = new Policy();

        // act
        // assert
        assertThrows(ResponseStatusException.class, () -> testee.registerPolicy(policy,
                null));
    }

    @Test
    void whenRegisterPolicyWithMissingConstraintShouldThrowException() {
        // arrange
        final Policy policy = Policy.builder()
                                    .permissions(List.of(
                                            Permission.builder().constraint(new Constraints(emptyList(), emptyList())).build(),
                                            Permission.builder().build()
                                            ))
                                    .build();

        // act
        // assert
        assertThrows(ResponseStatusException.class, () -> testee.registerPolicy(policy,
                null));
    }

    @Test
    void whenUpdate() {
        // arrange
        final OffsetDateTime now = OffsetDateTime.now(clock);
        when(persistence.readAll()).thenReturn(Map.of("bpn2", List.of(new Policy("testId", now, now.plusMinutes(1), emptyList()))));
        when(persistence.readAll(any())).thenReturn(List.of(new Policy("testId", now, now.plusMinutes(1), emptyList())));

        // act
        testee.updatePolicy("testId", new UpdatePolicyRequest(OffsetDateTime.now(), List.of("bpn1"), List.of("policyId")));

        // assert
        verify(persistence).delete("bpn2", "testId");
        verify(persistence).save(eq(List.of("bpn1")), any());
    }
}