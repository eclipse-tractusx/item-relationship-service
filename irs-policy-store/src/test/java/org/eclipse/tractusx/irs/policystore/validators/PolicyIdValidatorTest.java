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

import java.util.stream.Stream;

import jakarta.validation.ConstraintValidatorContext;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyIdValidatorTest {

    private static final Faker faker = new Faker();

    @InjectMocks
    private PolicyIdValidator validator;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext contextMock;

    @Test
    void withEmptyString() {
        assertThat(validator.isValid("", contextMock)).isFalse();
    }

    @Test
    void withNull() {
        assertThat(validator.isValid(null, contextMock)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("generateUUIDs")
    void withValidPolicyId(final String validPolicyId) {
        assertThat(validator.isValid(validPolicyId, contextMock)).isTrue();
    }

    private static Stream<String> generateUUIDs() {
        return Stream.generate(() -> faker.internet().uuid()).limit(10);
    }

    @ParameterizedTest
    @ValueSource(strings = { "a_",
                             "*",
                             "(",
                             ")",
                             "<",
                             ">",
                             "[",
                             "]",
                             "{",
                             "}",
                             "p3",
                             "123",
                             "abc",
                             "abc123",
                             "abc-def-4444"
    })
    void withInvalidPolicyId(final String invalidPolicyId) {
        assertThat(validator.isValid(invalidPolicyId, contextMock)).isFalse();
    }

}
