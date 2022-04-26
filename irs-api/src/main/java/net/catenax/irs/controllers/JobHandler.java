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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.aaswrapper.job.AASRecursiveJobHandler;
import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.AASTransferProcessManager;
import net.catenax.irs.aaswrapper.job.ItemDataRequest;
import net.catenax.irs.aaswrapper.job.ItemTreesAssembler;
import net.catenax.irs.aaswrapper.job.TreeRecursiveLogic;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.JobOrchestrator;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Service use to create Job, manipulate job state and get job result
 */
@RequiredArgsConstructor
@Service
public class JobHandler implements IJobHandler {

    /**
     * Orchestrate the job
     */
    JobOrchestrator orchestrator;

    @Override
    public JobInitiateResponse createJob(@NonNull final GlobalAssetIdentification globalAssetId) {
        Job job = this.createJob(globalAssetId.getGlobalAssetId());
        Map<String, String> jobData = Map.of("ROOT_ITEM_ID_KEY", globalAssetId.getGlobalAssetId());
        return orchestrator.startJob(job, jobData);
    }

    /**
     * @param jobHandle
     */
    @Override
    public void cancelJob(@NonNull final JobHandle jobHandle) {
        orchestrator.cancelJob(jobHandle);
    }

    @Override
    public JobState interruptJob(@NonNull final JobHandle jobHandle) {
        return null;
    }

    @Override
    public Jobs getResult(@NonNull final JobHandle jobHandle) {
        return null;
    }

    private Job createJob(final @NotNull String globalAssetId) {
        final var assetId = StringUtils.isEmpty(globalAssetId) ? UUID.randomUUID().toString() : globalAssetId;
        return Job.builder()
                .jobId(UUID.randomUUID())
                .globalAssetId(GlobalAssetIdentification.builder().globalAssetId(assetId).build())
                .createdOn(Instant.now())
                .lastModifiedOn(Instant.now())
                .jobState(JobState.UNSAVED)
                .build();
    }

    public JobOrchestrator<ItemDataRequest, AASTransferProcess> jobOrchestrator(
            final DigitalTwinRegistryFacade registryFacade, final SubmodelFacade submodelFacade,
            final BlobPersistence blobStore, final JobStore jobStore) {

        final var manager = new AASTransferProcessManager(registryFacade, submodelFacade,
                Executors.newCachedThreadPool(), blobStore);
        final var logic = new TreeRecursiveLogic(blobStore, new JsonUtil(), new ItemTreesAssembler());
        final var handler = new AASRecursiveJobHandler(logic);

        return new JobOrchestrator<>(manager, jobStore, handler);
    }

}
