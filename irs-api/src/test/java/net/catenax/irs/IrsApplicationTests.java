package net.catenax.irs;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.catenax.irs.aaswrapper.AASRecursiveJobHandler.DESTINATION_PATH_KEY;
import static net.catenax.irs.aaswrapper.AASRecursiveJobHandler.ROOT_ITEM_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.catenax.irs.aaswrapper.AASTransferProcess;
import net.catenax.irs.aaswrapper.ItemDataRequest;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.JobOrchestrator;
import net.catenax.irs.connector.job.JobState;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.persistence.BlobPersistence;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"local","test"})
class IrsApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JobStore jobStore;

    @Autowired
    private BlobPersistence inMemoryBlobStore;

    @Autowired
    private JobOrchestrator<ItemDataRequest, AASTransferProcess> jobOrchestrator;

    @Test
    void contextLoads() {
    }

    @Test
    void generatedOpenApiMatchesContract() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/api/api-docs.yaml",
                String.class)).isEqualToNormalizingNewlines(
                Files.readString(new File("../api/irs-v0.1.yaml").toPath(), UTF_8));
    }

    @Test
    void runAasDataJob() throws Exception {
        final var targetBlobId = "targetBlobId";

        final JobInitiateResponse response = jobOrchestrator.startJob(
                Map.of(ROOT_ITEM_ID_KEY, "rootitemid", DESTINATION_PATH_KEY, targetBlobId));

        assertThat(response.getStatus()).isEqualTo(ResponseStatus.OK);

        Awaitility.await()
                  .atMost(5, TimeUnit.SECONDS)
                  .pollInterval(100, TimeUnit.MILLISECONDS)
                  .until(() -> jobStore.find(response.getJobId())
                                       .map(MultiTransferJob::getState)
                                       .map(state -> state == JobState.COMPLETED)
                                       .orElse(false));

        assertThat(inMemoryBlobStore.getBlob(targetBlobId)).isNotEmpty();
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
