//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.jobs;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import net.catenax.irs.aaswrapper.AASWrapperClient;
import net.catenax.irs.aaswrapper.registry.domain.model.AssetAdministrationShellDescriptor;
import net.catenax.irs.aaswrapper.registry.domain.model.Endpoint;
import net.catenax.irs.aspectmodels.AspectModelTypes;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferInitiateResponse;
import net.catenax.irs.connector.job.TransferProcessManager;
import org.jetbrains.annotations.NotNull;

public class AASTransferProcessManager implements TransferProcessManager<ItemDataRequest, AASTransferProcess> {

    private final AASWrapperClient aasClient;

    private final ExecutorService executor;

    public AASTransferProcessManager(final AASWrapperClient aasClient, final ExecutorService executor) {
        this.aasClient = aasClient;
        this.executor = executor;
    }

    @Override
    public TransferInitiateResponse initiateRequest(final ItemDataRequest dataRequest,
            final Consumer<AASTransferProcess> completionCallback) {
        final String processId = UUID.randomUUID().toString();

        executor.submit(getRunnable(dataRequest, completionCallback, processId));

        return new TransferInitiateResponse(processId, ResponseStatus.OK);
    }

    @NotNull
    private Runnable getRunnable(final ItemDataRequest dataRequest,
            final Consumer<AASTransferProcess> transferProcessCompleted, final String processId) {
        return () -> {
            final AASTransferProcess aasTransferProcess = new AASTransferProcess(processId);
            ItemDataRequest req = dataRequest;

            final String itemId = req.getItemId();

            final AssetAdministrationShellDescriptor descriptor = aasClient.getAssetAdministrationShellDescriptor(
                    itemId);

            descriptor.getSubmodelDescriptors().forEach(submodel -> {
                final Endpoint endpoint = submodel.getEndpoints().get(0);
                final AssemblyPartRelationship typization = (AssemblyPartRelationship) aasClient.getSubmodel(endpoint.toString(),
                        AspectModelTypes.ASSEMBLY_PART_RELATIONSHIP);
                final String catenaXId = typization.getCatenaXId();
            });


            transferProcessCompleted.accept(aasTransferProcess);
        };
    }
}
