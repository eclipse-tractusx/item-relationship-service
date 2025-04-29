/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.aaswrapper.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

    private static final String CALLBACK_BASE_URL = "https://hostname.com";

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
        final String jobId = UUID.randomUUID().toString();
        final JobState jobState = JobState.COMPLETED;
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId,
                jobState.name(), CALLBACK_BASE_URL, Optional.empty());

        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        final String expectedCallbackUrl = "https://hostname.com?id=" + jobId + "&state=" + jobState;

        // then
        verify(this.restTemplate).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldCallCallbackUrlIfIsValidAndStateCompletedAndBatchProcessingFinishedEvent() throws URISyntaxException {
        // given
        final UUID orderId = UUID.randomUUID();
        final UUID batchId = UUID.randomUUID();
        final ProcessingState orderState = ProcessingState.COMPLETED;
        final ProcessingState batchState = ProcessingState.COMPLETED;
        final BatchProcessingFinishedEvent batchProcessingFinishedEvent = new BatchProcessingFinishedEvent(orderId,
                batchId, orderState, batchState, 1, CALLBACK_BASE_URL);

        // when
        callbackResponderEventListener.handleBatchProcessingFinishedEvent(batchProcessingFinishedEvent);
        final String expectedCallbackUrl =
                "https://hostname.com?orderId=" + orderId + "&batchId=" + batchId + "&orderState=" + orderState
                        + "&batchState=" + batchState;

        // then
        verify(this.restTemplate).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldCallCallbackUrlIfIsValidAndStateCompletedAndBatchOrderProcessingFinishedEvent()
            throws URISyntaxException {
        // given
        final UUID orderId = UUID.randomUUID();
        final ProcessingState orderState = ProcessingState.COMPLETED;
        final BatchOrderProcessingFinishedEvent batchOrderProcessingFinishedEvent = new BatchOrderProcessingFinishedEvent(
                orderId, orderState, CALLBACK_BASE_URL);

        // when
        callbackResponderEventListener.handleBatchOrderProcessingFinishedEvent(batchOrderProcessingFinishedEvent);
        final String expectedCallbackUrl =
                "https://hostname.com?orderId=" + orderId + "&batchId=" + "&orderState=" + orderState
                        + "&batchState=";

        // then
        verify(this.restTemplate).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldCallCallbackUrlIfIsValidAndStateError() throws URISyntaxException {
        // given
        final String jobId = UUID.randomUUID().toString();
        final JobState jobState = JobState.ERROR;
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId,
                jobState.name(), CALLBACK_BASE_URL, Optional.empty());

        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        final String expectedCallbackUrl = "https://hostname.com?id=" + jobId + "&state=" + jobState;

        // then
        verify(this.restTemplate).getForEntity(new URI(expectedCallbackUrl), Void.class);
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
        final String callbackUrlTemplate = "https://internal:1234";
        final String jobId = UUID.randomUUID().toString();
        final JobState jobState = JobState.COMPLETED;
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId,
                jobState.name(), callbackUrlTemplate, Optional.empty());
        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);

        // then
        final String expectedCallbackUrl = "https://internal:1234?id=" + jobId + "&state=" + jobState;
        verify(this.restTemplate).getForEntity(new URI(expectedCallbackUrl), Void.class);
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
        final String jobId = UUID.randomUUID().toString();
        final JobProcessingFinishedEvent jobProcessingFinishedEvent = new JobProcessingFinishedEvent(jobId,
                JobState.COMPLETED.toString(), CALLBACK_BASE_URL, Optional.empty());

        // when
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        callbackResponderEventListener.handleJobProcessingFinishedEvent(jobProcessingFinishedEvent);
        final String expectedCallbackUrl = "https://hostname.com?id=" + jobId + "&state=COMPLETED";

        // then
        verify(this.restTemplate).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldNotSendBatchCallbackIfAlreadyPublished() throws URISyntaxException {
        final UUID orderId = UUID.randomUUID();
        final UUID batchId = UUID.randomUUID();
        final ProcessingState orderState = ProcessingState.COMPLETED;
        final ProcessingState batchState = ProcessingState.COMPLETED;
        final BatchProcessingFinishedEvent batchProcessingFinishedEvent = new BatchProcessingFinishedEvent(orderId,
                batchId, orderState, batchState, 1, CALLBACK_BASE_URL);

        // when
        callbackResponderEventListener.handleBatchProcessingFinishedEvent(batchProcessingFinishedEvent);
        callbackResponderEventListener.handleBatchProcessingFinishedEvent(batchProcessingFinishedEvent);
        final String expectedCallbackUrl =
                "https://hostname.com?orderId=" + orderId + "&batchId=" + batchId + "&orderState=" + orderState
                        + "&batchState=" + batchState;

        // then
        verify(this.restTemplate).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }

    @Test
    void shouldNotSendBatchOrderCallbackIfAlreadyPublished() throws URISyntaxException {
        // given
        final UUID orderId = UUID.randomUUID();
        final ProcessingState orderState = ProcessingState.COMPLETED;
        final BatchOrderProcessingFinishedEvent batchOrderProcessingFinishedEvent = new BatchOrderProcessingFinishedEvent(
                orderId, orderState, CALLBACK_BASE_URL);

        // when
        callbackResponderEventListener.handleBatchOrderProcessingFinishedEvent(batchOrderProcessingFinishedEvent);
        callbackResponderEventListener.handleBatchOrderProcessingFinishedEvent(batchOrderProcessingFinishedEvent);
        final String expectedCallbackUrl =
                "https://hostname.com?orderId=" + orderId + "&batchId=" + "&orderState=" + orderState
                        + "&batchState=";

        // then
        verify(this.restTemplate).getForEntity(new URI(expectedCallbackUrl), Void.class);
    }
}
