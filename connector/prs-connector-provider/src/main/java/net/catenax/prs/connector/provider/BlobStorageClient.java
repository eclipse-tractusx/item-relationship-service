//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.provider;

import net.catenax.prs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.provision.azure.AzureSasToken;
import org.eclipse.dataspaceconnector.schema.azure.AzureBlobStoreSchema;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;

import java.io.ByteArrayInputStream;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

/**
 * Blob storage client for consumer connector
 */
public class BlobStorageClient {
    /**
     * Logger.
     */
    private final Monitor monitor;
    /**
     * Type manager to deserialize SAS token
     */
    private final JsonUtil jsonUtil;
    /**
     * Vault to retrieve secret to access blob storage
     */
    private final Vault vault;
    /**
     * Factory for BlobClient
     */
    private final BlobClientFactory blobClientFactory;

    /**
     * @param monitor           Logger
     * @param jsonUtil          Type manager
     * @param vault             Vault
     * @param blobClientFactory Blob client factory
     */
    public BlobStorageClient(final Monitor monitor, final JsonUtil jsonUtil, final Vault vault, final BlobClientFactory blobClientFactory) {
        this.monitor = monitor;
        this.jsonUtil = jsonUtil;
        this.vault = vault;
        this.blobClientFactory = blobClientFactory;
    }

    /**
     * Writes data into a blob
     *
     * @param destination Data destination specifying account and container name
     * @param blobName    Blob name
     * @param data        Data to write
     */
    public void writeToBlob(final DataAddress destination, final String blobName, final String data) {
        final var containerName = destination.getProperty(AzureBlobStoreSchema.CONTAINER_NAME);
        final var accountName = destination.getProperty(AzureBlobStoreSchema.ACCOUNT_NAME);
        final var destSecretName = destination.getKeyName();

        final var sasToken = getAzureSasToken(destSecretName);
        final var blobClient = blobClientFactory.getBlobClient(blobName, containerName, accountName, sasToken);
        final byte[] bytes = data.getBytes();

        blobClient.upload(new ByteArrayInputStream(bytes), bytes.length, true);

        monitor.info(format(
                "File uploaded to Azure storage account '%s', container '%s', blob '%s'",
                accountName, containerName, blobName));
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private AzureSasToken getAzureSasToken(final String destSecretName) {
        final var secret = ofNullable(vault.resolveSecret(destSecretName))
                .orElseThrow(() -> new EdcException("Can not retrieve SAS token"));
        return jsonUtil.fromString(secret, AzureSasToken.class);
    }
}
