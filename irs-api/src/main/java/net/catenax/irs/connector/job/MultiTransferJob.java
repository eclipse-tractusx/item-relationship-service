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

import static java.lang.String.format;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobErrorDetails;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.dto.JobParameter;
import org.jetbrains.annotations.Nullable;

/**
 * Entity for recursive jobs that potentially comprise multiple transfers.
 */
@ToString
@Builder(toBuilder = true)
@Slf4j
@JsonDeserialize(builder = MultiTransferJob.MultiTransferJobBuilder.class)
public class MultiTransferJob {

    /**
     * Collection of transfer IDs that have not yet completed for the job.
     */
    @Singular
    private final Set<String> transferProcessIds;
    /**
     * The attached job.
     */

    @NonNull
    @Getter
    private Job job;
    /**
     * Arbitrary data attached to the job.
     */

    @Getter
    private JobParameter jobParameter;

    /**
     * Collection of transfers that have completed for the job.
     */
    @Getter
    @Singular
    private List<TransferProcess> completedTransfers;

    public Collection<String> getTransferProcessIds() {
        return Collections.unmodifiableSet(this.transferProcessIds);
    }

    @JsonIgnore
    public UUID getJobId() {
        return job.getJobId();
    }

    /**
     * Builder for {@link MultiTransferJob}.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class MultiTransferJobBuilder {
        /**
         * Transition the job to the {@link JobState#INITIAL} state.
         */
        /* package */ MultiTransferJobBuilder transitionInitial() {
            return transition(JobState.INITIAL, JobState.UNSAVED).job(
                    job.toBuilder().startedOn(ZonedDateTime.now(ZoneOffset.UTC)).build());
        }

        /**
         * Transition the job to the {@link JobState#RUNNING} state.
         */
        /* package */ MultiTransferJobBuilder transitionInProgress() {
            return transition(JobState.RUNNING, JobState.INITIAL, JobState.RUNNING);
        }

        /**
         * Transition the job to the {@link JobState#TRANSFERS_FINISHED} state.
         */
        /* package */ MultiTransferJobBuilder transitionTransfersFinished() {
            return transition(JobState.TRANSFERS_FINISHED, JobState.RUNNING);
        }

        /**
         * Transition the job to the {@link JobState#COMPLETED} state.
         */
        /* package */ MultiTransferJobBuilder transitionComplete() {
            return transition(JobState.COMPLETED, JobState.TRANSFERS_FINISHED, JobState.INITIAL).job(
                    job.toBuilder().jobCompleted(ZonedDateTime.now(ZoneOffset.UTC)).build());
        }

        /**
         * Transition the job to the {@link JobState#ERROR} state.
         */
        /* package */ MultiTransferJobBuilder transitionError(final @Nullable String errorDetail,
                final String exceptionClassName) {
            this.job = this.job.toBuilder()
                               .jobState(JobState.ERROR)
                               .jobCompleted(ZonedDateTime.now(ZoneOffset.UTC))
                               .lastModifiedOn(ZonedDateTime.now(ZoneOffset.UTC))
                               .exception(JobErrorDetails.builder()
                                                         .errorDetail(errorDetail)
                                                         .exception(exceptionClassName)
                                                         .exceptionDate(ZonedDateTime.now(ZoneOffset.UTC))
                                                         .build())
                               .build();
            return this;
        }

        /**
         * Transition the job to the {@link JobState#CANCELED} state.
         */
        /* package */ MultiTransferJobBuilder transitionCancel() {
            return transition(JobState.CANCELED, JobState.UNSAVED, JobState.INITIAL, JobState.RUNNING);
        }

        private MultiTransferJobBuilder transition(final JobState end, final JobState... starts) {
            if (Arrays.stream(starts).noneMatch(s -> s == job.getJobState())) {
                throw new IllegalStateException(
                        format("Cannot transition from state %s to %s", job.getJobState(), end));
            }
            log.info("Transitioning job {} from {} to {}", job.getJobId().toString(), job.getJobState(), end);
            job = job.toBuilder().jobState(end).lastModifiedOn(ZonedDateTime.now(ZoneOffset.UTC)).build();
            return this;
        }
    }
}
