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

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.component.enums.JobState;

/**
 * A job to retrieve item relationship data.
 */
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@ToString
@Jacksonized
@SuppressWarnings("PMD.ShortClassName")
public class Job {

    private static final int INPUT_FIELD_MIN_LENGTH = 36;
    private static final int JOB_ID_FIELD_MAX_LENGTH = 36;

    @NotNull
    @Size(min = INPUT_FIELD_MIN_LENGTH, max = JOB_ID_FIELD_MAX_LENGTH)
    @Schema(description = "JobId of the job.", minLength = INPUT_FIELD_MIN_LENGTH,
            maxLength = JOB_ID_FIELD_MAX_LENGTH, implementation = UUID.class)
    private UUID jobId;

    @NotNull
    @Size(min = INPUT_FIELD_MIN_LENGTH, max = JOB_ID_FIELD_MAX_LENGTH)
    @Schema(description = "Part global unique Id", minLength = INPUT_FIELD_MIN_LENGTH,
            maxLength = JOB_ID_FIELD_MAX_LENGTH, implementation = GlobalAssetIdentification.class)
    @JsonUnwrapped
    private GlobalAssetIdentification globalAssetId;

    @NotBlank
    private JobState jobState;

    @Schema(description = "Job error details.", implementation = JobErrorDetails.class)
    private JobErrorDetails exception;

    /**
     * Timestamp when the job was created
     */
    @Schema(implementation = ZonedDateTime.class)
    private ZonedDateTime createdOn;

    /**
     * Timestamp when the job was started
     */
    @Schema(implementation = ZonedDateTime.class)
    private ZonedDateTime startedOn;

    /**
     * Last time job was modified
     */
    @Schema(implementation = ZonedDateTime.class)
    private ZonedDateTime lastModifiedOn;

    /**
     * Mark the time the was completed
     */
    @Schema(implementation = ZonedDateTime.class)
    private ZonedDateTime jobCompleted;

    /**
     * Owner of the job
     */
    @Schema(description = "The IRS api consumer.")
    private String owner;

    @Schema(description = "Summary of the job with statistics of the job processing.", implementation = Summary.class)
    private Summary summary;

    @Schema(description = "The passed job parameters", implementation = JobParameter.class)
    private JobParameter jobParameter;

}
