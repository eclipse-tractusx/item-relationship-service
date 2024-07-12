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
package org.eclipse.tractusx.irs.edc.client.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestConstants.FRAMEWORK_AGREEMENT_DISMANTLER;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestConstants.FRAMEWORK_AGREEMENT_TEST;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestConstants.ID_3_1_TRACE;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestConstants.MEMBERSHIP;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestConstants.PURPOSE;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestConstants.STATUS_ACTIVE;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createAndConstraintPolicy;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createAtomicConstraint;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createAtomicConstraintPolicy;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createOrConstraintPolicy;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createXOneConstraintPolicy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import org.eclipse.edc.policy.model.Policy;
import org.eclipse.tractusx.irs.edc.client.testutil.CamelCaseToSpacesDisplayNameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(CamelCaseToSpacesDisplayNameGenerator.class)
@ExtendWith(MockitoExtension.class)
class PolicyCheckerServiceTest {

    private PolicyCheckerService policyCheckerService;

    @Mock
    private AcceptedPoliciesProvider policyStore;

    @BeforeEach
    void setUp() {
        policyCheckerService = new PolicyCheckerService(policyStore, new ConstraintCheckerService());
    }

    @Nested
    class IsValidTests {

        @Test
        void shouldRejectWrongPolicy() {
            // given
            final String unknownRightExpression = "Wrong_Trace";
            final Policy policy = createAtomicConstraintPolicy(PURPOSE, unknownRightExpression);

            // when
            final boolean valid = policyCheckerService.isValid(policy, "bpn");
            // then
            assertThat(valid).isFalse();
        }

        @Test
        void shouldRejectWhenPolicyStoreIsEmpty() {
            // given
            final Policy policy = createAtomicConstraintPolicy(PURPOSE, ID_3_1_TRACE);
            when(policyStore.getAcceptedPolicies(any())).thenReturn(List.of());
            // when
            final boolean valid = policyCheckerService.isValid(policy, "bpn");
            // then
            assertThat(valid).isFalse();
        }

        @Test
        void shouldRejectAndConstraintsWhenOnlyOneMatch() {
            // given
            final var policyList = List.of(
                    new AcceptedPolicy(policy(FRAMEWORK_AGREEMENT_TRACEABILITY), OffsetDateTime.now().plusYears(1)),
                    new AcceptedPolicy(policy(FRAMEWORK_AGREEMENT_DISMANTLER), OffsetDateTime.now().plusYears(1)));
            when(policyStore.getAcceptedPolicies(any())).thenReturn(policyList);

            final Policy policy = createAndConstraintPolicy(
                    List.of(createAtomicConstraint(FRAMEWORK_AGREEMENT_TRACEABILITY, STATUS_ACTIVE),
                            createAtomicConstraint(MEMBERSHIP, STATUS_ACTIVE)));
            // when
            final boolean valid = policyCheckerService.isValid(policy, "bpn");

            // then
            assertThat(valid).isFalse();
        }

        @Test
        void shouldAcceptAndConstraintsWhenAcceptedPolicyContainsMoreConstraintsSuperSetOfProvidedPolicy() {
            // given
            final Constraint constraint1 = new Constraint(FRAMEWORK_AGREEMENT_TRACEABILITY,
                    new Operator(OperatorType.EQ), STATUS_ACTIVE);
            final Constraint constraint2 = new Constraint(MEMBERSHIP, new Operator(OperatorType.EQ), STATUS_ACTIVE);
            final Constraint constraint3 = new Constraint(FRAMEWORK_AGREEMENT_DISMANTLER, new Operator(OperatorType.EQ),
                    STATUS_ACTIVE);
            final var policyList = List.of(
                    new AcceptedPolicy(policy("and-policy", List.of(constraint1, constraint2, constraint3), List.of()),
                            OffsetDateTime.now().plusYears(1)));
            when(policyStore.getAcceptedPolicies(any())).thenReturn(policyList);

            final Policy policy = createAndConstraintPolicy(
                    List.of(createAtomicConstraint(FRAMEWORK_AGREEMENT_TRACEABILITY, STATUS_ACTIVE),
                            createAtomicConstraint(MEMBERSHIP, STATUS_ACTIVE)));

            // when
            final boolean valid = policyCheckerService.isValid(policy, "bpn");

            // then
            assertThat(valid).isTrue();
        }

