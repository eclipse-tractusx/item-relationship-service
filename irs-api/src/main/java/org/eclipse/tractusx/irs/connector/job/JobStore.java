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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.tractusx.irs.component.enums.IntegrityState;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.jetbrains.annotations.Nullable;

/**
 * Manages storage of {@link MultiTransferJob} state.
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface JobStore {
    /**
     * Retrieve a job by its identifier.
     *
     * @param jobId the identifier of the job to retrieve.
     * @return the job if found, otherwise empty.
     * @see MultiTransferJob#getJob()
     */
    Optional<MultiTransferJob> find(String jobId);

    /**
     * Retrieve jobs by state with completion date older than requested date
     *
     * @param jobState the job state
     * @param dateTime requested date
     * @return found jobs
     */
    List<MultiTransferJob> findByStateAndCompletionDateOlderThan(JobState jobState, ZonedDateTime dateTime);

    /**
     * Retrieve jobs with requested states
     *
     * @param jobStates requested job states
     * @return found jobs
     */
    List<MultiTransferJob> findByStates(List<JobState> jobStates);

    /**
     * Retrieve a job given a transfer id. Only retrieves jobs
     * for which the transfer has not been completed
     * with {@link #completeTransferProcess(String, TransferProcess)}.
     *
     * @param processId the transfer process identifier.
     * @return the job if found, otherwise empty.
     */
    Optional<MultiTransferJob> findByProcessId(String processId);

    /**
     * Create a job.
     *
     * @param job the job to create and manage.
     */
    void create(MultiTransferJob job);

    /**
     * Add a transfer process identifier to a job.
     *
     * @param jobId     the job identifier.
     * @param processId identifier of the transfer process to attach.
     */
    void addTransferProcess(String jobId, String processId);

    /**
     * Mark transfer process completed for the job.
     *
     * @param jobId   the job identifier.
     * @param process transfer process to mark completed.
     */
    void completeTransferProcess(String jobId, TransferProcess process);

    /**
     * Mark job as completed.
     *
     * @param jobId            the job identifier.
     * @param completionAction the action to perform before marking the job as complete
     * @see JobState#COMPLETED
     */
    void completeJob(String jobId, Function<MultiTransferJob, IntegrityState> completionAction);

    /**
     * Mark job as in error.
     *
     * @param jobId       the job identifier.
     * @param errorDetail an optional error message.
     * @param exceptionClassName name of the exception class
     * @see JobState#ERROR
     */
    void markJobInError(String jobId, @Nullable String errorDetail, String exceptionClassName);

    /**
     * Delete a job by its identifier.
     *
     * @param jobId the job identifier.
     * @return deleted job (if it existed)
     */
    Optional<MultiTransferJob> deleteJob(String jobId);

    /**
     * Cancel the job with identifier
     *
     * @param jobId
     * @return cancel job (if it existed)
     */
    Optional<MultiTransferJob> cancelJob(String jobId);

    /**
     * Retrieve all jobs
     *
     * @return jobs if found, otherwise empty.
     * @see MultiTransferJob#getJob()
     */
    List<MultiTransferJob> findAll();
}
