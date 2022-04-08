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

import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.connector.job.RecursiveJobHandler;

/**
 * Recursive job handler for AAS data
 */
@Slf4j
public class AASRecursiveJobHandler implements RecursiveJobHandler<ItemDataRequest, AASTransferProcess> {
    /**
     * Job Data key for root item ID
     */
    public static final String ROOT_ITEM_ID_KEY = "root.item.id.key";

    private final TreeRecursiveLogic logic;

    public AASRecursiveJobHandler(final TreeRecursiveLogic logic) {
        this.logic = logic;
    }

    @Override
    public Stream<ItemDataRequest> initiate(final MultiTransferJob job) {
        log.info("Initiating request for job {}", job.getJobId());
        final var partId = job.getJobData().get("partId");
        final var dataRequest = new ItemDataRequest(partId);
        return Stream.of(dataRequest);
    }

    @Override
    public Stream<ItemDataRequest> recurse(final MultiTransferJob job, final AASTransferProcess transferProcess) {
        log.info("Starting recursive request for job {}", job.getJobId());
        return transferProcess.getIdsToProcess().stream().map(ItemDataRequest::new);
    }

    @Override
    public void complete(final MultiTransferJob job) {
        log.info("Completed retrieval for Job {}", job.getJobId());
        final var completedTransfers = job.getCompletedTransfers();
        final var targetBlobName = job.getJobId();
        logic.assemblePartialPartTreeBlobs(completedTransfers, targetBlobName);
    }
}
