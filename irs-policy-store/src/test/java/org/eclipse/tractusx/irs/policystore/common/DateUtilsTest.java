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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DateUtilsTest {

    @ParameterizedTest
    @MethodSource("provideDatesForIsDateBefore")
    void testIsDateBefore(final OffsetDateTime dateTime, final String referenceDateString, final boolean expected) {
        assertThat(DateUtils.isDateBefore(dateTime, referenceDateString)).isEqualTo(expected);
    }

    static Stream<Arguments> provideDatesForIsDateBefore() {
        return Stream.of( //
                Arguments.of(atStartOfDay("2024-07-05"), "2024-07-04", false),  //
                Arguments.of(atStartOfDay("2024-07-05"), "2024-07-05", false), //
                Arguments.of(atStartOfDay("2024-07-05"), "2024-07-06", true), //
                Arguments.of(OffsetDateTime.parse("2024-07-23T15:30:00Z"), "2024-07-23T15:30:00Z", false), //
                Arguments.of(OffsetDateTime.parse("2024-07-23T15:30:00Z"), "2024-07-23T15:30:01Z", true), //
                Arguments.of(OffsetDateTime.parse("2023-12-01T08:45:00+05:30"), "2023-12-01T08:45:00+05:30", false), //
                Arguments.of(OffsetDateTime.parse("2023-12-01T08:45:00+05:30"), "2023-12-01T08:46:01+05:30", true), //
                Arguments.of(OffsetDateTime.parse("2022-11-15T22:15:30-04:00"), "2022-11-15T22:15:30-04:00", false), //
                Arguments.of(OffsetDateTime.parse("2022-11-15T22:15:30-04:00"), "2022-11-15T22:16:01-04:00", true), //
                Arguments.of(OffsetDateTime.parse("2021-06-30T14:00:00.123Z"), "2021-06-30T14:00:00.123Z", false), //
                Arguments.of(OffsetDateTime.parse("2021-06-30T14:00:00.123Z"), "2021-06-30T14:00:00.124Z", true), //
                Arguments.of(OffsetDateTime.parse("2021-06-29T00:00Z"), "2021-06-29T00:00Z", false), //
                Arguments.of(OffsetDateTime.parse("2021-06-29T00:00Z"), "2021-06-30T00:01Z", true) //
        );
    }

    private static OffsetDateTime atStartOfDay(final String date) {
        return LocalDate.parse(date).atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    @ParameterizedTest
    @MethodSource("provideDatesForIsDateAfter")
    void testIsDateAfter(final OffsetDateTime dateTime, final String dateString, final boolean expected) {
        assertThat(DateUtils.isDateAfter(dateTime, dateString)).isEqualTo(expected);
    }

    static Stream<Arguments> provideDatesForIsDateAfter() {

        return Stream.of( //
                Arguments.of(atStartOfDay("2024-07-05"), "2024-07-04", true),  //
                Arguments.of(atStartOfDay("2024-07-05"), "2024-07-05", false), //
                Arguments.of(atStartOfDay("2024-07-05"), "2024-07-06", false), //
                Arguments.of(OffsetDateTime.parse("2024-07-23T15:30:00Z"), "2024-07-23T15:30:00Z", false), //
                Arguments.of(OffsetDateTime.parse("2024-07-23T15:30:00Z"), "2024-07-23T15:29:59Z", true), //
                Arguments.of(OffsetDateTime.parse("2023-12-01T08:45:00+05:30"), "2023-12-01T08:45:00+05:30", false), //
                Arguments.of(OffsetDateTime.parse("2023-12-01T08:45:00+05:30"), "2023-12-01T08:44:59+05:30", true), //
                Arguments.of(OffsetDateTime.parse("2022-11-15T22:15:30-04:00"), "2022-11-15T22:15:30-04:00", false), //
                Arguments.of(OffsetDateTime.parse("2022-11-15T22:15:30-04:00"), "2022-11-15T22:15:29-04:00", true), //
                Arguments.of(OffsetDateTime.parse("2021-06-30T14:00:00.123Z"), "2021-06-30T14:00:00.123Z", false), //
                Arguments.of(OffsetDateTime.parse("2021-06-30T14:00:00.123Z"), "2021-06-30T14:00:00.122Z", true), //
                Arguments.of(OffsetDateTime.parse("2021-06-29T00:00Z"), "2021-06-29T00:00Z", false), //
                Arguments.of(OffsetDateTime.parse("2021-06-29T00:00Z"), "2021-06-28T00:01Z", true) //
        );
    }

    private static OffsetDateTime atEndOfDay(final String dateStr) {
        return LocalDate.parse(dateStr).atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
    }

    @ParameterizedTest
    @MethodSource("provideDatesForIsDateWithoutTime")
    public void isDateWithoutTime(final String dateString, final boolean expected) {
        assertThat(DateUtils.isDateWithoutTime(dateString)).isEqualTo(expected);
    }

    static Stream<Arguments> provideDatesForIsDateWithoutTime() {
        return Stream.of( //
                Arguments.of("2023-07-23", true),  //
                Arguments.of("2023-07-23T10:15:30+01:00", false), //
                Arguments.of("2023-07-23T10:15:30Z", false) //
        );
    }

    @Test
    public void testIsDateWithoutTimeWithInvalidDate() {
        assertThatThrownBy(() -> DateUtils.isDateWithoutTime("invalid-date")).isInstanceOf(
                IllegalArgumentException.class).hasMessageContaining("Invalid date format: invalid-date");
    }

    @ParameterizedTest
    @ValueSource(strings = { "3333-11-11T11:11:11.111Z",
                             "3333-11-",
                             "2222",
                             "asdf"
    })
    void testInvalidDate(final String referenceDateStr) {
        final ThrowingCallable call = () -> DateUtils.isDateAfter(OffsetDateTime.now(), referenceDateStr);
        assertThatThrownBy(call).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Invalid date")
                                .hasMessageContaining("refer to the documentation")
                                .hasCauseInstanceOf(DateTimeParseException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = { "   \t",
                             " ",
                             ""
    })
    @NullSource
    void testInvalidDateBlank(final String referenceDateStr) {
        final ThrowingCallable call = () -> DateUtils.isDateAfter(OffsetDateTime.now(), referenceDateStr);
        assertThatThrownBy(call).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Invalid date")
                                .hasMessageContaining("must not be blank")
                                .hasNoCause();
    }

}
