//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.aaswrapper.job;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.connector.job.ResponseStatus;
import org.eclipse.tractusx.irs.connector.job.TransferInitiateResponse;
import org.eclipse.tractusx.irs.connector.job.TransferProcessManager;
import org.eclipse.tractusx.irs.dto.JobParameter;
import org.eclipse.tractusx.irs.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.util.JsonUtil;

/**
 * Process manager for AAS Object transfers.
 * Communicates with the AAS Wrapper.
 */
@Slf4j
@SuppressWarnings("PMD.DoNotUseThreads") // We want to use threads at the moment ;-)
public class AASTransferProcessManager implements TransferProcessManager<ItemDataRequest, AASTransferProcess> {

    private final ExecutorService executor;

    private final BlobPersistence blobStore;

    private final AASHandler aasHandler;

    public AASTransferProcessManager(final AASHandler aasHandler, final ExecutorService executor,
            final BlobPersistence blobStore) {
        this.aasHandler = aasHandler;
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

            log.info("Starting processing Digital Twin Registry with itemId {}", itemId);
            final ItemContainer itemContainer = aasHandler.collectShellAndSubmodels(jobData, aasTransferProcess,
                    itemId);
            storeItemContainer(processId, itemContainer);

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

}
