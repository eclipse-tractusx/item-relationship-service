package net.catenax.irs.controllers;

import java.util.UUID;

import net.catenax.irs.TestConfig;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.exceptions.EntityNotFoundException;
import net.catenax.irs.services.IrsItemGraphQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@Import(TestConfig.class)
class IrsControllerTest {

    private final UUID jobId = UUID.randomUUID();

    @Autowired
    private IrsController controller;

    @MockBean
    private IrsItemGraphQueryService service;

    private Job canceledJob;

    @BeforeEach
    void setUp() {
        this.canceledJob = Job.builder().jobId(jobId).jobState(JobState.CANCELED).build();
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
        given(this.service.cancelJobById(jobId)).willReturn(canceledJob);

        final ResponseEntity<?> entity = this.controller.cancelJobById(jobId);

        assertNotNull(entity);
        assertEquals(entity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    void cancelJobById_throwEntityNotFoundException() {
        given(this.service.cancelJobById(jobId)).willThrow(
                new EntityNotFoundException("No job exists with id " + jobId));

        assertThrows(EntityNotFoundException.class, () -> this.controller.cancelJobById(jobId));
    }
}