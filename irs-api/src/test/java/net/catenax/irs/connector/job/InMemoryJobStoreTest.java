package net.catenax.irs.connector.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.github.javafaker.Faker;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobErrorDetails;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.util.TestMother;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class InMemoryJobStoreTest {
    final int TTL_IN_HOUR_SECONDS = 3600;
    InMemoryJobStore sut = new InMemoryJobStore();
    Faker faker = new Faker();
    TestMother generate = new TestMother();
    MultiTransferJob job = generate.job(JobState.UNSAVED);
    MultiTransferJob job2 = generate.job(JobState.UNSAVED);
    MultiTransferJob originalJob = job.toBuilder().build();
    String otherJobId = faker.lorem().characters();
    TransferProcess process1 = generate.transfer();
    TransferProcess process2 = generate.transfer();
    String processId1 = process1.getId();
    String processId2 = process2.getId();
    String errorDetail = faker.lorem().sentence();

    @Test
    void find_WhenNotFound() {
        assertThat(sut.find(otherJobId)).isEmpty();
    }

    @Test
    void findByProcessId_WhenFound() {
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        sut.create(job2);
        sut.addTransferProcess(job2.getJobIdString(), processId2);

        refreshJob();
        assertThat(sut.findByProcessId(processId1)).contains(job);
    }

    @Test
    void findByProcessId_WhenNotFound() {
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);

        assertThat(sut.findByProcessId(processId2)).isEmpty();
    }

    @Test
    void create_and_find() {

        sut.create(job);
        assertThat(sut.find(job.getJobIdString())).isPresent()
                                                  .get()
                                                  .usingRecursiveComparison()
                                                  .isEqualTo(originalJob.toBuilder().transitionInitial().build());
        assertThat(sut.find(otherJobId)).isEmpty();
    }

    @Test
    void addTransferProcess() {
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        refreshJob();
        assertThat(job.getTransferProcessIds()).containsExactly(processId1);
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.RUNNING);
    }

    @Test
    void completeTransferProcess_WhenJobNotFound() {
        sut.completeTransferProcess(otherJobId, process1);
    }

    @Test
    void completeTransferProcess_WhenTransferFound() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);

        // Act
        sut.completeTransferProcess(job.getJobIdString(), process1);

        // Assert
        assertThat(job.getTransferProcessIds()).isEmpty();
    }

    @Test
    void completeTransferProcess_WhenTransferNotFound() {
        // Act
        sut.completeTransferProcess(job.getJobIdString(), process1);

        // Assert
        assertThat(job.getTransferProcessIds()).isEmpty();
    }

    @Test
    void completeTransferProcess_WhenTransferAlreadyCompleted() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        sut.completeTransferProcess(job.getJobIdString(), process1);

        // Act
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> sut.completeTransferProcess(job.getJobIdString(), process1));

        // Assert
        refreshJob();
        assertThat(job.getTransferProcessIds()).isEmpty();
    }

    @Test
    void completeTransferProcess_WhenNotLastTransfer_DoesNotTransitionJob() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        sut.addTransferProcess(job.getJobIdString(), processId2);

        // Act
        sut.completeTransferProcess(job.getJobIdString(), process1);

        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.RUNNING);
    }

    @Test
    void completeTransferProcess_WhenLastTransfer_TransitionsJob() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        sut.addTransferProcess(job.getJobIdString(), processId2);

        // Act
        sut.completeTransferProcess(job.getJobIdString(), process1);
        sut.completeTransferProcess(job.getJobIdString(), process2);

        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.TRANSFERS_FINISHED);
    }

    @Test
    void completeJob_WhenJobNotFound() {
        // Arrange
        sut.create(job);
        // Act
        sut.completeJob(otherJobId, this::doNothing);
        refreshJob();
        // Assert
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.INITIAL);
    }

    private void doNothing(final MultiTransferJob multiTransferJob) {
    }

    @Test
    void completeJob_WhenJobInInitialState() {
        // Arrange
        sut.create(job);
        sut.create(job2);
        // Act
        sut.completeJob(job.getJobIdString(), this::doNothing);
        // Assert
        refreshJob();
        refreshJob2();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.COMPLETED);
        assertTrue(Optional.ofNullable(job.getJob().getJobCompleted()).isPresent());
        assertThat(job2.getJob().getJobState()).isEqualTo(JobState.INITIAL);
    }

    @Test
    void completeJob_WhenJobInTransfersCompletedState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        sut.completeTransferProcess(job.getJobIdString(), process1);
        // Act
        sut.completeJob(job.getJobIdString(), this::doNothing);
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.COMPLETED);
        assertTrue(Optional.ofNullable(job.getJob().getJobCompleted()).isPresent());
    }

    @Test
    void completeJob_WhenJobInTransfersInProgressState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        // Act
        sut.completeJob(job.getJobIdString(), this::doNothing);
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.RUNNING);
    }

    @Test
    void markJobInError_WhenJobNotFound() {
        // Arrange
        sut.create(job);
        // Act
        sut.markJobInError(otherJobId, errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.INITIAL);
    }

    @Test
    void markJobInError_WhenJobInInitialState() {
        // Arrange
        sut.create(job);
        sut.create(job2);
        // Act
        sut.markJobInError(job.getJobIdString(), errorDetail);
        // Assert
        refreshJob();
        refreshJob2();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.ERROR);
        assertThat(job2.getJob().getJobState()).isEqualTo(JobState.INITIAL);
        assertThat(job.getJob().getException().getErrorDetail()).isEqualTo(errorDetail);
        assertTrue(Optional.ofNullable(job.getJob().getJobCompleted()).isPresent());
    }

    @Test
    void markJobInError_WhenJobInTransfersCompletedState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        sut.completeTransferProcess(job.getJobIdString(), process1);
        // Act
        sut.markJobInError(job.getJobIdString(), errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.ERROR);
        assertTrue(Optional.ofNullable(job.getJob().getJobCompleted()).isPresent());
    }

    @Test
    void markJobInError_WhenJobInTransfersInProgressState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        // Act
        sut.markJobInError(job.getJobIdString(), errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.ERROR);
        assertTrue(Optional.ofNullable(job.getJob().getJobCompleted()).isPresent());
    }

    @Test
    void shouldFindCompletedJobsOlderThanFiveHours() {
        // Arrange
        final Instant nowPlusFiveHours = Instant.now().plusSeconds(TTL_IN_HOUR_SECONDS * 5);
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        sut.completeTransferProcess(job.getJobIdString(), process1);
        sut.completeJob(job.getJobIdString(), this::doNothing);
        // Act
        final List<MultiTransferJob> completedJobs = sut.findByStateAndCompletionDateOlderThan(JobState.COMPLETED,
                nowPlusFiveHours);
        // Assert
        assertThat(completedJobs).hasSize(1);
        assertThat(completedJobs.get(0).getJob().getJobState()).isEqualTo(JobState.COMPLETED);
        assertTrue(Optional.ofNullable(completedJobs.get(0).getJob().getJobCompleted()).isPresent());
    }

    @Test
    void shouldFindFailedJobsOlderThanFiveHours() {
        // Arrange
        final Instant nowPlusFiveHours = Instant.now().plusSeconds(TTL_IN_HOUR_SECONDS * 5);
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        sut.markJobInError(job.getJobIdString(), errorDetail);
        // Act
        final List<MultiTransferJob> failedJobs = sut.findByStateAndCompletionDateOlderThan(JobState.ERROR,
                nowPlusFiveHours);
        // Assert
        assertThat(failedJobs).hasSize(1);
        assertThat(failedJobs.get(0).getJob().getJobState()).isEqualTo(JobState.ERROR);
        assertTrue(Optional.ofNullable(failedJobs.get(0).getJob().getJobCompleted()).isPresent());
    }

    @Test
    void shouldDeleteJobById() {
        // Arrange
        sut.create(job);
        // Act
        sut.deleteJob(job.getJobIdString());
        // Assert
        assertThat(sut.find(job.getJobIdString())).isEmpty();
    }

    @Test
    void jobStateIsInitial() {
        sut.create(job);
        final Optional<MultiTransferJob> multiTransferJob = sut.get(job.getJobIdString());
        assertThat(multiTransferJob).isPresent();
        assertThat(multiTransferJob.get().getJob().getJobState()).isEqualTo(JobState.INITIAL);
    }

    @Test
    void jobStateIsInProgress() {
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        final Optional<MultiTransferJob> multiTransferJob = sut.get(job.getJobIdString());
        assertThat(multiTransferJob).isPresent();
        assertThat(multiTransferJob.get().getJob().getJobState()).isEqualTo(JobState.RUNNING);
    }

    @Test
    void shouldFindJobsByCompletedJobState() {
        // Arrange
        sut.create(job);
        sut.completeJob(job.getJobIdString(), this::doNothing);
        sut.create(job2);
        // Act
        final List<MultiTransferJob> foundJobs = sut.findByStates(List.of(JobState.COMPLETED));
        // Assert
        assertThat(foundJobs).hasSize(1);
        assertThat(foundJobs.get(0).getJobIdString()).isEqualTo(job.getJobIdString());
    }

    @Test
    void shouldFindJobsByErrorJobState() {
        // Arrange
        sut.create(job);
        sut.markJobInError(job.getJobIdString(), "errorDetail");
        // Act
        final List<MultiTransferJob> foundJobs = sut.findByStates(List.of(JobState.ERROR));
        // Assert
        assertThat(foundJobs).hasSize(1);
        assertThat(foundJobs.get(0).getJobIdString()).isEqualTo(job.getJobIdString());
    }

    private void refreshJob() {
        job = sut.find(job.getJobIdString()).get();
    }

    private void refreshJob2() {
        job2 = sut.find(job2.getJobIdString()).get();
    }

    private Job createJob() {
        GlobalAssetIdentification globalAssetId = GlobalAssetIdentification.builder()
                                                                           .globalAssetId(UUID.randomUUID().toString())
                                                                           .build();

        return Job.builder()
                  .globalAssetId(globalAssetId)
                  .jobId(UUID.randomUUID())
                  .jobState(JobState.INITIAL)
                  .createdOn(Instant.now())
                  .lastModifiedOn(Instant.now())
                  .requestUrl(fakeURL())
                  .action(HttpMethod.POST.toString())
                  .build();
    }

    private URL fakeURL() {
        try {
            return new URL("http://localhost:8888/fake/url");
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    void shouldStoreAndLoadJob() {
        // arrange
        final var jobId = UUID.randomUUID().toString();
        final MultiTransferJob job = createJob(jobId);

        // act
        sut.create(job);
        final Optional<MultiTransferJob> multiTransferJob = sut.find(jobId);

        // assert
        assertThat(multiTransferJob).isPresent();

        final MultiTransferJob storedJob = multiTransferJob.get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(storedJob.getJobIdString()).isEqualTo(job.getJobIdString());
            softly.assertThat(storedJob.getJob().getJobState()).isEqualTo(JobState.INITIAL);
            softly.assertThat(storedJob.getJob().getException().getErrorDetail())
                  .isEqualTo(job.getJob().getException().getErrorDetail());
            softly.assertThat(storedJob.getJob().getJobCompleted()).isEqualTo(job.getJob().getJobCompleted());
            softly.assertThat(storedJob.getJobData()).isEqualTo(job.getJobData());
            softly.assertThat(storedJob.getCompletedTransfers()).isEqualTo(job.getCompletedTransfers());
        });

    }

    private MultiTransferJob createJob(final String jobId) {
        return MultiTransferJob.builder()
                               .job(Job.builder()
                                       .jobId(UUID.fromString(jobId))
                                       .jobState(JobState.UNSAVED)
                                       .jobCompleted(Instant.now())
                                       .exception(JobErrorDetails.builder()
                                                                 .exception("SomeError")
                                                                 .exceptionDate(Instant.now())
                                                                 .build())
                                       .build())
                               .jobData(Map.of("dataKey", "dataValue"))
                               .build();
    }

    @Test
    void shouldTransitionJobToComplete() {
        // arrange
        final var jobId = UUID.randomUUID().toString();
        final MultiTransferJob job = createJob(jobId);

        // act
        sut.create(job);
        sut.completeJob(jobId, this::doNothing);
        final Optional<MultiTransferJob> multiTransferJob = sut.find(jobId);

        // assert
        assertThat(multiTransferJob).isPresent();

        final MultiTransferJob storedJob = multiTransferJob.get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(storedJob.getJobIdString()).isEqualTo(job.getJobIdString());
            softly.assertThat(storedJob.getJob().getJobState()).isEqualTo(JobState.COMPLETED);
            softly.assertThat(storedJob.getJob().getException().getErrorDetail())
                  .isEqualTo(job.getJob().getException().getErrorDetail());
            softly.assertThat(storedJob.getJobData()).isEqualTo(job.getJobData());
            softly.assertThat(storedJob.getCompletedTransfers()).isEqualTo(job.getCompletedTransfers());
        });
    }

}