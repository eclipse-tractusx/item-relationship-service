//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

/**
 * Processing Error Data Class
 */
@Data
@Builder(toBuilder = true, setterPrefix = "with")
@JsonDeserialize(builder = ProcessingError.ProcessingErrorBuilder.class)
public class ProcessingError {
    private final String exception;
    private final String errorDetail;
    private final Instant lastAttempt;
    private int retryCounter;

    public void incrementRetryCounter() {
        retryCounter += 1;
    }

    /**
     * Builder class
     */
    @JsonPOJOBuilder()
    public static class ProcessingErrorBuilder {

    }
}
