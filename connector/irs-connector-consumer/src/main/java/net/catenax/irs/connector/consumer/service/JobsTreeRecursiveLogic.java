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
import net.catenax.irs.component.ChildItem;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.connector.constants.IrsConnectorConstants;
import net.catenax.irs.connector.requests.JobsTreeByCatenaXIdRequest;
import net.catenax.irs.connector.requests.JobsTreeRequest;
import net.catenax.irs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.common.azure.BlobStoreApi;
import org.eclipse.dataspaceconnector.schema.azure.AzureBlobStoreSchema;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
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
public class JobsTreeRecursiveLogic {

    /**
     * Logger.
     */
    private final Monitor monitor;
    /**
     * Blob store API.
     */
    private final BlobStoreApi blobStoreApi;
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
    private final JobsTreesAssembler assembler;

    /**
     * Generates an EDC {@link DataRequest} populated for calling a Provider to invoke the IRS API
     * to retrieve the first partial parts tree.
     *
     * @param jobsTreeRequest client request.
     * @return a {@link DataRequest} if the requested Part ID was resolved in the registry,
     * otherwise empty.
     */
    /* package */ Stream<DataRequest> createInitialPartsTreeRequest(final JobsTreeRequest jobsTreeRequest) {
        final var childItem = toChildItem(jobsTreeRequest.getByObjectIdRequest());
        final var irsRequest = jobsTreeRequest.getByObjectIdRequest();
        final var requestContext = DataRequestFactory.RequestContext.builder()
                .requestTemplate(jobsTreeRequest)
                .childCatenaXId(childItem.getChildCatenaXId())
                .depth(irsRequest.getDepth())
                .build();
        return dataRequestFactory.createRequests(requestContext, childItem);
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
            final JobsTreeRequest requestTemplate) {
        final var previousUrl = transferProcess.getDataRequest().getConnectorAddress();
        final var requestAsString = transferProcess.getDataRequest().getProperties().get(IrsConnectorConstants.DATA_REQUEST_IRS_REQUEST_PARAMETERS);
        final var request = jsonUtil.fromString(requestAsString, JobsTreeByCatenaXIdRequest.class);
        final var queriedJobId = toChildItem(request).getChildCatenaXId();
        final var blob = downloadPartialPartsTree(transferProcess);
        final var childItem = jsonUtil.fromString(new String(blob), ChildItem.class);

        final var requestContext = DataRequestFactory.RequestContext.builder()
                .requestTemplate(requestTemplate)
                .previousUrlOrNull(previousUrl)
                .depth(request.getDepth())
                .childCatenaXId(queriedJobId)
                .relationship(Optional.ofNullable(childItem).orElse(childItem))
                .build();
        return dataRequestFactory.createRequests(requestContext, childItem);
    }

    /**
     * Assembles multiple partial parts trees into one overall parts tree.
     *
     * @param completedTransfers  the completed transfer processes, containing the location of the
     *                            blobs with partial parts trees.
     * @param targetAccountName   Storage account name to store overall parts tree.
     * @param targetContainerName Storage container name to store overall parts tree.
     * @param targetBlobName      Storage blob name to store overall parts tree.
     */
    /* package */ void assemblePartialPartTreeBlobs(
            final List<TransferProcess> completedTransfers,
            final String targetAccountName,
            final String targetContainerName,
            final String targetBlobName) {
        final var partialTrees = completedTransfers.stream()
                .map(this::downloadPartialPartsTree)
                .map(payload -> jsonUtil.fromString(new String(payload), Jobs.class));
        final var assembledTree = assembler.retrieveJobsTrees(partialTrees);
        final var blob = jsonUtil.asString(assembledTree).getBytes(StandardCharsets.UTF_8);

        monitor.info(format("Uploading assembled parts tree to %s/%s/%s",
                targetAccountName,
                targetContainerName,
                targetBlobName
        ));
        blobStoreApi.putBlob(targetAccountName, targetContainerName, targetBlobName, blob);
    }

    private byte[] downloadPartialPartsTree(final TransferProcess transfer) {
        final var destination = transfer.getDataRequest().getDataDestination();
        final var sourceAccountName = destination.getProperty(AzureBlobStoreSchema.ACCOUNT_NAME);
        final var sourceContainerName = destination.getProperty(AzureBlobStoreSchema.CONTAINER_NAME);
        final var sourceBlobName = transfer.getDataRequest().getProperties().get(IrsConnectorConstants.DATA_REQUEST_IRS_DESTINATION_PATH);
        monitor.info(format("Downloading partial parts tree from blob at %s/%s/%s",
                sourceAccountName,
                sourceContainerName,
                sourceBlobName
        ));
        return blobStoreApi.getBlob(sourceAccountName, sourceContainerName, sourceBlobName);
    }

    private ChildItem toChildItem(final JobsTreeByCatenaXIdRequest jobsTreeRequest) {
        final ZoneOffset zoneOffSet = ZoneOffset.of("+01:00");
        final var childItem = new ChildItem(null, null, null, null, null);
        childItem.toBuilder().childCatenaXId(jobsTreeRequest.getChildCatenaXId());
        childItem.toBuilder().lastModifiedOn(jobsTreeRequest.getLastModifiedOn().toInstant(zoneOffSet));
        return childItem;
    }
}
