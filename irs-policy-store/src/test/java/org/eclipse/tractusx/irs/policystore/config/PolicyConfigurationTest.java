package org.eclipse.tractusx.irs.policystore.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.testing.containers.MinioContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PolicyConfigurationTest {
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private static final MinioContainer minioContainer = new MinioContainer(
            new MinioContainer.CredentialsProvider(ACCESS_KEY, SECRET_KEY)).withReuse(true);

    @BeforeAll
    static void startContainer() {
        minioContainer.start();
    }

    @AfterAll
    static void stopContainer() {
        minioContainer.stop();
    }

//    @Test
    void blobStore() throws BlobPersistenceException {
        // arrange
        final var config = new PolicyBlobstoreConfiguration();
        config.setEndpoint("http://" + minioContainer.getHostAddress());
        config.setBucketName("test-policy");
        config.setAccessKey(ACCESS_KEY);
        config.setSecretKey(SECRET_KEY);

        // act
        final var blobPersistence = new PolicyConfiguration().blobStore(config);

        // assert
        assertThat(blobPersistence).isNotNull();

    }
}