//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.controllers;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.exceptions.EntityNotFoundException;
import net.catenax.irs.services.IrsItemGraphQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service use to create Job, manipulate job state and get job result
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class JobHandler implements IJobHandler {

    private static final String EMPTY_STRING = "";
    private static final String INTERNAL_ERROR = "An internal error has occur";

    /**
     * Job registration and query object
     */
    IrsItemGraphQueryService queryService;

    /**
     * Use to retrieve jobs from Digital Twins and the Blob Store
     *
     * @param queryService
     */

    @Autowired
    public JobHandler(IrsItemGraphQueryService queryService) {
        this.queryService = queryService;
    }

    @Async("AsyncJobExecutor")
    @Override
    public CompletableFuture<JobInitiateResponse> registerJob(@NonNull final RegisterJob request) {

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
    public CompletableFuture<Optional<Job>> interruptJob(@NonNull final UUID jobId) {
        return CompletableFuture.completedFuture(Optional.of(queryService.cancelJobById(jobId)));
    }

    @Async("asyncJobExecutor")
    @Override
    public CompletableFuture<Jobs> getJobResult(final UUID jobId) throws EntityNotFoundException {
        Jobs jobs = queryService.getJobForJobId(jobId);
        return CompletableFuture.completedFuture(jobs);
    }

}
