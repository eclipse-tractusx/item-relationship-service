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

import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.connector.job.TransferProcess;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.util.JsonUtil;

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
@RequiredArgsConstructor
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

    /**
     * Assembles multiple partial item graph into one overall item graph.
     *
     * @param completedTransfers the completed transfer processes, containing the location of the
     *                           blobs with partial item graph.
     * @param targetBlobName     Storage blob name to store overall item graph.
     */
    /* package */ void assemblePartialItemGraphBlobs(final List<TransferProcess> completedTransfers,
            final String targetBlobName) {
        final var partialTrees = completedTransfers.stream()
                                                   .map(this::downloadPartialItemGraphBlobs)
                                                   .map(payload -> jsonUtil.fromString(new String(payload, StandardCharsets.UTF_8),
                                                           ItemContainer.class));
        final var assembledTree = assembler.retrieveItemTrees(partialTrees);
        final String json = jsonUtil.asString(assembledTree);
        log.info("Storing assembled item graph: {}", json);
        final var blob = json.getBytes(StandardCharsets.UTF_8);

        log.info("Uploading assembled item graph to {}", targetBlobName);
        try {
            blobStoreApi.putBlob(targetBlobName, blob);
        } catch (BlobPersistenceException e) {
            log.error("Could not store blob", e);
        }
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
