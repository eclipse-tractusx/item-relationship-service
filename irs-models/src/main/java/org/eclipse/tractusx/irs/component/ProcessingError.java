//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.component;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

/**
 * Processing Error Data Class
 */
@Value
@Builder(toBuilder = true, setterPrefix = "with")
@JsonDeserialize(builder = ProcessingError.ProcessingErrorBuilder.class)
public class ProcessingError {
    private final String errorDetail;
    private final ZonedDateTime lastAttempt;
    private int retryCounter;

    /**
     * Builder class
     */
    @JsonPOJOBuilder()
    public static class ProcessingErrorBuilder {

    }
}
