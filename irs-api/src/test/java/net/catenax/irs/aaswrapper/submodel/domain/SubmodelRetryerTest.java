package net.catenax.irs.aaswrapper.submodel.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.resilience4j.retry.RetryRegistry;
import net.catenax.irs.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@Import(TestConfig.class)
class SubmodelExponentialRetryTest {

    @Autowired
    private SubmodelClient submodelClient;

    @MockBean
    @Qualifier("basicAuthRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private RetryRegistry retryRegistry;

    @Test
    void shouldRetryExecutionOfGetSubmodelOnClientMaxAttemptTimes() {
        // Arrange
        given(restTemplate.getForEntity(any(), any())).willThrow(
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "AASWrapper remote exception"));

        // Act
        assertThrows(HttpServerErrorException.class,
                () -> submodelClient.getSubmodel("http://test.test/urn:uuid:12345/submodel?content=value",
                        Object.class));

        // Assert
        verify(restTemplate, times(retryRegistry.getDefaultConfig().getMaxAttempts())).getForEntity(any(), any());
    }

    @Test
    void shouldRetryOnAnyRuntimeException() {
        // Arrange
        given(restTemplate.getForEntity(any(), any())).willThrow(new RuntimeException("AASWrapper remote exception"));

        // Act
        assertThrows(RuntimeException.class,
                () -> submodelClient.getSubmodel("http://test.test/urn:uuid:12345/submodel?content=value",
                        Object.class));

        // Assert
        verify(restTemplate, times(retryRegistry.getDefaultConfig().getMaxAttempts())).getForEntity(any(), any());
    }

}
