/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessPartnerNumberListValidatorTest {

    public static final String VALID_BPN_1 = "BPNL1234567890AB";
    public static final String VALID_BPN_2 = "BPNL123456789012";

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext contextMock;

    @Test
    void withEmptyListOfStrings() {
        assertThat(new BusinessPartnerNumberListValidatorBuilder().build()
                                                                  .isValid(Collections.emptyList(),
                                                                          contextMock)).isTrue();
    }

    @Test
    void withNull() {
        assertThat(new BusinessPartnerNumberListValidatorBuilder().build().isValid(null, contextMock)).isTrue();
    }

    @Test
    void withValidListOfStrings() {
        List<String> validList = Arrays.asList(VALID_BPN_1, VALID_BPN_2);
        assertThat(new BusinessPartnerNumberListValidatorBuilder().build().isValid(validList, contextMock)).isTrue();
    }

    @Test
    void withListContainingInvalidBPN() {
        List<String> invalidList = Arrays.asList(VALID_BPN_1, "INVALID_BPN", VALID_BPN_2);
        assertThat(new BusinessPartnerNumberListValidatorBuilder().build().isValid(invalidList, contextMock)).isFalse();
        verify(contextMock).buildConstraintViolationWithTemplate(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("BPN").contains(" index 1 ").contains("invalid");
    }

    @ParameterizedTest
    @ValueSource(strings = { "BPN",
                             "BPNL",
                             "BPNACB",
                             "BPNA1234567890AB",
                             "BPNS1234567890AB",
                             "DELETE * FROM Table",
                             "ERRRES"
    })
    void withInvalidBPN(final String invalidBPN) {
        assertThat(new BusinessPartnerNumberListValidatorBuilder().build()
                                                                  .isValid(Collections.singletonList(invalidBPN),
                                                                          contextMock)).isFalse();
        verify(contextMock).buildConstraintViolationWithTemplate(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("BPN").contains(" index 0 ").contains("invalid");
    }

    @Test
    void withAllowDefaultTrue_goodCase() {
        final BusinessPartnerNumberListValidator validator = new BusinessPartnerNumberListValidatorBuilder().allowDefault(
                true).build();
        final List<String> listWithDefault = Arrays.asList("BPNL1234567890AB", "default");
        assertThat(validator.isValid(listWithDefault, contextMock)).isTrue();
    }

    @Test
    void withAllowDefaultTrue_badCase() {
        final BusinessPartnerNumberListValidator validator = new BusinessPartnerNumberListValidatorBuilder().build();
        final List<String> listWithDefault = Arrays.asList("BPNL1234567890AB", "default");
        assertThat(validator.isValid(listWithDefault, contextMock)).isFalse();
        verify(contextMock).buildConstraintViolationWithTemplate(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).startsWith("The business partner number at index 1 is invalid");
    }

    /**
     * Builder for BusinessPartnerNumberListValidator.
     */
    public static class BusinessPartnerNumberListValidatorBuilder {

        private String message = "Invalid list of business partner numbers";
        private Class<?>[] groups = new Class<?>[0];
        private Class<? extends Payload>[] payload = new Class[0];
        private boolean allowDefault = false;

        public BusinessPartnerNumberListValidatorBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        public BusinessPartnerNumberListValidatorBuilder setGroups(Class<?>[] groups) {
            this.groups = groups;
            return this;
        }

        public BusinessPartnerNumberListValidatorBuilder setPayload(Class<? extends Payload>[] payload) {
            this.payload = payload;
            return this;
        }

        public BusinessPartnerNumberListValidatorBuilder allowDefault(boolean allowDefault) {
            this.allowDefault = allowDefault;
            return this;
        }

        public BusinessPartnerNumberListValidator build() {
            ValidListOfBusinessPartnerNumbers annotation = new ValidListOfBusinessPartnerNumbers() {
                public Class<? extends Annotation> annotationType() {
                    return ValidListOfBusinessPartnerNumbers.class;
                }

                public String message() {
                    return message;
                }

                public Class<?>[] groups() {
                    return groups;
                }

                public Class<? extends Payload>[] payload() {
                    return payload;
                }

                public boolean allowDefault() {
                    return allowDefault;
                }
            };

            final BusinessPartnerNumberListValidator validator = new BusinessPartnerNumberListValidator();
            validator.initialize(annotation);
            return validator;
        }
    }
}
