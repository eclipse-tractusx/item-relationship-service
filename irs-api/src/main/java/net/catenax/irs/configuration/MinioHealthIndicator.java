//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.configuration;

import lombok.RequiredArgsConstructor;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.MinioBlobPersistence;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health Actuator for Minio
 */
@Component
@RequiredArgsConstructor
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
     * Verifies if blobPersistence is instance of {@link MinioBlobPersistence}.
     * If yes that means that there is {@link io.minio.MinioClient} configured and bucket exists.
     * @return true if {@link MinioBlobPersistence, false otherwise}
     */
    private boolean thereIsMinioConnection() {
        return blobPersistence instanceof MinioBlobPersistence;
    }
}
