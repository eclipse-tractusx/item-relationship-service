package net.catenax.irs.aaswrapper.registry.domain;

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
class DigitalTwinRegistryExponentialRetryTest {

    @Autowired
    private DigitalTwinRegistryClient digitalTwinRegistryClient;

    @MockBean
    @Qualifier("oAuthRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private RetryRegistry retryRegistry;

    @Test
    void shouldRetryExecutionOfGetSubmodelOnClientMaxAttemptTimes() {
        // Arrange
        given(restTemplate.getForObject(any(), any())).willThrow(
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "AASWrapper remote exception"));

        // Act
        assertThrows(HttpServerErrorException.class,
                () -> digitalTwinRegistryClient.getAssetAdministrationShellDescriptor("urn:uuid:12345"));

        // Assert
        verify(restTemplate, times(retryRegistry.getDefaultConfig().getMaxAttempts())).getForObject(any(), any());
    }
}
