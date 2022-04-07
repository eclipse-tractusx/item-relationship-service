//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.consumer.service;


import lombok.RequiredArgsConstructor;
import net.catenax.irs.connector.consumer.configuration.ConsumerConfiguration;
import net.catenax.irs.connector.consumer.persistence.BlobPersistence;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.JobOrchestrator;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.requests.JobsTreeRequest;
import net.catenax.irs.connector.requests.PartsTreeRequest;
import net.catenax.irs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

import java.util.Map;
import java.util.Optional;

/**
 * Consumer Service.
 * Provides job management.
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement") // Monitor doesn't offer guard statements
public class ConsumerService {
    /**
     * Key for the serialized request stored in the Job Data.
     */
    /* package */ static final String PARTS_REQUEST_KEY = "ser-request";
    /**
     * Key for the Azure Storage container name stored in the Job Data.
     */
    /* package */ static final String CONTAINER_NAME_KEY = "blob-container-name";
    /**
     * Key for the Azure Storage blob name stored in the Job Data.
     */
    /* package */ static final String DESTINATION_PATH_KEY = "blob-destination-path";
    /**
     * Logger.
     */
    private final Monitor monitor;
    /**
     * JSON object mapper.
     */
    private final JsonUtil jsonUtil;
    /**
     * Job Orchestrator.
     */
    private final JobStore jobStore;
    /**
     * Job Orchestrator.
     */
    private final JobOrchestrator jobOrchestrator;
    /**
     * Blob store API
     */
    private final BlobPersistence blobStoreApi;
    /**
     * Consumer configuration
     */
    private final ConsumerConfiguration consumerConfiguration;

    /**
     * Endpoint to trigger a request, so that a file get copied into a specific destination.
     *
     * @param request Request parameters.
     * @return TransferInitiateResponse with process id.
     */
    public JobInitiateResponse retrievePartsTree(final PartsTreeRequest request) {
        monitor.info("Received request");

        final String serializedRequest = jsonUtil.asString(request);

        final String destinationPath = "partsTree.json";

        return jobOrchestrator.startJob(
                Map.of(
                        PARTS_REQUEST_KEY, serializedRequest,
                        DESTINATION_PATH_KEY, destinationPath
                )
        );
    }

    /**
     * Endpoint to trigger a request, so that a file get copied into a specific destination.
     *
     * @param request Request parameters.
     * @return TransferInitiateResponse with process id.
     */
    public JobInitiateResponse retrieveJobsTree(final JobsTreeRequest request) {
        monitor.info("Received request");

        final String serializedRequest = jsonUtil.asString(request);

        final var storageAccountName = consumerConfiguration.getStorageAccountName();
        final String containerName = UUID.randomUUID().toString();
        final String destinationPath = "partsTree.json";
        blobStoreApi.createContainer(storageAccountName, containerName);

        return jobOrchestrator.startJob(
                Map.of(
                        PARTS_REQUEST_KEY, serializedRequest,
                        CONTAINER_NAME_KEY, containerName,
                        DESTINATION_PATH_KEY, destinationPath
                )
        );
    }

    /**
     * Provides status of a job
     *
     * @param jobId If of the job
     * @return Job state
     */
    public Optional<StatusResponse> getStatus(final String jobId) {
        monitor.info("Getting status of job " + jobId);

        return jobStore.find(jobId).map(job -> {
            monitor.info("Status of job " + jobId + ":" + job.getState());
            final var response = StatusResponse.builder().status(job.getState());
            return response.build();
        });
    }
}
