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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Annotation for validating list of business partner numbers.
 */
@Documented
@Constraint(validatedBy = BusinessPartnerNumberListValidator.class)
@Target({ ElementType.FIELD,
          ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidListOfBusinessPartnerNumbers {

    String message() default "Invalid list of business partner numbers";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * Whether to allow "default" as a valid value
     * (This is used in {@link org.eclipse.tractusx.irs.policystore.controllers.PolicyStoreController}
     * for filtering default policies).
     *
     * @return the value of the flag
     */
    boolean allowDefault() default false;
}
