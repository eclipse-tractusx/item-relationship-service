package org.eclipse.tractusx.irs;

import org.eclipse.tractusx.irs.persistence.BlobPersistence;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Primary
    @Bean
    public BlobPersistence inMemoryBlobStore() {
        return new InMemoryBlobStore();
    }
}
