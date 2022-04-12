package net.catenax.irs.aaswrapper.submodel.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.github.resilience4j.retry.RetryRegistry;
import net.catenax.irs.InMemoryBlobStore;
import net.catenax.irs.persistence.BlobPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "local", "test" })
class SubmodelRetryerTest {
    private SubmodelRetryer submodelRetryer;

    @Mock
    private SubmodelClientImpl submodelClient;

    @Autowired
    private RetryRegistry retryRegistry;

    @BeforeEach
    void setUp() {
        when(this.submodelClient.getSubmodel(anyString(), any())).thenThrow(
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "AASWrapper remote exception"));

        this.submodelRetryer = new SubmodelRetryer(this.submodelClient);
    }

    @Test
    void submodelRetryerShouldRetryThreeTimes() {
        assertThatExceptionOfType(HttpServerErrorException.class).isThrownBy(
                                                                         () -> this.submodelRetryer.retrySubmodel("aasWrapperEndpoint", AssemblyPartRelationship.class))
                                                                 .withMessage("500 AASWrapper remote exception");

        final long attempts = this.retryRegistry.getDefaultConfig().getMaxAttempts();
        assertThat(attempts).isEqualTo(3L);
    }

    @TestConfiguration
    static class TestConfig {
        @Primary
        @Bean
        public BlobPersistence inMemoryBlobStore() {
            return new InMemoryBlobStore();
        }
    }
}
