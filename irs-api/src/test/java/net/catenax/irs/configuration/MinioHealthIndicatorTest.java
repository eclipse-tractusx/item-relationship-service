package net.catenax.irs.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import net.catenax.irs.InMemoryBlobStore;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.MinioBlobPersistence;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class MinioHealthIndicatorTest {

    @Test
    void shouldReturnStatusUpWhenMinioBlobPersistencePresent() {
        // given
        final BlobPersistence blobPersistence = mock(MinioBlobPersistence.class);
        final BlobstoreConfiguration blobstoreConfiguration = mock(BlobstoreConfiguration.class);
        when(blobstoreConfiguration.getBucketName()).thenReturn("bucketName");

        final MinioHealthIndicator minioHealthIndicator = new MinioHealthIndicator(blobPersistence, blobstoreConfiguration);

        // when
        final Health health = minioHealthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void shouldReturnStatusDownWhenBlobStorageIsNotMinio() {
        // given
        final BlobPersistence blobPersistence = mock(BlobPersistence.class);
        final BlobstoreConfiguration blobstoreConfiguration = mock(BlobstoreConfiguration.class);

        final MinioHealthIndicator minioHealthIndicator = new MinioHealthIndicator(blobPersistence, blobstoreConfiguration);

        // when
        final Health health = minioHealthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
