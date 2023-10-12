/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.connector.job;

import static java.lang.String.format;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobErrorDetails;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.enums.IntegrityState;
import org.eclipse.tractusx.irs.component.enums.JobState;
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

    @Getter
    private Optional<UUID> batchId;

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
        return job.getId();
    }

    @JsonIgnore
    public String getJobIdString() {
        return getJobId().toString();
    }

    @JsonIgnore
    public String getGlobalAssetId() {
        return getJob().getGlobalAssetId().getGlobalAssetId();
    }

    @JsonIgnore
    public JobParameter getJobParameter() {
        return getJob().getParameter();
    }

    public boolean jobIsCompleted() {
        return this.getJob().getState().equals(JobState.COMPLETED);
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
        /* package */ MultiTransferJobBuilder transitionComplete(final IntegrityState integrityState) {
            return transition(JobState.COMPLETED, JobState.TRANSFERS_FINISHED, JobState.INITIAL).job(
                    job.toBuilder().completedOn(ZonedDateTime.now(ZoneOffset.UTC)).integrityState(integrityState).build());
        }

        /**
         * Transition the job to the {@link JobState#ERROR} state.
         */
        /* package */ MultiTransferJobBuilder transitionError(final @Nullable String errorDetail,
                final String exceptionClassName) {
            this.job = this.job.toBuilder()
                               .state(JobState.ERROR)
                               .completedOn(ZonedDateTime.now(ZoneOffset.UTC))
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
            if (Arrays.stream(starts).noneMatch(s -> s == job.getState())) {
                throw new IllegalStateException(
                        format("Cannot transition from state %s to %s", job.getState(), end));
            }
            log.info("Transitioning job {} from {} to {}", job.getId().toString(), job.getState(), end);
            job = job.toBuilder().state(end).lastModifiedOn(ZonedDateTime.now(ZoneOffset.UTC)).build();
            return this;
        }
    }
}