        @Test
        void shouldAcceptOrConstraintsWhenAcceptedPolicyContainsMoreConstraintsSuperSetOfProvidedPolicy() {
            // given
            final Constraint constraint1 = new Constraint(FRAMEWORK_AGREEMENT_TRACEABILITY,
                    new Operator(OperatorType.EQ), STATUS_ACTIVE);
            final Constraint constraint2 = new Constraint(MEMBERSHIP, new Operator(OperatorType.EQ), STATUS_ACTIVE);
            final Constraint constraint3 = new Constraint(FRAMEWORK_AGREEMENT_DISMANTLER, new Operator(OperatorType.EQ),
                    STATUS_ACTIVE);
            final var policyList = List.of(
                    new AcceptedPolicy(policy("and-policy", List.of(), List.of(constraint1, constraint2, constraint3)),
                            OffsetDateTime.now().plusYears(1)));
            when(policyStore.getAcceptedPolicies(any())).thenReturn(policyList);

            final Policy policy = createOrConstraintPolicy(
                    List.of(createAtomicConstraint(FRAMEWORK_AGREEMENT_TRACEABILITY, STATUS_ACTIVE),
                            createAtomicConstraint(MEMBERSHIP, STATUS_ACTIVE)));

            // when
            final boolean valid = policyCheckerService.isValid(policy, "bpn");

            // then
            assertThat(valid).isTrue();
        }

        @Test
        void shouldAcceptConstraintsWithDefaultPolicy() {
            // given
            final Constraint constraint1 = new Constraint(FRAMEWORK_AGREEMENT_TRACEABILITY,
                    new Operator(OperatorType.EQ), STATUS_ACTIVE);
            final Constraint constraint2 = new Constraint(MEMBERSHIP, new Operator(OperatorType.EQ), STATUS_ACTIVE);
            final Constraint constraint3 = new Constraint(FRAMEWORK_AGREEMENT_DISMANTLER, new Operator(OperatorType.EQ),
                    STATUS_ACTIVE);

            final var policyList = List.of(new AcceptedPolicy(
                    policy("default-policy", List.of(constraint1, constraint2, constraint3),
                            List.of(constraint1, constraint2, constraint3)), OffsetDateTime.now().plusYears(1)));
            when(policyStore.getAcceptedPolicies(any())).thenReturn(policyList);

            final Policy policy = createOrConstraintPolicy(
                    List.of(createAtomicConstraint(FRAMEWORK_AGREEMENT_TRACEABILITY, STATUS_ACTIVE),
                            createAtomicConstraint(MEMBERSHIP, STATUS_ACTIVE)));

            // when
            final boolean valid = policyCheckerService.isValid(policy, "bpn");

            // then
            assertThat(valid).isTrue();
        }

        @Test
        void shouldRejectOrConstraintsWhenNoneMatch() {
            // given
            final var policyList = List.of(
                    new AcceptedPolicy(policy(FRAMEWORK_AGREEMENT_TEST), OffsetDateTime.now().plusYears(1)),
                    new AcceptedPolicy(policy(FRAMEWORK_AGREEMENT_DISMANTLER), OffsetDateTime.now().plusYears(1)));
            when(policyStore.getAcceptedPolicies(any())).thenReturn(policyList);

            final Policy policy = createAndConstraintPolicy(
                    List.of(createAtomicConstraint(FRAMEWORK_AGREEMENT_TRACEABILITY, STATUS_ACTIVE),
                            createAtomicConstraint(MEMBERSHIP, STATUS_ACTIVE)));

            // when
            final boolean valid = policyCheckerService.isValid(policy, "bpn");

            // then
            assertThat(valid).isFalse();
        }

        @Test
        void shouldRejectXOneConstraintsWhenNoneMatch() {
            // given
            final var policyList = List.of(
                    new AcceptedPolicy(policy(FRAMEWORK_AGREEMENT_TEST), OffsetDateTime.now().plusYears(1)),
                    new AcceptedPolicy(policy(FRAMEWORK_AGREEMENT_DISMANTLER), OffsetDateTime.now().plusYears(1)));
            when(policyStore.getAcceptedPolicies(any())).thenReturn(policyList);

            final Policy policy = createXOneConstraintPolicy(
                    List.of(createAtomicConstraint(FRAMEWORK_AGREEMENT_TRACEABILITY, STATUS_ACTIVE),
                            createAtomicConstraint(MEMBERSHIP, STATUS_ACTIVE)));

            // when
            final boolean valid = policyCheckerService.isValid(policy, "bpn");

            // then
            assertThat(valid).isFalse();
        }

