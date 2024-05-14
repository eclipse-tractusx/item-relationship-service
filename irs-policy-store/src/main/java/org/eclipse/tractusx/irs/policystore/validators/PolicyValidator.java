/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.policystore.validators;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Validator for policy
 */
public final class PolicyValidator {

    private static final String POLICY_INCOMPLETE_MESSAGE = "Policy does not contain all required fields. Missing: %s";

    record PolicyId(@ValidPolicyId String policyId) {
    }

    private PolicyValidator() {
        super();
    }

    /**
     * Validates the policy
     *
     * @param policy the policy
     */
    public static void validate(final Policy policy) {

        validateRequiredFields(policy);

        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            final Validator validator = validatorFactory.getValidator();
            // workaround to avoid manipulating the class policy which comes from other module
            validatePolicyId(policy, validator);
            validatePolicy(policy, validator);
        }

    }

    private static void validateRequiredFields(final Policy policy) {
        if (policy.getPolicyId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(POLICY_INCOMPLETE_MESSAGE, "@id"));
        }

        if (policy.getPermissions() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format(POLICY_INCOMPLETE_MESSAGE, "odrl:permission"));
        }

        if (policy.getPermissions().stream().anyMatch(p -> p.getConstraint() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format(POLICY_INCOMPLETE_MESSAGE, "odrl:constraint"));
        }
    }

    private static void validatePolicy(final Policy policy, final Validator validator) {
        final Set<ConstraintViolation<Policy>> policyViolations = validator.validate(policy);
        if (!policyViolations.isEmpty()) {
            throw new ConstraintViolationException(policyViolations);
        }
    }

    private static void validatePolicyId(final Policy policy, final Validator validator) {
        final Set<ConstraintViolation<PolicyId>> policyIdViolations = validator.validate(
                new PolicyId(policy.getPolicyId()));
        if (!policyIdViolations.isEmpty()) {
            throw new ConstraintViolationException(policyIdViolations);
        }
    }
}
