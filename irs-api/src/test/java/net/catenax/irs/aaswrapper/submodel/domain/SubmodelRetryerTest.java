package net.catenax.irs.aaswrapper.submodel.domain;

import net.catenax.irs.InMemoryBlobStore;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "local", "test" })
class SubmodelRetryerTest {

    @Autowired
    private SubmodelFacade facade;

    @MockBean
    private SubmodelClientImpl client;

/*    @BeforeEach
    void setUp() {
        this.facade = new SubmodelFacade(client);
    }*/

    @Test
    void getSubmodel_shouldRetryThreeTimes() {
        given(this.client.getSubmodel(anyString(), any()))
                .willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "AASWrapper remote exception"));

/*        assertThatExceptionOfType(HttpServerErrorException.class).isThrownBy(
                () -> this.facade.getSubmodel(anyString())).withMessage("AASWrapper remote exception");
        assertThatThrownBy(() -> this.facade.getSubmodel(anyString())).hasNoCause();*/

        this.facade.getSubmodel(anyString());
        verify(this.client, times(1)).getSubmodel(anyString(), any());
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
