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
package org.eclipse.tractusx.irs.policystore.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.function.Predicate;

import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RequestEntityValidationTest {

    public static final String PROPERTY_PATH_VALID_UNTIL = "validUntil";
    public static final String PROPERTY_PATH_PAYLOAD = "payload";
    public static final String MSG_MUST_BE_IN_FUTURE = "must be in future";
    public static final String MSG_TEMPLATE_MANDATORY = "%s should contain validation message that %s is mandatory";

    private Clock fixedClock;
    private Validator validator;

    @BeforeEach
    public void setUp() {
        this.fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        final ValidatorFactory validatorFactory = getValidatorFactory(() -> this.fixedClock);
        this.validator = validatorFactory.getValidator();
    }

    private ValidatorFactory getValidatorFactory(final ClockProvider clockProvider) {
        return Validation.byDefaultProvider().configure().clockProvider(clockProvider).buildValidatorFactory();
    }

    @Nested
    class CreatePolicyRequestTests {

        @Nested
        class ValidUntilTests {

            @Test
            void whenNull() {

                final CreatePolicyRequest requestEntity = CreatePolicyRequest.builder().validUntil(null).build();
                final Set<ConstraintViolation<Object>> violations = validator.validate(requestEntity);

                assertThat(violations).isNotEmpty();
                assertMandatory(requestEntity, PROPERTY_PATH_VALID_UNTIL, violations);
            }

            @Test
            void whenInFuture() {
                final OffsetDateTime oneSecondInFuture = OffsetDateTime.now(fixedClock).plusSeconds(1);
                assertThatNoValidUntilValidationMessage(
                        CreatePolicyRequest.builder().validUntil(oneSecondInFuture).build());
            }

            @Test
            void whenNotInFuture() {
                assertThatValidUntilMustBeInTheFutureMessage(CreatePolicyRequest.builder().validUntil(now()).build());
            }
        }

        @Nested
        class PayloadTests {

            @Test
            void whenNull() {

                final CreatePolicyRequest requestEntity = CreatePolicyRequest.builder().payload(null).build();
                final Set<ConstraintViolation<Object>> violations = validator.validate(requestEntity);

                assertThat(violations).isNotEmpty();
                assertMandatory(requestEntity, PROPERTY_PATH_PAYLOAD, violations);
            }

        }
    }

    @Nested
    class UpdatePolicyRequestTests {

        @Nested
        class ValidUntilTests {
            @Test
            void whenNull() {

                final UpdatePolicyRequest requestEntity = UpdatePolicyRequest.builder().validUntil(null).build();
                final Set<ConstraintViolation<Object>> violations = validator.validate(requestEntity);

                assertThat(violations).isNotEmpty();
                assertMandatory(requestEntity, PROPERTY_PATH_VALID_UNTIL, violations);
            }

            @Test
            void whenInFuture() {
                final OffsetDateTime oneSecondInFuture = OffsetDateTime.now(fixedClock).plusSeconds(1);
                assertThatNoValidUntilValidationMessage(
                        UpdatePolicyRequest.builder().validUntil(oneSecondInFuture).build());
            }

            @Test
            void whenNotInFuture() {
                assertThatValidUntilMustBeInTheFutureMessage(UpdatePolicyRequest.builder().validUntil(now()).build());
            }
        }
    }

    private void assertThatValidUntilMustBeInTheFutureMessage(final Object requestEntity) {

        final Set<ConstraintViolation<Object>> violations = this.validator.validate(requestEntity);

        assertThat(violations).isNotEmpty();

        final String propertyPath = PROPERTY_PATH_VALID_UNTIL;
        assertThat(violations.stream()).as("%s should contain validation message that %s has to be in the future",
                                               requestEntity.getClass().getSimpleName(), propertyPath)
                                       .anyMatch(hasMessageContaining(propertyPath, MSG_MUST_BE_IN_FUTURE));
    }

    private void assertMandatory(final Object requestEntity, final String propertyPath,
            final Set<ConstraintViolation<Object>> violations) {
        assertThat(violations.stream()).as(MSG_TEMPLATE_MANDATORY, requestEntity.getClass().getSimpleName(),
                propertyPath).anyMatch(hasMessageContaining(propertyPath, "NotNull"));
    }

    private void assertThatNoValidUntilValidationMessage(final Object requestEntity) {

        final Set<ConstraintViolation<Object>> violations = this.validator.validate(requestEntity);

        final String propertyPath = PROPERTY_PATH_VALID_UNTIL;
        assertThat(violations.stream()).as("%s should NOT contain validation message that %s has to be in the future",
                                               requestEntity.getClass().getSimpleName(), propertyPath)
                                       .noneMatch(hasMessageContaining(propertyPath, MSG_MUST_BE_IN_FUTURE));
    }

    private Predicate<ConstraintViolation<Object>> hasMessageContaining(final String propertyPath, final String msg) {
        return violation -> violation.getPropertyPath().toString().equals(propertyPath)
                && violation.getMessageTemplate().contains(msg);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(this.fixedClock);
    }

}