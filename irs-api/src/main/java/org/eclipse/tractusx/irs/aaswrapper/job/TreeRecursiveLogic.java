/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.enums.IntegrityState;
import org.eclipse.tractusx.irs.connector.job.TransferProcess;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.services.DataIntegrityService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Retrieves item graph from potentially multiple calls to IRS API behind
 * multiple EDC Providers, and assembles their outputs into
 * one overall item graph.
 * <p>
 * In this increment, the implementation only retrieves the first level
 * item graph, as a non-recursive implementation would do. In a next
 * increment, this class will be extended to perform recursive queries
 * by querying multiple IRS API instances.
 */
@Slf4j
public class TreeRecursiveLogic {

    /**
     * Blob store API.
     */
    private final BlobPersistence blobStoreApi;
    
    /**
     * Json Converter.
     */
    private final JsonUtil jsonUtil;

    /**
     * Assembles partial parts trees.
     */
    private final ItemTreesAssembler assembler;

    private final DataIntegrityService dataIntegrityService;

    public TreeRecursiveLogic(@Qualifier(JOB_BLOB_PERSISTENCE) final BlobPersistence blobStoreApi,
            final JsonUtil jsonUtil, final ItemTreesAssembler assembler, final DataIntegrityService dataIntegrityService) {
        this.blobStoreApi = blobStoreApi;
        this.jsonUtil = jsonUtil;
        this.assembler = assembler;
        this.dataIntegrityService = dataIntegrityService;
    }

    /**
     * Assembles multiple partial item graph into one overall item graph.
     *
     * @param completedTransfers the completed transfer processes, containing the location of the
     *                           blobs with partial item graph
     * @param integrityCheck flag to check processing of integrity check
     * @param globalAssetId global asset id
     * @param targetBlobName Storage blob name to store overall item graph.
     * @return
     */
    /* package */ IntegrityState assemblePartialItemGraphBlobs(final List<TransferProcess> completedTransfers, final boolean integrityCheck,
            final String globalAssetId, final String targetBlobName) {
        final var partialTrees = completedTransfers.stream()
                                                   .map(this::downloadPartialItemGraphBlobs)
                                                   .map(payload -> jsonUtil.fromString(
                                                           new String(payload, StandardCharsets.UTF_8),
                                                           ItemContainer.class));
        final var assembledTree = assembler.retrieveItemGraph(partialTrees);
        final IntegrityState integrityState = integrityCheck ? dataIntegrityService.chainDataIntegrityIsValid(assembledTree, globalAssetId)
                : IntegrityState.INACTIVE;

        final String json = jsonUtil.asString(assembledTree);
        final var blob = json.getBytes(StandardCharsets.UTF_8);

        log.info("Uploading assembled item graph to {}", targetBlobName);
        try {
            blobStoreApi.putBlob(targetBlobName, blob);
        } catch (BlobPersistenceException e) {
            log.error("Could not store blob", e);
        }
        return integrityState;
    }

    private byte[] downloadPartialItemGraphBlobs(final TransferProcess transfer) {
        log.info("Downloading partial item graph from blob at {}", transfer.getId());
        try {
            return blobStoreApi.getBlob(transfer.getId()).orElse(new byte[0]);
        } catch (BlobPersistenceException e) {
            log.error("Could not load blob", e);
            return new byte[0];
        }
    }

}
