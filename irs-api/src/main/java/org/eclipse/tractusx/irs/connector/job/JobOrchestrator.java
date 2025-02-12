/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
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

import static org.eclipse.tractusx.irs.controllers.IrsAppConstants.JOB_EXECUTION_FAILED;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.common.JobProcessingFinishedEvent;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.services.MeterRegistryService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Orchestrator service for recursive {@link MultiTransferJob}s that potentially
 * comprise multiple transfers.
 */
@SuppressWarnings({ "PMD.AvoidCatchingGenericException",
                    "PMD.TooManyMethods"
})
// Handle RuntimeException from callbacks
@Slf4j
public class JobOrchestrator<T extends DataRequest, P extends TransferProcess> {

    /**
     * Transfer process manager.
     */
    private final TransferProcessManager<T, P> processManager;

    /**
     * Job store.
     */
    private final JobStore jobStore;

    /**
     * Job handler containing the logic to start transfers and process
     * transfer results.
     */
    private final RecursiveJobHandler<T, P> handler;

    /**
     * Use to collect metrics
     */
    private final MeterRegistryService meterService;

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Time to live for jobs
     */
    private final JobTTL jobTTL;

    private final Object jobCompletionSyncObject = new Object();

    /**
     * Create a new instance of {@link JobOrchestrator}.
     *
     * @param processManager            the process manager
     * @param jobStore                  Job store.
     * @param handler                   Recursive job handler.
     * @param meterService              Collect metrics for monitoring
     * @param applicationEventPublisher publisher of spring application events
     * @param jobTTL                    time to live for jobs
     */
    public JobOrchestrator(final TransferProcessManager<T, P> processManager, final JobStore jobStore,
            final RecursiveJobHandler<T, P> handler, final MeterRegistryService meterService,
            final ApplicationEventPublisher applicationEventPublisher, final JobTTL jobTTL) {
        this.processManager = processManager;
        this.jobStore = jobStore;
        this.handler = handler;
        this.meterService = meterService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.jobTTL = jobTTL;
    }

    /**
     * Start a job with Batch
     *
     * @param identificationKey root id
     * @param jobData           additional data for the job to be managed by the {@link JobStore}.
     * @param batchId           batch id
     * @return response.
     */
    public JobInitiateResponse startJob(final PartChainIdentificationKey identificationKey, final JobParameter jobData, final UUID batchId) {
        final Job job = createJob(identificationKey, jobData);
        final var multiJob = MultiTransferJob.builder().job(job).batchId(Optional.ofNullable(batchId)).build();
        jobStore.create(multiJob);

        final Stream<T> requests;
        try {
            requests = handler.initiate(multiJob);
        } catch (RuntimeException e) {
            markJobInError(multiJob, e, JOB_EXECUTION_FAILED);
            meterService.incrementJobFailed();
            return JobInitiateResponse.builder()
                                      .jobId(multiJob.getJobIdString())
                                      .status(ResponseStatus.FATAL_ERROR)
                                      .build();
        }

        long transferCount;
        try {
            transferCount = startTransfers(multiJob, requests);
        } catch (JobException e) {
            return JobInitiateResponse.builder()
                                      .jobId(multiJob.getJobIdString())
                                      .status(convertMessage(e.getJobErrorDetails().getException()))
                                      .build();
        }

        // If no transfers are requested, job is already complete
        if (transferCount == 0) {
            try {
                callCompleteHandlerIfFinished(multiJob.getJobIdString());
            } catch (JobException e) {
                markJobInError(multiJob, e, "Error while completing Job.");
                meterService.incrementJobFailed();
                return JobInitiateResponse.builder()
                                          .jobId(multiJob.getJobIdString())
                                          .status(ResponseStatus.FATAL_ERROR)
                                          .error(e.getMessage())
                                          .build();
            }
        }

        return JobInitiateResponse.builder().jobId(multiJob.getJobIdString()).status(ResponseStatus.OK).build();
    }

    /**
     * Callback invoked when a transfer has completed.
     *
     * @param process the process that has completed
     */
    /* package */ void transferProcessCompleted(final P process) {
        final var jobEntry = jobStore.findByProcessId(process.getId());
        if (jobEntry.isEmpty()) {
            log.error("Job not found for transfer {}", process.getId());
            return;
        }
        final var job = jobEntry.get();

        if (job.getJob().getState() != JobState.RUNNING) {
            log.info("Ignoring transfer complete event for job {} in state {} ", job.getJob().getId(),
                    job.getJob().getState());
            return;
        }

        final Stream<T> requests;
        try {
            requests = handler.recurse(job, process);
        } catch (RuntimeException e) {
            markJobInError(job, e, JOB_EXECUTION_FAILED);
            return;
        }

        try {
            final long transfersStarted = startTransfers(job, requests);
            log.info("Started {} new transfers", transfersStarted);
        } catch (JobException e) {
            markJobInError(job, e, "Failed to start a transfer");
            return;
        }

        jobStore.completeTransferProcess(job.getJobIdString(), process);

        callCompleteHandlerIfFinished(job.getJobIdString());
    }

