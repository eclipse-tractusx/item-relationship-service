//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.configuration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.persistence.MinioBlobPersistence;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Minio health indicator for Spring actuator
 */
@Component
@RequiredArgsConstructor
@Slf4j
class MinioHealthIndicator implements HealthIndicator {

    private final BlobPersistence blobPersistence;
    private final BlobstoreConfiguration blobstoreConfiguration;

    @Override
    public Health health() {
        if (thereIsMinioConnection()) {
            return Health.up()
                    .withDetail("bucketName", blobstoreConfiguration.getBucketName())
                    .build();
        } else {
            return Health.down().build();
        }
    }

    /**
     * Verifies if blobPersistence is instance of {@link MinioBlobPersistence} and if bucket exists.
     * If yes it means that there is Minio connection.
     * If bucket does not exist, method tries to recreate it.
     * @return true if bucket exists, false otherwise
     */
    private boolean thereIsMinioConnection() {
        if (blobPersistence instanceof MinioBlobPersistence) {
            try {
                final MinioBlobPersistence minioBlobPersistence = (MinioBlobPersistence) blobPersistence;
                minioBlobPersistence.createBucketIfNotExists(blobstoreConfiguration.getBucketName());

                if (minioBlobPersistence.bucketExists(blobstoreConfiguration.getBucketName())) {
                    return true;
                }
            } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
                log.error("Lost connection to Minio", e);
            }
        }

        return false;
    }
}
