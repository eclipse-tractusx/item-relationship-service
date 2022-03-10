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

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import org.eclipse.dataspaceconnector.provision.azure.AzureSasToken;

/**
 * Blob Client Factory
 */
public class BlobClientFactory {
    /**
     * @param blobName      Blob name
     * @param containerName Container name
     * @param accountName   Account name
     * @param sasToken      SAS Token
     * @return Blob client
     */
    public BlobClient getBlobClient(
            final String blobName,
            final String containerName,
            final String accountName,
            final AzureSasToken sasToken) {
        return new BlobClientBuilder()
                .endpoint("https://" + accountName + ".blob.core.windows.net")
                .sasToken(sasToken.getSas())
                .containerName(containerName)
                .blobName(blobName)
                .buildClient();
    }
}
