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
import java.util.List;

import org.eclipse.edc.policy.model.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyCheckerServiceTest {

    private PolicyCheckerService policyCheckerService;
    @Mock
    private AcceptedPoliciesProvider policyStore;

    @BeforeEach
    void setUp() {
        final var policyList = List.of(new AcceptedPolicy("ID 3.0 Trace", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("FrameworkAgreement.traceability", OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        final List<String> leftOperands = List.of("PURPOSE");
        final List<String> rightOperands = List.of("active");
        policyCheckerService = new PolicyCheckerService(policyStore, rightOperands, leftOperands);
    }

    @ParameterizedTest
    @CsvSource(value = { "PURPOSE,ID 3.0 Trace",
                         "PURPOSE,ID%203.0%20Trace",
                         "FrameworkAgreement.traceability,active"
    }, delimiter = ',')
    void shouldConfirmValidPolicy(final String leftExpr, final String rightExpr) {
        // given
        Policy policy = createAtomicConstraintPolicy(leftExpr, rightExpr);
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectWrongPolicy() {
        // given
        Policy policy = createAtomicConstraintPolicy("PURPOSE", "Wrong_Trace");
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectWhenPolicyStoreIsEmpty() {
        // given
        Policy policy = createAtomicConstraintPolicy("PURPOSE", "ID 3.0 Trace");
        when(policyStore.getAcceptedPolicies()).thenReturn(List.of());
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldConfirmValidPolicyWhenWildcardIsSet() {
        // given
        final var policyList = List.of(new AcceptedPolicy("ID 3.0 Trace", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("*", OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createAtomicConstraintPolicy("FrameworkAgreement.traceability", "active");
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectWhenWildcardIsPartOfPolicy() {
        // given
        final var policyList = List.of(new AcceptedPolicy("Policy*", OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createAtomicConstraintPolicy("FrameworkAgreement.traceability", "active");
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldValidateAndConstraints() {
        // given
        final var policyList = List.of(
                new AcceptedPolicy("FrameworkAgreement.traceability", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("Membership", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("ID 3.1 Trace", OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createAndConstraintPolicy(
                List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                        createAtomicConstraint("Membership", "active"),
                        createAtomicConstraint("PURPOSE", "ID 3.1 Trace")));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectAndConstraintsWhenOnlyOneMatch() {
        // given
        final var policyList = List.of(
                new AcceptedPolicy("FrameworkAgreement.traceability", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("FrameworkAgreement.dismantler", OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createAndConstraintPolicy(
                List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                        createAtomicConstraint("Membership", "active")));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldValidateOrConstraints() {
        // given
        final var policyList = List.of(
                new AcceptedPolicy("FrameworkAgreement.traceability", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("FrameworkAgreement.dismantler", OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createOrConstraintPolicy(
                List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                        createAtomicConstraint("Membership", "active")));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectOrConstraintsWhenNoneMatch() {
        // given
        final var policyList = List.of(new AcceptedPolicy("FrameworkAgreement.test", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("FrameworkAgreement.dismantler", OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createAndConstraintPolicy(
                List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                        createAtomicConstraint("Membership", "active")));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldValidateXOneConstraints() {
        // given
        final var policyList = List.of(
                new AcceptedPolicy("FrameworkAgreement.traceability", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("FrameworkAgreement.dismantler", OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);

        Policy policy = createXOneConstraintPolicy(
                List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                        createAtomicConstraint("Membership", "active")));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectXOneConstraintsWhenNoneMatch() {
        // given
        final var policyList = List.of(new AcceptedPolicy("FrameworkAgreement.test", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("FrameworkAgreement.dismantler", OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createXOneConstraintPolicy(
                List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                        createAtomicConstraint("Membership", "active")));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectXOneConstraintsWhenMoreThanOneMatch() {
        // given
        final var policyList = List.of(
                new AcceptedPolicy("FrameworkAgreement.traceability", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("Membership", OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createXOneConstraintPolicy(
                List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                        createAtomicConstraint("Membership", "active")));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldAcceptMultipleRightOperands() {
        // given
        final var policyList = List.of(
                new AcceptedPolicy("FrameworkAgreement.traceability", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("FrameworkAgreement.dismantler", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("Membership", OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        final PolicyCheckerService testee = new PolicyCheckerService(policyStore, List.of("active", "inactive"),
                List.of());

        Policy policy = createAndConstraintPolicy(
                List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                        createAtomicConstraint("FrameworkAgreement.dismantler", "inactive"),
                        createAtomicConstraint("Membership", "active")));
        // when
        boolean result = testee.isValid(policy);

        // then
        assertThat(result).isTrue();
    }
}