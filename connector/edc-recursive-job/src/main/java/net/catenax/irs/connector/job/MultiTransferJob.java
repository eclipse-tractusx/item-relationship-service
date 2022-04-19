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

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobException;
import net.catenax.irs.component.enums.JobState;
import org.jetbrains.annotations.Nullable;

/**
 * Entity for recursive jobs that potentially comprise multiple transfers.
 */
@ToString
@Builder(toBuilder = true)
@SuppressWarnings({ "PMD.UselessParentheses" })
public class MultiTransferJob {

    /**
     * The attached job.
     */
    @NonNull
    @Getter
    private Job job;

    /**
     * Collection of transfer IDs that have not yet completed for the job.
     */
    @Singular
    private final Set<String> transferProcessIds;

    /**
     * Arbitrary data attached to the job.
     */
    @Getter
    @Singular("jobDatum")
    private Map<String, String> jobData;

    /**
     * Collection of transfers that have completed for the job.
     */
    @Getter
    @Singular
    private List<TransferProcess> completedTransfers;

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
            return transition(JobState.COMPLETED, JobState.TRANSFERS_FINISHED, JobState.INITIAL).job(
                    job.toBuilder().jobCompleted((Instant.now())).build());
        }

        /**
         * Transition the job to the {@link JobState#ERROR} state.
         */
        /* package */ MultiTransferJobBuilder transitionError(final @Nullable String errorDetail) {
            this.job.setJobState(JobState.ERROR);
            this.job.setJobCompleted(Instant.now());
            this.job.setException(JobException.builder().errorDetail(errorDetail).exceptionDate(Instant.now()).build());
            return this;
        }

        private MultiTransferJobBuilder transition(final JobState end, final JobState... starts) {
            if (Arrays.stream(starts).noneMatch(s -> s == job.getJobState())) {
                throw new IllegalStateException(
                        format("Cannot transition from state %s to %s", job.getJobState(), end));
            }

            this.job.setJobState(end);
            return this;
        }
    }
}
