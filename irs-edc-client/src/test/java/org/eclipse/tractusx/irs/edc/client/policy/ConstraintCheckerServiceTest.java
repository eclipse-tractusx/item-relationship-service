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
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createAtomicConstraint;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.edc.policy.model.AndConstraint;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.OrConstraint;
import org.junit.jupiter.api.Test;

class ConstraintCheckerServiceTest {

    ConstraintCheckerService cut = new ConstraintCheckerService();

    @Test
    void shouldAcceptSimpleAtomicConstraint() {
        final Policy acceptedPolicy = createPolicyWithAndConstraint(List.of(new Operand("PURPOSE", "ID 3.1 Trace")));
        final AtomicConstraint simpleAtomicConstraint = createAtomicConstraint("PURPOSE", "ID 3.1 Trace");

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(simpleAtomicConstraint));

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotAcceptWrongLeftOperandAtomicConstraint() {
        final Policy acceptedPolicy = createPolicyWithAndConstraint(List.of(new Operand("PURPOSE", "ID 3.1 Trace")));
        final AtomicConstraint simpleAtomicConstraint = createAtomicConstraint("wrongLeft", "ID 3.1 Trace");

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(simpleAtomicConstraint));

        assertThat(result).isFalse();
    }

    @Test
    void shouldNotAcceptWrongRightOperandAtomicConstraint() {
        final Policy acceptedPolicy = createPolicyWithAndConstraint(List.of(new Operand("PURPOSE", "ID 3.1 Trace")));
        final AtomicConstraint simpleAtomicConstraint = createAtomicConstraint("PURPOSE", "ID 3.1 Trace Wrong");

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(simpleAtomicConstraint));

        assertThat(result).isFalse();
    }

    @Test
    void shouldAcceptAndConstraint() {
        final AndConstraint andConstraint = createAndConstraint(List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                createAtomicConstraint("Membership", "active"),
                createAtomicConstraint("PURPOSE", "ID 3.1 Trace")));

        final Policy acceptedPolicy = createPolicyWithAndConstraint(List.of(
                new Operand("FrameworkAgreement.traceability", "active"),
                new Operand("Membership", "active"),
                new Operand("PURPOSE", "ID 3.1 Trace")
                ));

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(andConstraint));

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotAcceptAndConstraintWithOneLessElement() {
        final AndConstraint andConstraint = createAndConstraint(List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                createAtomicConstraint("Membership", "active"),
                createAtomicConstraint("PURPOSE", "ID 3.1 Trace")));

        final Policy acceptedPolicy = createPolicyWithAndConstraint(List.of(
                new Operand("FrameworkAgreement.traceability", "active"),
                new Operand("PURPOSE", "ID 3.1 Trace")
        ));

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(andConstraint));

        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectAndConstraintWhenOneIsDifferent() {
        final AndConstraint andConstraint = createAndConstraint(List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                createAtomicConstraint("Membership", "active"),
                createAtomicConstraint("PURPOSE", "ID 3.1 Trace")));

        final Policy acceptedPolicy = createPolicyWithAndConstraint(List.of(
                new Operand("FrameworkAgreement.traceability", "active"),
                new Operand("Ship", "active"),
                new Operand("PURPOSE", "ID 3.1 Trace")
        ));

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(andConstraint));

        assertThat(result).isFalse();
    }

    @Test
    void shouldAcceptOrConstraint() {
        final OrConstraint orConstraint = createOrConstraint(List.of(
                createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                createAtomicConstraint("Membership", "active")));

        final Policy acceptedPolicy = createPolicyWithOrConstraint(List.of(
                new Operand("FrameworkAgreement.traceability", "active"),
                new Operand("Membership", "active"),
                new Operand("PURPOSE", "ID 3.1 Trace")
        ));

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(orConstraint));

        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectOrConstraintIfAnyMatch() {
        final OrConstraint orConstraint = createOrConstraint(List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                createAtomicConstraint("Membership", "active")));

        final Policy acceptedPolicy = createPolicyWithOrConstraint(List.of(
                new Operand("Ship", "active"),
                new Operand("PURPOSE", "ID 3.1 Trace")
        ));

        boolean result = cut.hasAllConstraint(acceptedPolicy, List.of(orConstraint));

        assertThat(result).isFalse();
    }

    private Policy createPolicyWithAndConstraint(List<Operand> operands) {
        List<Constraint> and = operands.stream().map(operand -> new Constraint(operand.left, OperatorType.EQ, List.of(operand.right))).toList();
        Constraints constraints = new Constraints(and, new ArrayList<>());
        return createPolicyWithConstraint(constraints);
    }

    private Policy createPolicyWithOrConstraint(List<Operand> operands) {
        List<Constraint> or = operands.stream().map(operand -> new Constraint(operand.left, OperatorType.EQ, List.of(operand.right))).toList();
        Constraints constraints = new Constraints(new ArrayList<>(), or);
        return createPolicyWithConstraint(constraints);
    }

    private Policy createPolicyWithConstraint(Constraints constraints) {
        List<Constraints> constraintsList = List.of(constraints);
        Permission permission = new Permission(PolicyType.ACCESS, constraintsList);
        List<Permission> permissions = List.of(permission);
        return new Policy("policyId", OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), permissions);
    }

    public static AndConstraint createAndConstraint(final List<org.eclipse.edc.policy.model.Constraint> constraints) {
        return AndConstraint.Builder.newInstance().constraints(constraints).build();
    }

    public static OrConstraint createOrConstraint(final List<org.eclipse.edc.policy.model.Constraint> constraints) {
        return OrConstraint.Builder.newInstance().constraints(constraints).build();
    }

    private record Operand(String left, String right) {}


}