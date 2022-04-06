//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper;

import static java.lang.String.format;

import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.connector.job.TransferProcess;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.util.JsonUtil;

/**
 * Retrieves parts trees from potentially multiple calls to IRS API behind
 * multiple EDC Providers, and assembles their outputs into
 * one overall parts tree.
 * <p>
 * In this increment, the implementation only retrieves the first level
 * parts tree, as a non-recursive implementation would do. In a next
 * increment, this class will be extended to perform recursive queries
 * by querying multiple IRS API instances.
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement") // Monitor doesn't offer guard statements
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
    private final PartsTreesAssembler assembler;

    /**
     * Assembles multiple partial parts trees into one overall parts tree.
     *
     * @param completedTransfers the completed transfer processes, containing the location of the
     *                           blobs with partial parts trees.
     * @param targetBlobName     Storage blob name to store overall parts tree.
     */
    /* package */ void assemblePartialPartTreeBlobs(final List<TransferProcess> completedTransfers,
            final String targetBlobName) {
        final var partialTrees = completedTransfers.stream()
                                                   .map(this::downloadPartialPartsTree)
                                                   .map(payload -> jsonUtil.fromString(new String(payload),
                                                           ItemContainer.class));
        final var assembledTree = assembler.retrievePartsTrees(partialTrees);
        final var blob = jsonUtil.asString(assembledTree).getBytes(StandardCharsets.UTF_8);

        log.info(format("Uploading assembled parts tree to %s", targetBlobName));
        try {
            blobStoreApi.putBlob(targetBlobName, blob);
        } catch (BlobPersistenceException e) {
            log.error("Could not store blob", e);
        }
    }

    private byte[] downloadPartialPartsTree(final TransferProcess transfer) {
        log.info(format("Downloading partial parts tree from blob at %s", transfer.getId()));
        try {
            return blobStoreApi.getBlob(transfer.getId());
        } catch (BlobPersistenceException e) {
            log.error("Could not load blob", e);
            return new byte[0];
        }
    }

}
