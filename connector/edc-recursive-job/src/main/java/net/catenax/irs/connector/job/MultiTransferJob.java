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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

/**
 * Entity for recursive jobs that potentially comprise multiple transfers.
 */
@ToString
@Builder(toBuilder = true)
public class MultiTransferJob {

    /**
     * Job identifier.
     */
    @Getter
    private final String jobId;
    /**
     * Collection of transfer IDs that have not yet completed for the job.
     */
    @Singular
    private final Set<String> transferProcessIds;
    /**
     * Job state.
     */
    @Getter
    private JobState state;
    /**
     * Arbitrary data attached to the job.
     */
    @Getter
    @Singular("jobDatum")
    private Map<String, String> jobData;
    /**
     * Error detail, potentially set if {@link #getState() state} is {@link JobState#ERROR}.
     */
    @Getter
    private String errorDetail;
    /**
     * Collection of transfers that have completed for the job.
     */
    @Getter
    @Singular
    private List<TransferProcess> completedTransfers;
    /**
     * Sets completion date for jobs with {@link JobState#COMPLETED} and {@link JobState#ERROR} state.
     */
    @Getter
    private Optional<LocalDateTime> completionDate;

    /* package */ Collection<String> getTransferProcessIds() {
        return Collections.unmodifiableSet(this.transferProcessIds);
    }

    /**
     * Builder for {@link MultiTransferJob}.
     */
    public static class MultiTransferJobBuilder {
        /**
         * Transition the job to the {@link JobState#INITIAL} state.
         */
        /* package */ MultiTransferJobBuilder transitionInitial() {
            return transition(JobState.INITIAL, JobState.UNSAVED);
        }

        /**
         * Transition the job to the {@link JobState#IN_PROGRESS} state.
         */
        /* package */ MultiTransferJobBuilder transitionInProgress() {
            return transition(JobState.IN_PROGRESS, JobState.INITIAL, JobState.IN_PROGRESS);
        }

        /**
         * Transition the job to the {@link JobState#TRANSFERS_FINISHED} state.
         */
        /* package */ MultiTransferJobBuilder transitionTransfersFinished() {
            return transition(JobState.TRANSFERS_FINISHED, JobState.IN_PROGRESS);
        }

        /**
         * Transition the job to the {@link JobState#COMPLETED} state.
         */
        /* package */ MultiTransferJobBuilder transitionComplete() {
            return transition(JobState.COMPLETED, JobState.TRANSFERS_FINISHED, JobState.INITIAL)
                  .completionDate(Optional.of(LocalDateTime.now()));
        }

        /**
         * Transition the job to the {@link JobState#ERROR} state.
         */
        /* package */ MultiTransferJobBuilder transitionError(final @Nullable String errorDetail) {
            this.state = JobState.ERROR;
            this.completionDate = Optional.of(LocalDateTime.now());
            this.errorDetail = errorDetail;
            return this;
        }

        /**
         * Transition the job to the {@link JobState#CANCELED} state.
         */
        /* package */ MultiTransferJobBuilder transitionCancel() {
            return transition(JobState.CANCELED, JobState.UNSAVED, JobState.INITIAL, JobState.IN_PROGRESS);
        }

        private MultiTransferJobBuilder transition(final JobState end, final JobState... starts) {
            if (Arrays.stream(starts).noneMatch(s -> s == state)) {
                throw new IllegalStateException(format("Cannot transition from state %s to %s", state, end));
            }
            this.state = end;
            return this;
        }
    }
}
