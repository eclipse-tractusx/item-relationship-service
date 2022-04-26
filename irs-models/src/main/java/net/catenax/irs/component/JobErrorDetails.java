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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Exception container for job
 */
@Getter
@Setter
@Builder(toBuilder = true)
@JsonDeserialize(builder = JobErrorDetails.JobErrorDetailsBuilder.class)
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"PMD.ShortClassName", "PMD.MethodArgumentCouldBeFinal"})
public class JobErrorDetails {

    public static final int EXCEPTION_NAME_MAX_LENGTH = 100;
    public static final int ERROR_DETAIL_MAX_LENGTH = 4000;

    @Schema(description = "Name of the exception occurred.", implementation = String.class,
            maxLength = EXCEPTION_NAME_MAX_LENGTH)
    private String exception;

    @Schema(description = "Detail information for the error occurred.", implementation = String.class,
            maxLength = ERROR_DETAIL_MAX_LENGTH)
    private String errorDetail;

    @Schema(description = "Datetime when error occurred.", implementation = Instant.class)
    private Instant exceptionDate;

    @Override
    public String toString() {
        return "JobErrorDetails{" + "exception='" + exception + '\''
                + ", errorDetail='" + errorDetail + '\''
                + ", exceptionDate=" + exceptionDate
                + '}';
    }

    /**
     * Builder class
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static final class JobErrorDetailsBuilder {

        private final JobErrorDetails errorDetails;

        private JobErrorDetailsBuilder() {
            errorDetails = new JobErrorDetails();
        }

        @JsonCreator
        public static JobErrorDetailsBuilder instance() {
            return new JobErrorDetailsBuilder();
        }

        public JobErrorDetailsBuilder exception(String exception) {
            errorDetails.exception = exception;
            return this;
        }

        public JobErrorDetailsBuilder errorDetail(String errorDetail) {
            errorDetails.errorDetail = errorDetail;
            return this;
        }

        public JobErrorDetailsBuilder exceptionDate(Instant exceptionDate) {
            errorDetails.exceptionDate = exceptionDate;
            return this;
        }

        public JobErrorDetails build() {
            return this.errorDetails;
        }

    }

}
