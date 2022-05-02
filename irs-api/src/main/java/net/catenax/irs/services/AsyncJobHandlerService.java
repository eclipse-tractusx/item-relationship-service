//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.services;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.JobException;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.exceptions.EntityCancelException;
import net.catenax.irs.exceptions.EntityNotFoundException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service use to create Job, manipulate job state and get job result
 */
@Slf4j
@RequiredArgsConstructor
@Service
@ExcludeFromCodeCoverageGeneratedReport
public class AsyncJobHandlerService implements IAsyncJobHandlerService {
    private static final int WAIT_TIME = 5000;
    private static final String EMPTY_STRING = "";
    private static final String INTERNAL_ERROR = "An internal error has occur";
    private static final String JOB_ALREADY_CANCELLED = "Job been requested had already been cancelled";

    /**
     * Job registration and query object
     */
    private final IrsItemGraphQueryService queryService;

    @Override
    public CompletableFuture<JobInitiateResponse> registerJob(@NonNull final RegisterJob request)
            throws InterruptedException {

        Optional<JobHandle> handle = Optional.ofNullable(queryService.registerItemJob(request));

        final JobInitiateResponse response = handle.map(jh ->
                JobInitiateResponse.builder()
                                   .jobId(jh.getJobId().toString())
                                   .status(ResponseStatus.OK)
                                   .error(EMPTY_STRING)
                                   .build()
        ).orElseGet(() ->
                JobInitiateResponse.builder()
                                   .jobId(EMPTY_STRING)
                                   .status(ResponseStatus.ERROR_RETRY)
                                   .error(INTERNAL_ERROR)
                                   .build()
        );

        return CompletableFuture.completedFuture(response);
    }

    /**
     * @param jobId
     */
    @Async("asyncJobExecutor")
    @Override
    public CompletableFuture<Optional<Job>> cancelJob(@NonNull final UUID jobId) {
        return CompletableFuture.completedFuture(Optional.of(queryService.cancelJobById(jobId)));
    }

    @Async("asyncJobExecutor")
    @Override
    public CompletableFuture<Jobs> getPartialJobResult(UUID jobId)
            throws EntityNotFoundException, InterruptedException {
        Jobs jobs = queryService.getJobForJobId(jobId);
        return CompletableFuture.completedFuture(jobs);
    }

    @Async("asyncJobExecutor")
    @Override
    public CompletableFuture<Jobs> getCompleteJobResult(final UUID jobId)
            throws EntityNotFoundException {

        // Check and handle job in bad states
        JobState[] badStates = { JobState.ERROR,
                                 JobState.CANCELED
        };

        // Check and handle job in progress state
        JobState[] progressStates = { JobState.UNSAVED,
                                      JobState.INITIAL,
                                      JobState.RUNNING,
                                      JobState.TRANSFERS_FINISHED
        };

        // TODO (Dapo): Maximum amount of time to wait when requesting complete job result
        return CompletableFuture.supplyAsync(() -> {
            Jobs jobs = queryService.getJobForJobId(jobId);
            do {
                try {
                    for (JobState badState : badStates) {
                        if (jobs.getJob().getJobState() == badState) {
                            throw new EntityCancelException(JOB_ALREADY_CANCELLED);
                        }
                    }

                    for (JobState progressState : progressStates) {
                        if (jobs.getJob().getJobState() == progressState) {
                            jobs = queryService.getJobForJobId(jobId);
                            Thread.currentThread().wait(WAIT_TIME);
                        }
                    }
                } catch (InterruptedException e) {
                    throw new JobException(e.getMessage());
                }

                // TODO (Dapo): Condition to limit the amount of time to keep trying to get a job complete state

            } while (jobs.getJob().getJobState() != JobState.COMPLETED);

            return jobs;
        });

    }

}
