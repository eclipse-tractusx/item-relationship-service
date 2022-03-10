//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.consumer.service;


import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import net.catenax.prs.client.model.PartId;
import net.catenax.prs.client.model.PartRelationship;
import net.catenax.prs.connector.constants.PrsConnectorConstants;
import net.catenax.prs.connector.consumer.configuration.ConsumerConfiguration;
import net.catenax.prs.connector.consumer.registry.StubRegistryClient;
import net.catenax.prs.connector.requests.PartsTreeRequest;
import net.catenax.prs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.schema.azure.AzureBlobStoreSchema;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Generates EDC {@link DataRequest}s populated for calling Providers to invoke the PRS API
 * to retrieve partial parts trees.
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement") // Monitor doesn't offer guard statements
public class DataRequestFactory {

    /**
     * The name of the blob to be created in each Provider call.
     * The suffix ".complete" is required in order to signal to the
     * EDC ObjectContainerStatusChecker that a transfer is complete.
     * The checker lists blobs on the destination container until a blob with this suffix
     * in the name is present.
     */
    /* package */ static final String PARTIAL_PARTS_TREE_BLOB_NAME = "partialPartsTree.complete";
    /**
     * Logger.
     */
    private final Monitor monitor;
    /**
     * Storage account name.
     */
    private final ConsumerConfiguration configuration;
    /**
     * Json Converter.
     */
    private final JsonUtil jsonUtil;
    /**
     * Registry client to resolve Provider URL by Part ID.
     */
    private final StubRegistryClient registryClient;

    /**
     * Generates EDC {@link DataRequest}s populated for calling Providers to invoke the PRS API
     * to retrieve partial parts trees.
     * <p>
     * If the {@code previousUrlOrNull} argument is non-{@code null}, this method will not return
     * data requests pointing to that Provider URL. This ensures only parts tree queries pointing
     * to other providers are issued in subsequent recursive retrievals.
     *
     * @param requestContext current PRS request data.
     * @param partIds        the parts for which to retrieve partial parts trees.
     * @return a {@link DataRequest} for each item {@code partIds} for which the Provider URL
     * was resolves in the registry <b>and</b> is not identical to {@code previousUrlOrNull},
     * that allows retrieving the partial parts tree for the given part.
     */
    /* package */ Stream<DataRequest> createRequests(
            final RequestContext requestContext,
            final Stream<PartId> partIds) {
        return partIds
                .flatMap(partId -> createRequest(requestContext, partId).stream());
    }

    private Optional<DataRequest> createRequest(
            final RequestContext requestContext,
            final PartId partId) {

        // Resolve Provider URL for part from registry
        final var registryResponse = registryClient.getUrl(partId);
        if (registryResponse.isEmpty()) {
            monitor.info(format("Registry did not resolve %s", partId));
            return Optional.empty();
        }

        final var providerUrlForPartId = registryResponse.get();

        // If provider URL has not changed between requests, then children
        // for that part have already been fetched in the previous request.
        if (Objects.equals(requestContext.previousUrlOrNull, providerUrlForPartId)) {
            monitor.debug(format("Not issuing a new request for %s, URL unchanged", partId));
            return Optional.empty();
        }

        int remainingDepth = requestContext.depth;
        if (requestContext.previousUrlOrNull != null) {
            final var usedDepth = Dijkstra.shortestPathLength(requestContext.queryResultRelationships, requestContext.queriedPartId, partId)
                    .orElseThrow(() -> new EdcException("Unconnected parts returned by PRS"));
            remainingDepth -= usedDepth;
            if (remainingDepth <= 0) {
                monitor.debug(format("Not issuing a new request for %s, depth exhausted", partId));
                return Optional.empty();
            }
        }

        final var newPrsRequest = requestContext.requestTemplate.getByObjectIdRequest().toBuilder()
                .oneIDManufacturer(partId.getOneIDManufacturer())
                .objectIDManufacturer(partId.getObjectIDManufacturer())
                .depth(remainingDepth)
                .build();

        final var prsRequestAsString = jsonUtil.asString(newPrsRequest);

        monitor.info(format("Mapped data request to url: %s, previous depth: %d, new depth: %d",
                providerUrlForPartId,
                requestContext.depth,
                remainingDepth));

        return Optional.of(DataRequest.Builder.newInstance()
                .id(UUID.randomUUID().toString()) //this is not relevant, thus can be random
                .connectorAddress(providerUrlForPartId) //the address of the provider connector
                .protocol("ids-rest") //must be ids-rest
                .connectorId("consumer")
                .dataEntry(DataEntry.Builder.newInstance() //the data entry is the source asset
                        .id(PrsConnectorConstants.PRS_REQUEST_ASSET_ID)
                        .policyId(PrsConnectorConstants.PRS_REQUEST_POLICY_ID)
                        .build())
                .dataDestination(DataAddress.Builder.newInstance()
                        .type(AzureBlobStoreSchema.TYPE) //the provider uses this to select the correct DataFlowController
                        .property(AzureBlobStoreSchema.ACCOUNT_NAME, configuration.getStorageAccountName())
                        .build())
                .properties(Map.of(
                        PrsConnectorConstants.DATA_REQUEST_PRS_REQUEST_PARAMETERS, prsRequestAsString,
                        PrsConnectorConstants.DATA_REQUEST_PRS_DESTINATION_PATH, PARTIAL_PARTS_TREE_BLOB_NAME
                ))
                .managedResources(true)
                .build());
    }

    /**
     * Parameter Object used to pass information about the previous PRS request
     * and its results, to the {@link #createRequest(RequestContext, PartId)}
     * method for creatin subsequent PRS requests.
     */
    @Value
    @Builder
    /* package */ static class RequestContext {
        /**
         * The original PRS request received from the client.
         */
        private PartsTreeRequest requestTemplate;
        /**
         * the Provider URL used for retrieving the {@code partIds}, or {@code null} for the first retrieval.
         */
        private String previousUrlOrNull;
        /**
         * The queried partId in the {@link #requestTemplate}.
         */
        private PartId queriedPartId;
        /**
         * The relationships returned in the current query response.
         */
        @Singular
        private Collection<PartRelationship> queryResultRelationships;
        /**
         * The query depth used in the current query.
         */
        private int depth;
    }
}
