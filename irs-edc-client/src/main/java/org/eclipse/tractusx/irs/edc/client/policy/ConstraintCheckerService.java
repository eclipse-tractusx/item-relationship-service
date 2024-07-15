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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.policy.model.AndConstraint;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.Constraint;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.OrConstraint;
import org.springframework.stereotype.Service;

/**
 * Check and validate Constraint from Policy in Catalog
 * fetch from EDC providers against accepted Policies.
 */
@Slf4j
@Service
public class ConstraintCheckerService {

    public boolean hasAllConstraint(final Policy acceptedPolicy, final List<Constraint> constraints) {
        final List<Constraints> acceptedConstraintsList = acceptedPolicy.getPermissions()
                                                                        .stream()
                                                                        .map(Permission::getConstraint)
                                                                        .toList();

        return constraints.stream().allMatch(constraint -> isValidOnList(constraint, acceptedConstraintsList));
    }

    private boolean isValidOnList(final Constraint constraint, final List<Constraints> acceptedConstraintsList) {
        return acceptedConstraintsList.stream()
                                      .allMatch(acceptedConstraints -> isSameAs(constraint, acceptedConstraints));
    }

    private boolean isSameAs(final Constraint constraint, final Constraints acceptedConstraints) {

        if (constraint instanceof AtomicConstraint atomicConstraint) {
            return acceptedConstraints.getOr().stream().anyMatch(p -> isSameAs(atomicConstraint, p))
                    || acceptedConstraints.getAnd().stream().anyMatch(p -> isSameAs(atomicConstraint, p));
        }

        if (constraint instanceof AndConstraint andConstraint) {

            // AND means the number of constraints must be the same
            if (acceptedConstraints.getAnd() != null
                    && acceptedConstraints.getAnd().size() != andConstraint.getConstraints().size()) {
                return false;
            }

            return andConstraint.getConstraints()
                                .stream()
                                .allMatch(constr -> isInList(constr, Optional.ofNullable(acceptedConstraints.getAnd())
                                                                             .orElse(Collections.emptyList())));
        }

        if (constraint instanceof OrConstraint orConstraint) {
            return orConstraint.getConstraints()
                               .stream()
                               .anyMatch(constr -> isInList(constr, Optional.ofNullable(acceptedConstraints.getOr())
                                                                            .orElse(Collections.emptyList())));
        }

        return false;
    }

    private boolean isInList(final Constraint constraint,
            final List<org.eclipse.tractusx.irs.edc.client.policy.Constraint> acceptedConstraints) {
        if (constraint instanceof AtomicConstraint atomicConstraint) {
            return acceptedConstraints.stream().anyMatch(ac -> isSameAs(atomicConstraint, ac));
        } else {
            return false;
        }
    }

    private boolean isSameAs(final AtomicConstraint atomicConstraint,
            final org.eclipse.tractusx.irs.edc.client.policy.Constraint acceptedConstraint) {
        return AtomicConstraintValidator.builder()
                                        .atomicConstraint(atomicConstraint)
                                        .leftExpressionValue(acceptedConstraint.getLeftOperand())
                                        .rightExpressionValue(acceptedConstraint.getRightOperand())
                                        .expectedOperator(Operator.valueOf(
                                                acceptedConstraint.getOperator().getOperatorType().name()))
                                        .build()
                                        .isValid();
    }

}
