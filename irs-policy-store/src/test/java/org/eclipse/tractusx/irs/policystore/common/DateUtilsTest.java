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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateUtilsTest {

    @ParameterizedTest
    @MethodSource("provideDatesForIsDateBefore")
    void testIsDateBefore(final OffsetDateTime dateTime, final String referenceDateString, final boolean expected) {
        assertThat(DateUtils.isDateBefore(dateTime, referenceDateString)).isEqualTo(expected);
    }

    static Stream<Arguments> provideDatesForIsDateBefore() {
        final OffsetDateTime referenceDateTime = LocalDate.parse("2024-07-05").atStartOfDay().atOffset(ZoneOffset.UTC);
        return Stream.of( //
                Arguments.of(referenceDateTime, "2024-07-04", false),
                Arguments.of(referenceDateTime, "2024-07-05", false),
                Arguments.of(referenceDateTime, "2024-07-06", true));
    }

    @ParameterizedTest
    @MethodSource("provideDatesForIsDateAfter")
    void testIsDateAfter(final OffsetDateTime dateTime, final String dateString, final boolean expected) {
        assertThat(DateUtils.isDateAfter(dateTime, dateString)).isEqualTo(expected);
    }

    static Stream<Arguments> provideDatesForIsDateAfter() {
        final OffsetDateTime referenceDateTime = LocalDate.parse("2023-07-05")
                                                          .atTime(LocalTime.MAX)
                                                          .atOffset(ZoneOffset.UTC);

        return Stream.of( //
                Arguments.of(referenceDateTime, "2023-07-04", true),
                Arguments.of(referenceDateTime, "2023-07-05", false),
                Arguments.of(referenceDateTime, "2023-07-06", false));
    }

}