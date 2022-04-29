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

import static net.catenax.irs.dtos.IrsCommonConstants.DEPTH_ID_KEY;
import static net.catenax.irs.dtos.IrsCommonConstants.LIFE_CYCLE_CONTEXT;
import static net.catenax.irs.dtos.IrsCommonConstants.ROOT_ITEM_ID_KEY;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemContainer;
import net.catenax.irs.aaswrapper.job.ItemDataRequest;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.component.ChildItem;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.JobOrchestrator;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferProcess;
import net.catenax.irs.controllers.IrsApiConstants;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.exceptions.EntityNotFoundException;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.util.JsonUtil;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving parts tree.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ExcludeFromCodeCoverageGeneratedReport
@SuppressWarnings("PMD.ExcessiveImports")
public class IrsItemGraphQueryService implements IIrsItemGraphQueryService {

    private final JobOrchestrator<ItemDataRequest, AASTransferProcess> orchestrator;

    private final JobStore jobStore;

    private final BlobPersistence blobStore;

    @Override
    public JobHandle registerItemJob(final @NonNull RegisterJob request) {
        JobHandle jobHandle = null;

        final String uuid = request.getGlobalAssetId().substring(IrsApiConstants.URN_PREFIX_SIZE);

        final BomLifecycle bomLifecycleFormRequest = request.getBomLifecycle();
        if (bomLifecycleFormRequest != null) {
            final String lifecyleContextFromRequest = bomLifecycleFormRequest.toString();

            final var params = Map.of(ROOT_ITEM_ID_KEY, uuid,
                                                      DEPTH_ID_KEY, String.valueOf(request.getDepth()),
                                                      LIFE_CYCLE_CONTEXT, lifecyleContextFromRequest);

            final JobInitiateResponse jobInitiateResponse = orchestrator.startJob(params);

            if (jobInitiateResponse.getStatus().equals(ResponseStatus.OK)) {
                final String jobId = jobInitiateResponse.getJobId();

                jobHandle = JobHandle.builder().jobId(UUID.fromString(jobId)).build();
            } else {
                // TODO (jkreutzfeld) Improve with better response (proper exception for error responses?)
                throw new IllegalArgumentException("Could not start job: " + jobInitiateResponse.getError());
            }
        }
        return jobHandle;
    }

    @Override
    public Jobs jobLifecycle(final @NonNull String jobId) {
        return null;
    }

    @Override
    public List<UUID> getJobsByJobState(final @NonNull List<JobState> jobStates) {
        final List<MultiTransferJob> jobs = jobStore.findByStates(jobStates);

        return jobs.stream().map(x -> x.getJob().getJobId()).collect(Collectors.toList());
    }

    @Override
    public Job cancelJobById(final @NonNull UUID jobId) {
        final String idAsString = String.valueOf(jobId);

        final Optional<MultiTransferJob> canceled = this.jobStore.cancelJob(idAsString);
        if (canceled.isPresent()) {
            final MultiTransferJob job = canceled.get();

            return job.getJob();
        } else {
            throw new EntityNotFoundException("No job exists with id " + jobId);
        }
    }

    @Override
    public Jobs getJobForJobId(final UUID jobId, final boolean includePartialResults) {
        log.info("Retrieving job with id {} (includePartialResults: {})", jobId, includePartialResults);

        final Optional<MultiTransferJob> multiTransferJob = jobStore.find(jobId.toString());

        if (multiTransferJob.isPresent()) {
            final MultiTransferJob multiJob = multiTransferJob.get();

            final var relationships = new ArrayList<Relationship>();
            final var tombstones = new ArrayList<Tombstone>();

            if (jobIsCompleted(multiJob)) {
                final var container = retrieveJobResultRelationships(multiJob.getJob().getJobId());
                relationships.addAll(convert(container.getAssemblyPartRelationships()));
                tombstones.addAll(container.getTombstones());

            } else if (includePartialResults) {
                final var container = retrievePartialResults(multiJob);
                relationships.addAll(convert(container.getAssemblyPartRelationships()));
                tombstones.addAll(container.getTombstones());

            }

            log.info("Found job with id {} in status {} with {} relationships and {} tombstones", jobId,
                    multiJob.getJob().getJobState(), relationships.size(), tombstones.size());

            return Jobs.builder().job(multiJob.getJob()).relationships(relationships).tombstones(tombstones).build();
        } else {
            throw new EntityNotFoundException("No job exists with id " + jobId);
        }
    }

    private ItemContainer retrievePartialResults(final MultiTransferJob multiJob) {
        final List<TransferProcess> completedTransfers = multiJob.getCompletedTransfers();
        final List<String> transferIds = completedTransfers.stream()
                                                           .map(TransferProcess::getId)
                                                           .collect(Collectors.toList());

        final var relationships = new ArrayList<AssemblyPartRelationshipDTO>();
        final var tombstones = new ArrayList<Tombstone>();

        for (final String id : transferIds) {
            try {
                final Optional<byte[]> blob = blobStore.getBlob(id);
                blob.ifPresent(bytes -> {
                    final ItemContainer itemContainer = toItemContainer(bytes);
                    relationships.addAll(itemContainer.getAssemblyPartRelationships());
                    tombstones.addAll(itemContainer.getTombstones());
                });

            } catch (BlobPersistenceException e) {
                log.error("Unable to read transfer result", e);
            }
        }
        return ItemContainer.builder().assemblyPartRelationships(relationships).tombstones(tombstones).build();
    }

    private ItemContainer toItemContainer(final byte[] blob) {
        return new JsonUtil().fromString(new String(blob, StandardCharsets.UTF_8), ItemContainer.class);
    }

    private ItemContainer retrieveJobResultRelationships(final UUID jobId) {
        try {
            final Optional<byte[]> blob = blobStore.getBlob(jobId.toString());
            final byte[] bytes = blob.orElseThrow(
                    () -> new EntityNotFoundException("Could not find stored data for multiJob with id " + jobId));
            return toItemContainer(bytes);
        } catch (BlobPersistenceException e) {
            log.error("Unable to read blob", e);
            throw new EntityNotFoundException("Could not load stored data for multiJob with id " + jobId, e);
        }
    }

    private boolean jobIsCompleted(final MultiTransferJob multiJob) {
        return multiJob.getJob().getJobState().equals(JobState.COMPLETED);
    }

    private Collection<Relationship> convert(final Collection<AssemblyPartRelationshipDTO> assemblyPartRelationships) {
        return assemblyPartRelationships.stream().flatMap(this::convert).collect(Collectors.toList());
    }

    private Stream<Relationship> convert(final AssemblyPartRelationshipDTO dto) {
        return dto.getChildParts()
                  .stream()
                  .map(child -> Relationship.builder()
                                            .catenaXId(GlobalAssetIdentification.builder()
                                                                                .globalAssetId(dto.getCatenaXId())
                                                                                .build())
                                            .childItem(ChildItem.builder()
                                                                .childCatenaXId(GlobalAssetIdentification.builder()
                                                                                                         .globalAssetId(
                                                                                                                 child.getChildCatenaXId())
                                                                                                         .build())
                                                                .lifecycleContext(
                                                                        BomLifecycle.fromLifecycleContextCharacteristic(
                                                                                child.getLifecycleContext()))
                                                                .build())
                                            .build());
    }
}
