/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
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
package org.eclipse.tractusx.irs.data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps objects to strings and vice-versa.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringMapper {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new Jdk8Module());
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String mapToString(final Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e);
        }
    }

    public static <T> T mapFromBase64String(final String value, final TypeReference<T> typeReference) {
        if (value == null) {
            return null;
        }
        return mapFromString(fromBase64(value), typeReference);
    }

    public static <T> T mapFromString(final String value, final Class<T> clazz) {
        try {
            return MAPPER.readValue(value, clazz);
        } catch (final JsonProcessingException e) {
            throw new JsonParseException(e);
        }
    }

    public static <T> T mapFromString(final String value, final TypeReference<T> typeReference) {
        try {
            return MAPPER.readValue(value, typeReference);
        } catch (final JsonProcessingException e) {
            throw new JsonParseException(e);
        }
    }

    public static String toBase64(final String str) {
        Objects.requireNonNull(str);
        return new String(Base64.getEncoder().encode(str.trim().getBytes(CHARSET)));
    }

    public static String fromBase64(final String value) {
        Objects.requireNonNull(value);
        return new String(Base64.getDecoder().decode(value.trim()), CHARSET);
    }

}
