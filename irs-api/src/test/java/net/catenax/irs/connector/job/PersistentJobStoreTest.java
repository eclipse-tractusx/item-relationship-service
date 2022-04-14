package net.catenax.irs.connector.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;

import com.github.javafaker.Faker;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.persistence.MinioBlobPersistence;
import net.catenax.irs.testing.containers.MinioContainer;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class PersistentJobStoreTest {

    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private PersistentJobStore testee;
    private final Faker faker = new Faker();

    @Container
    private final MinioContainer minioContainer = new MinioContainer(
            new MinioContainer.CredentialsProvider(ACCESS_KEY, SECRET_KEY));

    @BeforeEach
    void setUp() throws BlobPersistenceException {
        final MinioBlobPersistence blobStore = new MinioBlobPersistence("http://" + minioContainer.getHostAddress(),
                ACCESS_KEY, SECRET_KEY, "testbucket");
        testee = new PersistentJobStore(blobStore);
    }

    @Test
    void shouldStoreAndLoadJob() {
        // arrange
        final var jobId = faker.lorem().characters(36);
        final MultiTransferJob job = createJob(jobId, JobState.IN_PROGRESS);

        // act
        testee.create(job);
        final Optional<MultiTransferJob> multiTransferJob = testee.find(jobId);

        // assert
        assertThat(multiTransferJob).isPresent();

        final MultiTransferJob storedJob = multiTransferJob.get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(storedJob.getJobId()).isEqualTo(job.getJobId());
            softly.assertThat(storedJob.getState()).isEqualTo(job.getState());
            softly.assertThat(storedJob.getErrorDetail()).isEqualTo(job.getErrorDetail());
            softly.assertThat(storedJob.getCompletionDate()).isEqualTo(job.getCompletionDate());
            softly.assertThat(storedJob.getJobData()).isEqualTo(job.getJobData());
            softly.assertThat(storedJob.getCompletedTransfers()).isEqualTo(job.getCompletedTransfers());
        });

    }

    private MultiTransferJob createJob(final String jobId, final JobState state) {
        return MultiTransferJob.builder()
                               .jobId(jobId)
                               .jobData(Map.of("dataKey", "dataValue"))
                               .state(state)
                               .errorDetail("SomeError")
                               .completionDate(Optional.empty())
                               .build();
    }


    @Test
    void shouldTransitionJobToComplete() {
        // arrange
        final var jobId = faker.lorem().characters(36);
        final MultiTransferJob job = createJob(jobId, JobState.TRANSFERS_FINISHED);

        // act
        testee.create(job);
        testee.completeJob(jobId);
        final Optional<MultiTransferJob> multiTransferJob = testee.find(jobId);

        // assert
        assertThat(multiTransferJob).isPresent();

        final MultiTransferJob storedJob = multiTransferJob.get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(storedJob.getJobId()).isEqualTo(job.getJobId());
            softly.assertThat(storedJob.getState()).isEqualTo(JobState.COMPLETED);
            softly.assertThat(storedJob.getErrorDetail()).isEqualTo(job.getErrorDetail());
            softly.assertThat(storedJob.getCompletionDate()).isPresent();
            softly.assertThat(storedJob.getJobData()).isEqualTo(job.getJobData());
            softly.assertThat(storedJob.getCompletedTransfers()).isEqualTo(job.getCompletedTransfers());
        });
    }


}