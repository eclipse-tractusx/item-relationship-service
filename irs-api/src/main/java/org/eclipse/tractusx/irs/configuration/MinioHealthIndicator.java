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
package org.eclipse.tractusx.irs.configuration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.MinioBlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.config.BlobStoreConfiguration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Minio health indicator for Spring actuator
 */
@Component
@Slf4j
@RequiredArgsConstructor
class MinioHealthIndicator implements HealthIndicator {

    private final List<BlobPersistence> blobPersistences;
    private final BlobStoreConfiguration blobstoreConfiguration;

    @Override
    public Health health() {
        if (thereIsMinioConnection()) {
            return Health.up().withDetail("bucketName", blobstoreConfiguration.getJobs().getContainerName()).build();
        } else {
            return Health.down().build();
        }
    }

    /**
     * Verifies if blobPersistence is instance of {@link MinioBlobPersistence} and if bucket exists.
     * If yes it means that there is Minio connection.
     * If bucket does not exist, method tries to recreate it.
     *
     * @return true if bucket exists, false otherwise
     */
    private boolean thereIsMinioConnection() {
        return blobPersistences.stream().allMatch(this::connectionWorks);
    }

    @SuppressWarnings("checkstyle:operatorwrap")
    private boolean connectionWorks(final BlobPersistence blobPersistence) {
        if (blobPersistence instanceof MinioBlobPersistence minioBlobPersistence) {
            try {
                final String bucketName = blobstoreConfiguration.getJobs().getContainerName();
                minioBlobPersistence.createBucketIfNotExists(bucketName);

                if (minioBlobPersistence.bucketExists(bucketName)) {
                    return true;
                }
            } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                     NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                     InternalException e) {
                if (log.isErrorEnabled()) {
                    log.error("Lost connection to Minio", e);
                }
            }
        }
        return false;
    }
}