    @IrsTimer("cleancompletedjobs")
    @Scheduled(cron = "${irs.job.cleanup.scheduler.completed}")
    public void findAndCleanupCompletedJobs() {
        log.info("Running cleanup of completed jobs");
        final ZonedDateTime currentDateMinusSeconds = ZonedDateTime.now(ZoneOffset.UTC)
                                                                   .minus(jobTTL.getTtlCompletedJobs());
        final List<MultiTransferJob> completedJobs = jobStore.findByStateAndCompletionDateOlderThan(JobState.COMPLETED,
                currentDateMinusSeconds);

        final List<MultiTransferJob> multiTransferJobs = deleteJobs(completedJobs);
        log.info("Deleted {} completed jobs", multiTransferJobs.size());
    }

    @IrsTimer("cleanfailedjobs")
    @Scheduled(cron = "${irs.job.cleanup.scheduler.failed}")
    public void findAndCleanupFailedJobs() {
        log.info("Running cleanup of failed jobs");

        final ZonedDateTime currentDateMinusSeconds = ZonedDateTime.now(ZoneOffset.UTC)
                                                                   .minus(jobTTL.getTtlFailedJobs());
        final List<MultiTransferJob> failedJobs = jobStore.findByStateAndCompletionDateOlderThan(JobState.ERROR,
                currentDateMinusSeconds);

        final List<MultiTransferJob> multiTransferJobs = deleteJobs(failedJobs);
        log.info("Deleted {} failed jobs", multiTransferJobs.size());
    }

    private List<MultiTransferJob> deleteJobs(final List<MultiTransferJob> jobs) {
        final List<MultiTransferJob> deletedJobs = jobs.stream()
                                                       .map(job -> jobStore.deleteJob(job.getJobIdString()))
                                                       .flatMap(Optional::stream)
                                                       .toList();
        meterService.setNumberOfJobsInJobStore((long) jobStore.findAll().size());
        return deletedJobs;
    }

    private void callCompleteHandlerIfFinished(final String jobId) {
        synchronized (jobCompletionSyncObject) {
            jobStore.completeJob(jobId, this::completeJob);
            publishJobProcessingFinishedEventIfFinished(jobId);
        }
    }

    private void completeJob(final MultiTransferJob job) {
        try {
            handler.complete(job);
        } catch (RuntimeException e) {
            meterService.incrementJobFailed();
            markJobInError(job, e, JOB_EXECUTION_FAILED);
        }
    }

    private void markJobInError(final MultiTransferJob job, final Throwable exception, final String message) {
        log.error(message, exception);
        jobStore.markJobInError(job.getJobIdString(), message, exception.getClass().getName());
        publishJobProcessingFinishedEventIfFinished(job.getJobIdString());
    }

    private void publishJobProcessingFinishedEventIfFinished(final String jobId) {
        jobStore.find(jobId).ifPresentOrElse(job -> {
            if (JobState.COMPLETED.equals(job.getJob().getState()) || JobState.ERROR.equals(job.getJob().getState())) {
                log.info("Publishing JobProcessingFinishedEvent for job '{}' with status '{}'.", job.getJobIdString(),
                        job.getJob().getState());
                applicationEventPublisher.publishEvent(
                        new JobProcessingFinishedEvent(job.getJobIdString(), job.getJob().getState().name(),
                                job.getJobParameter().getCallbackUrl(), job.getBatchId()));
            } else {
                log.warn("Could not publish JobProcessingFinishedEvent. Job '{}' not in state COMPLETED or ERROR.",
                        jobId);
            }
        }, () -> log.warn("Could not publish JobProcessingFinishedEvent. Job '{}' not present.", jobId));
    }

    private long startTransfers(final MultiTransferJob job, final Stream<T> dataRequests) /* throws JobErrorDetails */ {
        return dataRequests.map(r -> startTransfer(job, r)).toList().size();
    }

    private TransferInitiateResponse startTransfer(final MultiTransferJob job,
            final T dataRequest)  /* throws JobErrorDetails */ {
        final JobParameter jobData = job.getJobParameter();
        final String jobId = job.getJobIdString();
        final var response = processManager.initiateRequest(dataRequest,
                transferId -> jobStore.addTransferProcess(job.getJobIdString(), transferId),
                this::transferProcessCompleted, jobData, jobId);

        if (response.getStatus() != ResponseStatus.OK) {
            throw new JobException(response.getStatus().toString());
        }

        return response;
    }

    private Job createJob(final PartChainIdentificationKey identificationKey, final JobParameter jobData) {
        final Job.JobBuilder jobBuilder = Job.builder()
                                             .id(UUID.randomUUID())
                                             .createdOn(ZonedDateTime.now(ZoneOffset.UTC))
                                             .lastModifiedOn(ZonedDateTime.now(ZoneOffset.UTC))
                                             .state(JobState.UNSAVED)
                                             .parameter(jobData);

        if (StringUtils.isEmpty(identificationKey.getGlobalAssetId())) {
            jobBuilder.aasIdentifier(identificationKey.getIdentifier());
        } else {
            jobBuilder.globalAssetId(GlobalAssetIdentification.of(identificationKey.getGlobalAssetId()));
        }

        return jobBuilder.build();
    }

    private ResponseStatus convertMessage(final String message) {
        return ResponseStatus.valueOf(message);
    }

}
