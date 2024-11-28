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
package org.eclipse.tractusx.irs.util;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
/**
 * Custom JSON deserializer for {@link ZonedDateTime} objects.
 * This deserializer can handle date/time strings in multiple formats, including full ISO-8601 format with time zone,
 * and date-only format with default time and time zone.
 */
public class ZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {
    @Override
    public ZonedDateTime deserialize(final JsonParser jsonParser, final DeserializationContext context)
            throws IOException {

        String value = jsonParser.getText();

        // Try parsing with multiple formats
        try {
            // Full ISO-8601 format with time zone
            return ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        } catch (DateTimeParseException e) {
            // Fallback to a date-only format and assign a default time and time zone
            try {
                return ZonedDateTime.parse(value + "T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException fallbackException) {
                throw context.weirdStringException(value, ZonedDateTime.class, "Invalid date/time format");
            }
        }
    }
}
