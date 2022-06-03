package net.catenax.irs;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemDataRequest;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.JobOrchestrator;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.dto.JobParameter;
import net.catenax.irs.persistence.BlobPersistence;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "local",
                             "test"
})
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
    void generatedOpenApiMatchesContract() throws Exception {
        final String generatedYaml =
                this.restTemplate.getForObject("http://localhost:" + port + "/api/api-docs.yaml", String.class);
        final String fixedYaml = Files.readString(new File("../api/irs-v1.0.yaml").toPath(), UTF_8);
        assertThat(generatedYaml).isEqualToIgnoringWhitespace(fixedYaml);
    }

    @Test
    void shouldStoreBlobResultWhenRunningJob() throws Exception {
        final JobParameter jobParameter = JobParameter.builder().rootItemId("rootitemid").treeDepth(5).aspectTypes(List.of()).build();

        final JobInitiateResponse response = jobOrchestrator.startJob(jobParameter);

        assertThat(response.getStatus()).isEqualTo(ResponseStatus.OK);

        Awaitility.await()
                  .atMost(10, TimeUnit.SECONDS)
                  .pollInterval(100, TimeUnit.MILLISECONDS)
                  .until(() -> jobStore.find(response.getJobId())
                                       .map(s -> s.getJob().getJobState())
                                       .map(state -> state == JobState.COMPLETED)
                                       .orElse(false));

        assertThat(inMemoryBlobStore.getBlob(response.getJobId())).isNotEmpty();
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
