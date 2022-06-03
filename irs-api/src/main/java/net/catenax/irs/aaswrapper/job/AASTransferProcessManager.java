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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.component.ProcessingError;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.Endpoint;
import net.catenax.irs.component.assetadministrationshell.ProtocolInformation;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferInitiateResponse;
import net.catenax.irs.connector.job.TransferProcessManager;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;
import net.catenax.irs.dto.JobParameter;
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
    private final AspectTypeFilter aspectTypeFilter = new AspectTypeFilter();

    public AASTransferProcessManager(final DigitalTwinRegistryFacade registryFacade,
            final SubmodelFacade submodelFacade, final ExecutorService executor, final BlobPersistence blobStore) {
        this.registryFacade = registryFacade;
        this.submodelFacade = submodelFacade;
        this.executor = executor;
        this.blobStore = blobStore;
    }

    @Override
    public TransferInitiateResponse initiateRequest(final ItemDataRequest dataRequest,
            final Consumer<String> preExecutionHandler, final Consumer<AASTransferProcess> completionCallback,
            final JobParameter jobData) {

        final String processId = UUID.randomUUID().toString();
        preExecutionHandler.accept(processId);

        executor.execute(getRunnable(dataRequest, completionCallback, processId, jobData));

        return new TransferInitiateResponse(processId, ResponseStatus.OK);
    }

    private Runnable getRunnable(final ItemDataRequest dataRequest,
            final Consumer<AASTransferProcess> transferProcessCompleted, final String processId,
            final JobParameter jobData) {

        return () -> {
            final AASTransferProcess aasTransferProcess = new AASTransferProcess(processId, dataRequest.getDepth());

            final String itemId = dataRequest.getItemId();

            final ItemContainer.ItemContainerBuilder itemContainerBuilder = ItemContainer.builder();

            log.info("Calling Digital Twin Registry with itemId {}", itemId);
            try {
                final AssetAdministrationShellDescriptor aasShell = registryFacade.getAASShellDescriptor(itemId,
                        jobData);
                final List<SubmodelDescriptor> aasSubmodelDescriptors = aasShell.getSubmodelDescriptors();

                log.info("Retrieved {} SubmodelDescriptor for itemId {}", aasSubmodelDescriptors.size(), itemId);

                getAssemblyPartRelationshipEndpointAddresses(aasSubmodelDescriptors).forEach(address -> {
                    try {
                        final AssemblyPartRelationshipDTO submodel = submodelFacade.getSubmodel(address, jobData);
                        processEndpoint(aasTransferProcess, itemContainerBuilder, submodel);
                    } catch (RestClientException e) {
                        log.info("Submodel Endpoint could not be retrieved for Endpoint: {}. Creating Tombstone.",
                                address);
                        itemContainerBuilder.tombstone(createTombstone(itemId, address, e));
                    }
                });
                final List<SubmodelDescriptor> filteredSubmodelDescriptorsByAspectType = aspectTypeFilter.filterDescriptorsByAspectTypes(
                        aasSubmodelDescriptors, jobData.getAspectTypes());

                log.info("Unfiltered SubmodelDescriptor: {}", aasSubmodelDescriptors);
                log.info("Filtered SubmodelDescriptor: {}", filteredSubmodelDescriptorsByAspectType);

                itemContainerBuilder.shell(
                        aasShell.toBuilder().submodelDescriptors(filteredSubmodelDescriptorsByAspectType).build());
            } catch (RestClientException e) {
                log.info("Shell Endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
                itemContainerBuilder.tombstone(createTombstone(itemId, null, e));
            }
            storeItemContainer(processId, itemContainerBuilder.build());

            transferProcessCompleted.accept(aasTransferProcess);
        };
    }

    private Stream<String> getAssemblyPartRelationshipEndpointAddresses(
            final List<SubmodelDescriptor> aasSubmodelEndpoints) {
        final List<SubmodelDescriptor> submodelDescriptors = aspectTypeFilter.filterDescriptorsByAssemblyPartRelationship(
                aasSubmodelEndpoints);
        return submodelDescriptors.stream()
                                  .map(SubmodelDescriptor::getEndpoints)
                                  .flatMap(endpoints -> endpoints.stream()
                                                                 .map(Endpoint::getProtocolInformation)
                                                                 .map(ProtocolInformation::getEndpointAddress));

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

    private void processEndpoint(final AASTransferProcess aasTransferProcess,
            final ItemContainer.ItemContainerBuilder itemContainer, final AssemblyPartRelationshipDTO relationship) {
        log.info("Processing AssemblyPartRelationship with {} children", relationship.getChildParts().size());
        final List<String> childIds = relationship.getChildParts()
                                                  .stream()
                                                  .map(ChildDataDTO::getChildCatenaXId)
                                                  .collect(Collectors.toList());
        aasTransferProcess.addIdsToProcess(childIds);
        // TODO (jkreutzfeld) what do we actually need to store here?
        itemContainer.assemblyPartRelationship(relationship);
    }

}
