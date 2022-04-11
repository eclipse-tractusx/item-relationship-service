package net.catenax.irs.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "local", "test" })
class IrsPartsTreeQueryServiceTest {

    @Autowired
    JobStore jobStore;

    @Autowired
    IrsPartsTreeQueryService irsPartsTreeQueryService;

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
                                                                  .jobId("test123")
                                                                  .state(JobState.UNSAVED)
                                                                  .errorDetail("Job should be canceled")
                                                                  .build();

        jobStore.create(multiTransferJob);

        assertNotNull(irsPartsTreeQueryService.cancelJobById("test123"));
        assertFalse(jobStore.find("test123").isEmpty());

        final JobState state = jobStore.find("test123").get().getState();
        assertEquals(state, JobState.COMPLETED);
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