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

import static net.catenax.irs.dtos.IrsCommonConstants.DEPTH_ID_KEY;
import static net.catenax.irs.dtos.IrsCommonConstants.ROOT_ITEM_ID_KEY;

import java.util.Map;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.connector.job.RecursiveJobHandler;

/**
 * Recursive job handler for AAS data
 */
@Slf4j
public class AASRecursiveJobHandler implements RecursiveJobHandler<ItemDataRequest, AASTransferProcess> {

    private final TreeRecursiveLogic logic;

    public AASRecursiveJobHandler(final TreeRecursiveLogic logic) {
        this.logic = logic;
    }

    @Override
    public Stream<ItemDataRequest> initiate(final MultiTransferJob job) {
        log.info("Initiating request for job {}", job.getJob().getJobId().toString());
        final var partId = job.getJobData().get(ROOT_ITEM_ID_KEY);
        final var dataRequest = ItemDataRequest.rootNode(partId);
        return Stream.of(dataRequest);
    }

    @Override
    public Stream<ItemDataRequest> recurse(final MultiTransferJob job, final AASTransferProcess transferProcess) {
        log.info("Starting recursive request for job {}", job.getJob().getJobId().toString());

        final Integer expectedDepth = getExpectedTreeDepth(job.getJobData());
        final Integer currentDepth = transferProcess.getDepth();

        if (expectedDepthOfTreeIsNotReached(expectedDepth, currentDepth)) {
            return transferProcess.getIdsToProcess()
                                  .stream()
                                  .map(itemId -> ItemDataRequest.nextDepthNode(itemId, currentDepth));
        }

        return Stream.empty();
    }



    @Override
    public void complete(final MultiTransferJob job) {
        log.info("Completed retrieval for Job {}", job.getJob().getJobId().toString());
        final var completedTransfers = job.getCompletedTransfers();
        final var targetBlobName = job.getJob().getJobId();
        logic.assemblePartialItemGraphBlobs(completedTransfers, targetBlobName.toString());
    }

    private Integer getExpectedTreeDepth(final Map<String, String> jobData) {
        return Integer.parseInt(jobData.get(DEPTH_ID_KEY));
    }

    private boolean expectedDepthOfTreeIsNotReached(final Integer expectedDepth, final Integer currentDepth) {
        log.info("Expected tree depth is {}, current depth is {}", expectedDepth, currentDepth);
        return currentDepth < expectedDepth;
    }
}
