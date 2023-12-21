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
package org.eclipse.tractusx.irs.edc.client.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createAndConstraintPolicy;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createAtomicConstraint;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createAtomicConstraintPolicy;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createOrConstraintPolicy;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createXOneConstraintPolicy;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import org.eclipse.edc.policy.model.Policy;
import org.eclipse.tractusx.irs.edc.client.testutil.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyCheckerServiceTest {

    private PolicyCheckerService policyCheckerService;
    @Mock
    private AcceptedPoliciesProvider policyStore;

    @BeforeEach
    void setUp() {
        final var policyList = List.of(
                new AcceptedPolicy(policy(TestConstants.ID_3_0_TRACE), OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy(policy(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY),
                        OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        policyCheckerService = new PolicyCheckerService(policyStore, new ConstraintCheckerService());
    }

    @Test
    void shouldRejectWrongPolicy() {
        // given
        final String unknownRightExpression = "Wrong_Trace";
        Policy policy = createAtomicConstraintPolicy(TestConstants.PURPOSE, unknownRightExpression);
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectWhenPolicyStoreIsEmpty() {
        // given
        Policy policy = createAtomicConstraintPolicy(TestConstants.PURPOSE, TestConstants.ID_3_0_TRACE);
        when(policyStore.getAcceptedPolicies()).thenReturn(List.of());
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldConfirmValidPolicyWhenWildcardIsSet() {
        // given
        final String wildcardPolicyId = "*";
        final var policyList = List.of(
                new AcceptedPolicy(policy(TestConstants.ID_3_0_TRACE), OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy(policy(wildcardPolicyId), OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createAtomicConstraintPolicy(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                TestConstants.STATUS_ACTIVE);
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectWhenWildcardIsPartOfPolicy() {
        // given
        final String invalidWildcardPolicy = "Policy*";
        final var policyList = List.of(
                new AcceptedPolicy(policy(invalidWildcardPolicy), OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createAtomicConstraintPolicy(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                TestConstants.STATUS_ACTIVE);
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectAndConstraintsWhenOnlyOneMatch() {
        // given
        final var policyList = List.of(new AcceptedPolicy(policy(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY),
                        OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy(policy(TestConstants.FRAMEWORK_AGREEMENT_DISMANTLER),
                        OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createAndConstraintPolicy(
                List.of(createAtomicConstraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                                TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE)));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldAcceptAndConstraintsWhenAcceptedPolicyContainsMoreConstraintsSuperSetOfProvidedPolicy() {
        // given
        final Constraint constraint1 = new Constraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY, OperatorType.EQ,
                List.of(TestConstants.STATUS_ACTIVE));
        final Constraint constraint2 = new Constraint(TestConstants.MEMBERSHIP, OperatorType.EQ,
                List.of(TestConstants.STATUS_ACTIVE));
        final Constraint constraint3 = new Constraint(TestConstants.FRAMEWORK_AGREEMENT_DISMANTLER, OperatorType.EQ,
                List.of(TestConstants.STATUS_ACTIVE));
        final var policyList = List.of(
                new AcceptedPolicy(policy("and-policy", List.of(constraint1, constraint2, constraint3), List.of()),
                        OffsetDateTime.now().plusYears(1)));

        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createAndConstraintPolicy(
                List.of(createAtomicConstraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                                TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE)));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAcceptOrConstraintsWhenAcceptedPolicyContainsMoreConstraintsSuperSetOfProvidedPolicy() {
        // given
        final Constraint constraint1 = new Constraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY, OperatorType.EQ,
                List.of(TestConstants.STATUS_ACTIVE));
        final Constraint constraint2 = new Constraint(TestConstants.MEMBERSHIP, OperatorType.EQ,
                List.of(TestConstants.STATUS_ACTIVE));
        final Constraint constraint3 = new Constraint(TestConstants.FRAMEWORK_AGREEMENT_DISMANTLER, OperatorType.EQ,
                List.of(TestConstants.STATUS_ACTIVE));
        final var policyList = List.of(
                new AcceptedPolicy(policy("and-policy", List.of(), List.of(constraint1, constraint2, constraint3)),
                        OffsetDateTime.now().plusYears(1)));

        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createOrConstraintPolicy(
                List.of(createAtomicConstraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                                TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE)));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAcceptConstraintsWithDefaultPolicy() {
        // given
        final Constraint constraint1 = new Constraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY, OperatorType.EQ,
                List.of(TestConstants.STATUS_ACTIVE));
        final Constraint constraint2 = new Constraint(TestConstants.MEMBERSHIP, OperatorType.EQ,
                List.of(TestConstants.STATUS_ACTIVE));
        final Constraint constraint3 = new Constraint(TestConstants.FRAMEWORK_AGREEMENT_DISMANTLER, OperatorType.EQ,
                List.of(TestConstants.STATUS_ACTIVE));

        final var policyList = List.of(new AcceptedPolicy(
                policy("default-policy", List.of(constraint1, constraint2, constraint3),
                        List.of(constraint1, constraint2, constraint3)), OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);

        Policy policy = createOrConstraintPolicy(
                List.of(createAtomicConstraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                                TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE)));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectOrConstraintsWhenNoneMatch() {
        // given
        final var policyList = List.of(
                new AcceptedPolicy(policy(TestConstants.FRAMEWORK_AGREEMENT_TEST), OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy(policy(TestConstants.FRAMEWORK_AGREEMENT_DISMANTLER),
                        OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createAndConstraintPolicy(
                List.of(createAtomicConstraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                                TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE)));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectXOneConstraintsWhenNoneMatch() {
        // given
        final var policyList = List.of(
                new AcceptedPolicy(policy(TestConstants.FRAMEWORK_AGREEMENT_TEST), OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy(policy(TestConstants.FRAMEWORK_AGREEMENT_DISMANTLER),
                        OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createXOneConstraintPolicy(
                List.of(createAtomicConstraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                                TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE)));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectXOneConstraintsWhenMoreThanOneMatch() {
        // given
        final var policyList = List.of(new AcceptedPolicy(policy(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY),
                        OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy(policy(TestConstants.MEMBERSHIP), OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createXOneConstraintPolicy(
                List.of(createAtomicConstraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                                TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE)));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    private org.eclipse.tractusx.irs.edc.client.policy.Policy policy(final String policyId) {
        return new org.eclipse.tractusx.irs.edc.client.policy.Policy(policyId, OffsetDateTime.now(),
                OffsetDateTime.now().plusYears(1), Collections.emptyList());
    }

    private org.eclipse.tractusx.irs.edc.client.policy.Policy policy(final String policyId,
            final List<Constraint> andConstraint, final List<Constraint> orConstraint) {
        final List<Constraints> constraints = List.of(new Constraints(andConstraint, orConstraint));
        final List<Permission> permissions = List.of(new Permission(PolicyType.USE, constraints));
        return new org.eclipse.tractusx.irs.edc.client.policy.Policy(policyId, OffsetDateTime.now(),
                OffsetDateTime.now().plusYears(1), permissions);
    }

}