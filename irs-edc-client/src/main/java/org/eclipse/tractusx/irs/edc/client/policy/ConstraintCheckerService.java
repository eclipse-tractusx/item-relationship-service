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

import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.policy.model.AndConstraint;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.Constraint;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.OrConstraint;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConstraintCheckerService {

    public boolean hasAllConstraint(final Policy acceptedPolicy, final List<Constraint> constraints) {
        final List<Constraints> acceptedConstraintsList = acceptedPolicy.getPermissions()
                                                     .stream()
                                                     .map(Permission::getConstraints)
                                                     .flatMap(Collection::stream)
                                                     .toList();

        return constraints.stream().allMatch(constraint -> isValidOnList(constraint, acceptedConstraintsList));
    }

    private boolean isValidOnList(final Constraint constraint, final List<Constraints> acceptedConstraintsList) {
        return acceptedConstraintsList.stream().anyMatch(acceptedConstraints -> isSameAs(constraint, acceptedConstraints));
    }

    private boolean isSameAs(final Constraint constraint, final Constraints acceptedConstraints) {
        if (constraint instanceof AtomicConstraint atomicConstraint) {
            return acceptedConstraints.getOr().stream().anyMatch(p -> isSameAs(atomicConstraint, p))
                    || acceptedConstraints.getAnd().stream().anyMatch(p -> isSameAs(atomicConstraint, p));
        } else if (constraint instanceof AndConstraint andConstraint) {
            return andConstraint.getConstraints().stream().allMatch(constr -> isOnAndList(constr, acceptedConstraints));
        } else if (constraint instanceof OrConstraint orConstraint) {
            return orConstraint.getConstraints().stream().anyMatch(constr -> isOnOrList(constr, acceptedConstraints));
        } else {
            return false;
        }
    }

    private boolean isOnAndList(final Constraint constraint, final Constraints acceptedConstraints) {
        if (constraint instanceof AtomicConstraint atomicConstraint) {
            return acceptedConstraints.getAnd().stream().anyMatch(ac -> isSameAs(atomicConstraint, ac));
        } else {
            return false;
        }
    }

    private boolean isOnOrList(final Constraint constraint, final Constraints acceptedConstraints) {
        if (constraint instanceof AtomicConstraint atomicConstraint) {
            return acceptedConstraints.getOr().stream().anyMatch(ac -> isSameAs(atomicConstraint, ac));
        } else {
            return false;
        }
    }

    private boolean isSameAs(final AtomicConstraint atomicConstraint, final org.eclipse.tractusx.irs.edc.client.policy.Constraint ac) {
        return AtomicConstraintValidator.builder()
                                        .atomicConstraint(atomicConstraint)
                                        .leftExpressionValue(ac.getLeftOperand())
                                        .rightExpressionValue(ac.getRightOperand().stream().findFirst().orElse(""))
                                        .expectedOperator(Operator.valueOf(ac.getOperator().name()))
                                        .build()
                                        .isValid();
    }

}
