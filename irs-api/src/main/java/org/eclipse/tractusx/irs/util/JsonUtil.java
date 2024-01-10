/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.util;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.connector.job.TransferProcess;
import org.eclipse.tractusx.irs.data.JsonParseException;

/**
 * JSON object mapper.
 */
@Slf4j
@RequiredArgsConstructor
public class JsonUtil {

    /**
     * JSON object mapper implementation.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Map Type Reference helper
     */
    private static final class MapTypeReference extends TypeReference<Map<String, Object>> { }

    static {
        final SimpleModule simpleModule = new SimpleModule().addAbstractTypeMapping(TransferProcess.class,
                AASTransferProcess.class);

        MAPPER.registerModule(simpleModule);
        MAPPER.registerModule(new Jdk8Module());
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Serialize an object as a JSON {@link String}.
     *
     * @param input the object to serialize.
     * @return the JSON representation of the object
     * @throws RuntimeException on serialization error.
     */
    public String asString(final Object input) {
        try {
            return MAPPER.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            log.error("Error serializing payload", e);
            throw new JsonParseException(e);
        }
    }

    /**
     * Serialize an object as a Map {@link Map}.
     *
     * @param input the object to serialize.
     * @return the Map representation of the object
     */
    public Map<String, Object> asMap(final Object input) {
        return MAPPER.convertValue(input, new MapTypeReference());
    }

    /**
     * Deserialize an object from a JSON {@link String}.
     *
     * @param input the JSON string to deserialize.
     * @param type  the type to return.
     * @param <T>   the type to return.
     * @return deserialized object.
     * @throws RuntimeException on deserialization error.
     */
    public <T> T fromString(final String input, final Class<T> type) {
        try {
            return MAPPER.readValue(input, type);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e);
        }
    }
}
