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
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createAtomicConstraint;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.edc.policy.model.AndConstraint;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.OrConstraint;
import org.eclipse.tractusx.irs.edc.client.testutil.TestConstants;
import org.junit.jupiter.api.Test;

class ConstraintCheckerServiceTest {

    ConstraintCheckerService cut = new ConstraintCheckerService();

    @Test
    void shouldAcceptSimpleAtomicConstraint() {
        final Policy acceptedPolicy = createPolicyWithAndConstraint(
                List.of(new Operand(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));
        final AtomicConstraint simpleAtomicConstraint = createAtomicConstraint(TestConstants.PURPOSE,
                TestConstants.ID_3_1_TRACE);

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(simpleAtomicConstraint));

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotAcceptWrongLeftOperandAtomicConstraint() {
        final Policy acceptedPolicy = createPolicyWithAndConstraint(
                List.of(new Operand(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));
        final String unknownLeftExpression = "wrongLeft";
        final AtomicConstraint simpleAtomicConstraint = createAtomicConstraint(unknownLeftExpression,
                TestConstants.ID_3_1_TRACE);

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(simpleAtomicConstraint));

        assertThat(result).isFalse();
    }

    @Test
    void shouldNotAcceptWrongRightOperandAtomicConstraint() {
        final Policy acceptedPolicy = createPolicyWithAndConstraint(
                List.of(new Operand(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));
        final String unknownRightExpression = "ID 3.1 Trace Wrong";
        final AtomicConstraint simpleAtomicConstraint = createAtomicConstraint(TestConstants.PURPOSE,
                unknownRightExpression);

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(simpleAtomicConstraint));

        assertThat(result).isFalse();
    }

    @Test
    void shouldAcceptAndConstraint() {
        final AndConstraint andConstraint = createAndConstraint(
                List.of(createAtomicConstraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                                TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));

        final Policy acceptedPolicy = createPolicyWithAndConstraint(
                List.of(new Operand(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY, TestConstants.STATUS_ACTIVE),
                        new Operand(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE),
                        new Operand(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(andConstraint));

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotAcceptAndConstraintWithOneLessElement() {
        final AndConstraint andConstraint = createAndConstraint(
                List.of(createAtomicConstraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                                TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));

        final Policy acceptedPolicy = createPolicyWithAndConstraint(
                List.of(new Operand(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY, TestConstants.STATUS_ACTIVE),
                        new Operand(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(andConstraint));

        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectAndConstraintWhenOneIsDifferent() {
        final AndConstraint andConstraint = createAndConstraint(
                List.of(createAtomicConstraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                                TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));

        final String unknownLeftExpression = "Ship";
        final Policy acceptedPolicy = createPolicyWithAndConstraint(
                List.of(new Operand(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY, TestConstants.STATUS_ACTIVE),
                        new Operand(unknownLeftExpression, TestConstants.STATUS_ACTIVE),
                        new Operand(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(andConstraint));

        assertThat(result).isFalse();
    }

    @Test
    void shouldAcceptOrConstraint() {
        final OrConstraint orConstraint = createOrConstraint(
                List.of(createAtomicConstraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                                TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE)));

        final Policy acceptedPolicy = createPolicyWithOrConstraint(
                List.of(new Operand(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY, TestConstants.STATUS_ACTIVE),
                        new Operand(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE),
                        new Operand(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(orConstraint));

        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectOrConstraintIfAnyMatch() {
        final OrConstraint orConstraint = createOrConstraint(
                List.of(createAtomicConstraint(TestConstants.FRAMEWORK_AGREEMENT_TRACEABILITY,
                                TestConstants.STATUS_ACTIVE),
                        createAtomicConstraint(TestConstants.MEMBERSHIP, TestConstants.STATUS_ACTIVE)));

        final String unknownLeftExpression = "Ship";
        final Policy acceptedPolicy = createPolicyWithOrConstraint(
                List.of(new Operand(unknownLeftExpression, TestConstants.STATUS_ACTIVE),
                        new Operand(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(orConstraint));

        assertThat(result).isFalse();
    }

    @Test
    void shouldBeNullsafeOnNoAnd() {
        final OrConstraint orConstraint = createOrConstraint(
                List.of(createAtomicConstraint(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));
        final AndConstraint andConstraint = createAndConstraint(
                List.of(createAtomicConstraint(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));

        final List<Operand> operands = List.of(new Operand(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE));
        final List<Constraint> constraints = operands.stream()
                                                     .map(operand -> new Constraint(operand.left(),
                                                             new Operator(OperatorType.EQ), operand.right()))
                                                     .toList();

        final Policy acceptedOrPolicy = createPolicyWithConstraint(new Constraints(null, constraints));
        final boolean resultOr = cut.hasAllConstraint(acceptedOrPolicy, List.of(orConstraint, andConstraint));
        assertThat(resultOr).isFalse();
    }

    @Test
    void shouldBeNullsafeOnNoOr() {
        final OrConstraint orConstraint = createOrConstraint(
                List.of(createAtomicConstraint(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));
        final AndConstraint andConstraint = createAndConstraint(
                List.of(createAtomicConstraint(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE)));

        final List<Operand> operands = List.of(new Operand(TestConstants.PURPOSE, TestConstants.ID_3_1_TRACE));
        final List<Constraint> constraints = operands.stream()
                                                     .map(operand -> new Constraint(operand.left(),
                                                             new Operator(OperatorType.EQ), operand.right()))
                                                     .toList();

        final Policy acceptedAndPolicy = createPolicyWithConstraint(new Constraints(constraints, null));
        final boolean resultAnd = cut.hasAllConstraint(acceptedAndPolicy, List.of(orConstraint, andConstraint));
        assertThat(resultAnd).isFalse();
    }

    private Policy createPolicyWithAndConstraint(List<Operand> operands) {
        List<Constraint> and = operands.stream()
                                       .map(operand -> new Constraint(operand.left, new Operator(OperatorType.EQ),
                                               operand.right))
                                       .toList();
        Constraints constraints = new Constraints(and, new ArrayList<>());
        return createPolicyWithConstraint(constraints);
    }

    private Policy createPolicyWithOrConstraint(List<Operand> operands) {
        List<Constraint> or = operands.stream()
                                      .map(operand -> new Constraint(operand.left, new Operator(OperatorType.EQ),
                                              operand.right))
                                      .toList();
        Constraints constraints = new Constraints(new ArrayList<>(), or);
        return createPolicyWithConstraint(constraints);
    }

    private Policy createPolicyWithConstraint(Constraints constraints) {
        Permission permission = new Permission(PolicyType.ACCESS, constraints);
        List<Permission> permissions = List.of(permission);
        final String policyId = "policyId";
        return new Policy(policyId, OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), permissions);
    }

    public AndConstraint createAndConstraint(final List<org.eclipse.edc.policy.model.Constraint> constraints) {
        return AndConstraint.Builder.newInstance().constraints(constraints).build();
    }

    public OrConstraint createOrConstraint(final List<org.eclipse.edc.policy.model.Constraint> constraints) {
        return OrConstraint.Builder.newInstance().constraints(constraints).build();
    }

    private record Operand(String left, String right) {
    }

}