        @Test
        void shouldRejectXOneConstraintsWhenMoreThanOneMatch() {
            // given
            final var policyList = List.of(
                    new AcceptedPolicy(policy(FRAMEWORK_AGREEMENT_TRACEABILITY), OffsetDateTime.now().plusYears(1)),
                    new AcceptedPolicy(policy(MEMBERSHIP), OffsetDateTime.now().plusYears(1)));
            when(policyStore.getAcceptedPolicies(any())).thenReturn(policyList);

            final Policy policy = createXOneConstraintPolicy(
                    List.of(createAtomicConstraint(FRAMEWORK_AGREEMENT_TRACEABILITY, STATUS_ACTIVE),
                            createAtomicConstraint(MEMBERSHIP, STATUS_ACTIVE)));

            // when
            final boolean valid = policyCheckerService.isValid(policy, "bpn");

            // then
            assertThat(valid).isFalse();
        }
    }

    private org.eclipse.tractusx.irs.edc.client.policy.Policy policy(final String policyId) {
        return org.eclipse.tractusx.irs.edc.client.policy.Policy.builder()
                                                                .policyId(policyId)
                                                                .createdOn(OffsetDateTime.now())
                                                                .validUntil(OffsetDateTime.now().plusYears(1))
                                                                .permissions(Collections.emptyList())
                                                                .build();
    }

    private org.eclipse.tractusx.irs.edc.client.policy.Policy policy(final String policyId,
            final List<Constraint> andConstraint, final List<Constraint> orConstraint) {

        final Constraints constraints = new Constraints(andConstraint, orConstraint);
        final List<Permission> permissions = List.of(new Permission(PolicyType.USE, constraints));
        return org.eclipse.tractusx.irs.edc.client.policy.Policy.builder()
                                                                .policyId(policyId)
                                                                .validUntil(OffsetDateTime.now().plusYears(1))
                                                                .createdOn(OffsetDateTime.now())
                                                                .permissions(permissions)
                                                                .build();
    }

    @Nested
    class ExpirationTests {

        @Test
        void shouldHaveNoExpiredConstraints() {
            // given
            final Constraint constraint1 = new Constraint(FRAMEWORK_AGREEMENT_TRACEABILITY,
                    new Operator(OperatorType.EQ), STATUS_ACTIVE);
            final Constraint constraint2 = new Constraint(MEMBERSHIP, new Operator(OperatorType.EQ), STATUS_ACTIVE);
            final Constraint constraint3 = new Constraint(FRAMEWORK_AGREEMENT_DISMANTLER, new Operator(OperatorType.EQ),
                    STATUS_ACTIVE);
            final var policyList = List.of(
                    new AcceptedPolicy(policy("and-policy", List.of(), List.of(constraint1, constraint2, constraint3)),
                            OffsetDateTime.now().plusYears(1)));
            when(policyStore.getAcceptedPolicies(any())).thenReturn(policyList);

            final Policy policy = createOrConstraintPolicy(
                    List.of(createAtomicConstraint(FRAMEWORK_AGREEMENT_TRACEABILITY, STATUS_ACTIVE),
                            createAtomicConstraint(MEMBERSHIP, STATUS_ACTIVE)));

            // when
            final boolean expired = policyCheckerService.isExpired(policy, "bpn");

            // then
            assertThat(expired).isFalse();
        }

        @Test
        void shouldHaveExpiredConstraints() {
            // given
            final Constraint constraint1 = new Constraint(FRAMEWORK_AGREEMENT_TRACEABILITY,
                    new Operator(OperatorType.EQ), STATUS_ACTIVE);
            final Constraint constraint2 = new Constraint(MEMBERSHIP, new Operator(OperatorType.EQ), STATUS_ACTIVE);
            final Constraint constraint3 = new Constraint(FRAMEWORK_AGREEMENT_DISMANTLER, new Operator(OperatorType.EQ),
                    STATUS_ACTIVE);
            final var policyList = List.of(
                    new AcceptedPolicy(policy("and-policy", List.of(), List.of(constraint1, constraint2, constraint3)),
                            OffsetDateTime.now().minusYears(1)));
            when(policyStore.getAcceptedPolicies(any())).thenReturn(policyList);

            final Policy policy = createOrConstraintPolicy(
                    List.of(createAtomicConstraint(FRAMEWORK_AGREEMENT_TRACEABILITY, STATUS_ACTIVE),
                            createAtomicConstraint(MEMBERSHIP, STATUS_ACTIVE)));

            // when
            final boolean expired = policyCheckerService.isExpired(policy, "bpn");

            // then
            assertThat(expired).isTrue();
        }
    }

}