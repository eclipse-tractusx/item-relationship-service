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

import static net.catenax.irs.controllers.IrsAppConstants.JOB_EXECUTION_FAILED;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.dto.JobParameter;
import net.catenax.irs.services.SecurityHelperService;
import org.apache.commons.lang3.StringUtils;
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

    private static final int TTL_CLEANUP_COMPLETED_JOBS_HOURS = 1;

    private static final int TTL_CLEANUP_FAILED_JOBS_HOURS = 24;

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
     * Helper for retrieving data from JWT token
     */
    private final SecurityHelperService securityHelperService;

    /**
     * Create a new instance of {@link JobOrchestrator}.
     *
     * @param processManager the process manager
     * @param jobStore       Job store.
     * @param handler        Recursive job handler.
     */
    public JobOrchestrator(final TransferProcessManager<T, P> processManager, final JobStore jobStore,
            final RecursiveJobHandler<T, P> handler) {

        this.processManager = processManager;
        this.jobStore = jobStore;
        this.handler = handler;
        this.securityHelperService = new SecurityHelperService();
    }

    /**
     * Start a job.
     *
     * @param jobData additional data for the job to be managed by the {@link JobStore}.
     * @return response.
     */
    public JobInitiateResponse startJob(final JobParameter jobData) {
        final Job job = createJob(jobData.getRootItemId(), jobData);
        final var multiJob = MultiTransferJob.builder().job(job).jobParameter(jobData).build();
        jobStore.create(multiJob);

        final Stream<T> requests;
        try {
            requests = handler.initiate(multiJob);
        } catch (RuntimeException e) {
            markJobInError(multiJob, e, JOB_EXECUTION_FAILED);
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
            callCompleteHandlerIfFinished(multiJob.getJobIdString());
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

        if (job.getJob().getJobState() != JobState.RUNNING) {
            log.info("Ignoring transfer complete event for job {} in state {} ", job.getJob().getJobId(),
                    job.getJob().getJobState());
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

    @Scheduled(cron = "${irs.job.cleanup.scheduler.completed}")
    public void findAndCleanupCompletedJobs() {
        log.info("Running cleanup of completed jobs");
        final ZonedDateTime currentDateMinusSeconds = ZonedDateTime.now(ZoneOffset.UTC)
                                                                   .minus(TTL_CLEANUP_COMPLETED_JOBS_HOURS,
                                                                           ChronoUnit.HOURS);
        final List<MultiTransferJob> completedJobs = jobStore.findByStateAndCompletionDateOlderThan(JobState.COMPLETED,
                currentDateMinusSeconds);

        final List<MultiTransferJob> multiTransferJobs = deleteJobs(completedJobs);
        log.info("Deleted {} completed jobs", multiTransferJobs.size());
    }

    @Scheduled(cron = "${irs.job.cleanup.scheduler.failed}")
    public void findAndCleanupFailedJobs() {
        log.info("Running cleanup of failed jobs");

        final ZonedDateTime currentDateMinusSeconds = ZonedDateTime.now(ZoneOffset.UTC)
                                                                   .minus(TTL_CLEANUP_FAILED_JOBS_HOURS,
                                                                           ChronoUnit.HOURS);
        final List<MultiTransferJob> failedJobs = jobStore.findByStateAndCompletionDateOlderThan(JobState.ERROR,
                currentDateMinusSeconds);

        final List<MultiTransferJob> multiTransferJobs = deleteJobs(failedJobs);
        log.info("Deleted {} failed jobs", multiTransferJobs.size());
    }

    private List<MultiTransferJob> deleteJobs(final List<MultiTransferJob> jobs) {
        return jobs.stream()
                   .map(job -> jobStore.deleteJob(job.getJobIdString()))
                   .flatMap(Optional::stream)
                   .collect(Collectors.toList());
    }

    private void callCompleteHandlerIfFinished(final String jobId) {
        jobStore.completeJob(jobId, this::completeJob);
    }

    private void completeJob(final MultiTransferJob job) {
        try {
            handler.complete(job);
        } catch (RuntimeException e) {
            markJobInError(job, e, JOB_EXECUTION_FAILED);
        }
    }

    private void markJobInError(final MultiTransferJob job, final Throwable exception, final String message) {
        log.error(message, exception);
        jobStore.markJobInError(job.getJobIdString(), message, exception.getClass().getName());
    }

    private long startTransfers(final MultiTransferJob job, final Stream<T> dataRequests) /* throws JobErrorDetails */ {
        return dataRequests.map(r -> startTransfer(job, r)).collect(Collectors.counting());
    }

    private TransferInitiateResponse startTransfer(final MultiTransferJob job,
            final T dataRequest)  /* throws JobErrorDetails */ {
        final JobParameter jobData = job.getJobParameter();

        final var response = processManager.initiateRequest(dataRequest,
                transferId -> jobStore.addTransferProcess(job.getJobIdString(), transferId),
                this::transferProcessCompleted, jobData);

        if (response.getStatus() != ResponseStatus.OK) {
            throw new JobException(response.getStatus().toString());
        }

        return response;
    }

    private Job createJob(final String globalAssetId, final JobParameter jobData) {
        if (StringUtils.isEmpty(globalAssetId)) {
            throw new JobException("GlobalAsset Identifier cannot be null or empty string");
        }

        return Job.builder()
                  .jobId(UUID.randomUUID())
                  .globalAssetId(GlobalAssetIdentification.builder().globalAssetId(globalAssetId).build())
                  .createdOn(ZonedDateTime.now(ZoneOffset.UTC))
                  .lastModifiedOn(ZonedDateTime.now(ZoneOffset.UTC))
                  .jobState(JobState.UNSAVED)
                  .owner(securityHelperService.getClientIdClaim())
                  .jobParameter(buildJobParameter(jobData))
                  .build();
    }

    @SuppressWarnings("PMD.NullAssignment")
    private net.catenax.irs.component.JobParameter buildJobParameter(final JobParameter jobData) {
        return net.catenax.irs.component.JobParameter.builder()
                                                     .depth(jobData.getTreeDepth())
                                                     .direction(Direction.DOWNWARD)
                                                     .aspects(jobData.getAspectTypes()
                                                                     .stream()
                                                                     .map(AspectType::fromValue)
                                                                     .collect(Collectors.toList()))
                                                     .bomLifecycle(StringUtils.isNotBlank(jobData.getBomLifecycle())
                                                             ? BomLifecycle.fromLifecycleContextCharacteristic(
                                                             jobData.getBomLifecycle())
                                                             : null)
                                                     .collectAspects(jobData.isCollectAspects())
                                                     .build();
    }

    private ResponseStatus convertMessage(final String message) {
        return ResponseStatus.valueOf(message);
    }

}
