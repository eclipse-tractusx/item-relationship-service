package org.eclipse.tractusx.irs.policystore;

import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.common.persistence.MinioBlobPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PolicyConfiguration {
    @Profile("!test")
    @Bean(name = "PolicyStorePersistence")
    public BlobPersistence blobStore(final PolicyBlobstoreConfiguration config) throws BlobPersistenceException {
        return new MinioBlobPersistence(config.getEndpoint(), config.getAccessKey(), config.getSecretKey(),
                config.getBucketName());
    }
}
