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
package org.eclipse.tractusx.irs.policystore.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.tractusx.irs.policystore.models.SearchCriteria.Operation.AFTER_LOCAL_DATE;
import static org.eclipse.tractusx.irs.policystore.models.SearchCriteria.Operation.EQUALS;

import java.util.List;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.eclipse.tractusx.irs.policystore.models.SearchCriteria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SearchParameterParserTest {

    @ParameterizedTest
    @ValueSource(strings = {
            // operation and right hand side missing
            "bpn",
            // right hand side missing
            "policyId,EQUALS"
    })
    void invalidSearchCriteria(final String searchParam) {
        final ThrowingCallable call = () -> new SearchParameterParser(List.of(searchParam));
        assertThatThrownBy(call).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining(
                                        "Illegal search parameter at index 0. Format should be <propertyName>,<operation>,<value>.");
    }

    @Test
    void notExistingOperation() {
        final ThrowingCallable call = () -> new SearchParameterParser(List.of("policyId,NOT_EXISTING_OP,test"));
        assertThatThrownBy(call).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void betweenNotForNonDateProperties() {
        final ThrowingCallable call = () -> new SearchParameterParser(
                List.of("policyId," + AFTER_LOCAL_DATE + ",test"));
        assertThatThrownBy(call).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Date operation are only supported for date properties");
    }

    @Test
    void propertyDoesNotSupportFiltering() {
        final ThrowingCallable call = () -> new SearchParameterParser(List.of("unsupportedProperty,EQUALS,test"));
        assertThatThrownBy(call).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Only the following properties support filtering: ");
    }

    @Test
    void shouldTrimEachPart() {
        final List<SearchCriteria<?>> searchCriteriaList = new SearchParameterParser(
                List.of("  policyId  ,   EQUALS   ,   policy1  ")).getSearchCriteria();
        assertThat(searchCriteriaList).hasSize(1);
        assertThat(searchCriteriaList.get(0).getProperty()).isEqualTo("policyId");
        assertThat(searchCriteriaList.get(0).getOperation()).isEqualTo(EQUALS);
        assertThat(searchCriteriaList.get(0).getValue()).isEqualTo("policy1");
    }

}