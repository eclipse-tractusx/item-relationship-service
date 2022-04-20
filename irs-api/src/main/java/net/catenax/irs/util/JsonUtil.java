//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.exceptions.JsonParseException;

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

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
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
