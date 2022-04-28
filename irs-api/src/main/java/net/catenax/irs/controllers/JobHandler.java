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

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.validation.constraints.NotNull;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.JobOrchestrator;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.services.IrsItemGraphQueryService;
import org.apache.commons.lang3.StringUtils;
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

    /**
     * Orchestrate the job
     */
    JobOrchestrator orchestrator;

    /**
     * Use to retrieve jobs from Digital Twins and the Blob Store
     *
     * @param orchestrator
     * @param queryService
     */

    @Autowired
    public JobHandler(JobOrchestrator orchestrator, IrsItemGraphQueryService queryService) {
        this.orchestrator = orchestrator;
    }

    @Async("AsyncJobExecutor")
    @Override
    public CompletableFuture<JobInitiateResponse> createJob(@NonNull final GlobalAssetIdentification globalAssetId) {
        Map<String, String> jobData = Map.of("ROOT_ITEM_ID_KEY", globalAssetId.getGlobalAssetId());
        JobInitiateResponse response = orchestrator.startJob(jobData);
        return CompletableFuture.completedFuture(response);
    }

    /**
     * @param jobId
     */
    @Async("asyncJobExecutor")
    @Override
    public CompletableFuture<Optional<MultiTransferJob>> cancelJob(@NonNull final String jobId) {
        return CompletableFuture.completedFuture(orchestrator.cancelJob(jobId));
    }

    @Async("asyncJobExecutor")
    @Override
    public CompletableFuture<JobState> interruptJob(@NonNull final JobHandle jobHandle) {
        return null;
    }

    @Async("asyncJobExecutor")
    @Override
    public CompletableFuture<Jobs> getResult(@NonNull final JobHandle jobHandle) {
        return null;
    }

    @Async("asyncJobExecutor")
    @Override
    public CompletableFuture<Jobs> getJobForJobId(final UUID jobId) {
        return null;
    }

    private CompletableFuture<Job> createJob(final @NotNull String globalAssetId) {
        final var assetId = StringUtils.isEmpty(globalAssetId) ? UUID.randomUUID().toString() : globalAssetId;
        Job job = Job.builder()
                     .jobId(UUID.randomUUID())
                     .globalAssetId(GlobalAssetIdentification.builder().globalAssetId(assetId).build())
                     .createdOn(Instant.now())
                     .lastModifiedOn(Instant.now())
                     .jobState(JobState.UNSAVED)
                     .build();

        return CompletableFuture.completedFuture(job);
    }

}
