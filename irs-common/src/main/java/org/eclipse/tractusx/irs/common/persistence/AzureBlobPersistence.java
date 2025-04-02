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
package org.eclipse.tractusx.irs.common.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import lombok.extern.slf4j.Slf4j;

/**
 * BlobPersistence implementation using the min.io library
 */
@Slf4j
public class AzureBlobPersistence implements BlobPersistence {
    private final BlobContainerClient containerClient;

    public AzureBlobPersistence(final String endpoint, final String clientId, final String clientSecret,
            final String tenantId, final String containerName) {
        this(createClient(endpoint, servicePrincipalAuth(clientId, clientSecret, tenantId)), containerName);
    }

    public AzureBlobPersistence(final String connectionString, final String containerName) {
        this(createClient(connectionString), containerName);
    }

    public AzureBlobPersistence(final BlobServiceClient blobServiceClient, final String containerName) {
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!this.containerClient.exists()) {
            this.containerClient.create();
        }
    }

    private static BlobServiceClient createClient(final String endpoint, final TokenCredential credential) {
        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();
    }

    private static BlobServiceClient createClient(final String connectionString) {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    private static TokenCredential servicePrincipalAuth(final String clientId, final String clientSecret, final String tenantId) {
        return new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();
    }

    @Override
    public void putBlob(final String targetBlobName, final byte[] blob) throws BlobPersistenceException {
        final BlobClient blobClient = containerClient.getBlobClient(targetBlobName);
        final InputStream dataStream = new ByteArrayInputStream(blob);
        blobClient.upload(dataStream, blob.length, true);
    }

    @Override
    public Optional<byte[]> getBlob(final String sourceBlobName) throws BlobPersistenceException {
        final BlobClient blobClient = containerClient.getBlobClient(sourceBlobName);
        if (Boolean.FALSE.equals(blobClient.exists())) {
            return Optional.empty();
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.downloadStream(outputStream);
        return Optional.of(outputStream.toByteArray());
    }

    @Override
    public Map<String, byte[]> getAllBlobs() throws BlobPersistenceException {
        final Map<String, byte[]> blobs = new ConcurrentHashMap<>();
        for (final BlobItem blobItem : containerClient.listBlobs()) {
            blobs.put(blobItem.getName(), getBlob(blobItem.getName()).orElse(null));
        }
        return blobs;
    }

    @Override
    public Collection<byte[]> findBlobByPrefix(final String prefix) throws BlobPersistenceException {
        final List<byte[]> result = new ArrayList<>();
        final Iterator<BlobItem> iterator = containerClient.listBlobs().stream().iterator();
        while (iterator.hasNext()) {
            final BlobItem blobItem = iterator.next();
            if (blobItem.getName().startsWith(prefix)) {
                getBlob(blobItem.getName()).ifPresent(result::add);
            }
        }
        return result;
    }

    @Override
    public boolean delete(final String blobId, final List<String> processIds) throws BlobPersistenceException {
        final BlobClient blobClient = containerClient.getBlobClient(blobId);
        if (Boolean.TRUE.equals(blobClient.exists())) {
            blobClient.delete();
            return true;
        }
        return false;
    }
}
