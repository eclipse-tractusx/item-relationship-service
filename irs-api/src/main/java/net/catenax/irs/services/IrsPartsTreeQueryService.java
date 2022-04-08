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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.job.AASRecursiveJobHandler;
import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemDataRequest;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.JobOrchestrator;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.connector.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.requests.IrsPartsTreeRequest;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving parts tree.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ExcludeFromCodeCoverageGeneratedReport
public class IrsPartsTreeQueryService implements IIrsPartTreeQueryService {

    private final JobOrchestrator<ItemDataRequest, AASTransferProcess> orchestrator;

    @Override
    public JobHandle registerItemJob(final @NonNull IrsPartsTreeRequest request) {
        final JobInitiateResponse jobInitiateResponse = orchestrator.startJob(
                Map.of(AASRecursiveJobHandler.ROOT_ITEM_ID_KEY, request.getGlobalAssetId()));
        final String jobId = jobInitiateResponse.getJobId();
        return JobHandle.builder().jobId(UUID.fromString(jobId)).build();
    }

    @Override
    public Jobs jobLifecycle(final @NonNull String jobId) {
        return null;
    }

    @Override
    public Optional<List<Job>> getJobsByProcessingState(final @NonNull String processingState) {
        return Optional.empty();
    }

    @Override
    public Job cancelJobById(final @NonNull String jobId) {
        return null;
    }
}
