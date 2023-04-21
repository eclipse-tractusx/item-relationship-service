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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.edc.policy;

import org.eclipse.dataspaceconnector.policy.model.AtomicConstraint;
import org.eclipse.dataspaceconnector.policy.model.Constraint;
import org.eclipse.dataspaceconnector.policy.model.Operator;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.springframework.stereotype.Service;

/**
 * Check and validate Policy in Catalog fetch from EDC providers.
 */
@Service
public class PolicyCheckerService {

    public boolean isValid(Policy policy) {
        PolicyDefinition policyDefinition = r2Traceability();
        return policy.getPermissions().stream().anyMatch(permission -> isValid(permission, policyDefinition));
    }

    private boolean isValid(Permission permission, PolicyDefinition policyDefinition) {
        return permission.getAction().getType().equals(policyDefinition.getPermissionActionType())
                && permission.getConstraints().stream().anyMatch(constraint -> isValid(constraint, policyDefinition));
    }

    private boolean isValid(Constraint constraint, PolicyDefinition policyDefinition) {
        if (constraint instanceof AtomicConstraint atomicConstraint) {
            return AtomicConstraintValidator.builder()
                                     .atomicConstraint(atomicConstraint)
                                     .leftExpressionValue(policyDefinition.getLeftExpressionValue())
                                     .rightExpressionValue(policyDefinition.getRightExpressionValue())
                                     .expectedOperator(Operator.valueOf(policyDefinition.getConstraintOperator()))
                                     .build().isValid();
        }
        return false;
    }

    private static PolicyDefinition r2Traceability() {
        return PolicyDefinition.builder()
                               .permissionActionType("USE")
                               .constraintType("AtomicConstraint")
                               .leftExpressionValue("idsc:PURPOSE")
                               .rightExpressionValue("R2_Traceability")
                               .constraintOperator("EQ")
                               .build();
    }

}
