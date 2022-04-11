//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Exception container for job
 */
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = JobException.JobExceptionBuilder.class)
@AllArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
@ExcludeFromCodeCoverageGeneratedReport
public class JobException {

    public static final int EXCEPTION_NAME_MAX_LENGHT = 100;
    public static final int ZERO = 0;
    public static final int ERROR_DETAIL_MAX_LENGHT = 4000;

    @Schema(description = "Name of the exception occurred", implementation = String.class, minLength = ZERO, maxLength = EXCEPTION_NAME_MAX_LENGHT)
    private String exceptionName;

    @Schema(description = "Detail information for the error occurred", implementation = String.class, minLength = ZERO, maxLength = ERROR_DETAIL_MAX_LENGHT)
    private String errorDetail;

    @Schema(description = "Datetime when error occurred", implementation = Instant.class)
    private Instant exceptionDate;

    /**
     * Builder class
     */
    @JsonPOJOBuilder(withPrefix = "with")
    public static class JobExceptionBuilder {
    }

}
