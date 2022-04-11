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

import lombok.Builder;
import lombok.Value;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.enums.JobState;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.transfer.TransferInitiateResponse;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessObservable;
import org.eclipse.dataspaceconnector.spi.transfer.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;

/**
 * Orchestrator service for recursive {@link MultiTransferJob}s that potentially
 * comprise multiple transfers.
 */
@SuppressWarnings({
    "PMD.GuardLogStatement", // Monitor doesn't offer guard statements
    "PMD.AvoidCatchingGenericException"}) // Handle RuntimeException from callbacks
public class JobOrchestrator {

    private static final int TTL_CLEANUP_COMPLETED_JOBS_HOURS  = 1;
    private static final int TTL_CLEANUP_COMPLETED_JOBS_SECONDS = TTL_CLEANUP_COMPLETED_JOBS_HOURS * 3600;
    private static final int TTL_CLEANUP_FAILED_JOBS_HOURS = 24;
    private static final int TTL_CLEANUP_FAILED_JOBS_SECONDS = TTL_CLEANUP_FAILED_JOBS_HOURS * 3600 * 24;

    /**
     * Transfer process manager.
     */
    private final TransferProcessManager processManager;

    /**
     * Job store.
     */
    private final JobStore jobStore;

    /**
     * Job handler containing the logic to start transfers and process
     * transfer results.
     */
    private final RecursiveJobHandler handler;

    /**
     * Logger.
     */
    private final Monitor monitor;

    /**
     * Create a new instance of {@link JobOrchestrator}.
     *
     * @param processManager            Process manager.
     * @param jobStore                  Job store.
     * @param handler                   Recursive job handler.
     * @param transferProcessObservable Transfer process observable.
     * @param monitor                   Logger.
     */
    public JobOrchestrator(
            final TransferProcessManager processManager,
            final JobStore jobStore,
            final RecursiveJobHandler handler,
            final TransferProcessObservable transferProcessObservable,
            final Monitor monitor) {
        this.processManager = processManager;
        this.jobStore = jobStore;
        this.handler = handler;
        this.monitor = monitor;

        transferProcessObservable.registerListener(new JobTransferCallback(this));
    }

    /**
     * Start a job.
     * @param job job attached to the request
     * @param jobData additional data for the job to managed by the {@link JobStore}.
     * @return response.
     */
    public JobInitiateResponse startJob(final Job job, final Map<String, String> jobData) {
        final var multiJob = MultiTransferJob.builder()
                .job(job).jobData(jobData).build();

        jobStore.create(multiJob);

        final Stream<DataRequest> requests;
        try {
            requests = handler.initiate(multiJob);
        } catch (RuntimeException e) {
            markJobInError(multiJob, e, "Handler method failed");
            return JobInitiateResponse.builder().jobId(multiJob.getJob().getJobId()).status(ResponseStatus.FATAL_ERROR).build();
        }

        long transferCount;
        try {
            transferCount = startTransfers(multiJob, requests);
        } catch (JobException e) {
            return JobInitiateResponse.builder().jobId(multiJob.getJob().getJobId()).status(e.getStatus()).build();
        }

        // If no transfers are requested, job is already complete
        if (transferCount == 0) {
            completeJob(multiJob);
        }

        return JobInitiateResponse.builder().jobId(multiJob.getJob().getJobId()).status(ResponseStatus.OK).build();
    }



    /**
     * Callback invoked when a transfer has completed.
     *
     * @param process
     */
    /* package */ void transferProcessCompleted(final TransferProcess process) {
        final var jobEntry = jobStore.findByProcessId(process.getId());
        if (!jobEntry.isPresent()) {
            monitor.severe("Job not found for transfer " + process.getId());
            return;
        }
        final var job = jobEntry.get();

        if (job.getJob().getJobState() != JobState.IN_PROGRESS) {
            monitor.info("Ignoring transfer complete event for job " + job.getJob().getJobId() + " in state " + job.getJob().getJobState());
            return;
        }

        final Stream<DataRequest> requests;
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

        jobStore.completeTransferProcess(job.getJob().getJobId(), process);

        callCompleteHandlerIfFinished(job.getJob().getJobId());
    }

    public List<MultiTransferJob> findAndCleanupCompletedJobs() {
        final Instant currentDateMinusSeconds = Instant.now().minusSeconds(TTL_CLEANUP_COMPLETED_JOBS_SECONDS);
        final List<MultiTransferJob> completedJobs = jobStore.findByStateAndCompletionDateOlderThan(JobState.COMPLETED, currentDateMinusSeconds);
        return deleteJobs(completedJobs);
    }

    public List<MultiTransferJob> findAndCleanupFailedJobs() {
        final Instant currentDateMinusSeconds = Instant.now().minusSeconds(TTL_CLEANUP_FAILED_JOBS_SECONDS);
        final List<MultiTransferJob> failedJobs = jobStore.findByStateAndCompletionDateOlderThan(JobState.ERROR, currentDateMinusSeconds);
        return deleteJobs(failedJobs);
    }

    private List<MultiTransferJob> deleteJobs(final List<MultiTransferJob> jobs) {
        return jobs.stream().map(job -> jobStore.deleteJob(job.getJob().getJobId())).collect(Collectors.toList());
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
        jobStore.completeJob(job.getJob().getJobId());
    }

    private void markJobInError(final MultiTransferJob job, final Throwable exception, final String message) {
        monitor.severe(message, exception);
        jobStore.markJobInError(job.getJob().getJobId(), message);
    }

    private long startTransfers(final MultiTransferJob job, final Stream<DataRequest> dataRequests) /* throws JobException */ {
        return dataRequests
                .map(r -> startTransfer(job, r))
                .collect(Collectors.counting());
    }

    private TransferInitiateResponse startTransfer(final MultiTransferJob job, final DataRequest dataRequest)  /* throws JobException */ {
        final var response = processManager.initiateConsumerRequest(dataRequest);

        if (response.getStatus() != ResponseStatus.OK) {
            throw JobException.builder().status(response.getStatus()).build();
        }

        jobStore.addTransferProcess(job.getJob().getJobId(), response.getId());
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
}
