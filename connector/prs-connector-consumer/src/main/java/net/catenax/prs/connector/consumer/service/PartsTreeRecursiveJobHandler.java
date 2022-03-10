//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.consumer.service;


import lombok.RequiredArgsConstructor;
import net.catenax.prs.connector.consumer.configuration.ConsumerConfiguration;
import net.catenax.prs.connector.job.MultiTransferJob;
import net.catenax.prs.connector.job.RecursiveJobHandler;
import net.catenax.prs.connector.requests.PartsTreeRequest;
import net.catenax.prs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;

import java.util.stream.Stream;

/**
 * Implementation of {@link RecursiveJobHandler} that retrieves
 * parts trees from potentially multiple calls to PRS API behind
 * multiple EDC Providers, and assembles their outputs into
 * one overall parts tree.
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement") // Monitor doesn't offer guard statements
public class PartsTreeRecursiveJobHandler implements RecursiveJobHandler {

    /**
     * Logger.
     */
    private final Monitor monitor;
    /**
     * Storage account name.
     */
    private final ConsumerConfiguration configuration;
    /**
     * Json Converter.
     */
    private final JsonUtil jsonUtil;
    /**
     * Recursive retrieval logic implementation.
     */
    private final PartsTreeRecursiveLogic logic;

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<DataRequest> initiate(final MultiTransferJob job) {
        monitor.info("Initiating recursive retrieval for Job " + job.getJobId());
        final PartsTreeRequest partsTreeRequest = getPartsTreeRequest(job);
        return logic.createInitialPartsTreeRequest(partsTreeRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<DataRequest> recurse(final MultiTransferJob job, final TransferProcess transferProcess) {
        monitor.info("Proceeding with recursive retrieval for Job " + job.getJobId());

        final var requestTemplate = getPartsTreeRequest(job);
        return logic.createSubsequentPartsTreeRequests(transferProcess, requestTemplate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete(final MultiTransferJob job) {
        monitor.info("Completed retrieval for Job " + job.getJobId());
        final var completedTransfers = job.getCompletedTransfers();
        final var targetAccountName = configuration.getStorageAccountName();
        final var targetContainerName = job.getJobData().get(ConsumerService.CONTAINER_NAME_KEY);
        final var targetBlobName = job.getJobData().get(ConsumerService.DESTINATION_PATH_KEY);
        logic.assemblePartialPartTreeBlobs(completedTransfers, targetAccountName, targetContainerName, targetBlobName);
    }

    private PartsTreeRequest getPartsTreeRequest(final MultiTransferJob job) {
        final var partsTreeRequestAsString = job.getJobData().get(ConsumerService.PARTS_REQUEST_KEY);
        return jsonUtil.fromString(partsTreeRequestAsString, PartsTreeRequest.class);
    }
}
