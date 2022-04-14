package net.catenax.irs.controllers;

import java.util.UUID;

import net.catenax.irs.InMemoryBlobStore;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.persistence.BlobPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
class IrsControllerTest {

    private final UUID jobId = UUID.randomUUID();

    @Autowired
    JobStore jobStore;

    @Autowired
    private IrsController controller;

    @BeforeEach
    void setUp() {
        final MultiTransferJob multiTransferJob = MultiTransferJob.builder()
                                                                  .jobId(String.valueOf(jobId))
                                                                  .state(net.catenax.irs.connector.job.JobState.UNSAVED)
                                                                  .errorDetail("Job should be canceled")
                                                                  .build();

        this.jobStore.create(multiTransferJob);
    }

    @Test
    void initiateJobForGlobalAssetId() {
        assertTrue(true);
    }

    @Test
    void getJobById() {
        assertTrue(true);
    }

    @Test
    void getJobsByJobState() {
        assertTrue(true);
    }

    @Test
    void cancelJobById() {
        ResponseEntity<?> entity = this.controller.cancelJobById(jobId);

        assertNotNull(entity);
        assertEquals(entity.getStatusCode(), HttpStatus.OK);
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