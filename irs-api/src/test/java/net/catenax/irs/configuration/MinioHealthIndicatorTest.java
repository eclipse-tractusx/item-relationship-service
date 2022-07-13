package net.catenax.irs.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.minio.MinioClient;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.MinioBlobPersistence;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class MinioHealthIndicatorTest {

    @Test
    void shouldReturnStatusUpWhenMinioBlobPersistencePresentAndBucketExists() throws Exception {
        // given
        final MinioClient minioClient = mock(MinioClient.class);
        final BlobstoreConfiguration blobstoreConfiguration = mock(BlobstoreConfiguration.class);
        when(blobstoreConfiguration.getBucketName()).thenReturn("bucket-name");
        when(minioClient.bucketExists(any())).thenReturn(Boolean.TRUE);

        final MinioBlobPersistence blobPersistence = new MinioBlobPersistence("bucket-name", minioClient);
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
