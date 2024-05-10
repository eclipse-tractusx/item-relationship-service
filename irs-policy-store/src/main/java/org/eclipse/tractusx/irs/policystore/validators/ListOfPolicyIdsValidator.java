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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for list of policyIDs
 */
public class ListOfPolicyIdsValidator implements ConstraintValidator<ListOfPolicyIds, List<String>> {

    @Override
    public boolean isValid(final List<String> value, final ConstraintValidatorContext context) {

        // allow null and empty here (in order to allow flexible combination with @NotNull and @NotEmpty)
        if (value == null || value.isEmpty()) {
            return true;
        }

        if (containsDuplicates(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The list of policyIds must not contain duplicates")
                   .addConstraintViolation();
            return false;
        }

        for (int index = 0; index < value.size(); index++) {
            if (!PolicyIdValidator.isValid(value.get(index))) {
                context.disableDefaultConstraintViolation();
                final String msg = "The policyId at index %d is invalid (must be a valid UUID)";
                context.buildConstraintViolationWithTemplate(msg.formatted(index)).addConstraintViolation();
                return false;
            }
        }

        return true;
    }

    /* package */
    static boolean containsDuplicates(final List<String> strings) {
        final Set<String> set = new HashSet<>(strings);
        return set.size() < strings.size();
    }
}
