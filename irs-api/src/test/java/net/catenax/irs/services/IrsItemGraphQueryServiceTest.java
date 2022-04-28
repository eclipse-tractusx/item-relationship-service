package net.catenax.irs.services;

import static net.catenax.irs.util.TestMother.registerJobWithDepth;
import static net.catenax.irs.util.TestMother.registerJobWithoutDepth;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.catenax.irs.TestConfig;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobErrorDetails;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static net.catenax.irs.util.TestMother.registerJobWithDepth;
import static net.catenax.irs.util.TestMother.registerJobWithoutDepth;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@Import(TestConfig.class)
class IrsItemGraphQueryServiceTest {

    private final UUID jobId = UUID.randomUUID();

    @MockBean
    private JobStore jobStore;

    @Autowired
    private IrsItemGraphQueryService service;

    @Test
    @Disabled // its not consistent before TRI-390 not resolved
    void registerItemJobWithoutDepthShouldBuildFullTree() {
        // given
        final RegisterJob registerJob = registerJobWithoutDepth();
        final int expectedRelationshipsSizeFullTree = 6; // stub

        // when
        final JobHandle registeredJob = service.registerItemJob(registerJob);

        // then
        given().ignoreException(EntityNotFoundException.class)
               .await()
               .atMost(10, TimeUnit.SECONDS)
               .until(() -> getRelationshipsSize(registeredJob.getJobId()), equalTo(expectedRelationshipsSizeFullTree));
    }

    @Test
    @Disabled // its not consistent before TRI-390 not resolved
    void registerItemJobWithDepthShouldBuildTreeUntilGivenDepth() {
        // given
        final RegisterJob registerJob = registerJobWithDepth(0);
        final int expectedRelationshipsSizeFirstDepth = 3; // stub

        // when
        final JobHandle registeredJob = service.registerItemJob(registerJob);

        // then
        given().ignoreException(EntityNotFoundException.class)
               .await()
               .atMost(10, TimeUnit.SECONDS)
               .until(() -> getRelationshipsSize(registeredJob.getJobId()),
                      equalTo(expectedRelationshipsSizeFirstDepth));
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
        final Job job = Job.builder()
                           .jobId(jobId)
                           .createdOn(Instant.now())
                           .lastModifiedOn(Instant.now())
                           .jobState(JobState.CANCELED)
                           .build();

        final MultiTransferJob multiTransferJob = MultiTransferJob.builder().job(job).build();

        when(jobStore.cancelJob(jobId.toString())).thenReturn(Optional.ofNullable(multiTransferJob));
        final Job canceledJob = service.cancelJobById(jobId);

        assertNotNull(canceledJob);
        assertEquals(canceledJob.getJobId(), jobId);
        assertEquals(canceledJob.getJobState().name(), JobState.CANCELED.name());
    }

    @Test
    void cancelJobById_throwEntityNotFoundException() {
        when(jobStore.cancelJob(jobId.toString())).thenThrow(
                new EntityNotFoundException("No job exists with id " + jobId));

        assertThrows(EntityNotFoundException.class, () -> service.cancelJobById(jobId));
    }

    private int getRelationshipsSize(final UUID jobId) {
        return service.getJobForJobId(jobId).getRelationships().size();
    }

}