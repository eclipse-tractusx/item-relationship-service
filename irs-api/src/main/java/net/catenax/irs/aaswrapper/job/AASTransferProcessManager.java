//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.job;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.component.ProcessingError;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferInitiateResponse;
import net.catenax.irs.connector.job.TransferProcessManager;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;
import net.catenax.irs.dto.SubmodelEndpoint;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.util.JsonUtil;
import org.springframework.web.client.RestClientException;

/**
 * Process manager for AAS Object transfers.
 * Communicates with the AAS Wrapper.
 */
@Slf4j
@SuppressWarnings("PMD.DoNotUseThreads") // We want to use threads at the moment ;-)
public class AASTransferProcessManager implements TransferProcessManager<ItemDataRequest, AASTransferProcess> {

    private final DigitalTwinRegistryFacade registryFacade;

    private final SubmodelFacade submodelFacade;

    private final ExecutorService executor;

    private final BlobPersistence blobStore;

    public AASTransferProcessManager(final DigitalTwinRegistryFacade registryFacade,
            final SubmodelFacade submodelFacade, final ExecutorService executor, final BlobPersistence blobStore) {
        this.registryFacade = registryFacade;
        this.submodelFacade = submodelFacade;
        this.executor = executor;
        this.blobStore = blobStore;
    }

    @Override
    public TransferInitiateResponse initiateRequest(final ItemDataRequest dataRequest,
                                                    final Consumer<String> transferProcessStarted,
                                                    final Consumer<AASTransferProcess> completionCallback,
                                                    final String lifecyleContext) {

        final String processId = UUID.randomUUID().toString();

        executor.submit(getRunnable(dataRequest, transferProcessStarted, completionCallback, processId, lifecyleContext));

        return new TransferInitiateResponse(processId, ResponseStatus.OK);
    }

    private Runnable getRunnable(final ItemDataRequest dataRequest, final Consumer<String> transferProcessStarted,
            final Consumer<AASTransferProcess> transferProcessCompleted, final String processId, final String lifecyleContext) {
        return () -> {
            transferProcessStarted.accept(processId);
            final AASTransferProcess aasTransferProcess = new AASTransferProcess(processId, dataRequest.getDepth());

            final String itemId = dataRequest.getItemId();

            final ItemContainer.ItemContainerBuilder itemContainerBuilder = ItemContainer.builder();

            log.info("Calling Digital Twin Registry with itemId {}", itemId);
            try {
                final List<SubmodelEndpoint> aasSubmodelEndpoints;
                aasSubmodelEndpoints = registryFacade.getAASSubmodelEndpoints(itemId);

                log.info("Retrieved {} SubmodelEndpoints for itemId {}", aasSubmodelEndpoints.size(), itemId);

                aasSubmodelEndpoints.stream().map(SubmodelEndpoint::getAddress).forEach(address -> {
                    try {
                        final var submodel = submodelFacade.getSubmodel(address);
                        final var processedSubmodel = processEndpoint(
                                aasTransferProcess, submodel, lifecyleContext);
                        itemContainerBuilder.assemblyPartRelationship(processedSubmodel);
                    } catch (RestClientException e) {
                        log.info("Submodel Endpoint could not be retrieved for Endpoint: {}. Creating Tombstone.",
                                address);
                        itemContainerBuilder.tombstone(createTombstone(itemId, address, e));
                    }
                });
            } catch (FeignException e) {
                log.info("Shell Endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
                itemContainerBuilder.tombstone(createTombstone(itemId, null, e));
            }
            storeItemContainer(processId, itemContainerBuilder.build());

            transferProcessCompleted.accept(aasTransferProcess);
        };
    }

    private void storeItemContainer(final String processId, final ItemContainer itemContainer) {
        try {
            final JsonUtil jsonUtil = new JsonUtil();
            blobStore.putBlob(processId, jsonUtil.asString(itemContainer).getBytes(StandardCharsets.UTF_8));
        } catch (BlobPersistenceException e) {
            log.error("Unable to store AAS result", e);
        }
    }

    private Tombstone createTombstone(final String itemId, final String address, final Exception exception) {
        final ProcessingError processingError = ProcessingError.builder()
                                                               .withRetryCounter(0)
                                                               .withLastAttempt(Instant.now())
                                                               .withErrorDetail(exception.getMessage())
                                                               .build();
        return Tombstone.builder().endpointURL(address).catenaXId(itemId).processingError(processingError).build();
    }

    private AssemblyPartRelationshipDTO processEndpoint(final AASTransferProcess aasTransferProcess,
                                 final AssemblyPartRelationshipDTO relationship,
                                 final String lifecycleContext) {

        log.info("Processing AssemblyPartRelationship with {} children", relationship.getChildParts().size());

        final AssemblyPartRelationshipDTO filteredDto = filterChildren(relationship, lifecycleContext);
        final List<String> childIds = mapToChildIds(filteredDto);
        aasTransferProcess.addIdsToProcess(childIds);

        // TODO (jkreutzfeld) what do we actually need to store here?
        return filteredDto;
    }

    private AssemblyPartRelationshipDTO filterChildren(final AssemblyPartRelationshipDTO relationship, final String lifecyleContext) {
        if (lifecyleContext != null) {
            final Set<ChildDataDTO> filteredChildren = relationship.getChildParts()
                                                                   .stream()
                                                                   .filter(childDataDTO -> lifecyleContext
                                                                           .equals(childDataDTO.getLifecycleContext()))
                                                                   .collect(Collectors.toSet());

            return AssemblyPartRelationshipDTO.builder()
                                              .catenaXId(relationship.getCatenaXId())
                                              .childParts(filteredChildren)
                                              .build();
        }
        return relationship;
    }

    private List<String> mapToChildIds(final AssemblyPartRelationshipDTO relationship) {
        return relationship.getChildParts().stream().map(ChildDataDTO::getChildCatenaXId).collect(
                Collectors.toList());
    }

}
