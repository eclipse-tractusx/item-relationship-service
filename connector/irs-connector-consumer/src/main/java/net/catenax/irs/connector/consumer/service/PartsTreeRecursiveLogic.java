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
import net.catenax.irs.client.model.PartId;
import net.catenax.irs.client.model.PartRelationship;
import net.catenax.irs.client.model.PartRelationshipsWithInfos;
import net.catenax.irs.connector.constants.IrsConnectorConstants;
import net.catenax.irs.connector.consumer.persistence.BlobPersistence;
import net.catenax.irs.connector.consumer.persistence.BlobPersistenceException;
import net.catenax.irs.connector.requests.PartsTreeRequest;
import net.catenax.irs.connector.requests.PartsTreeByObjectIdRequest;
import net.catenax.irs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;

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
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement") // Monitor doesn't offer guard statements
public class PartsTreeRecursiveLogic {

    /**
     * Logger.
     */
    private final Monitor monitor;
    /**
     * Blob store API.
     */
    private final BlobPersistence blobStoreApi;
    /**
     * Json Converter.
     */
    private final JsonUtil jsonUtil;
    /**
     * Generate for IRS Data requests.
     */
    private final DataRequestFactory dataRequestFactory;
    /**
     * Assembles partial parts trees.
     */
    private final PartsTreesAssembler assembler;

    /**
     * Generates an EDC {@link DataRequest} populated for calling a Provider to invoke the IRS API
     * to retrieve the first partial parts tree.
     *
     * @param partsTreeRequest client request.
     * @return a {@link DataRequest} if the requested Part ID was resolved in the registry,
     * otherwise empty.
     */
    /* package */ Stream<DataRequest> createInitialPartsTreeRequest(final PartsTreeRequest partsTreeRequest) {
        final var partId = toPartId(partsTreeRequest.getByObjectIdRequest());
        final var irsRequest = partsTreeRequest.getByObjectIdRequest();
        final var requestContext = DataRequestFactory.RequestContext.builder()
                .requestTemplate(partsTreeRequest)
                .queriedPartId(partId)
                .depth(irsRequest.getDepth())
                .build();
        return dataRequestFactory.createRequests(requestContext, Stream.of(partId));
    }

    /**
     * Generates EDC {@link DataRequest}s populated for calling a Provider to invoke the IRS API
     * to retrieve subsequent partial parts trees,
     * based on the child Part IDs returned by previous requests.
     * <p>
     * In this increment, the implementation returns empty.
     *
     * @param transferProcess the completed transfer process, containing the location of the
     *                        blob with the partial parts tree.
     * @param requestTemplate client request.
     * @return {@link DataRequest}s for each child Part ID that resolves to a different Provider URL.
     */
    /* package */ Stream<DataRequest> createSubsequentPartsTreeRequests(
            final TransferProcess transferProcess,
            final PartsTreeRequest requestTemplate) {
        final var previousUrl = transferProcess.getDataRequest().getConnectorAddress();
        final var requestAsString = transferProcess.getDataRequest().getProperties().get(IrsConnectorConstants.DATA_REQUEST_IRS_REQUEST_PARAMETERS);
        final var request = jsonUtil.fromString(requestAsString, PartsTreeByObjectIdRequest.class);
        final var queriedPartId = toPartId(request);
        final var blob = downloadPartialPartsTree(transferProcess);
        final var tree = jsonUtil.fromString(new String(blob), PartRelationshipsWithInfos.class);

        final var relationships = Optional
                .ofNullable(tree.getRelationships())
                .orElse(List.of());
        final var partIds = relationships.stream()
                .map(PartRelationship::getChild);
        final var requestContext = DataRequestFactory.RequestContext.builder()
                .requestTemplate(requestTemplate)
                .previousUrlOrNull(previousUrl)
                .depth(request.getDepth())
                .queriedPartId(queriedPartId)
                .queryResultRelationships(Optional.ofNullable(tree.getRelationships()).orElse(List.of()))
                .build();
        return dataRequestFactory.createRequests(requestContext, partIds);
    }

    /**
     * Assembles multiple partial parts trees into one overall parts tree.
     *
     * @param completedTransfers  the completed transfer processes, containing the location of the
     *                            blobs with partial parts trees.
     * @param targetBlobName      Storage blob name to store overall parts tree.
     */
    /* package */ void assemblePartialPartTreeBlobs(
            final List<TransferProcess> completedTransfers,
            final String targetBlobName) {
        final var partialTrees = completedTransfers.stream()
                .map(this::downloadPartialPartsTree)
                .map(payload -> jsonUtil.fromString(new String(payload), PartRelationshipsWithInfos.class));
        final var assembledTree = assembler.retrievePartsTrees(partialTrees);
        final var blob = jsonUtil.asString(assembledTree).getBytes(StandardCharsets.UTF_8);

        monitor.info(format("Uploading assembled parts tree to %s",
                targetBlobName
        ));
        try {
            blobStoreApi.putBlob(targetBlobName, blob);
        } catch (BlobPersistenceException e) {
            monitor.severe("Could not store blob", e);
        }
    }

    private byte[] downloadPartialPartsTree(final TransferProcess transfer) {
        final var sourceBlobName = transfer.getDataRequest().getProperties().get(IrsConnectorConstants.DATA_REQUEST_IRS_DESTINATION_PATH);
        monitor.info(format("Downloading partial parts tree from blob at %s",
                sourceBlobName
        ));
        try {
            return blobStoreApi.getBlob(sourceBlobName);
        } catch (BlobPersistenceException e) {
            monitor.severe("Could not load blob", e);
            return new byte[0];
        }
    }

    private PartId toPartId(final PartsTreeByObjectIdRequest partsTreeRequest) {
        final var partId = new PartId();
        partId.setOneIDManufacturer(partsTreeRequest.getOneIDManufacturer());
        partId.setObjectIDManufacturer(partsTreeRequest.getObjectIDManufacturer());
        return partId;
    }
}
