/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
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
