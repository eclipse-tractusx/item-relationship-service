/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.aaswrapper.job;

import static org.eclipse.tractusx.irs.configuration.JobConfiguration.JOB_BLOB_PERSISTENCE;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.aaswrapper.job.delegate.AbstractDelegate;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.connector.job.ResponseStatus;
import org.eclipse.tractusx.irs.connector.job.TransferInitiateResponse;
import org.eclipse.tractusx.irs.connector.job.TransferProcessManager;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Process manager for AAS Object transfers.
 */
@Slf4j
@SuppressWarnings("PMD.DoNotUseThreads") // We want to use threads at the moment ;-)
public class AASTransferProcessManager implements TransferProcessManager<ItemDataRequest, AASTransferProcess> {

    private final ExecutorService executor;

    private final BlobPersistence blobStore;

    private final AbstractDelegate abstractDelegate;

    private final JsonUtil jsonUtil;

    public AASTransferProcessManager(final AbstractDelegate abstractDelegate, final ExecutorService executor,
            @Qualifier(JOB_BLOB_PERSISTENCE) final BlobPersistence blobStore, final JsonUtil jsonUtil) {
        this.abstractDelegate = abstractDelegate;
        this.executor = executor;
        this.blobStore = blobStore;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public TransferInitiateResponse initiateRequest(final ItemDataRequest dataRequest,
            final Consumer<String> preExecutionHandler, final Consumer<AASTransferProcess> completionCallback,
            final JobParameter jobData, final String jobId) {

        final String processId = UUID.randomUUID().toString();
        preExecutionHandler.accept(processId);

        executor.execute(getRunnable(dataRequest, completionCallback, processId, jobData, jobId));

        return new TransferInitiateResponse(processId, ResponseStatus.OK);
    }

    private Runnable getRunnable(final ItemDataRequest dataRequest,
            final Consumer<AASTransferProcess> transferProcessCompleted, final String processId,
            final JobParameter jobData, final String jobId) {

        return () -> {
            final AASTransferProcess aasTransferProcess = new AASTransferProcess(processId, dataRequest.getDepth());

            final PartChainIdentificationKey itemId = dataRequest.getItemId();

            log.info("Starting processing Digital Twin Registry with itemId {}", itemId);
            final ItemContainer itemContainer = abstractDelegate.process(ItemContainer.builder(), jobData,
                    aasTransferProcess, itemId);
            itemContainer.addJobId(jobId);
            storeItemContainer(processId, itemContainer);

            transferProcessCompleted.accept(aasTransferProcess);
        };
    }

    private void storeItemContainer(final String processId, final ItemContainer itemContainer) {
        try {
            blobStore.putBlob(processId, jsonUtil.asString(itemContainer).getBytes(StandardCharsets.UTF_8));
        } catch (BlobPersistenceException e) {
            log.error("Unable to store AAS result", e);
        }
    }

}
