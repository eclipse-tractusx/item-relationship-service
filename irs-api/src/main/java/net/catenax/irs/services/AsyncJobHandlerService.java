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
@SuppressWarnings({ "PMD.PreserveStackTrace",
                    "PMD.AvoidUncheckedExceptionsInSignatures"
})
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

        final Optional<JobHandle> handle = Optional.ofNullable(queryService.registerItemJob(request));

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
    public CompletableFuture<Optional<Job>> cancelJob(final UUID jobId) {
        return CompletableFuture.completedFuture(Optional.of(queryService.cancelJobById(jobId)));
    }

    @Async("asyncJobExecutor")
    @Override
    public CompletableFuture<Jobs> getPartialJobResult(final UUID jobId)
            throws EntityNotFoundException {
        final Jobs jobs = queryService.getJobForJobId(jobId, true);
        return CompletableFuture.completedFuture(jobs);
    }

    @Async("asyncJobExecutor")
    @Override
    public CompletableFuture<Jobs> getCompleteJobResult(final UUID jobId)
            throws EntityNotFoundException {

        // Check and handle job in bad states
        final JobState[] badStates = { JobState.ERROR,
                                       JobState.CANCELED
        };

        return CompletableFuture.supplyAsync(() -> {
            Jobs jobs = queryService.getJobForJobId(jobId, false);
            do {

                checkBadState(badStates, jobs);

                if (jobs.getJob().getJobState() == JobState.COMPLETED) {
                    jobs = queryService.getJobForJobId(jobId, false);
                }

            } while (jobs.getJob().getJobState() != JobState.COMPLETED);

            return jobs;
        });

    }

    private void checkBadState(final JobState[] badStates, final Jobs jobs) {
        for (final JobState badState : badStates) {
            if (jobs.getJob().getJobState() == badState) {
                throw new EntityCancelException(JOB_ALREADY_CANCELLED);
            }
        }
    }

}
