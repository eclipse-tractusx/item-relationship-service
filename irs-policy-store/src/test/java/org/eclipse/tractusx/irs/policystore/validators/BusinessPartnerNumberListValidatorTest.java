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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BusinessPartnerNumberListValidatorTest {

    public static final String VALID_BPN_1 = "BPNL1234567890AB";
    public static final String VALID_BPN_2 = "BPNL123456789012";

    @InjectMocks
    private BusinessPartnerNumberListValidator validator;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext contextMock;

    @Test
    void withEmptyListOfStrings() {
        assertThat(validator.isValid(Collections.emptyList(), contextMock)).isTrue();
    }

    @Test
    void withNull() {
        assertThat(validator.isValid(null, contextMock)).isTrue();
    }

    @Test
    void withValidListOfStrings() {
        List<String> validList = Arrays.asList(VALID_BPN_1, VALID_BPN_2);
        assertThat(validator.isValid(validList, contextMock)).isTrue();
    }

    @Test
    void withListContainingInvalidBPN() {
        List<String> invalidList = Arrays.asList(VALID_BPN_1, "INVALID_BPN", VALID_BPN_2);
        assertThat(validator.isValid(invalidList, contextMock)).isFalse();
        verify(contextMock).buildConstraintViolationWithTemplate(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("BPN").contains(" index 1 ").contains("invalid");
    }
}
