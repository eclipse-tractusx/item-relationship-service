package net.catenax.irs.aaswrapper.submodel.domain;

import io.github.resilience4j.retry.RetryRegistry;
import net.catenax.irs.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@Import(TestConfig.class)
class SubmodelRetryerTest {

    @Autowired
    private SubmodelFacade facade;

    @Autowired
    private RetryRegistry retryRegistry;

    @MockBean
    private SubmodelClient client;

    @Test
    void shouldRetryExecutionOfGetSubmodelMaxAttemptTimes() {
        given(this.client.getSubmodel(anyString(), any())).willThrow(
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "AASWrapper remote exception"));

        assertThrows(HttpServerErrorException.class, () -> facade.getSubmodel("TEST"));

        verify(this.client, times(retryRegistry.getDefaultConfig().getMaxAttempts())).getSubmodel(anyString(), any());
    }
}
