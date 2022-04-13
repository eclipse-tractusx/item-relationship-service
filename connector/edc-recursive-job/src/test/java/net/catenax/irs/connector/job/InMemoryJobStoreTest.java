package net.catenax.irs.connector.job;

<<<<<<< HEAD
=======
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;

>>>>>>> 040c1a75e4e29f85dad02f4607d2e21498b33315
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.github.javafaker.Faker;
import jakarta.ws.rs.HttpMethod;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.enums.JobState;
import org.eclipse.dataspaceconnector.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.junit.jupiter.api.Test;

class InMemoryJobStoreTest {

<<<<<<< HEAD
    final int TTL_IN_HOUR_SECONDS = 3600;

    Monitor monitor = new ConsoleMonitor();
    InMemoryJobStore sut = new InMemoryJobStore(monitor);
=======
    InMemoryJobStore sut = new InMemoryJobStore();
>>>>>>> 040c1a75e4e29f85dad02f4607d2e21498b33315
    Faker faker = new Faker();
    TestMother generate = new TestMother();
    MultiTransferJob job = generate.job(JobState.UNSAVED);
    MultiTransferJob job2 = generate.job(JobState.UNSAVED);
    MultiTransferJob originalJob = job.toBuilder().job(createJob()).build();
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
        sut.addTransferProcess(job.getJob().getJobId(), processId1);
        sut.create(job2);
        sut.addTransferProcess(job2.getJob().getJobId(), processId2);

