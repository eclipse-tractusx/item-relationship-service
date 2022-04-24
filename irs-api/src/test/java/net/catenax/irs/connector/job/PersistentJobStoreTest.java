package net.catenax.irs.connector.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.github.javafaker.Faker;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobErrorDetails;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.persistence.MinioBlobPersistence;
import net.catenax.irs.testing.containers.MinioContainer;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PersistentJobStoreTest {
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";

    private static final MinioContainer minioContainer = new MinioContainer(
        new MinioContainer.CredentialsProvider(ACCESS_KEY, SECRET_KEY)).withReuse(true);

    PersistentJobStore sut;
    Faker faker = new Faker();
    TestMother generate = new TestMother();
    MultiTransferJob job = generate.job(JobState.UNSAVED);
    MultiTransferJob job2 = generate.job(JobState.UNSAVED);
    MultiTransferJob originalJob = job.toBuilder().build();
    String otherJobId = faker.lorem().characters(36);
    TransferProcess process1 = generate.transfer();
    TransferProcess process2 = generate.transfer();
    String processId1 = process1.getId();
    String processId2 = process2.getId();
    String errorDetail = faker.lorem().sentence();
    MinioBlobPersistence blobStoreSpy;

    @BeforeAll
    static void startContainer() {
        minioContainer.start();
    }

    @AfterAll
    static void stopContainer() {
        minioContainer.stop();
    }

    @BeforeEach
    void setUp() throws BlobPersistenceException {
        final MinioBlobPersistence blobStore = new MinioBlobPersistence("http://" + minioContainer.getHostAddress(),
            ACCESS_KEY, SECRET_KEY, "testbucket");
        blobStoreSpy = Mockito.spy(blobStore);
        sut = new PersistentJobStore(blobStoreSpy);
    }

    @Test
    void find_WhenNotFound() {
        assertThat(sut.find(otherJobId)).isEmpty();
    }

    @Test
    void findByProcessId_WhenFound() {
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);
        sut.create(job2);
        sut.addTransferProcess(job2.getJob().getJobId().toString(), processId2);

        refreshJob();
        assertThat(sut.findByProcessId(processId1)).isPresent().get().usingRecursiveComparison().isEqualTo(job);
    }

    @Test
    void findByProcessId_WhenNotFound() {
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);

        assertThat(sut.findByProcessId(processId2)).isEmpty();
    }

    @Test
    void create_and_find() {
        sut.create(job);
        assertThat(sut.find(job.getJob().getJobId().toString())).isPresent()
                                                                .get()
                                                                .usingRecursiveComparison()
                                                                .isEqualTo(originalJob.toBuilder()
                                                                                      .job(Job.builder()
                                                                                              .jobCompleted(
                                                                                                  Instant.now())
                                                                                              .jobState(
                                                                                                  JobState.INITIAL)
                                                                                              .build())
                                                                                      .build());
        assertThat(sut.find(otherJobId)).isEmpty();
    }

    @Test
    void addTransferProcess() {
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);
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
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);

        // Act
        sut.completeTransferProcess(job.getJob().getJobId().toString(), process1);

        // Assert
        assertThat(job.getTransferProcessIds()).isEmpty();
    }

    @Test
    void completeTransferProcess_WhenTransferNotFound() {
        // Act
        sut.completeTransferProcess(job.getJob().getJobId().toString(), process1);

        // Assert
        assertThat(job.getTransferProcessIds()).isEmpty();
    }

    @Test
    void completeTransferProcess_WhenTransferAlreadyCompleted() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);
        sut.completeTransferProcess(job.getJob().getJobId().toString(), process1);

        // Act
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
            () -> sut.completeTransferProcess(job.getJob().getJobId().toString(), process1));

        // Assert
        refreshJob();
        assertThat(job.getTransferProcessIds()).isEmpty();
    }

    @Test
    void completeTransferProcess_WhenNotLastTransfer_DoesNotTransitionJob() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId2);

        // Act
        sut.completeTransferProcess(job.getJob().getJobId().toString(), process1);

        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.RUNNING);
    }

    @Test
    void completeTransferProcess_WhenLastTransfer_TransitionsJob() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId2);

        // Act
        sut.completeTransferProcess(job.getJob().getJobId().toString(), process1);
        sut.completeTransferProcess(job.getJob().getJobId().toString(), process2);

        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.TRANSFERS_FINISHED);
    }

    @Test
    void completeJob_WhenJobNotFound() {
        // Arrange
        sut.create(job);
        // Act
        sut.completeJob(otherJobId);
        refreshJob();
        // Assert
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.INITIAL);
    }

    @Test
    void completeJob_WhenJobInInitialState() {
        // Arrange
        sut.create(job);
        sut.create(job2);
        // Act
        sut.completeJob(job.getJob().getJobId().toString());
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.COMPLETED);
        assertThat(Optional.of(job.getJob().getJobCompleted()).isPresent());
        assertThat(job2.getJob().getJobState()).isEqualTo(JobState.UNSAVED);
    }

    @Test
    void completeJob_WhenJobInTransfersCompletedState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);
        sut.completeTransferProcess(job.getJob().getJobId().toString(), process1);
        // Act
        sut.completeJob(job.getJob().getJobId().toString());
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.COMPLETED);
        assertThat(Optional.of(job.getJob().getJobCompleted()).isPresent());
    }

    @Test
    void completeJob_WhenJobInTransfersInProgressState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);
        // Act
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> sut.completeJob(job.getJob().getJobId().toString()));
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
        sut.markJobInError(job.getJob().getJobId().toString(), errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.ERROR);
        assertThat(job2.getJob().getJobState()).isEqualTo(JobState.UNSAVED);
        assertThat(job.getJob().getException().getErrorDetail()).isEqualTo(errorDetail);
        assertThat(Optional.of(job.getJob().getJobCompleted()).isPresent());
    }

    @Test
    void markJobInError_WhenJobInTransfersCompletedState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);
        sut.completeTransferProcess(job.getJob().getJobId().toString(), process1);
        // Act
        sut.markJobInError(job.getJob().getJobId().toString(), errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.ERROR);
        assertThat(Optional.of(job.getJob().getJobCompleted())).isPresent();
    }

    @Test
    void markJobInError_WhenJobInTransfersInProgressState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);
        // Act
        sut.markJobInError(job.getJob().getJobId().toString(), errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.ERROR);
        assertThat(Optional.of(job.getJob().getJobCompleted())).isPresent();
    }

    @Test
    void shouldFindCompletedJobsOlderThanFiveHours() {
        // Arrange
        final Instant nowPlusFiveHours = Instant.now().plusSeconds(3600 * 5);
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);
        sut.completeTransferProcess(job.getJob().getJobId().toString(), process1);
        sut.completeJob(job.getJob().getJobId().toString());
        // Act
        final List<MultiTransferJob> completedJobs = sut.findByStateAndCompletionDateOlderThan(JobState.COMPLETED,
            nowPlusFiveHours);
        // Assert
        assertThat(completedJobs).hasSize(1);
        assertThat(completedJobs.get(0).getJob().getJobState()).isEqualTo(JobState.COMPLETED);
        assertThat(Optional.of(completedJobs.get(0).getJob().getJobCompleted())).isPresent();
    }

    @Test
    void shouldFindFailedJobsOlderThanFiveHours() {
        // Arrange
        final Instant nowPlusFiveHours = Instant.now().plusSeconds(3600 * 5);
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId().toString(), processId1);
        sut.markJobInError(job.getJob().getJobId().toString(), errorDetail);
        // Act
        final List<MultiTransferJob> failedJobs = sut.findByStateAndCompletionDateOlderThan(JobState.ERROR,
            nowPlusFiveHours);
        // Assert
        assertThat(failedJobs).isNotEmpty();
        final Optional<MultiTransferJob> foundJob = failedJobs.stream()
                                                              .filter(
                                                                  failedJob -> failedJob.getJob().getJobId().toString()
                                                                                        .equals(job.getJob()
                                                                                                   .getJobId()
                                                                                                   .toString()))
                                                              .findFirst();
        assertThat(foundJob).isPresent();
        assertThat(foundJob.get().getJob().getJobState()).isEqualTo(JobState.ERROR);
        assertThat(Optional.of(foundJob.get().getJob().getJobCompleted())).isPresent();
    }

    @Test
    void shouldDeleteJobById() {
        // Arrange
        sut.create(job);
        // Act
        sut.deleteJob(job.getJob().getJobId().toString());
        // Assert
        assertThat(sut.find(job.getJob().getJobId().toString())).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenDeleteJobByIllegalId() {
        // Arrange
        final var illegalId = faker.lorem().characters(5000);

        // Act+Assert
        assertThatExceptionOfType(JobException.class).isThrownBy(() -> sut.deleteJob(illegalId));
    }

    @Test
    void shouldThrowExceptionWhenFindJobByIllegalId() {
        // Arrange
        final var illegalId = faker.lorem().characters(5000);

        // Act
        final Optional<MultiTransferJob> job = sut.find(illegalId);

        // Assert
        assertThat(job).isEmpty();
    }

    @Test
    void shouldStoreAndLoadJob() {
        // arrange
        final var jobId = faker.lorem().characters(36);
        final MultiTransferJob job = createJob(jobId);

        // act
        sut.create(job);
        final Optional<MultiTransferJob> multiTransferJob = sut.find(jobId);

        // assert
        assertThat(multiTransferJob).isPresent();

        final MultiTransferJob storedJob = multiTransferJob.get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(storedJob.getJob().getJobId().toString()).isEqualTo(job.getJob().getJobId().toString());
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
        final var jobId = faker.lorem().characters(36);
        final MultiTransferJob job = createJob(jobId);

        // act
        sut.create(job);
        sut.completeJob(jobId);
        final Optional<MultiTransferJob> multiTransferJob = sut.find(jobId);

        // assert
        assertThat(multiTransferJob).isPresent();

        final MultiTransferJob storedJob = multiTransferJob.get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(storedJob.getJob().getJobId().toString()).isEqualTo(job.getJob().getJobId().toString());
            softly.assertThat(storedJob.getJob().getJobState()).isEqualTo(JobState.INITIAL);
            softly.assertThat(storedJob.getJob().getException().getErrorDetail())
                  .isEqualTo(job.getJob().getException().getErrorDetail());
            softly.assertThat(storedJob.getJob().getJobCompleted()).isEqualTo(job.getJob().getJobCompleted());
            softly.assertThat(storedJob.getJobData()).isEqualTo(job.getJobData());
            softly.assertThat(storedJob.getCompletedTransfers()).isEqualTo(job.getCompletedTransfers());
        });
    }

    private void refreshJob() {
        job = sut.find(job.getJob().getJobId().toString()).get();
    }

    @Test
    void shouldThrowExceptionWhenCreatingJob() throws BlobPersistenceException {
        // Arrange
        final var ex = new BlobPersistenceException("test", new RuntimeException());
        doThrow(ex).when(blobStoreSpy).putBlob(anyString(), any());

        // Act
        sut.create(job);

        // Assert
        assertThat(sut.find(job.getJob().getJobId().toString())).isEmpty();
    }
}