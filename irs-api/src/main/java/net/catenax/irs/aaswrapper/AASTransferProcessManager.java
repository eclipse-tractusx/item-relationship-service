//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.registry.domain.model.AssetAdministrationShellDescriptor;
import net.catenax.irs.aaswrapper.registry.domain.model.Endpoint;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelDescriptor;
import net.catenax.irs.aspectmodels.AspectModelTypes;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import net.catenax.irs.aspectmodels.assemblypartrelationship.ChildData;
import net.catenax.irs.aspectmodels.serialparttypization.SerialPartTypization;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferInitiateResponse;
import net.catenax.irs.connector.job.TransferProcessManager;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.util.JsonUtil;

/**
 * Process manager for AAS Object transfers.
 * Communicates with the AAS Wrapper.
 */
@Slf4j
@SuppressWarnings("PMD.DoNotUseThreads") // We want to use threads at the moment ;-)
public class AASTransferProcessManager implements TransferProcessManager<ItemDataRequest, AASTransferProcess> {

    private final AASWrapperClient aasClient;

    private final ExecutorService executor;

    private final BlobPersistence blobStore;

    public AASTransferProcessManager(final AASWrapperClient aasClient, final ExecutorService executor,
            final BlobPersistence blobStore) {
        this.aasClient = aasClient;
        this.executor = executor;
        this.blobStore = blobStore;
    }

    @Override
    public TransferInitiateResponse initiateRequest(final ItemDataRequest dataRequest,
            final Consumer<AASTransferProcess> completionCallback) {
        final String processId = UUID.randomUUID().toString();

        executor.submit(getRunnable(dataRequest, completionCallback, processId));

        return new TransferInitiateResponse(processId, ResponseStatus.OK);
    }

    private Runnable getRunnable(final ItemDataRequest dataRequest,
            final Consumer<AASTransferProcess> transferProcessCompleted, final String processId) {
        return () -> {
            final AASTransferProcess aasTransferProcess = new AASTransferProcess(processId);

            final String itemId = dataRequest.getItemId();

            final AssetAdministrationShellDescriptor descriptor = aasClient.getAssetAdministrationShellDescriptor(
                    itemId);

            final ItemContainer itemContainer = new ItemContainer();

            descriptor.getSubmodelDescriptors()
                      .forEach(submodel -> processSubmodel(aasTransferProcess, itemContainer, submodel));

            try {
                final JsonUtil jsonUtil = new JsonUtil();
                blobStore.putBlob(processId, jsonUtil.asString(itemContainer).getBytes(StandardCharsets.UTF_8));
            } catch (BlobPersistenceException e) {
                log.error("Unable to store AAS result", e);
            }
            transferProcessCompleted.accept(aasTransferProcess);
        };
    }

    private void processSubmodel(final AASTransferProcess aasTransferProcess, final ItemContainer itemContainer,
            final SubmodelDescriptor submodel) {
        submodel.getEndpoints().forEach(endpoint -> processEndpoint(aasTransferProcess, itemContainer, endpoint));
    }

    private void processEndpoint(final AASTransferProcess aasTransferProcess, final ItemContainer itemContainer,
            final Endpoint endpoint) {
        final SerialPartTypization typization = (SerialPartTypization) aasClient.getSubmodel(endpoint.toString(),
                AspectModelTypes.SERIAL_PART_TYPIZATION);

        final AssemblyPartRelationship relationship = (AssemblyPartRelationship) aasClient.getSubmodel(
                endpoint.toString(), AspectModelTypes.ASSEMBLY_PART_RELATIONSHIP);

        final List<String> childIds = relationship.getChildParts()
                                                  .stream()
                                                  .map(ChildData::getChildCatenaXId)
                                                  .collect(Collectors.toList());
        aasTransferProcess.addIdsToProcess(childIds);
        // TODO (jkreutzfeld) what do we actually need to store here?
        itemContainer.add(typization, relationship);
    }
}
