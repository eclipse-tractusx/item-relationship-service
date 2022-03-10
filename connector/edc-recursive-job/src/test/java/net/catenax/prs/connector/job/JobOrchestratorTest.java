package net.catenax.prs.connector.job;

import org.eclipse.dataspaceconnector.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.transfer.TransferInitiateResponse;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessObservable;
import org.eclipse.dataspaceconnector.spi.transfer.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobOrchestratorTest {

    @Spy
    Monitor monitor = new ConsoleMonitor();
    @Mock
    TransferProcessManager processManager;
    @Mock
    JobStore jobStore;
    @Mock
    RecursiveJobHandler handler;
    @Mock
    TransferProcessObservable transferProcessObservable;
    @InjectMocks
    JobOrchestrator sut;

    @Captor
    ArgumentCaptor<MultiTransferJob> jobCaptor;
    @Captor
    ArgumentCaptor<JobTransferCallback> callbackCaptor;

    Pattern uuid = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    TestMother generate = new TestMother();
    MultiTransferJob job = generate.job(JobState.IN_PROGRESS);
    DataRequest dataRequest = generate.dataRequest();
    DataRequest dataRequest2 = generate.dataRequest();
    TransferInitiateResponse okResponse = generate.okResponse();
    TransferInitiateResponse okResponse2 = generate.okResponse();
    TransferProcess transfer = generate.transfer();

    @Test
    void startJob_storesJobWithDataAndState() {
        assertThat(startJob())
                .usingRecursiveComparison()
                .ignoringFields("jobId")
                .isEqualTo(MultiTransferJob.builder()
                        .jobData(job.getJobData())
                        .state(JobState.UNSAVED)
                        .build());
    }

    @Test
    void startJob_storesJobWithUuidAsIdentifier() {
        assertThat(startJob().getJobId())
                .matches(uuid);
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
        when(processManager.initiateConsumerRequest(dataRequest))
                .thenReturn(okResponse);
        when(processManager.initiateConsumerRequest(dataRequest2))
                .thenReturn(okResponse2);

        // Act
        var newJob = startJob();

        // Assert
        verify(processManager).initiateConsumerRequest(dataRequest);
        verify(jobStore).addTransferProcess(newJob.getJobId(), okResponse.getId());
        verify(processManager).initiateConsumerRequest(dataRequest2);
        verify(jobStore).addTransferProcess(newJob.getJobId(), okResponse2.getId());
    }

    @Test
    void startJob_WithZeroDataRequest_CompletesJob() {
        // Arrange
        when(handler.initiate(any(MultiTransferJob.class)))
                .thenReturn(Stream.empty());

        // Act
        var response = sut.startJob(job.getJobData());
        var newJob = getStartedJob();

        // Assert
        verifyNoInteractions(processManager);
        verify(jobStore).completeJob(newJob.getJobId());
        verifyNoMoreInteractions(jobStore);
        verify(handler).complete(newJob);

        assertThat(response)
                .isEqualTo(
                        JobInitiateResponse.builder().jobId(newJob.getJobId()).status(ResponseStatus.OK).build());
    }

    @Test
    void startJob_WithSuccessfulTransferStarts_ReturnsOk() {
        // Arrange
        when(handler.initiate(any(MultiTransferJob.class)))
                .thenReturn(Stream.of(dataRequest));
        when(processManager.initiateConsumerRequest(dataRequest))
                .thenReturn(okResponse);

        // Act
        var response = sut.startJob(job.getJobData());

        // Assert
        var newJob = getStartedJob();
        assertThat(response)
                .isEqualTo(
                        JobInitiateResponse.builder().jobId(newJob.getJobId()).status(ResponseStatus.OK).build());
    }

    @ParameterizedTest
    @EnumSource(value = ResponseStatus.class, names = "OK", mode = EXCLUDE)
    void startJob_WhenTransferStartUnsuccessful_Abort(ResponseStatus status) {
        // Arrange
        when(handler.initiate(any(MultiTransferJob.class)))
                .thenReturn(Stream.of(dataRequest, dataRequest2));
        when(processManager.initiateConsumerRequest(dataRequest))
                .thenReturn(generate.response(status));

        // Act
        var response = sut.startJob(job.getJobData());

        // Assert
        verify(processManager).initiateConsumerRequest(dataRequest);
        verify(processManager, never()).initiateConsumerRequest(dataRequest2);

        // temporarily created job should be deleted
        verify(jobStore).create(jobCaptor.capture());
        verifyNoMoreInteractions(jobStore);

        assertThat(response)
                .isEqualTo(
                        JobInitiateResponse.builder().jobId(jobCaptor.getValue().getJobId()).status(status).build());
    }


    @Test
    void startJob_WhenHandlerInitiateThrows_StopJob() {
        // Arrange
        when(handler.initiate(any(MultiTransferJob.class)))
                .thenThrow(new RuntimeException());

        // Act
        var response = sut.startJob(job.getJobData());

        // Assert
        verify(jobStore).create(jobCaptor.capture());
        verify(jobStore).markJobInError(jobCaptor.getValue().getJobId(), "Handler method failed");
        verifyNoMoreInteractions(jobStore);
        verifyNoInteractions(processManager);

        assertThat(response)
                .isEqualTo(
                        JobInitiateResponse.builder().jobId(jobCaptor.getValue().getJobId()).status(ResponseStatus.FATAL_ERROR).build());
    }

    @Test
    void transferProcessCompleted_WhenCalledBackForCompletedTransfer_RunsNextTransfers() {
        // Arrange
        when(processManager.initiateConsumerRequest(dataRequest))
                .thenReturn(okResponse);
        when(processManager.initiateConsumerRequest(dataRequest2))
                .thenReturn(okResponse2);

        // Act
        callCompleteAndReturnNextTransfers(Stream.of(dataRequest, dataRequest2));

        // Assert
        verify(processManager).initiateConsumerRequest(dataRequest);
        verify(jobStore).addTransferProcess(job.getJobId(), okResponse.getId());
        verify(jobStore).addTransferProcess(job.getJobId(), okResponse2.getId());
        verify(jobStore).completeTransferProcess(job.getJobId(), transfer);
    }

    @Test
    void transferProcessCompleted_WhenCalledBackForCompletedTransfer_WithoutNextTransfer() {
        // Act
        callCompleteAndReturnNextTransfers(Stream.empty());

        // Assert
        verify(jobStore).completeTransferProcess(job.getJobId(), transfer);
        verify(jobStore).find(job.getJobId());
        verifyNoInteractions(processManager);
        verifyNoMoreInteractions(jobStore);
    }

    @Test
    void transferProcessCompleted_WhenJobNotCompleted_DoesNotCallComplete() {
        // Act
        callCompleteAndReturnNextTransfers(Stream.empty());

        // Assert
        verify(jobStore).completeTransferProcess(job.getJobId(), transfer);
        verify(jobStore).find(job.getJobId());
        verifyNoMoreInteractions(jobStore);
        verifyNoMoreInteractions(handler);
    }

    @Test
    void transferProcessCompleted_WhenJobCompleted_CallsComplete() {
        // Arrange
        doAnswer(i -> byCompletingJob())
                .when(jobStore).completeTransferProcess(job.getJobId(), transfer);

        // Act
        callCompleteAndReturnNextTransfers(Stream.empty());

        // Assert
        verify(handler).complete(job);
        verify(jobStore).completeJob(job.getJobId());
    }


    @Test
    void transferProcessCompleted_WhenHandlerCompleteThrows_StopJob() {
        // Arrange
        doAnswer(i -> byCompletingJob())
                .when(jobStore).completeTransferProcess(job.getJobId(), transfer);
        doAnswer(i -> {
            throw new RuntimeException();
        })
                .when(handler).complete(any());

        // Act
        callCompleteAndReturnNextTransfers(Stream.empty());

        // Assert
        verify(jobStore).markJobInError(job.getJobId(), "Handler method failed");
        verify(jobStore).find(job.getJobId());
        verifyNoMoreInteractions(jobStore);
        verifyNoInteractions(processManager);
    }

    @Test
    void transferProcessCompleted_WhenJobNotFound_Ignore() {
        // Arrange
        when(jobStore.findByProcessId(transfer.getId()))
                .thenReturn(Optional.empty());

        // Act
        callTransferProcessCompletedViaCallback();

        // Assert
        verify(monitor).severe("Job not found for transfer " + transfer.getId());
        verifyNoInteractions(handler);
        verifyNoMoreInteractions(jobStore);
    }

    @ParameterizedTest
    @EnumSource(value = JobState.class, names = "IN_PROGRESS", mode = EXCLUDE)
    void transferProcessCompleted_WhenJobNotInProgress_Ignore(JobState state) {
        // Arrange
        job = job.toBuilder().state(state).build();

        // Act
        when(jobStore.findByProcessId(transfer.getId()))
                .thenReturn(Optional.of(job));
        callTransferProcessCompletedViaCallback();

        // Assert
        verifyNoMoreInteractions(jobStore);
        verifyNoInteractions(handler);
    }

    @ParameterizedTest
    @EnumSource(value = ResponseStatus.class, names = "OK", mode = EXCLUDE)
    void transferProcessCompleted_WhenNextTransferStartUnsuccessful_Abort(ResponseStatus status) {
        // Arrange
        when(processManager.initiateConsumerRequest(dataRequest))
                .thenReturn(generate.response(status));

        // Act
        callCompleteAndReturnNextTransfers(Stream.of(dataRequest, dataRequest2));

        // Assert
        verify(processManager).initiateConsumerRequest(dataRequest);
        verify(processManager, never()).initiateConsumerRequest(dataRequest2);

        // temporarily created job should be deleted
        verify(jobStore).markJobInError(job.getJobId(), "Failed to start a transfer");
        verifyNoMoreInteractions(jobStore);
    }

    @Test
    void transferProcessCompleted_WhenHandlerRecurseThrows_StopJob() {
        // Arrange
        when(jobStore.findByProcessId(transfer.getId()))
                .thenReturn(Optional.of(job));
        when(handler.recurse(job, transfer))
                .thenThrow(new RuntimeException());

        // Act
        callTransferProcessCompletedViaCallback();

        // Assert
        verify(jobStore).markJobInError(job.getJobId(), "Handler method failed");
        verifyNoMoreInteractions(jobStore);
        verifyNoInteractions(processManager);
    }

    private Object byCompletingJob() {
        job = job.toBuilder().transitionTransfersFinished().build();
        lenient().when(jobStore.find(job.getJobId()))
                .thenReturn(Optional.of(job));
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
        when(jobStore.findByProcessId(transfer.getId()))
                .thenReturn(Optional.of(job));
        lenient().when(jobStore.find(job.getJobId()))
                .thenReturn(Optional.of(job));
        when(handler.recurse(job, transfer))
                .thenReturn(dataRequestStream);
        callTransferProcessCompletedViaCallback();
    }

    private void callTransferProcessCompletedViaCallback() {
        verify(transferProcessObservable).registerListener(callbackCaptor.capture());
        callbackCaptor.getValue().completed(transfer);
    }
}
