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
import net.catenax.irs.component.Job;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.exceptions.EntityNotFoundException;

/**
 * Interface for JobHandler
 */
public interface IAsyncJobHandlerService {

    CompletableFuture<JobInitiateResponse> registerJob(@NonNull RegisterJob request) throws InterruptedException;

    CompletableFuture<Optional<Job>> cancelJob(@NonNull UUID jobId) throws EntityNotFoundException;

    CompletableFuture<Jobs> getPartialJobResult(UUID jobId) throws EntityNotFoundException;

    CompletableFuture<Jobs> getCompleteJobResult(UUID jobId) throws EntityNotFoundException;

}
