/********************************************************************************
 * Copyright (c) 2023,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.policystore.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.common.persistence.config.AzureBlobstoreConfiguration;
import org.eclipse.tractusx.irs.common.persistence.config.BlobStoreConfiguration;
import org.eclipse.tractusx.irs.common.persistence.config.BlobStoreContainerConfiguration;
import org.eclipse.tractusx.irs.common.persistence.config.BlobStorePersistenceConfiguration;
import org.eclipse.tractusx.irs.common.persistence.config.MinioBlobstoreConfiguration;
import org.eclipse.tractusx.irs.testing.containers.AzuriteContainer;
import org.eclipse.tractusx.irs.testing.containers.MinioContainer;
import org.junit.jupiter.api.Test;

class PolicyConfigurationTest {
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private static final MinioContainer minioContainer = new MinioContainer(
            new MinioContainer.CredentialsProvider(ACCESS_KEY, SECRET_KEY)).withReuse(true);

    private static final AzuriteContainer azuriteContainer = new AzuriteContainer().withReuse(true);

    @Test
    void minioBlobStore() throws BlobPersistenceException {
        minioContainer.start();
        // arrange

        MinioBlobstoreConfiguration minioConfig = new MinioBlobstoreConfiguration();
        minioConfig.setEndpoint("http://" + minioContainer.getHostAddress());
        minioConfig.setAccessKey(ACCESS_KEY);
        minioConfig.setSecretKey(SECRET_KEY);

        BlobStorePersistenceConfiguration persistenceConfig = new BlobStorePersistenceConfiguration();
        persistenceConfig.setMinio(minioConfig);

        BlobStoreContainerConfiguration containerConfig = new BlobStoreContainerConfiguration();
        containerConfig.setContainerName("test-policy");

        final var config = new BlobStoreConfiguration();
        config.setPersistence(persistenceConfig);
        config.setPolicies(containerConfig);

        // act
        final var blobPersistence = new PolicyConfiguration().minioBlobStore(config);

        // assert
        assertThat(blobPersistence).isNotNull();

        minioContainer.stop();
    }

    @Test
    void azureBlobStore() {
        azuriteContainer.start();
        // arrange

        AzureBlobstoreConfiguration azureConfig = new AzureBlobstoreConfiguration();
        azureConfig.setUseConnectionString(true);
        azureConfig.setConnectionString(azuriteContainer.getConnectionString());

        BlobStorePersistenceConfiguration persistenceConfig = new BlobStorePersistenceConfiguration();
        persistenceConfig.setAzure(azureConfig);

        BlobStoreContainerConfiguration containerConfig = new BlobStoreContainerConfiguration();
        containerConfig.setContainerName("test-policy");

        final var config = new BlobStoreConfiguration();
        config.setPersistence(persistenceConfig);
        config.setPolicies(containerConfig);

        // act
        final var blobPersistence = new PolicyConfiguration().azureBlobStore(config);

        // assert
        assertThat(blobPersistence).isNotNull();

        azuriteContainer.stop();
    }
}