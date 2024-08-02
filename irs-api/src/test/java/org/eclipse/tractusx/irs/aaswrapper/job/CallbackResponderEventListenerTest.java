package org.eclipse.tractusx.irs.aaswrapper.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.tractusx.irs.common.JobProcessingFinishedEvent;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.services.events.BatchOrderProcessingFinishedEvent;
import org.eclipse.tractusx.irs.services.events.BatchProcessingFinishedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class CallbackResponderEventListenerTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final CallbackResponderEventListener callbackResponderEventListener = new CallbackResponderEventListener(
            restTemplate);

    @BeforeEach
    void mockRestTemplate() {
        when(restTemplate.getForEntity(any(), eq(Void.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    }

    @Test
    void shouldCallCallbackUrlIfIsValidAndStateCompletedAndJobProcessingFinishedEvent() throws URISyntaxException {
        // given
        final String callbackUrlTemplate = "https://hostname.com/callback?id={id}&state={state}";
        final String jobId = UUID.randomUUID().toString();
        final JobState jobState = JobState.COMPLETED;
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId,
                jobState.name(), callbackUrlTemplate, Optional.empty());

        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        final String expectedCallbackUrl = "https://hostname.com/callback?id=" + jobId + "&state=" + jobState;

        // then
        verify(this.restTemplate, times(1)).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldCallCallbackUrlIfIsValidAndStateCompletedAndBatchProcessingFinishedEvent() throws URISyntaxException {
        // given
        final String callbackUrlTemplate = "https://hostname.com/callback?orderId={orderId}&batchId={batchId}&orderState={orderState}&batchState={batchState}";
        final UUID orderId = UUID.randomUUID();
        final UUID batchId = UUID.randomUUID();
        final ProcessingState orderState = ProcessingState.COMPLETED;
        final ProcessingState batchState = ProcessingState.COMPLETED;
        final BatchProcessingFinishedEvent batchProcessingFinishedEvent = new BatchProcessingFinishedEvent(orderId,
                batchId, orderState, batchState, 1, callbackUrlTemplate);

        // when
        callbackResponderEventListener.handleBatchProcessingFinishedEvent(batchProcessingFinishedEvent);
        final String expectedCallbackUrl =
                "https://hostname.com/callback?orderId=" + orderId + "&batchId=" + batchId + "&orderState=" + orderState
                        + "&batchState=" + batchState;

        // then
        verify(this.restTemplate, times(1)).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldCallCallbackUrlIfIsValidAndStateCompletedAndBatchOrderProcessingFinishedEvent()
            throws URISyntaxException {
        // given
        final String callbackUrlTemplate = "https://hostname.com/callback?orderId={orderId}&batchId={batchId}&orderState={orderState}&batchState={batchState}";
        final UUID orderId = UUID.randomUUID();
        final ProcessingState orderState = ProcessingState.COMPLETED;
        final BatchOrderProcessingFinishedEvent batchOrderProcessingFinishedEvent = new BatchOrderProcessingFinishedEvent(
                orderId, orderState, callbackUrlTemplate);

        // when
        callbackResponderEventListener.handleBatchOrderProcessingFinishedEvent(batchOrderProcessingFinishedEvent);
        final String expectedCallbackUrl =
                "https://hostname.com/callback?orderId=" + orderId + "&batchId=" + "&orderState=" + orderState
                        + "&batchState=";

        // then
        verify(this.restTemplate, times(1)).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldCallCallbackUrlIfIsValidAndStateError() throws URISyntaxException {
        // given
        final String callbackUrlTemplate = "http://qwerty.de/{id}/{state}";
        final String jobId = UUID.randomUUID().toString();
        final JobState jobState = JobState.ERROR;
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId,
                jobState.name(), callbackUrlTemplate, Optional.empty());

        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        final String expectedCallbackUrl = "http://qwerty.de/" + jobId + "/" + jobState;

        // then
        verify(this.restTemplate, times(1)).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldCallCallbackUrlIfUrlIsValidAndWithoutPlaceholders() throws URISyntaxException {
        // given
        final String callbackUrlTemplate = "https://hostname.com/";
        final String jobId = UUID.randomUUID().toString();
        final JobState jobState = JobState.COMPLETED;
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId,
                jobState.name(), callbackUrlTemplate, Optional.empty());

        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        final String expectedCallbackUrl = "https://hostname.com/";

        // then
        verify(this.restTemplate, times(1)).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldCallCallbackUrlIfUrlIsValidAndWithOnePlaceholder() throws URISyntaxException {
        // given
        final String callbackUrlTemplate = "https://hostname.com/callback?id={id}";
        final String jobId = UUID.randomUUID().toString();
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId,
                JobState.COMPLETED.name(), callbackUrlTemplate, Optional.empty());

        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        final String expectedCallbackUrl = "https://hostname.com/callback?id=" + jobId;

        // then
        verify(this.restTemplate, times(1)).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldNotCallCallbackUrlIfIsNotValidAndJobProcessingFinishedEvent() {
        // given
        final String callbackUrlTemplate = "wrongCallbackUrl/id={id}";
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(
                UUID.randomUUID().toString(), JobState.COMPLETED.name(), callbackUrlTemplate, Optional.empty());

        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verifyNoInteractions(this.restTemplate);
    }

    @Test
    void shouldNotCallCallbackUrlIfIsNotValidAndBatchProcessingFinishedEvent() {
        // given
        final String callbackUrlTemplate = "wrongCallbackUrl/id={id}";
        final BatchProcessingFinishedEvent batchProcessingFinishedEvent = new BatchProcessingFinishedEvent(
                UUID.randomUUID(), UUID.randomUUID(), ProcessingState.COMPLETED, ProcessingState.COMPLETED, 1,
                callbackUrlTemplate);

        // when
        callbackResponderEventListener.handleBatchProcessingFinishedEvent(batchProcessingFinishedEvent);

        // then
        verifyNoInteractions(this.restTemplate);
    }

    @Test
    void shouldNotCallCallbackUrlIfIsNotValidAndBatchOrderProcessingFinishedEvent() {
        // given
        final String callbackUrlTemplate = "wrongCallbackUrl/id={id}";
        final BatchOrderProcessingFinishedEvent batchOrderProcessingFinishedEvent = new BatchOrderProcessingFinishedEvent(
                UUID.randomUUID(), ProcessingState.COMPLETED, callbackUrlTemplate);

        // when
        callbackResponderEventListener.handleBatchOrderProcessingFinishedEvent(batchOrderProcessingFinishedEvent);

        // then
        verifyNoInteractions(this.restTemplate);
    }

    @Test
    void shouldCallCallbackUrlIfIsInternalAndStateCompletedAndJobProcessingFinishedEvent() throws URISyntaxException {
        // given
        final String callbackUrlTemplate = "https://internal:1234/callback?id={id}&state={state}";
        final String jobId = UUID.randomUUID().toString();
        final JobState jobState = JobState.COMPLETED;
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId,
                jobState.name(), callbackUrlTemplate, Optional.empty());
        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        final String expectedCallbackUrl = "https://internal:1234/callback?id=" + jobId + "&state=" + jobState;
        verify(this.restTemplate, times(1)).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldNotCallCallbackUrlIfTldIsNotValidAndStateCompletedAndJobProcessingFinishedEvent() {
        // given
        final String callbackUrlTemplateWithInvalidTld = "https://domain.unknown/callback?id={id}&state={state}";
        final String jobId = UUID.randomUUID().toString();
        final JobState jobState = JobState.COMPLETED;
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId,
                jobState.name(), callbackUrlTemplateWithInvalidTld, Optional.empty());
        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verifyNoInteractions(this.restTemplate);
    }

    @Test
    void shouldNotCallCallbackUrlIfCallbackUrlIsMissing() {
        // given
        final String emptyCallbackUrlTemplate = "";
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(
                UUID.randomUUID().toString(), JobState.COMPLETED.name(), emptyCallbackUrlTemplate, Optional.empty());

        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verifyNoInteractions(this.restTemplate);
    }

    @Test
    void shouldNotSendCallbackIfAlreadyPublished() throws URISyntaxException {
        final String callbackUrlTemplate = "https://hostname.com/callback?id={id}";
        final String jobId = UUID.randomUUID().toString();
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId,
                JobState.COMPLETED.toString(), callbackUrlTemplate, Optional.empty());

        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        final String expectedCallbackUrl = "https://hostname.com/callback?id=" + jobId;

        // then
        verify(this.restTemplate, times(1)).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldNotSendBatchCallbackIfAlreadyPublished() throws URISyntaxException {
        final String callbackUrlTemplate = "https://hostname.com/callback?orderId={orderId}&batchId={batchId}&orderState={orderState}&batchState={batchState}";
        final UUID orderId = UUID.randomUUID();
        final UUID batchId = UUID.randomUUID();
        final ProcessingState orderState = ProcessingState.COMPLETED;
        final ProcessingState batchState = ProcessingState.COMPLETED;
        final BatchProcessingFinishedEvent batchProcessingFinishedEvent = new BatchProcessingFinishedEvent(orderId,
                batchId, orderState, batchState, 1, callbackUrlTemplate);

        // when
        callbackResponderEventListener.handleBatchProcessingFinishedEvent(batchProcessingFinishedEvent);
        callbackResponderEventListener.handleBatchProcessingFinishedEvent(batchProcessingFinishedEvent);
        final String expectedCallbackUrl =
                "https://hostname.com/callback?orderId=" + orderId + "&batchId=" + batchId + "&orderState=" + orderState
                        + "&batchState=" + batchState;

        // then
        verify(this.restTemplate, times(1)).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldNotSendBatchOrderCallbackIfAlreadyPublished()
            throws URISyntaxException {
        // given
        final String callbackUrlTemplate = "https://hostname.com/callback?orderId={orderId}&batchId={batchId}&orderState={orderState}&batchState={batchState}";
        final UUID orderId = UUID.randomUUID();
        final ProcessingState orderState = ProcessingState.COMPLETED;
        final BatchOrderProcessingFinishedEvent batchOrderProcessingFinishedEvent = new BatchOrderProcessingFinishedEvent(
                orderId, orderState, callbackUrlTemplate);

        // when
        callbackResponderEventListener.handleBatchOrderProcessingFinishedEvent(batchOrderProcessingFinishedEvent);
        callbackResponderEventListener.handleBatchOrderProcessingFinishedEvent(batchOrderProcessingFinishedEvent);
        final String expectedCallbackUrl =
                "https://hostname.com/callback?orderId=" + orderId + "&batchId=" + "&orderState=" + orderState
                        + "&batchState=";

        // then
        verify(this.restTemplate, times(1)).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }
}
