package net.catenax.irs.connector.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.github.javafaker.Faker;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobException;
import net.catenax.irs.component.enums.JobState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

@ExtendWith(MockitoExtension.class)
class JobOrchestratorTest {

    @Mock
    TransferProcessManager<DataRequest, TransferProcess> processManager;
    @Mock
    JobStore jobStore;
    @Mock
    RecursiveJobHandler<DataRequest, TransferProcess> handler;
    @InjectMocks
    JobOrchestrator<DataRequest, TransferProcess> sut;

    @Captor
    ArgumentCaptor<MultiTransferJob> jobCaptor;
    @Captor
    ArgumentCaptor<Consumer<TransferProcess>> callbackCaptor;

    Pattern uuid = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    TestMother generate = new TestMother();
    MultiTransferJob job = generate.job(JobState.RUNNING);
    DataRequest dataRequest = generate.dataRequest();
    DataRequest dataRequest2 = generate.dataRequest();
    TransferInitiateResponse okResponse = generate.okResponse();
    TransferInitiateResponse okResponse2 = generate.okResponse();
    TransferProcess transfer = generate.transfer();
    Faker faker = new Faker();
    GlobalAssetIdentification globalAssetId = GlobalAssetIdentification.builder()
                                                                       .globalAssetId(faker.lorem().characters())
                                                                       .build();

    @Test
    void startJob_storesJobWithDataAndState() {
        MultiTransferJob job2 = startJob();
        assertThat(job2).usingRecursiveComparison()
                        .ignoringFields("job.job.jobId")
                        .isEqualTo(MultiTransferJob.builder()
                                                   .jobData(job.getJobData())
                                                   .job(job2.getJob().toBuilder().jobState(JobState.UNSAVED).build())
                                                   .build());
    }

    @Test
    void startJob_storesJobWithUuidAsIdentifier() {
        assertThat(startJob().getJob().getJobId().toString()).matches(uuid.asPredicate());
    }

    @Test
    void startJob_callsHandlerWithJob() {
        // Act
        var newJob = startJob();

        // Assert
        verify(handler).initiate(jobCaptor.capture());
        MultiTransferJob job1 = jobCaptor.getValue();
        assertThat(job1).isEqualTo(newJob);
    }

    @Test
    void startJob_WithTwoDataRequests_StartsTransfers() {
        // Arrange
        when(handler.initiate(any(MultiTransferJob.class)))
            .thenReturn(Stream.of(dataRequest, dataRequest2));
        when(processManager.initiateRequest(eq(dataRequest), any(), any()))
            .thenReturn(okResponse);
        when(processManager.initiateRequest(eq(dataRequest2), any(), any()))
            .thenReturn(okResponse2);

        // Act
        var newJob = startJob();

        // Assert
        verify(processManager).initiateRequest(eq(dataRequest), any(), any());
        verify(processManager).initiateRequest(eq(dataRequest2), any(), any());
    }

    @Test
    void startJob_WithZeroDataRequest_CompletesJob() {
        // Arrange
        when(handler.initiate(any(MultiTransferJob.class))).thenReturn(Stream.empty());

        var response = sut.startJob(job.getJobData());
        var newJob = getStartedJob();

        // Assert
        verifyNoInteractions(processManager);
        verify(jobStore).completeJob(newJob.getJob().getJobId().toString());
        verifyNoMoreInteractions(jobStore);
        verify(handler).complete(newJob);

        assertThat(response).isEqualTo(JobInitiateResponse.builder()
                                                          .jobId(newJob.getJob().getJobId().toString())
                                                          .status(ResponseStatus.OK)
                                                          .build());
    }

    @Test
    void startJob_WithSuccessfulTransferStarts_ReturnsOk() {
        // Arrange
        when(handler.initiate(any(MultiTransferJob.class)))
            .thenReturn(Stream.of(dataRequest));
        when(processManager.initiateRequest(eq(dataRequest), any(), any()))
            .thenReturn(okResponse);

        // Act
        var response = sut.startJob(job.getJobData());

        // Assert
        var newJob = getStartedJob();
        assertThat(response).isEqualTo(JobInitiateResponse.builder()
                                                          .jobId(newJob.getJob().getJobId().toString())
                                                          .status(ResponseStatus.OK)
                                                          .build());
    }

