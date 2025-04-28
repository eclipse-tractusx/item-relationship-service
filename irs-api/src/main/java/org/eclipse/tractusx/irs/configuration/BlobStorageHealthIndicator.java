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

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.common.persistence.AzureBlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.MinioBlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.config.BlobStoreConfiguration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Blob storage health indicator for Spring actuator
 */
@Component
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({ "PMD.AvoidCatchingGenericException" })
class BlobStorageHealthIndicator implements HealthIndicator {

    private final List<BlobPersistence> blobPersistences;
    private final BlobStoreConfiguration blobstoreConfiguration;

    @Override
    public Health health() {
        final boolean allHealthy = blobPersistences.stream()
                                                   .map(this::checkConnection)
                                                   .reduce(true, Boolean::logicalAnd);

        return allHealthy ? Health.up()
                                  .withDetail("bucketName", blobstoreConfiguration.getJobs().getContainerName())
                                  .build() : Health.down().build();
    }

    private boolean checkConnection(final BlobPersistence blobPersistence) {
        if (blobPersistence instanceof MinioBlobPersistence minio) {
            return checkMinioConnection(minio);
        }

        if (blobPersistence instanceof AzureBlobPersistence azure) {
            return azure.checkConnection();
        }

        log.warn("Unknown BlobPersistence implementation: {}", blobPersistence.getClass().getSimpleName());
        return false;
    }

    private boolean checkMinioConnection(final MinioBlobPersistence minio) {
        final String bucketName = blobstoreConfiguration.getJobs().getContainerName();
        try {
            minio.createBucketIfNotExists(bucketName);
            return minio.bucketExists(bucketName);
        } catch (Exception e) {
            log.error("MinIO connection check failed", e);
            return false;
        }
    }
}
