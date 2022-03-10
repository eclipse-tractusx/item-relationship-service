//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

/**
 * JSON object mapper.
 */
@RequiredArgsConstructor
public class JsonUtil {

    /**
     * Logger.
     */
    private final Monitor monitor;

    /**
     * JSON object mapper implementation.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Serialize an object as a JSON {@link String}.
     *
     * @param input the object to serialize.
     * @return the JSON representation of the object
     * @throws EdcException on serialization error.
     */
    public String asString(final Object input) {
        try {
            return MAPPER.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            monitor.severe("Error serializing payload", e);
            throw new EdcException(e);
        }
    }

    /**
     * Deserialize an object from a JSON {@link String}.
     *
     * @param input the JSON string to deserialize.
     * @param type  the type to return.
     * @param <T>   the type to return.
     * @return deserialized object.
     * @throws EdcException on deserialization error.
     */
    public <T> T fromString(final String input, final Class<T> type) {
        try {
            return MAPPER.readValue(input, type);
        } catch (JsonProcessingException e) {
            throw new EdcException(e);
        }
    }
}
