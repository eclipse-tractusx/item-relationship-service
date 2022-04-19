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

import static net.catenax.irs.dtos.IrsCommonConstants.ROOT_ITEM_ID_KEY;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.enums.JobState;
import org.apache.commons.lang3.StringUtils;

/**
 * Orchestrator service for recursive {@link MultiTransferJob}s that potentially
 * comprise multiple transfers.
 */
@SuppressWarnings({ "PMD.AvoidCatchingGenericException", "PMD.TooManyMethods" })
// Handle RuntimeException from callbacks
@Slf4j
public class JobOrchestrator<T extends DataRequest, P extends TransferProcess> {

    private static final int TTL_CLEANUP_COMPLETED_JOBS_HOURS = 1;
    private static final int TTL_CLEANUP_COMPLETED_JOBS_SECONDS = TTL_CLEANUP_COMPLETED_JOBS_HOURS * 3600;

    private static final int TTL_CLEANUP_FAILED_JOBS_HOURS = 24;
    private static final int TTL_CLEANUP_FAILED_JOBS_SECONDS = TTL_CLEANUP_FAILED_JOBS_HOURS * 3600 * 24;

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
    }

    /**
     * Start a job.
     *
     * @param jobData additional data for the job to managed by the {@link JobStore}.
     * @return response.
     */
    public JobInitiateResponse startJob(final Map<String, String> jobData) {
        final Job job = createJob(jobData.get(ROOT_ITEM_ID_KEY));
        final var multiJob = MultiTransferJob.builder().job(job).jobData(jobData).build();
        jobStore.create(multiJob);

        final Stream<T> requests;
        try {
            requests = handler.initiate(multiJob);
        } catch (RuntimeException e) {
            markJobInError(multiJob, e, "Handler method failed");
            return JobInitiateResponse.builder()
                                      .jobId(multiJob.getJob().getJobId().toString())
                                      .status(ResponseStatus.FATAL_ERROR)
                                      .build();
        }

        long transferCount;
        try {
            transferCount = startTransfers(multiJob, requests);
        } catch (JobException e) {
            return JobInitiateResponse.builder()
                                      .jobId(multiJob.getJob().getJobId().toString())
                                      .status(e.getStatus())
                                      .build();
        }

        // If no transfers are requested, job is already complete
        if (transferCount == 0) {
            completeJob(multiJob);
        }

        return JobInitiateResponse.builder()
                                  .jobId(multiJob.getJob().getJobId().toString())
                                  .status(ResponseStatus.OK)
                                  .build();
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

        if (job.getJob().getJobState() != JobState.IN_PROGRESS) {
            log.info("Ignoring transfer complete event for job {} in state {} ", job.getJob().getJobId(),
                    job.getJob().getJobState());
            return;
        }

        final Stream<T> requests;
        try {
            requests = handler.recurse(job, process);
        } catch (RuntimeException e) {
            markJobInError(job, e, "Handler method failed");
            return;
        }

        try {
            startTransfers(job, requests);
        } catch (JobException e) {
            markJobInError(job, e, "Failed to start a transfer");
            return;
        }

        jobStore.completeTransferProcess(job.getJob().getJobId().toString(), process);

        callCompleteHandlerIfFinished(job.getJob().getJobId().toString());
    }

    public List<MultiTransferJob> findAndCleanupCompletedJobs() {
        final Instant currentDateMinusSeconds = Instant.now().minusSeconds(TTL_CLEANUP_COMPLETED_JOBS_SECONDS);
        final List<MultiTransferJob> completedJobs = jobStore.findByStateAndCompletionDateOlderThan(JobState.COMPLETED,
                currentDateMinusSeconds);

        return deleteJobs(completedJobs);
    }

    public List<MultiTransferJob> findAndCleanupFailedJobs() {
        final Instant currentDateMinusSeconds = Instant.now().minusSeconds(TTL_CLEANUP_FAILED_JOBS_SECONDS);
        final List<MultiTransferJob> failedJobs = jobStore.findByStateAndCompletionDateOlderThan(JobState.ERROR,
                currentDateMinusSeconds);
        return deleteJobs(failedJobs);
    }

    private List<MultiTransferJob> deleteJobs(final List<MultiTransferJob> jobs) {
        return jobs.stream()
                   .map(job -> jobStore.deleteJob(job.getJob().getJobId().toString()))
                   .collect(Collectors.toList());
    }

    private void callCompleteHandlerIfFinished(final String jobId) {
        jobStore.find(jobId).ifPresent(job -> {
            if (job.getJob().getJobState() != JobState.TRANSFERS_FINISHED) {
                return;
            }
            completeJob(job);
        });
    }

    private void completeJob(final MultiTransferJob job) {
        try {
            handler.complete(job);
        } catch (RuntimeException e) {
            markJobInError(job, e, "Handler method failed");
            return;
        }
        jobStore.completeJob(job.getJob().getJobId().toString());
    }

    private void markJobInError(final MultiTransferJob job, final Throwable exception, final String message) {

        log.error(message, exception);
        jobStore.markJobInError(job.getJob().getJobId().toString(), message);
    }

    private long startTransfers(final MultiTransferJob job, final Stream<T> dataRequests) /* throws JobException */ {
        return dataRequests.map(r -> startTransfer(job, r)).collect(Collectors.counting());
    }

    private TransferInitiateResponse startTransfer(final MultiTransferJob job,
            final T dataRequest)  /* throws JobException */ {
        final var response = processManager.initiateRequest(dataRequest, this::transferProcessCompleted);

        if (response.getStatus() != ResponseStatus.OK) {
            throw JobException.builder().status(response.getStatus()).build();
        }

        jobStore.addTransferProcess(job.getJob().getJobId().toString(), response.getTransferId());
        return response;
    }

    /**
     * Exception used to stop creating additional transfers if one transfer creation fails.
     */
    @Value
    @Builder
    private static class JobException extends RuntimeException {
        /**
         * The status of the transfer in error.
         */
        private final ResponseStatus status;
    }

    /**
     * @return
     */
    private Job createJob(final String globalAssetId) {
        final var assetId = StringUtils.isEmpty(globalAssetId) ? UUID.randomUUID().toString() : globalAssetId;
        return Job.builder()
                  .jobId(UUID.randomUUID())
                  .globalAssetId(GlobalAssetIdentification.builder().globalAssetId(assetId).build())
                  .createdOn(Instant.now())
                  .lastModifiedOn(Instant.now())
                  .jobState(JobState.UNSAVED)
                  .build();
    }

}
