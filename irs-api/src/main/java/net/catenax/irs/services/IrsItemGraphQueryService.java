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

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
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
import net.catenax.irs.aaswrapper.job.AASRecursiveJobHandler;
import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemContainer;
import net.catenax.irs.aaswrapper.job.ItemDataRequest;
import net.catenax.irs.component.ChildItem;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.annotations.ExcludeFromCodeCoverageGeneratedReport;
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
        final String uuid = request.getGlobalAssetId().substring(IrsApiConstants.URN_PREFIX_SIZE);
        final var params = Map.of(AASRecursiveJobHandler.ROOT_ITEM_ID_KEY, uuid, AASRecursiveJobHandler.DEPTH_ID_KEY, String.valueOf(request.getDepth()));
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
    public List<UUID> getJobsByJobState(final @NonNull List<JobState> jobStates) {
        final List<MultiTransferJob> jobs = jobStore.findByStates(
                jobStates.stream().map(this::convert).collect(Collectors.toList()));

        return jobs.stream().map(MultiTransferJob::getJobId).map(UUID::fromString).collect(Collectors.toList());
    }

    @Override
    public Job cancelJobById(final @NonNull UUID jobId) {
        final String idAsString = String.valueOf(jobId);

        final Optional<MultiTransferJob> canceled = this.jobStore.cancelJob(idAsString);
        if (canceled.isPresent()) {
            final MultiTransferJob job = canceled.get();

            return Job.builder().jobId(jobId).jobState(convert(job.getState())).build();
        } else {
            throw new EntityNotFoundException("No job exists with id " + jobId);
        }
    }

    @Override
    public Jobs getJobForJobId(final UUID jobId) {
        final Optional<MultiTransferJob> multiTransferJob = jobStore.find(jobId.toString());
        if (multiTransferJob.isPresent()) {
            final MultiTransferJob job = multiTransferJob.get();
            final Job.JobBuilder builder = Job.builder()
                                              .jobId(UUID.fromString(job.getJobId()))
                                              .jobState(convert(job.getState()));
            job.getCompletionDate().ifPresent(date -> builder.jobCompleted(date.toInstant(ZoneOffset.UTC)));
            final Job jobToReturn = builder.build();

            final var relationships = new ArrayList<Relationship>();
            try {
                final Optional<byte[]> blob = blobStore.getBlob(job.getJobId());
                final byte[] bytes = blob.orElseThrow(
                        () -> new EntityNotFoundException("Could not find stored data for job with id " + jobId));
                final ItemContainer itemContainer = new JsonUtil().fromString(new String(bytes, StandardCharsets.UTF_8),
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
                                                                        BomLifecycle.fromLifecycleContextCharacteristic(
                                                                                child.getLifecycleContext()))
                                                                .build())
                                            .build());
    }

    private JobState convert(final net.catenax.irs.connector.job.JobState state) {
        switch (state) {
            case COMPLETED:
                return JobState.COMPLETED;
            case IN_PROGRESS:
                return JobState.RUNNING;
            case ERROR:
                return JobState.ERROR;
            case INITIAL:
                return JobState.INITIAL;
            case TRANSFERS_FINISHED:
                return JobState.TRANSFERS_FINISHED;
            case CANCELED:
                return JobState.CANCELED;
            default:
                throw new IllegalArgumentException("Cannot convert JobState of type " + state);
        }
    }

    private net.catenax.irs.connector.job.JobState convert(final JobState state) {
        switch (state) {
            case COMPLETED:
                return net.catenax.irs.connector.job.JobState.COMPLETED;
            case RUNNING:
                return net.catenax.irs.connector.job.JobState.IN_PROGRESS;
            case ERROR:
                return net.catenax.irs.connector.job.JobState.ERROR;
            case INITIAL:
                return net.catenax.irs.connector.job.JobState.INITIAL;
            case TRANSFERS_FINISHED:
                return net.catenax.irs.connector.job.JobState.TRANSFERS_FINISHED;
            default:
                throw new IllegalArgumentException("Cannot convert JobState of type " + state);
        }
    }

}
