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

import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.connector.job.MultiTransferJob;
import org.eclipse.tractusx.irs.connector.job.RecursiveJobHandler;
import org.eclipse.tractusx.irs.dto.JobParameter;

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
        log.info("Initiating request for job {}", job.getJobIdString());
        final var partId = job.getJobParameter().getRootItemId();
        final var dataRequest = ItemDataRequest.rootNode(partId);
        return Stream.of(dataRequest);
    }

    @Override
    public Stream<ItemDataRequest> recurse(final MultiTransferJob job, final AASTransferProcess transferProcess) {
        log.info("Starting recursive request for job {}", job.getJobIdString());

        final JobParameter jobParameter = job.getJobParameter();
        final int expectedDepth = jobParameter.getTreeDepth();
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
        log.info("Completed retrieval for Job {}", job.getJobIdString());
        final var completedTransfers = job.getCompletedTransfers();
        final var targetBlobName = job.getJob().getJobId();
        logic.assemblePartialItemGraphBlobs(completedTransfers, targetBlobName.toString());
    }

    private boolean expectedDepthOfTreeIsNotReached(final Integer expectedDepth, final Integer currentDepth) {
        log.info("Expected tree depth is {}, current depth is {}", expectedDepth, currentDepth);
        return currentDepth < expectedDepth;
    }
}
