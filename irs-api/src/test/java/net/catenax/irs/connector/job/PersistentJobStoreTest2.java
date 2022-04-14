package net.catenax.irs.connector.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.github.javafaker.Faker;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.persistence.MinioBlobPersistence;
import net.catenax.irs.testing.containers.MinioContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PersistentJobStoreTest2 {
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
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
    private static final MinioContainer minioContainer = new MinioContainer(
            new MinioContainer.CredentialsProvider(ACCESS_KEY, SECRET_KEY)).withReuse(true);

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
        sut = new PersistentJobStore(blobStore);
    }

    @Test
    void find_WhenNotFound() {
        assertThat(sut.find(otherJobId)).isEmpty();
    }

    @Test
    void findByProcessId_WhenFound() {
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);
        sut.create(job2);
        sut.addTransferProcess(job2.getJobId(), processId2);

        refreshJob();
        assertThat(sut.findByProcessId(processId1)).isPresent().get().usingRecursiveComparison().isEqualTo(job);
    }

    @Test
    void findByProcessId_WhenNotFound() {
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);

        assertThat(sut.findByProcessId(processId2)).isEmpty();
    }

    @Test
    void create_and_find() {
        sut.create(job);
        assertThat(sut.find(job.getJobId())).isPresent()
                                            .get()
                                            .usingRecursiveComparison()
                                            .isEqualTo(originalJob.toBuilder()
                                                                  .completionDate(Optional.empty())
                                                                  .state(JobState.INITIAL)
                                                                  .build());
        assertThat(sut.find(otherJobId)).isEmpty();
    }

    @Test
    void addTransferProcess() {
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);
        refreshJob();
        assertThat(job.getTransferProcessIds()).containsExactly(processId1);
        assertThat(job.getState()).isEqualTo(JobState.IN_PROGRESS);
    }

    @Test
    void completeTransferProcess_WhenJobNotFound() {
        sut.completeTransferProcess(otherJobId, process1);
    }

    @Test
    void completeTransferProcess_WhenTransferFound() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);

        // Act
        sut.completeTransferProcess(job.getJobId(), process1);

        // Assert
        assertThat(job.getTransferProcessIds()).isEmpty();
    }

    @Test
    void completeTransferProcess_WhenTransferNotFound() {
        // Act
        sut.completeTransferProcess(job.getJobId(), process1);

        // Assert
        assertThat(job.getTransferProcessIds()).isEmpty();
    }

    @Test
    void completeTransferProcess_WhenTransferAlreadyCompleted() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);
        sut.completeTransferProcess(job.getJobId(), process1);

        // Act
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> sut.completeTransferProcess(job.getJobId(), process1));

        // Assert
        refreshJob();
        assertThat(job.getTransferProcessIds()).isEmpty();
    }

    @Test
    void completeTransferProcess_WhenNotLastTransfer_DoesNotTransitionJob() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);
        sut.addTransferProcess(job.getJobId(), processId2);

        // Act
        sut.completeTransferProcess(job.getJobId(), process1);

        // Assert
        refreshJob();
        assertThat(job.getState()).isEqualTo(JobState.IN_PROGRESS);
    }

    @Test
    void completeTransferProcess_WhenLastTransfer_TransitionsJob() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);
        sut.addTransferProcess(job.getJobId(), processId2);

        // Act
        sut.completeTransferProcess(job.getJobId(), process1);
        sut.completeTransferProcess(job.getJobId(), process2);

        // Assert
        refreshJob();
        assertThat(job.getState()).isEqualTo(JobState.TRANSFERS_FINISHED);
    }

    @Test
    void completeJob_WhenJobNotFound() {
        // Arrange
        sut.create(job);
        // Act
        sut.completeJob(otherJobId);
        refreshJob();
        // Assert
        assertThat(job.getState()).isEqualTo(JobState.INITIAL);
    }

    @Test
    void completeJob_WhenJobInInitialState() {
        // Arrange
        sut.create(job);
        sut.create(job2);
        // Act
        sut.completeJob(job.getJobId());
        // Assert
        refreshJob();
        assertThat(job.getState()).isEqualTo(JobState.COMPLETED);
        assertTrue(job.getCompletionDate().isPresent());
        assertThat(job2.getState()).isEqualTo(JobState.UNSAVED);
    }

    @Test
    void completeJob_WhenJobInTransfersCompletedState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);
        sut.completeTransferProcess(job.getJobId(), process1);
        // Act
        sut.completeJob(job.getJobId());
        // Assert
        refreshJob();
        assertThat(job.getState()).isEqualTo(JobState.COMPLETED);
        assertTrue(job.getCompletionDate().isPresent());
    }

    @Test
    void completeJob_WhenJobInTransfersInProgressState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);
        // Act
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> sut.completeJob(job.getJobId()));
        // Assert
        refreshJob();
        assertThat(job.getState()).isEqualTo(JobState.IN_PROGRESS);
    }

    @Test
    void markJobInError_WhenJobNotFound() {
        // Arrange
        sut.create(job);
        // Act
        sut.markJobInError(otherJobId, errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getState()).isEqualTo(JobState.INITIAL);
    }

    @Test
    void markJobInError_WhenJobInInitialState() {
        // Arrange
        sut.create(job);
        sut.create(job2);
        // Act
        sut.markJobInError(job.getJobId(), errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getState()).isEqualTo(JobState.ERROR);
        assertThat(job2.getState()).isEqualTo(JobState.UNSAVED);
        assertThat(job.getErrorDetail()).isEqualTo(errorDetail);
        assertTrue(job.getCompletionDate().isPresent());
    }

    @Test
    void markJobInError_WhenJobInTransfersCompletedState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);
        sut.completeTransferProcess(job.getJobId(), process1);
        // Act
        sut.markJobInError(job.getJobId(), errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getState()).isEqualTo(JobState.ERROR);
        assertTrue(job.getCompletionDate().isPresent());
    }

    @Test
    void markJobInError_WhenJobInTransfersInProgressState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);
        // Act
        sut.markJobInError(job.getJobId(), errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getState()).isEqualTo(JobState.ERROR);
        assertTrue(job.getCompletionDate().isPresent());
    }

    @Test
    void shouldFindCompletedJobsOlderThanFiveHours() {
        // Arrange
        final LocalDateTime nowPlusFiveHours = LocalDateTime.now().plusHours(5);
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);
        sut.completeTransferProcess(job.getJobId(), process1);
        sut.completeJob(job.getJobId());
        // Act
        final List<MultiTransferJob> completedJobs = sut.findByStateAndCompletionDateOlderThan(JobState.COMPLETED,
                nowPlusFiveHours);
        // Assert
        assertThat(completedJobs.size()).isEqualTo(1);
        assertThat(completedJobs.get(0).getState()).isEqualTo(JobState.COMPLETED);
        assertTrue(completedJobs.get(0).getCompletionDate().isPresent());
    }

    @Test
    void shouldFindFailedJobsOlderThanFiveHours() {
        // Arrange
        final LocalDateTime nowPlusFiveHours = LocalDateTime.now().plusHours(5);
        sut.create(job);
        sut.addTransferProcess(job.getJobId(), processId1);
        sut.markJobInError(job.getJobId(), errorDetail);
        // Act
        final List<MultiTransferJob> failedJobs = sut.findByStateAndCompletionDateOlderThan(JobState.ERROR,
                nowPlusFiveHours);
        // Assert
        assertThat(failedJobs.size()).isPositive();
        final Optional<MultiTransferJob> foundJob = failedJobs.stream()
                                                           .filter(failedJob -> failedJob.getJobId()
                                                                                         .equals(job.getJobId()))
                                                           .findFirst();
        assertThat(foundJob).isPresent();
        assertThat(foundJob.get().getState()).isEqualTo(JobState.ERROR);
        assertTrue(foundJob.get().getCompletionDate().isPresent());
    }

    @Test
    void shouldDeleteJobById() {
        // Arrange
        sut.create(job);
        // Act
        sut.deleteJob(job.getJobId());
        // Assert
        assertThat(sut.find(job.getJobId())).isEmpty();
    }

    private void refreshJob() {
        job = sut.find(job.getJobId()).get();
    }
}