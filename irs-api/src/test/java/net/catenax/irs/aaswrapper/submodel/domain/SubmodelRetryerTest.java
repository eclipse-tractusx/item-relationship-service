package net.catenax.irs.aaswrapper.submodel.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import io.github.resilience4j.retry.Retry.Metrics;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import net.catenax.irs.exceptions.AspectNotSupportedException;
import net.catenax.irs.exceptions.MaxDepthTooLargeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

class SubmodelRetryerTest {
    private SubmodelRetryer submodelRetryer;

    @BeforeEach
    void setUp() {
        final SubmodelClientImplStub submodelClient = new SubmodelClientImplStub();
        this.submodelRetryer = new SubmodelRetryer(submodelClient);
    }

    @Test
    void submodelRetryerShouldRetryThreeTimes() {
        assertThatExceptionOfType(HttpServerErrorException.class).isThrownBy(
                                                                         () -> this.submodelRetryer.retrySubmodel("aasWrapperEndpoint", AssemblyPartRelationship.class))
                                                                 .withMessage("500 AASWrapper remote exception");

        final Metrics metrics = this.getMetrics();
        final long attempts = metrics.getNumberOfFailedCallsWithRetryAttempt();
        assertThat(attempts).isEqualTo(0L);

    }

    private Metrics getMetrics() {
        final RetryConfig config = RetryConfig.custom()
                                              .maxAttempts(3)
                                              .waitDuration(Duration.ofMillis(1000))
                                              .retryOnException(e -> e instanceof AspectNotSupportedException)
                                              .retryExceptions(HttpServerErrorException.class, IOException.class,
                                                      TimeoutException.class)
                                              .ignoreExceptions(MaxDepthTooLargeException.class)
                                              .failAfterMaxAttempts(true)
                                              .build();

        final RetryRegistry registry = RetryRegistry.of(config);

        return registry.retry("submodelRetryer").getMetrics();
    }
}