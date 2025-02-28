/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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

import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.connector.job.MultiTransferJob;
import org.eclipse.tractusx.irs.connector.job.RecursiveJobHandler;

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
        final var globalAssetId = job.getGlobalAssetId();
        final var bpn = job.getJobParameter().getBpn();

        PartChainIdentificationKey.PartChainIdentificationKeyBuilder builder = PartChainIdentificationKey.builder().bpn(bpn);

        if (StringUtils.isNotBlank(globalAssetId)) {
            builder = builder.globalAssetId(globalAssetId);
        } else {
            builder.identifier(job.getAasId());
        }

        return Stream.of(ItemDataRequest.rootNode(builder.build()));
    }

    @Override
    public Stream<ItemDataRequest> recurse(final MultiTransferJob job, final AASTransferProcess transferProcess) {
        log.info("Starting recursive request for job {}", job.getJobIdString());

        return transferProcess.getIdsToProcess()
                              .stream()
                              .map(itemId -> ItemDataRequest.nextDepthNode(itemId, transferProcess.getDepth()));
    }

    @Override
    public void complete(final MultiTransferJob job) {
        log.info("Completed retrieval for Job {}", job.getJobIdString());
        final var completedTransfers = job.getCompletedTransfers();
        final var targetBlobName = job.getJob().getId();
        logic.assemblePartialItemGraphBlobs(completedTransfers, targetBlobName.toString());
    }

}
