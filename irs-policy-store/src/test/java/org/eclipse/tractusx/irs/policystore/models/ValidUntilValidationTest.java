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
import static org.junit.jupiter.api.Assertions.assertAll;

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
import org.junit.jupiter.api.Test;

class ValidUntilValidationTest {

    public static final String PROPERTY_PATH_VALID_UNTIL = "validUntil";
    public static final String MSG_MUST_BE_IN_FUTURE = "must be in future";

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

    @Test
    void CreatePolicyRequest_validUntil_null_shouldHaveValidationMessage() {

        final CreatePolicyRequest requestEntity = CreatePolicyRequest.builder().validUntil(null).build();
        final Set<ConstraintViolation<Object>> violations = this.validator.validate(requestEntity);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream()).describedAs(
                                               requestEntity.getClass().getSimpleName() + " should contain validation message that "
                                                       + PROPERTY_PATH_VALID_UNTIL + " is mandatory")
                                       .anyMatch(hasValidUntilMessageContaining("NotNull"));
    }

    @Test
    void UpdatePolicyRequest_validUntil_null_shouldHaveValidationMessage() {

        final UpdatePolicyRequest requestEntity = UpdatePolicyRequest.builder().validUntil(null).build();
        final Set<ConstraintViolation<Object>> violations = this.validator.validate(requestEntity);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream()).describedAs(
                                               requestEntity.getClass().getSimpleName() + " should contain validation message that "
                                                       + PROPERTY_PATH_VALID_UNTIL + " is mandatory")
                                       .anyMatch(hasValidUntilMessageContaining("NotNull"));
    }

    @Test
    void validUntil_inFuture_shouldNotHaveValidationMessage() {

        final OffsetDateTime now = OffsetDateTime.now(this.fixedClock);
        final OffsetDateTime oneSecondInFuture = now.plusSeconds(1);

        assertAll(() -> assertThatNoValidUntilValidationMessage(
                        CreatePolicyRequest.builder().validUntil(oneSecondInFuture).build()),
                () -> assertThatNoValidUntilValidationMessage(
                        UpdatePolicyRequest.builder().validUntil(oneSecondInFuture).build()));

    }

    @Test
    void validUntil_notInFuture_shouldHaveValidationMessage() {

        final OffsetDateTime now = OffsetDateTime.now(this.fixedClock);

        assertAll(() -> assertThatValidUntilMustBeInTheFutureMessage(
                        CreatePolicyRequest.builder().validUntil(now).build()),
                () -> assertThatValidUntilMustBeInTheFutureMessage(
                        UpdatePolicyRequest.builder().validUntil(now).build()));

    }

    private void assertThatValidUntilMustBeInTheFutureMessage(final Object requestEntity) {

        final Set<ConstraintViolation<Object>> violations = this.validator.validate(requestEntity);

        assertThat(violations).isNotEmpty();
        final String msg = MSG_MUST_BE_IN_FUTURE;
        assertThat(violations.stream()).describedAs(
                                               requestEntity.getClass().getSimpleName() + " should contain validation message that "
                                                       + PROPERTY_PATH_VALID_UNTIL + " has to be in the future")
                                       .anyMatch(hasValidUntilMessageContaining(msg));
    }

    private void assertThatNoValidUntilValidationMessage(final Object requestEntity) {

        final Set<ConstraintViolation<Object>> violations = this.validator.validate(requestEntity);

        assertThat(violations.stream()).describedAs(
                                               requestEntity.getClass().getSimpleName() + " should not contain validation message that "
                                                       + PROPERTY_PATH_VALID_UNTIL + " has to be in the future")
                                       .noneMatch(hasValidUntilMessageContaining(MSG_MUST_BE_IN_FUTURE));
    }

    private Predicate<ConstraintViolation<Object>> hasValidUntilMessageContaining(final String msg) {
        return violation -> violation.getPropertyPath().toString().equals(PROPERTY_PATH_VALID_UNTIL)
                && violation.getMessageTemplate().contains(msg);
    }

}