    @ParameterizedTest
    @EnumSource(value = ResponseStatus.class, names = "OK", mode = EXCLUDE)
    void startJob_WhenTransferStartUnsuccessful_Abort(ResponseStatus status) {
        // Arrange
        when(handler.initiate(any()))
            .thenReturn(Stream.of(dataRequest, dataRequest2));
        when(processManager.initiateRequest(eq(dataRequest), any(), any()))
            .thenReturn(generate.response(status));

        // Act
        var response = sut.startJob(job.getJobData());

        // Assert
        verify(processManager).initiateRequest(eq(dataRequest), any(), any());
        verify(processManager, never()).initiateRequest(eq(dataRequest2), any(), any());

        // temporarily created job should be deleted
        verify(jobStore).create(jobCaptor.capture());
        verifyNoMoreInteractions(jobStore);

        assertThat(response).isEqualTo(JobInitiateResponse.builder()
                                                          .jobId(jobCaptor.getValue().getJob().getJobId().toString())
                                                          .status(status)
                                                          .build());
    }

    @Test
    void startJob_WhenHandlerInitiateThrows_StopJob() {
        // Arrange
        when(handler.initiate(any(MultiTransferJob.class))).thenThrow(new RuntimeException());

        // Act
        var response = sut.startJob(job.getJobData());

        // Assert
        verify(jobStore).create(jobCaptor.capture());
        verify(jobStore).markJobInError(jobCaptor.getValue().getJob().getJobId().toString(), "Handler method failed");
        verifyNoMoreInteractions(jobStore);
        verifyNoInteractions(processManager);

        assertThat(response).isEqualTo(JobInitiateResponse.builder()
                                                          .jobId(jobCaptor.getValue().getJob().getJobId().toString())
                                                          .status(ResponseStatus.FATAL_ERROR)
                                                          .build());
    }

    @Test
    void transferProcessCompleted_WhenCalledBackForCompletedTransfer_RunsNextTransfers() {
        // Arrange
        when(processManager.initiateRequest(eq(dataRequest), any(), any()))
            .thenReturn(okResponse);
        when(processManager.initiateRequest(eq(dataRequest2), any(), any()))
            .thenReturn(okResponse2);
        // Act
        callCompleteAndReturnNextTransfers(Stream.of(dataRequest, dataRequest2));

        // Assert
        verify(processManager).initiateRequest(eq(dataRequest), any(), any());
        verify(jobStore).completeTransferProcess(job.getJob().getJobId().toString(), transfer);

    }

    @Test
    void transferProcessCompleted_WhenCalledBackForCompletedTransfer_WithoutNextTransfer() {
        // Act
        callCompleteAndReturnNextTransfers(Stream.empty());

        // Assert
        verify(jobStore).completeTransferProcess(job.getJob().getJobId().toString(), transfer);
        verify(jobStore).find(job.getJob().getJobId().toString());
        verifyNoInteractions(processManager);
        verifyNoMoreInteractions(jobStore);
    }

    @Test
    void transferProcessCompleted_WhenJobNotCompleted_DoesNotCallComplete() {
        // Act
        callCompleteAndReturnNextTransfers(Stream.empty());

        // Assert
        verify(jobStore).completeTransferProcess(job.getJob().getJobId().toString(), transfer);
        verify(jobStore).find(job.getJob().getJobId().toString());
        verifyNoMoreInteractions(jobStore);
        verifyNoMoreInteractions(handler);
    }

    @Test
    void transferProcessCompleted_WhenJobCompleted_CallsComplete() {
        // Arrange
        doAnswer(i -> byCompletingJob()).when(jobStore)
                                        .completeTransferProcess(job.getJob().getJobId().toString(), transfer);

        // Act
        callCompleteAndReturnNextTransfers(Stream.empty());

        // Assert
        verify(handler).complete(job);
        verify(jobStore).completeJob(job.getJob().getJobId().toString());
    }

