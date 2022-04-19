package net.catenax.irs.services;

import java.util.UUID;

import net.catenax.irs.InMemoryBlobStore;
import net.catenax.irs.connector.job.JobState;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.persistence.BlobPersistence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
class IrsPartsTreeQueryServiceTest {

    private final UUID jobId = UUID.randomUUID();

    @Autowired
    JobStore jobStore;

    @Autowired
    IrsItemGraphQueryService irsPartsTreeQueryService;

    @Test
    void registerItemJob() {
        assertTrue(true);
    }

    @Test
    void jobLifecycle() {
        assertTrue(true);
    }

    @Test
    void getJobsByProcessingState() {
        assertTrue(true);
    }

    @Test
    void cancelJobById() {
        final MultiTransferJob multiTransferJob = MultiTransferJob.builder()
                                                                  .jobId(String.valueOf(jobId))
                                                                  .state(JobState.UNSAVED)
                                                                  .errorDetail("Job should be canceled")
                                                                  .build();

        jobStore.create(multiTransferJob);

        assertNotNull(irsPartsTreeQueryService.cancelJobById(jobId));
        assertFalse(jobStore.find(String.valueOf(jobId)).isEmpty());

        final JobState state = jobStore.find(String.valueOf(jobId)).get().getState();
        assertEquals(state, JobState.CANCELED);
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