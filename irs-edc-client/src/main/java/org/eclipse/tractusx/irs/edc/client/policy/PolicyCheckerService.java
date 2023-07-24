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
package org.eclipse.tractusx.irs.edc.client.policy;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.policy.model.AndConstraint;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.Constraint;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.OrConstraint;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.XoneConstraint;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

/**
 * Check and validate Policy in Catalog fetch from EDC providers.
 */
@Slf4j
@Service("irsEdcClientPolicyCheckerService")
@RequiredArgsConstructor
public class PolicyCheckerService {

    public static final String LEFT_EXPRESSION = "PURPOSE";
    public static final String RIGHT_EXPRESSION = "active";
    private final AcceptedPoliciesProvider policyStore;

    private static boolean validateAtomicConstraint(final AtomicConstraint atomicConstraint,
            final PolicyDefinition policyDefinition1) {
        return AtomicConstraintValidator.builder()
                                        .atomicConstraint(atomicConstraint)
                                        .leftExpressionValue(policyDefinition1.getLeftExpressionValue())
                                        .rightExpressionValue(policyDefinition1.getRightExpressionValue())
                                        .expectedOperator(Operator.valueOf(policyDefinition1.getConstraintOperator()))
                                        .build()
                                        .isValid();
    }

    public boolean isValid(final Policy policy) {
        final List<PolicyDefinition> policyList = getAllowedPolicies();
        log.info("Checking policy {} against allowed policies: {}", StringMapper.mapToString(policy),
                String.join(",", policyList.stream().map(PolicyDefinition::getRightExpressionValue).toList()));
        if (getValidStoredPolicyIds().contains("*")) {
            return true;
        }
        return policy.getPermissions().stream().allMatch(permission -> isValid(permission, policyList));
    }

    private List<PolicyDefinition> getAllowedPolicies() {
        final List<String> policyIds = getValidStoredPolicyIds();
        final List<PolicyDefinition> allowedPolicies = new ArrayList<>();
        allowedPolicies.addAll(policyIds.stream().map(policy -> createPolicy(LEFT_EXPRESSION, policy)).toList());
        allowedPolicies.addAll(policyIds.stream().map(policy -> createPolicy(policy, RIGHT_EXPRESSION)).toList());

        return allowedPolicies;
    }

    private List<String> getValidStoredPolicyIds() {
        return policyStore.getAcceptedPolicies()
                          .stream()
                          .filter(p -> p.validUntil().isAfter(OffsetDateTime.now()))
                          .map(AcceptedPolicy::policyId)
                          .flatMap(this::addEncodedVersion)
                          .toList();
    }

    private boolean isValid(final Permission permission, final List<PolicyDefinition> policyDefinitions) {
        final boolean permissionTypesMatch = policyDefinitions.stream()
                                                             .allMatch(
                                                                     policyDefinition -> policyDefinition.getPermissionActionType()
                                                                                                         .equals(permission.getAction()
                                                                                                                           .getType()));
        final boolean constraintsMatch = permission.getConstraints()
                                                   .stream()
                                                   .allMatch(constraint -> isValid(constraint, policyDefinitions));
        return permissionTypesMatch && constraintsMatch;
    }

    private boolean isValid(final Constraint constraint, final List<PolicyDefinition> policyDefinitions) {
        if (constraint instanceof AtomicConstraint atomicConstraint) {
            return validateAtomicConstraint(atomicConstraint, policyDefinitions);
        } else if (constraint instanceof AndConstraint andConstraint) {
            return andConstraint.getConstraints().stream().allMatch(constr -> isValid(constr, policyDefinitions));
        } else if (constraint instanceof OrConstraint orConstraint) {
            return orConstraint.getConstraints().stream().anyMatch(constr -> isValid(constr, policyDefinitions));
        } else if (constraint instanceof XoneConstraint xoneConstraint) {
            return xoneConstraint.getConstraints().stream().filter(constr -> isValid(constr, policyDefinitions)).count()
                    == 1;
        }
        return false;
    }

    private boolean validateAtomicConstraint(final AtomicConstraint atomicConstraint,
            final List<PolicyDefinition> policyDefinitions) {
        return policyDefinitions.stream()
                                .anyMatch(policyDefinition -> validateAtomicConstraint(atomicConstraint,
                                        policyDefinition));
    }

    private PolicyDefinition createPolicy(final String leftExpression, final String rightExpression) {
        return PolicyDefinition.builder()
                               .permissionActionType("USE")
                               .constraintType("AtomicConstraint")
                               .leftExpressionValue(leftExpression)
                               .rightExpressionValue(rightExpression)
                               .constraintOperator("EQ")
                               .build();
    }

    private Stream<String> addEncodedVersion(final String original) {
        return Stream.of(original, UriUtils.encode(original, "UTF-8"));
    }

}
