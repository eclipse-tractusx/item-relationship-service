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
import java.util.UUID;

import org.eclipse.tractusx.irs.component.enums.JobState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class JobProcessingEventListenerTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final JobProcessingEventListener jobProcessingEventListener = new JobProcessingEventListener(restTemplate);

    @BeforeEach
    void mockRestTemplate() {
        when(restTemplate.getForEntity(any(), eq(Void.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    }

    @Test
    void shouldCallCallbackUrlIfIsValidAndStateCompleted() throws URISyntaxException {
        // given
        final String callbackUrlTemplate = "https://hostname.com/callback?jobId={jobId}&jobState={jobState}";
        final String jobId = UUID.randomUUID().toString();
        final JobState jobState = JobState.COMPLETED;
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId, jobState, callbackUrlTemplate);

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        final String expectedCallbackUrl = "https://hostname.com/callback?jobId=" + jobId + "&jobState=" + jobState;

        // then
        verify(this.restTemplate, times(1)).getForEntity(eq(new URI(expectedCallbackUrl)), eq(Void.class));
    }

    @Test
    void shouldCallCallbackUrlIfIsValidAndStateError() throws URISyntaxException {
        // given
        final String callbackUrlTemplate = "http://qwerty.de/{jobId}/{jobState}";
        final String jobId = UUID.randomUUID().toString();
        final JobState jobState = JobState.ERROR;
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId, jobState, callbackUrlTemplate);

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        final String expectedCallbackUrl = "http://qwerty.de/" + jobId + "/" + jobState;

        // then
        verify(this.restTemplate, times(1)).getForEntity(eq(new URI(expectedCallbackUrl)), eq(Void.class));
    }

    @Test
    void shouldCallCallbackUrlIfUrlIsValidAndWithoutPlaceholders() throws URISyntaxException {
        // given
        final String callbackUrlTemplate = "https://hostname.com/";
        final String jobId = UUID.randomUUID().toString();
        final JobState jobState = JobState.COMPLETED;
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId, jobState, callbackUrlTemplate);

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        final String expectedCallbackUrl = "https://hostname.com/";

        // then
        verify(this.restTemplate, times(1)).getForEntity(eq(new URI(expectedCallbackUrl)), eq(Void.class));
    }

    @Test
    void shouldCallCallbackUrlIfUrlIsValidAndWithOnePlaceholder() throws URISyntaxException {
        // given
        final String callbackUrlTemplate = "https://hostname.com/callback?jobId={jobId}";
        final String jobId = UUID.randomUUID().toString();
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId, JobState.COMPLETED, callbackUrlTemplate);

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        final String expectedCallbackUrl = "https://hostname.com/callback?jobId=" + jobId;

        // then
        verify(this.restTemplate, times(1)).getForEntity(eq(new URI(expectedCallbackUrl)), eq(Void.class));
    }

    @Test
    void shouldNotCallCallbackUrlIfIsNotValid() {
        // given
        final String callbackUrlTemplate = "wrongCallbackUrl/jobId={jobId}";
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(UUID.randomUUID().toString(), JobState.COMPLETED, callbackUrlTemplate);

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verifyNoInteractions(this.restTemplate);
    }

    @Test
    void shouldNotCallCallbackUrlIfCallbackUrlIsMissing() {
        // given
        final String emptyCallbackUrlTemplate = "";
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(UUID.randomUUID().toString(), JobState.COMPLETED, emptyCallbackUrlTemplate);

        // when
        jobProcessingEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        verifyNoInteractions(this.restTemplate);
    }
}
