/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.connector.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameter;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.datafaker.Faker;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.common.persistence.MinioBlobPersistence;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobErrorDetails;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.services.MeterRegistryService;
import org.eclipse.tractusx.irs.testing.containers.MinioContainer;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.eclipse.tractusx.irs.util.TestMother;
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
    final int TTL_IN_HOUR_SECONDS = 3600;
    PersistentJobStore sut;
    Faker faker = new Faker();
    TestMother generate = new TestMother();
    MultiTransferJob job = generate.job(JobState.UNSAVED);
    MultiTransferJob originalJob = job.toBuilder().build();
    MultiTransferJob job2 = generate.job(JobState.UNSAVED);
    String otherJobId = faker.lorem().characters(36);
    TransferProcess process1 = generate.transfer();
    String processId1 = process1.getId();
    TransferProcess process2 = generate.transfer();
    String processId2 = process2.getId();
    String errorDetail = faker.lorem().sentence();
    MinioBlobPersistence blobStoreSpy;

    MeterRegistryService meterRegistryService = TestMother.simpleMeterRegistryService(); // mock(MeterRegistryService.class);

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
                ACCESS_KEY, SECRET_KEY, "testbucket", 1);
        blobStoreSpy = Mockito.spy(blobStore);
        sut = new PersistentJobStore(blobStoreSpy, meterRegistryService);
    }

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
        assertThat(sut.findByProcessId(processId1)).isPresent().get().usingRecursiveComparison().isEqualTo(job);
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
        assertThat(sut.find(job.getJobIdString())).isPresent();
        assertThat(sut.find(otherJobId)).isEmpty();
    }

    @Test
    void addTransferProcess() {
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        refreshJob();
        assertThat(job.getTransferProcessIds()).containsExactly(processId1);
        assertThat(job.getJob().getState()).isEqualTo(JobState.RUNNING);
    }

    @Test
    void completeTransferProcess_WhenJobNotFound() {
        sut.completeTransferProcess(otherJobId, process1);

        // Assertion for sonar
        assertThat(otherJobId).isNotBlank();
    }

    @Test
    void shouldSerializeAndDeserializeMultiTransferJob() {
        final JsonUtil jsonUtil = new JsonUtil();
        final String firstSerialization = jsonUtil.asString(job);
        final MultiTransferJob result = jsonUtil.fromString(firstSerialization, MultiTransferJob.class);
        final String secondSerialization = jsonUtil.asString(result);
        assertThat(firstSerialization).isEqualTo(secondSerialization);
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
        final String jobId = job.getJobIdString();
        sut.addTransferProcess(jobId, processId1);
        sut.completeTransferProcess(jobId, process1);

        // Act
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> sut.completeTransferProcess(jobId, process1));

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
        assertThat(job.getJob().getState()).isEqualTo(JobState.RUNNING);
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
        assertThat(job.getJob().getState()).isEqualTo(JobState.TRANSFERS_FINISHED);
    }

    @Test
    void completeJob_WhenJobNotFound() {
        // Arrange
        sut.create(job);
        // Act
        sut.completeJob(otherJobId, this::doNothing);
        refreshJob();
        // Assert
        assertThat(job.getJob().getState()).isEqualTo(JobState.INITIAL);
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
        assertThat(job.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(Optional.of(job.getJob().getCompletedOn())).isPresent();
        assertThat(job2.getJob().getState()).isEqualTo(JobState.INITIAL);
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
        assertThat(job.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(Optional.of(job.getJob().getCompletedOn())).isPresent();
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
        assertThat(job.getJob().getState()).isEqualTo(JobState.RUNNING);
    }

    private void doNothing(final MultiTransferJob multiTransferJob) {
    }

    @Test
    void markJobInError_WhenJobNotFound() {
        // Arrange
        sut.create(job);
        // Act
        sut.markJobInError(otherJobId, errorDetail, errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getJob().getState()).isEqualTo(JobState.INITIAL);
    }

    @Test
    void markJobInError_WhenJobInInitialState() {
        // Arrange
        sut.create(job);
        sut.create(job2);
        // Act
        sut.markJobInError(job.getJobIdString(), errorDetail, errorDetail);
        // Assert
        refreshJob();
        refreshJob2();
        assertThat(job.getJob().getState()).isEqualTo(JobState.ERROR);
        assertThat(job2.getJob().getState()).isEqualTo(JobState.INITIAL);
        assertThat(job.getJob().getException().getErrorDetail()).isEqualTo(errorDetail);
        assertThat(job.getJob().getException().getException()).isEqualTo(errorDetail);
        assertThat(Optional.of(job.getJob().getCompletedOn())).isPresent();
    }

    @Test
    void markJobInError_WhenJobInTransfersCompletedState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        sut.completeTransferProcess(job.getJobIdString(), process1);
        // Act
        sut.markJobInError(job.getJobIdString(), errorDetail, errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getJob().getState()).isEqualTo(JobState.ERROR);
        assertThat(Optional.of(job.getJob().getCompletedOn())).isPresent();
    }

    @Test
    void markJobInError_WhenJobInTransfersInProgressState() {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        // Act
        sut.markJobInError(job.getJobIdString(), errorDetail, errorDetail);
        // Assert
        refreshJob();
        assertThat(job.getJob().getState()).isEqualTo(JobState.ERROR);
        assertThat(Optional.of(job.getJob().getCompletedOn())).isPresent();
    }

    @Test
    void shouldFindCompletedJobsOlderThanFiveHours() {
        // Arrange
        final ZonedDateTime nowPlusFiveHours = ZonedDateTime.now().plusSeconds(TTL_IN_HOUR_SECONDS * 5);
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        sut.completeTransferProcess(job.getJobIdString(), process1);
        sut.completeJob(job.getJobIdString(), this::doNothing);
        // Act
        final List<MultiTransferJob> completedJobs = sut.findByStateAndCompletionDateOlderThan(JobState.COMPLETED,
                nowPlusFiveHours);
        // Assert
        assertThat(completedJobs).hasSize(1);
        assertThat(completedJobs.get(0).getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(Optional.of(completedJobs.get(0).getJob().getCompletedOn())).isPresent();
    }

    @Test
    void shouldFindFailedJobsOlderThanFiveHours() {
        // Arrange
        final ZonedDateTime nowPlusFiveHours = ZonedDateTime.now().plusSeconds(3600 * 5);
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        sut.markJobInError(job.getJobIdString(), errorDetail, errorDetail);
        // Act
        final List<MultiTransferJob> failedJobs = sut.findByStateAndCompletionDateOlderThan(JobState.ERROR,
                nowPlusFiveHours);
        // Assert
        assertThat(failedJobs).isNotEmpty();
        final Optional<MultiTransferJob> foundJob = failedJobs.stream()
                                                              .filter(failedJob -> failedJob.getJob()
                                                                                            .getId()
                                                                                            .toString()
                                                                                            .equals(job.getJob()
                                                                                                       .getId()
                                                                                                       .toString()))
                                                              .findFirst();
        assertThat(foundJob).isPresent();
        assertThat(foundJob.get().getJob().getState()).isEqualTo(JobState.ERROR);
        assertThat(Optional.of(foundJob.get().getJob().getCompletedOn())).isPresent();
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
            softly.assertThat(storedJob.getJob().getState()).isEqualTo(JobState.INITIAL);
            softly.assertThat(storedJob.getJob().getException().getErrorDetail())
                  .isEqualTo(job.getJob().getException().getErrorDetail());
            softly.assertThat(storedJob.getJob().getCompletedOn()).isEqualTo(job.getJob().getCompletedOn());
            softly.assertThat(storedJob.getJobParameter()).isEqualTo(job.getJobParameter());
            softly.assertThat(storedJob.getCompletedTransfers()).isEqualTo(job.getCompletedTransfers());
        });

    }

    private MultiTransferJob createJob(final String jobId) {
        return MultiTransferJob.builder()
                               .job(Job.builder()
                                       .id(UUID.fromString(jobId))
                                       .state(JobState.UNSAVED)
                                       .completedOn(ZonedDateTime.now())
                                       .exception(JobErrorDetails.builder()
                                                                 .exception("SomeError")
                                                                 .exceptionDate(ZonedDateTime.now())
                                                                 .build())
                                       .parameter(jobParameter())
                                       .build())
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

        // assertec
        assertThat(multiTransferJob).isPresent();

        final MultiTransferJob storedJob = multiTransferJob.get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(storedJob.getJobIdString()).isEqualTo(job.getJobIdString());
            softly.assertThat(storedJob.getJob().getState()).isEqualTo(JobState.COMPLETED);
            softly.assertThat(storedJob.getJob().getException().getErrorDetail())
                  .isEqualTo(job.getJob().getException().getErrorDetail());
            softly.assertThat(storedJob.getJobParameter()).isEqualTo(job.getJobParameter());
            softly.assertThat(storedJob.getCompletedTransfers()).isEqualTo(job.getCompletedTransfers());
        });
    }

    private void refreshJob() {
        job = sut.find(job.getJobIdString()).get();
    }

    private void refreshJob2() {
        job2 = sut.find(job2.getJobIdString()).get();
    }

    @Test
    void shouldThrowExceptionWhenCreatingJob() throws BlobPersistenceException {
        // Arrange
        final var ex = new BlobPersistenceException("test", new RuntimeException());
        doThrow(ex).when(blobStoreSpy).putBlob(any(), any());

        // Act
        sut.create(job);

        // Assert
        assertThat(sut.find(job.getJobIdString())).isEmpty();
    }

    @Test
    void jobStateIsInProgress() {
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        final Optional<MultiTransferJob> multiTransferJob = sut.get(job.getJobIdString());
        assertThat(multiTransferJob).isPresent();
        assertThat(multiTransferJob.get().getJob().getState()).isEqualTo(JobState.RUNNING);
    }

    @Test
    void checkLastModifiedOnAfterCreation() {
        // Arrange
        sut.create(job);
        MultiTransferJob job1 = job.toBuilder().build();

        // Act
        sut.addTransferProcess(job.getJobId().toString(), processId1);
        MultiTransferJob job2 = sut.find(job.getJob().getId().toString()).get();

        // Assert
        assertThat(job2.getJob().getLastModifiedOn()).isAfter(job1.getJob().getLastModifiedOn());
    }

    @Test
    void shouldRemoveJobAndExecuteDeleteMethodWithFoundCompletedTransferIds() throws BlobPersistenceException {
        // Arrange
        sut.create(job);
        sut.addTransferProcess(job.getJobIdString(), processId1);
        sut.completeTransferProcess(job.getJobIdString(), process1);
        sut.completeJob(job.getJobIdString(), this::doNothing);

        // Act
        sut.remove(job.getJobIdString());

        // Assert
        verify(blobStoreSpy).delete(
            argThat(s -> s.contains(job.getJobIdString())), // jobId
            argThat(s -> s.size() == 2) // jobId + processId
        );
        verify(blobStoreSpy, times(1)).delete(anyString(), anyList());
    }

    @Test
    void shouldGetAllCorrectJobEvenCorruptedBlobIsStored() throws BlobPersistenceException {
        // Arrange

        String wrongJson = "{\"key\": \"value\"}";
        blobStoreSpy.putBlob("job:123", wrongJson.getBytes(StandardCharsets.UTF_8));
        sut.create(job);

        // Act
        Collection<MultiTransferJob> actual = sut.getAll();

        // Assert
        assertThat(actual).isNotEmpty();
    }

}