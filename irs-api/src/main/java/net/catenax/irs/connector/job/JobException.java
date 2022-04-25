//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.job;

import java.time.Instant;

import lombok.Getter;
import net.catenax.irs.component.JobErrorDetails;

/**
 * Job Exception with embedded JobErrorDetails
 */
public class JobException extends RuntimeException {
    private static final String DEFAULT_ERROR_MESSAGE = "Critical error occur!!";

    @Getter
    private final JobErrorDetails jobErrorDetails;

    public JobException() {
        super(DEFAULT_ERROR_MESSAGE);
        jobErrorDetails = JobErrorDetails.builder()
                                         .exception(ResponseStatus.FATAL_ERROR.toString())
                                         .errorDetail(DEFAULT_ERROR_MESSAGE)
                                         .exceptionDate(Instant.now())
                                         .build();
    }

    public JobException(final String message) {
        super(message);
        jobErrorDetails = JobErrorDetails.builder()
                                         .exception(message)
                                         .errorDetail(DEFAULT_ERROR_MESSAGE)
                                         .exceptionDate(Instant.now())
                                         .build();
    }

    public JobException(final String message, final Throwable cause) {
        super(message, cause);
        jobErrorDetails = JobErrorDetails.builder()
                                         .exception(cause.getMessage())
                                         .errorDetail(message)
                                         .exceptionDate(Instant.now())
                                         .build();
    }
}
