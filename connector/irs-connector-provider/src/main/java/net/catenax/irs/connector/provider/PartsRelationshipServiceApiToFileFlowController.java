//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.catenax.irs.client.ApiException;
import net.catenax.irs.client.api.PartsRelationshipServiceApi;
import net.catenax.irs.client.model.PartRelationshipsWithInfos;
import net.catenax.irs.connector.requests.PartsTreeByObjectIdRequest;
import org.eclipse.dataspaceconnector.schema.azure.AzureBlobStoreSchema;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowController;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowInitiateResponse;
import org.eclipse.dataspaceconnector.spi.transfer.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;

import static java.lang.String.format;
import static net.catenax.irs.connector.constants.IrsConnectorConstants.DATA_REQUEST_IRS_DESTINATION_PATH;
import static net.catenax.irs.connector.constants.IrsConnectorConstants.DATA_REQUEST_IRS_REQUEST_PARAMETERS;

/**
 * Handles a data flow to call IRS API and save the result to a file.
 */
@SuppressWarnings("PMD.GuardLogStatement") // Monitor doesn't offer guard statements
public class PartsRelationshipServiceApiToFileFlowController implements DataFlowController {

    /**
     * JSON serializer / deserializer.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Logger.
     */
    private final Monitor monitor;

    /**
     * Client stub to call IRS API.
     */
    private final PartsRelationshipServiceApi irsClient;

    /**
     * Blob storage client
     */
    private final BlobStorageClient blobStorageClient;

    /**
     * @param monitor   Logger
     * @param irsClient Client used to call IRS API
     * @param blobStorageClient Blob storage client
     */
    public PartsRelationshipServiceApiToFileFlowController(final Monitor monitor, final PartsRelationshipServiceApi irsClient, final BlobStorageClient blobStorageClient) {
        this.monitor = monitor;
        this.irsClient = irsClient;
        this.blobStorageClient = blobStorageClient;
    }

    @Override
    public boolean canHandle(final DataRequest dataRequest) {
        return AzureBlobStoreSchema.TYPE.equalsIgnoreCase(dataRequest.getDataDestination().getType());
    }

    @Override
    public DataFlowInitiateResponse initiateFlow(final DataRequest dataRequest) {
        // verify partsTreeRequest
        final var serializedRequest = dataRequest.getProperties().get(DATA_REQUEST_IRS_REQUEST_PARAMETERS);
        final var destinationPath = dataRequest.getProperties().get(DATA_REQUEST_IRS_DESTINATION_PATH);

        // Read API Request from message payload
        PartsTreeByObjectIdRequest request;
        monitor.info("Received request " + serializedRequest + " with destination path " + destinationPath);
        try {
            request = MAPPER.readValue(serializedRequest, PartsTreeByObjectIdRequest.class);
            monitor.info("request with " + request.getObjectIDManufacturer());
        } catch (JsonProcessingException e) {
            final String message = "Error deserializing " + PartsTreeByObjectIdRequest.class.getName() + ": " + e.getMessage();
            monitor.severe(message, e);
            return new DataFlowInitiateResponse(ResponseStatus.FATAL_ERROR, message);
        }

        // call API
        final PartRelationshipsWithInfos response;
        try {
            response = irsClient.getPartsTreeByOneIdAndObjectId(request.getOneIDManufacturer(), request.getObjectIDManufacturer(),
                    request.getView(), request.getAspect(), request.getDepth());
        } catch (ApiException e) {
            final String message = "Error with API call: " + e.getMessage();
            monitor.severe(message, e);
            return new DataFlowInitiateResponse(ResponseStatus.FATAL_ERROR, message);
        }

        // serialize API response
        final String partRelationshipsWithInfos;
        try {
            partRelationshipsWithInfos = MAPPER.writeValueAsString(response);
            // We suspect the connectorSystemTests to be flaky when running right after the deployment workflow.
            // The issue is hard to reproduce. Login the IRS response, to help when this will happen again.
            monitor.info(format("partRelationshipsWithInfos: %s", partRelationshipsWithInfos));
        } catch (JsonProcessingException e) {
            final String message = "Error serializing API response: " + e.getMessage();
            monitor.severe(message);
            return new DataFlowInitiateResponse(ResponseStatus.FATAL_ERROR, message);
        }

        // write API response to blob storage
        try {
            blobStorageClient.writeToBlob(dataRequest.getDataDestination(), destinationPath, partRelationshipsWithInfos);
        } catch (EdcException e) {
            final String message = "Data transfer to Azure Blob Storage failed";
            monitor.severe(message, e);
            return new DataFlowInitiateResponse(ResponseStatus.FATAL_ERROR, message);
        }

        return DataFlowInitiateResponse.OK;
    }
}
