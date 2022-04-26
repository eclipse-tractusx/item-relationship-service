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
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.catenax.irs.component.enums.JobState;

/**
 * A job to retrieve item relationship data.
 */
@Getter
@Builder(toBuilder = true)
@JsonDeserialize(builder = Job.JobBuilder.class)
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"PMD.ShortClassName", "PMD.MethodArgumentCouldBeFinal", "PMD.TooManyMethods"})
public class Job {

    /**
     * jobId
     */
    @NotNull
    @Size(min = INPUT_FIELD_MIN_LENGTH, max = JOB_ID_FIELD_MAX_LENGTH)

    @Schema(description = "Job ID for the requested item.", minLength = INPUT_FIELD_MIN_LENGTH,
            maxLength = JOB_ID_FIELD_MAX_LENGTH, implementation = UUID.class)
    private UUID jobId;

    /**
     * globalAssetId
     */
    @NotNull
    @Size(min = INPUT_FIELD_MIN_LENGTH, max = JOB_ID_FIELD_MAX_LENGTH)
    @Schema(description = "Part global unique Id", minLength = INPUT_FIELD_MIN_LENGTH,
            maxLength = JOB_ID_FIELD_MAX_LENGTH, implementation = GlobalAssetIdentification.class)
    @JsonUnwrapped
    private GlobalAssetIdentification globalAssetId;

    @NotBlank
    @Setter
    @Schema()
    private JobState jobState;

    @Setter
    @Schema(description = "Exception state for this job.", implementation = JobErrorDetails.class)
    private JobErrorDetails exception;

    /**
     * Timestamp when the job was created
     */
    @Setter
    @Schema(implementation = Instant.class)
    private Instant createdOn;

    /**
     * Timestamp when the job was started
     */
    @Setter
    @Schema(implementation = Instant.class)
    private Instant startedOn;

    /**
     * Last time job was modified
     */
    @Setter
    @Schema(implementation = Instant.class)
    private Instant lastModifiedOn;

    /**
     * Mark the time the was completed
     */
    @Setter
    @Schema(implementation = Instant.class)
    private Instant jobCompleted;

    /**
     * Url of request that resulted to this job
     */
    @Setter
    @Schema(implementation = URL.class)
    private URL requestUrl;

    /**
     * Http method, only GET is supported
     */
    @Setter
    @Schema(description = "HTTP verbs used by request.")
    private String action;

    /**
     * Owner of the job
     */
    @Setter
    @Schema(description = "The requester of the request.")
    private String owner;

    @Setter
    @Schema(description = "Summary of the job", implementation = Summary.class)
    private Summary summary;

    @Setter
    @Schema(description = "The passed query parameters", implementation = QueryParameter.class)
    private QueryParameter queryParameter;

    @Override
    public String toString() {
        return "Job{" + "jobId=" + jobId
                + ", globalAssetId=" + globalAssetId
                + ", jobState=" + jobState
                + ", exception=" + exception
                + ", createdOn=" + createdOn
                + ", startedOn=" + startedOn
                + ", lastModifiedOn=" + lastModifiedOn
                + ", jobCompleted=" + jobCompleted
                + ", requestUrl=" + requestUrl
                + ", action='" + action + '\''
                + ", owner='" + owner + '\''
                + ", summary=" + summary
                + ", queryParameter=" + queryParameter
                + '}';
    }

    /**
     * Builder class
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static final class JobBuilder {
        private final Job job;

        private JobBuilder() {
            job = new Job();
        }

        @JsonCreator
        public static JobBuilder instance() {
            return new JobBuilder();
        }

        public JobBuilder jobId(String jobId) {
            job.jobId = UUID.fromString(jobId);
            return this;
        }

        public JobBuilder jobId(UUID jobId) {
            job.jobId = jobId;
            return this;
        }

        public JobBuilder globalAssetId(GlobalAssetIdentification globalAssetId) {
            job.globalAssetId = globalAssetId;
            return this;
        }

        public JobBuilder globalAssetId(String globalAssetId) {
            job.globalAssetId = GlobalAssetIdentification.builder().globalAssetId(globalAssetId).build();
            return this;
        }

        public JobBuilder jobState(JobState jobState) {
            job.jobState = jobState;
            return this;
        }

        public JobBuilder exception(JobErrorDetails exception) {
            job.exception = exception;
            return this;
        }

        public JobBuilder createdOn(Instant createdOn) {
            job.createdOn = createdOn;
            return this;
        }

        public JobBuilder startedOn(Instant startedOn) {
            job.startedOn = startedOn;
            return this;
        }

        public JobBuilder lastModifiedOn(Instant lastModifiedOn) {
            job.lastModifiedOn = lastModifiedOn;
            return this;
        }

        public JobBuilder jobCompleted(Instant jobCompleted) {
            job.jobCompleted = jobCompleted;
            return this;
        }

        public JobBuilder requestUrl(URL requestUrl) {
            job.requestUrl = requestUrl;
            return this;
        }

        public JobBuilder action(String action) {
            job.action = action;
            return this;
        }

        public JobBuilder owner(String owner) {
            job.owner = owner;
            return this;
        }

        public JobBuilder summary(Summary summary) {
            job.summary = summary;
            return this;
        }

        public JobBuilder queryParameter(QueryParameter queryParameter) {
            job.queryParameter = queryParameter;
            return this;
        }

        public Job build() {
            return job;
        }

    }
}
