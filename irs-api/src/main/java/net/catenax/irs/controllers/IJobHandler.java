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
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.MultiTransferJob;

/**
 * Interface for JobHandler
 */
public interface IJobHandler {

    CompletableFuture<JobInitiateResponse> createJob(@NonNull GlobalAssetIdentification globalAssetId);

    CompletableFuture<Optional<MultiTransferJob>> cancelJob(@NonNull String jobIdS);

    CompletableFuture<JobState> interruptJob(@NonNull JobHandle jobHandle);

    CompletableFuture<Jobs> getResult(@NonNull JobHandle jobHandle);

    CompletableFuture<Jobs> getJobForJobId(UUID jobId);

}
