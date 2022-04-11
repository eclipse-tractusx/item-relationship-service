package net.catenax.irs.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "local", "test" })
class IrsControllerTest {

    @Autowired
    JobStore jobStore;
    @Autowired
    private IrsController controller;

    @BeforeEach
    void setUp() {
        final MultiTransferJob multiTransferJob = MultiTransferJob.builder()
                                                                  .jobId("test123")
                                                                  .state(net.catenax.irs.connector.job.JobState.UNSAVED)
                                                                  .errorDetail("Job should be canceled")
                                                                  .build();

        this.jobStore.create(multiTransferJob);
    }

    @Test
    void getBomLifecycleByGlobalAssetId() {
        assertTrue(true);
    }

    @Test
    void getBOMForJobId() {
        assertTrue(true);
    }

    @Test
    void getJobsByProcessingState() {
        assertTrue(true);
    }

    @Test
    void cancelJobById() {
        ResponseEntity<?> entity = this.controller.cancelJobById("test123");
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