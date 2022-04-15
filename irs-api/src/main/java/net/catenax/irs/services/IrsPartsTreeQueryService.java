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
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.JobOrchestrator;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.controllers.IrsApiConstants;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.exceptions.EntityNotFoundException;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.requests.IrsPartsTreeRequest;
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
public class IrsPartsTreeQueryService implements IIrsPartTreeQueryService {

    private final JobOrchestrator<ItemDataRequest, AASTransferProcess> orchestrator;

    private final JobStore jobStore;

    private final BlobPersistence blobStore;

    @Override
    public JobHandle registerItemJob(final @NonNull IrsPartsTreeRequest request) {
        final String uuid = request.getGlobalAssetId().substring(IrsApiConstants.URN_PREFIX_SIZE);
        final var params = Map.of(ROOT_ITEM_ID_KEY, uuid);
        final JobInitiateResponse jobInitiateResponse = orchestrator.startJob(params);

        if (jobInitiateResponse.getStatus().equals(ResponseStatus.OK)) {
            final String jobId = jobInitiateResponse.getJobId();
            return JobHandle.builder().jobId(UUID.fromString(jobId)).build();
        } else {
            // TODO (jkreutzfeld) Improve with better response (proper exception for error responses?)
            throw new IllegalArgumentException("Could not start job: " + jobInitiateResponse.getError());
        }
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

    @Override
    public Jobs getBOMForJobId(final UUID jobId) {
        final Optional<MultiTransferJob> multiTransferJob = jobStore.find(jobId.toString());
        if (multiTransferJob.isPresent()) {
            final MultiTransferJob job = multiTransferJob.get();
            final Job.JobBuilder builder = Job.builder()
                                              .jobId(UUID.fromString(job.getJob().getJobId().toString()))
                                              .jobState(convert(job.getJob().getJobState()));
            Optional.of(job.getJob().getJobCompleted()).ifPresent(date -> builder.jobCompleted(date));
            final Job jobToReturn = builder.build();

            final var relationships = new ArrayList<Relationship>();
            try {
                final byte[] blob = blobStore.getBlob(job.getJob().getJobId().toString());
                final ItemContainer itemContainer = new JsonUtil().fromString(new String(blob, StandardCharsets.UTF_8),
                        ItemContainer.class);
                final List<AssemblyPartRelationshipDTO> assemblyPartRelationships = itemContainer.getAssemblyPartRelationships();
                relationships.addAll(convert(assemblyPartRelationships));
            } catch (BlobPersistenceException e) {
                log.error("Unable to read blob", e);
            }
            return Jobs.builder().job(jobToReturn).relationships(relationships).build();
        } else {
            throw new EntityNotFoundException("No job exists with id " + jobId);
        }
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
                                                                        BomLifecycle.value(child.getLifecycleContext()))
                                                                .build())
                                            .build());
    }

    private JobState convert(final JobState state) {
        switch (state) {
        case COMPLETED:
            return JobState.COMPLETED;
        case IN_PROGRESS:
            return JobState.IN_PROGRESS;
        case ERROR:
            return JobState.ERROR;
        case INITIAL:
            return JobState.INITIAL;
        case TRANSFERS_FINISHED:
            return JobState.TRANSFERS_FINISHED;
        default:
            throw new IllegalArgumentException("Cannot convert JobState of type " + state);
        }
    }
}
