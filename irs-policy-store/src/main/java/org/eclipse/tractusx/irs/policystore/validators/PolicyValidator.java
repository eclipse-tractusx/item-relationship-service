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

public class PolicyValidator {

    record PolicyId(@ValidPolicyId String policyId) {
    }

    public static void validate(final Policy policy) {

        try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {

            final Validator validator = validatorFactory.getValidator();

            // workaround to avoid manipulating the class policy which comes from other module
            final Set<ConstraintViolation<PolicyId>> policyIdViolations = validator.validate(
                    new PolicyId(policy.getPolicyId()));
            if (!policyIdViolations.isEmpty()) {
                throw new ConstraintViolationException(policyIdViolations);
            }

            final Set<ConstraintViolation<Policy>> policyViolations = validator.validate(policy);
            if (!policyViolations.isEmpty()) {
                throw new ConstraintViolationException(policyViolations);
            }
        }

    }
}
