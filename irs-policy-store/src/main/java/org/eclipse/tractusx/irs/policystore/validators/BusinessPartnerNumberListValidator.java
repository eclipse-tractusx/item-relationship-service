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

import java.util.List;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for list of business partner numbers (BPN).
 */
public class BusinessPartnerNumberListValidator
        implements ConstraintValidator<ValidListOfBusinessPartnerNumbers, List<String>> {

    /**
     * Regex for BPN.
     */
    public static final String BPN_REGEX = "(BPN)[LSA][\\w\\d]{10}[\\w\\d]{2}";

    private static final Pattern PATTERN = Pattern.compile(BPN_REGEX);

    @Override
    public boolean isValid(final List<String> value, final ConstraintValidatorContext context) {

        // allow null and empty here (in order to allow flexible combination with @NotNull and @NotEmpty)
        if (value == null || value.isEmpty()) {
            return true;
        }

        for (int index = 0; index < value.size(); index++) {
            if (!PATTERN.matcher(value.get(index)).matches()) {
                context.disableDefaultConstraintViolation();
                final String msg = "The business partner number at index %d is invalid (should conform to pattern '%s')";
                context.buildConstraintViolationWithTemplate(msg.formatted(index, BPN_REGEX)).addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
