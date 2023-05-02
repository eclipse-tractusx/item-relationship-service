package org.eclipse.tractusx.irs.edc.client;

import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.internal.InMemoryRetryRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RetryRegistryConfiguration {
    @Bean
    public RetryRegistry retryRegistry() {
        return new InMemoryRetryRegistry();
    }
}
