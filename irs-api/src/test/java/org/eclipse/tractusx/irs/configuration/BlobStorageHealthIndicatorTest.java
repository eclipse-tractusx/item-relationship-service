/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import io.minio.MinioClient;
import org.eclipse.tractusx.irs.common.persistence.AzureBlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.MinioBlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.config.BlobStoreConfiguration;
import org.eclipse.tractusx.irs.common.persistence.config.BlobStoreContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class BlobStorageHealthIndicatorTest {

    @Test
    void shouldReturnStatusUpWhenMinioBlobPersistencePresentAndBucketExists() throws Exception {
        // given
        final MinioClient minioClient = mock(MinioClient.class);
        final BlobStoreConfiguration blobstoreConfiguration = mock(BlobStoreConfiguration.class);
        BlobStoreContainerConfiguration containerConfig = new BlobStoreContainerConfiguration();
        containerConfig.setContainerName("bucket-name");
        when(blobstoreConfiguration.getJobs()).thenReturn(containerConfig);
        when(minioClient.bucketExists(any())).thenReturn(Boolean.TRUE);

        final MinioBlobPersistence blobPersistence = new MinioBlobPersistence("bucket-name", minioClient, 1);
        final BlobStorageHealthIndicator minioHealthIndicator = new BlobStorageHealthIndicator(List.of(blobPersistence), blobstoreConfiguration);

        // when
        final Health health = minioHealthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void shouldReturnStatusDownWhenBlobStorageIsUnknown() {
        // given
        final BlobPersistence blobPersistence = mock(BlobPersistence.class);
        final BlobStoreConfiguration blobstoreConfiguration = mock(BlobStoreConfiguration.class);

        final BlobStorageHealthIndicator healthIndicator = new BlobStorageHealthIndicator(List.of(blobPersistence), blobstoreConfiguration);

        // when
        final Health health = healthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void shouldReturnStatusUpWhenAzureBlobPersistencePresentAndBucketExists() {
        // given
        final BlobStoreConfiguration blobstoreConfiguration = mock(BlobStoreConfiguration.class);
        BlobStoreContainerConfiguration containerConfig = new BlobStoreContainerConfiguration();
        containerConfig.setContainerName("bucket-name");
        when(blobstoreConfiguration.getJobs()).thenReturn(containerConfig);

        final BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        final BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        when(blobServiceClient.getBlobContainerClient("container-name")).thenReturn(blobContainerClient);
        when(blobContainerClient.exists()).thenReturn(true);

        final AzureBlobPersistence blobPersistence = new AzureBlobPersistence(blobServiceClient, "container-name");
        final BlobStorageHealthIndicator healthIndicator = new BlobStorageHealthIndicator(List.of(blobPersistence), blobstoreConfiguration);

        // when
        final Health health = healthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void shouldReturnStatusUpWhenAzureBlobPersistencePresentAndBucketDoesNotExist() {
        // given
        final BlobStoreConfiguration blobstoreConfiguration = mock(BlobStoreConfiguration.class);
        BlobStoreContainerConfiguration containerConfig = new BlobStoreContainerConfiguration();
        containerConfig.setContainerName("bucket-name");
        when(blobstoreConfiguration.getJobs()).thenReturn(containerConfig);

        final BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        final BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        when(blobServiceClient.getBlobContainerClient("container-name")).thenReturn(blobContainerClient);
        when(blobContainerClient.exists()).thenReturn(false);

        final AzureBlobPersistence blobPersistence = new AzureBlobPersistence(blobServiceClient, "container-name");
        final BlobStorageHealthIndicator healthIndicator = new BlobStorageHealthIndicator(List.of(blobPersistence), blobstoreConfiguration);

        // when
        final Health health = healthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
