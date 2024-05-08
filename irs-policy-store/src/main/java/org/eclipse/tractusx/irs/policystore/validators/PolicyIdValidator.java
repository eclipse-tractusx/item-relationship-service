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

import java.util.UUID;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for list of business partner numbers (BPN).
 */
public class PolicyIdValidator implements ConstraintValidator<ValidPolicyId, String> {

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {

        // allow null and empty here (in order to allow flexible combination with @NotNull)
        if (value == null) {
            return true;
        }

        if (isInvalid(value)) {
            return false;
        }

        return true;
    }

    private static boolean isInvalid(final String policyId) {
        return !isValid(policyId);
    }

    private static boolean isValid(final String policyId) {
        return validateUUID(policyId);
    }

    static boolean validateUUID(String uuidStr) {
        try {
            UUID.fromString(uuidStr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
