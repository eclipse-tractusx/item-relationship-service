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

import static net.catenax.irs.dtos.ValidationConstants.INPUT_FIELD_MIN_LENGTH;
import static net.catenax.irs.dtos.ValidationConstants.JOB_ID_FIELD_MAX_LENGTH;

import java.net.URL;
import java.time.Instant;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.component.enums.JobState;

/**
 * A job to retrieve item relationship data.
 */
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = Job.JobBuilder.class)
@AllArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
@ExcludeFromCodeCoverageGeneratedReport
public class Job {

    /**
     * jobId
     */
    @NotNull
    @Size(min = INPUT_FIELD_MIN_LENGTH, max = JOB_ID_FIELD_MAX_LENGTH)
    @Schema(description = "Job Id for the request Item", minLength = INPUT_FIELD_MIN_LENGTH,
            maxLength = JOB_ID_FIELD_MAX_LENGTH, implementation = GlobalAssetIdentification.class)
    private final GlobalAssetIdentification jobId;

    /**
     * globalAssetId
     */
    @NotNull
    @Size(min = INPUT_FIELD_MIN_LENGTH, max = JOB_ID_FIELD_MAX_LENGTH)
    @Schema(description = "Part global unique Id", minLength = INPUT_FIELD_MIN_LENGTH,
            maxLength = JOB_ID_FIELD_MAX_LENGTH, implementation = GlobalAssetIdentification.class)
    private final GlobalAssetIdentification globalAssetId;

    @NotBlank
    @Schema()
    private JobState jobState;

    @Schema(description = "Exception state for this job", implementation = JobException.class)
    private JobException jobException;

    /**
     * Timestamp when the job was created
     */
    private Instant createdOn;

    /**
     * Last time job was modified
     */
    private Instant lastModifiedOn;

    /**
     * Mark the time the was completed
     */
    private Instant jobFinished;

    /**
     * Url of request that resulted to this job
     */
    @Schema(implementation = URL.class)
    private URL requestUrl;

    /**
     * Http method, only GET is supported
     */
    @Schema(description = "HTTP verbs used by request")
    private String action;

    /**
     * Owner of the job
     */
    @Schema(description = "The requestor of the request")
    private String owner;

    @Schema(description = "List of asyncFetchedItems")
    private Summary summary;


    /**
     * Builder class
     */
    @JsonPOJOBuilder(withPrefix = "with")
    public static class JobBuilder {
    }

}
