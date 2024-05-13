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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
class PolicyIdListValidatorTest {

    @InjectMocks
    private ListOfPolicyIdsValidator validator;

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
        final String policyId1 = UUID.randomUUID().toString();
        final String policyId2 = UUID.randomUUID().toString();
        List<String> validList = Arrays.asList(policyId1, policyId2);
        assertThat(validator.isValid(validList, contextMock)).isTrue();
    }

    @Test
    void withListContainingInvalidPolicyId() {
        List<String> invalidList = Arrays.asList(UUID.randomUUID().toString(), "_INVALID_POLICY_ID_");
        assertThat(validator.isValid(invalidList, contextMock)).isFalse();
        verify(contextMock).buildConstraintViolationWithTemplate(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("policyId").contains(" index 1 ").contains("invalid");
    }

}