    @Test
    void transferProcessCompleted_WhenHandlerCompleteThrows_StopJob() {
        // Arrange
        doAnswer(i -> byCompletingJob()).when(jobStore)
                                        .completeTransferProcess(job.getJob().getJobId().toString(), transfer);
        doAnswer(i -> {
            throw JobException.builder().build();
        }).when(handler).complete(any());

        // Act
        callCompleteAndReturnNextTransfers(Stream.empty());

        // Assert
        verify(jobStore).markJobInError(job.getJob().getJobId().toString(), "Handler method failed");
        verify(jobStore).find(job.getJob().getJobId().toString());
        verifyNoMoreInteractions(jobStore);
        verifyNoInteractions(processManager);
    }

    @Test
    void transferProcessCompleted_WhenJobNotFound_Ignore() {
        // Arrange
        when(jobStore.findByProcessId(transfer.getId())).thenReturn(Optional.empty());

        // Act
        callTransferProcessCompletedViaCallback();

        // Assert
        verifyNoInteractions(handler);
        verifyNoMoreInteractions(jobStore);
    }

    @ParameterizedTest
    @EnumSource(value = JobState.class, names = "IN_PROGRESS", mode = EXCLUDE)
    void transferProcessCompleted_WhenJobNotInProgress_Ignore(JobState state) {
        // Arrange
        job = job.toBuilder().job(generate.fakeJob(state)).build();

        // Act
        when(jobStore.findByProcessId(transfer.getId())).thenReturn(Optional.of(job));
        callTransferProcessCompletedViaCallback();

        // Assert
        verifyNoMoreInteractions(jobStore);
        verifyNoInteractions(handler);
    }

    @ParameterizedTest
    @EnumSource(value = ResponseStatus.class, names = "OK", mode = EXCLUDE)
    void transferProcessCompleted_WhenNextTransferStartUnsuccessful_Abort(ResponseStatus status) {
        // Arrange
        when(processManager.initiateRequest(eq(dataRequest), any(), any()))
            .thenReturn(generate.response(status));

        // Act
        callCompleteAndReturnNextTransfers(Stream.of(dataRequest, dataRequest2));

        // Assert
        verify(processManager).initiateRequest(eq(dataRequest), any(), any());
        verify(processManager, never()).initiateRequest(eq(dataRequest2), any(), any());

        // temporarily created job should be deleted
        verify(jobStore).markJobInError(job.getJob().getJobId().toString(), "Failed to start a transfer");
        verifyNoMoreInteractions(jobStore);
    }

    @Test
    void transferProcessCompleted_WhenHandlerRecurseThrows_StopJob() {
        // Arrange
        when(jobStore.findByProcessId(transfer.getId())).thenReturn(Optional.of(job));
        when(handler.recurse(job, transfer)).thenThrow(new RuntimeException());

        // Act
        callTransferProcessCompletedViaCallback();

        // Assert
        verify(jobStore).markJobInError(job.getJob().getJobId().toString(), "Handler method failed");
        verifyNoMoreInteractions(jobStore);
        verifyNoInteractions(processManager);
    }

    private Object byCompletingJob() {
        job = job.toBuilder().transitionTransfersFinished().build();
        lenient().when(jobStore.find(job.getJob().getJobId().toString())).thenReturn(Optional.of(job));
        return null;
    }

    private MultiTransferJob startJob() {
        sut.startJob(job.getJobData());
        return getStartedJob();
    }

    private MultiTransferJob getStartedJob() {
        verify(jobStore).create(jobCaptor.capture());
        return jobCaptor.getValue();
    }

    private void callCompleteAndReturnNextTransfers(Stream<DataRequest> dataRequestStream) {
        when(jobStore.findByProcessId(transfer.getId())).thenReturn(Optional.of(job));
        lenient().when(jobStore.find(job.getJob().getJobId().toString())).thenReturn(Optional.of(job));
        when(handler.recurse(job, transfer)).thenReturn(dataRequestStream);
        callTransferProcessCompletedViaCallback();
    }

    private void callTransferProcessCompletedViaCallback() {
        sut.transferProcessCompleted(transfer);
    }

    private Job createJob() {
        GlobalAssetIdentification globalAssetId = GlobalAssetIdentification.builder()
                                                                           .globalAssetId(UUID.randomUUID().toString())
                                                                           .build();

        return Job.builder()
                  .globalAssetId(globalAssetId)
                  .jobId(UUID.randomUUID())
                  .jobState(JobState.UNSAVED)
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
}