        refreshJob();
        assertThat(sut.findByProcessId(processId1)).contains(job);
    }

    @Test
    void findByProcessId_WhenNotFound() {
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);

        assertThat(sut.findByProcessId(processId2)).isEmpty();
    }

    @Test
    void create_and_find() {
        sut.create(job);
        assertThat(sut.find(job.getJob().getJobId())).isPresent()
                                                     .get()
                                                     .usingRecursiveComparison()
                                                     .isEqualTo(originalJob.toBuilder()
                                                                           .job(job.getJob()
                                                                                   .toBuilder()
                                                                                   .jobState(JobState.INITIAL)
                                                                                   .build())
                                                                           .build());
        assertThat(sut.find(otherJobId)).isEmpty();
    }

    @Test
    void addTransferProcess() {
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);
        refreshJob();
        assertThat(job.getTransferProcessIds()).containsExactly(processId1);
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.IN_PROGRESS);
    }

    @Test
    void completeTransferProcess_WhenJobNotFound() {
        sut.completeTransferProcess(otherJobId, process1);
    }

    @Test
    void completeTransferProcess_WhenTransferFound() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);

        // Act
        sut.completeTransferProcess(job.getJob().getJobId(), process1);

        // Assert
        assertThat(job.getTransferProcessIds()).isEmpty();
    }

    @Test
    void completeTransferProcess_WhenTransferNotFound() {
        // Act
        sut.completeTransferProcess(job.getJob().getJobId(), process1);

        // Assert
        assertThat(job.getTransferProcessIds()).isEmpty();
    }

    @Test
    void completeTransferProcess_WhenTransferAlreadyCompleted() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);
        sut.completeTransferProcess(job.getJob().getJobId(), process1);

        // Act
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> sut.completeTransferProcess(job.getJob().getJobId(), process1));

        // Assert
        refreshJob();
        assertThat(job.getTransferProcessIds()).isEmpty();
    }

    @Test
    void completeTransferProcess_WhenNotLastTransfer_DoesNotTransitionJob() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);
        sut.addTransferProcess(job.getJob().getJobId(), processId2);

        // Act
        sut.completeTransferProcess(job.getJob().getJobId(), process1);

        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.IN_PROGRESS);
    }

    @Test
    void completeTransferProcess_WhenLastTransfer_TransitionsJob() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);
        sut.addTransferProcess(job.getJob().getJobId(), processId2);

        // Act
        sut.completeTransferProcess(job.getJob().getJobId(), process1);
        sut.completeTransferProcess(job.getJob().getJobId(), process2);

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
        sut.completeJob(job.getJob().getJobId());
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.COMPLETED);
        assertTrue(Optional.ofNullable(job.getJob().getJobFinished()).isPresent());
        assertThat(job2.getJob().getJobState()).isEqualTo(JobState.INITIAL);
    }

    @Test
    void completeJob_WhenJobInTransfersCompletedState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);
        sut.completeTransferProcess(job.getJob().getJobId(), process1);
        // Act
        sut.completeJob(job.getJob().getJobId());
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.COMPLETED);
        assertTrue(Optional.ofNullable(job.getJob().getJobFinished()).isPresent());
    }

    @Test
    void completeJob_WhenJobInTransfersInProgressState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);
        // Act
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> sut.completeJob(job.getJob().getJobId()));
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.IN_PROGRESS);
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
        sut.markJobInError(job.getJob().getJobId(), errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.ERROR);
        assertThat(job2.getJob().getJobState()).isEqualTo(JobState.INITIAL);
        assertThat(job.getJob().getException()).isEqualTo(errorDetail);
        assertTrue(Optional.ofNullable(job.getJob().getJobFinished()).isPresent());
    }

    @Test
    void markJobInError_WhenJobInTransfersCompletedState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);
        sut.completeTransferProcess(job.getJob().getJobId(), process1);
        // Act
        sut.markJobInError(job.getJob().getJobId(), errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.ERROR);
        assertTrue(Optional.ofNullable(job.getJob().getJobFinished()).isPresent());
    }

    @Test
    void markJobInError_WhenJobInTransfersInProgressState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);
        // Act
        sut.markJobInError(job.getJob().getJobId(), errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getJob().getJobState()).isEqualTo(JobState.ERROR);
        assertTrue(Optional.ofNullable(job.getJob().getJobFinished()).isPresent());
    }

    @Test
    void shouldFindCompletedJobsOlderThanFiveHours() {
        // Arrange
        final Instant nowPlusFiveHours = Instant.now().plusSeconds(TTL_IN_HOUR_SECONDS * 5);
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);
        sut.completeTransferProcess(job.getJob().getJobId(), process1);
        sut.completeJob(job.getJob().getJobId());
        // Act
        final List<MultiTransferJob> completedJobs = sut.findByStateAndCompletionDateOlderThan(JobState.COMPLETED,
                nowPlusFiveHours);
        // Assert
        assertThat(completedJobs.size()).isEqualTo(1);
        assertThat(completedJobs.get(0).getJob().getJobState()).isEqualTo(JobState.COMPLETED);
        assertTrue(Optional.ofNullable(completedJobs.get(0).getJob().getJobFinished()).isPresent());
    }

    @Test
    void shouldFindFailedJobsOlderThanFiveHours() {
        // Arrange
        final Instant nowPlusFiveHours = Instant.now().plusSeconds(TTL_IN_HOUR_SECONDS * 5);
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);
        sut.markJobInError(job.getJob().getJobId(), errorDetail);
        // Act
        final List<MultiTransferJob> failedJobs = sut.findByStateAndCompletionDateOlderThan(JobState.ERROR,
                nowPlusFiveHours);
        // Assert
        assertThat(failedJobs.size()).isEqualTo(1);
        assertThat(failedJobs.get(0).getJob().getJobState()).isEqualTo(JobState.ERROR);
        assertTrue(Optional.ofNullable(failedJobs.get(0).getJob().getJobFinished()).isPresent());
    }

    @Test
    void shouldDeleteJobById() {
        // Arrange
        sut.create(job);
        // Act
        sut.deleteJob(job.getJob().getJobId());
        // Assert
        assertThat(sut.find(job.getJob().getJobId())).isEmpty();
    }

    @Test
    void jobStateIsInitial() {
        sut.create(job);

        assertThat(sut.getJobState(job.getJob().getJobId())).isEqualTo(JobState.INITIAL);
    }

    @Test
    void jobStateIsInProgress() {
        sut.create(job);
        sut.addTransferProcess(job.getJob().getJobId(), processId1);
        assertThat(sut.getJobState(job.getJob().getJobId())).isEqualTo(JobState.IN_PROGRESS);
    }

    private void refreshJob() {
        job = sut.find(job.getJob().getJobId()).get();
    }

    private Job createJob() {
        GlobalAssetIdentification globalAssetId = GlobalAssetIdentification.builder()
                                                                           .globalAssetId(UUID.randomUUID().toString())
                                                                           .build();

        return Job.builder()
                  .globalAssetId(globalAssetId)
                  .jobId(UUID.randomUUID().toString())
                  .jobState(JobState.INITIAL)
                  .createdOn(Instant.now())
                  .lastModifiedOn(Instant.now())
                  .requestUrl(faker.lorem().characters())
                  .action(HttpMethod.POST)
                  .build();
    }
}