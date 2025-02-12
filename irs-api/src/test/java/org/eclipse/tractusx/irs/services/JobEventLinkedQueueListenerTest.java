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
package org.eclipse.tractusx.irs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.tractusx.irs.common.JobProcessingFinishedEvent;
import org.eclipse.tractusx.irs.component.JobProgress;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.BatchOrder;
import org.eclipse.tractusx.irs.connector.batch.BatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.eclipse.tractusx.irs.connector.batch.InMemoryBatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.InMemoryBatchStore;
import org.eclipse.tractusx.irs.services.events.BatchProcessingFinishedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class JobEventLinkedQueueListenerTest {

    private final static UUID BATCH_ID = UUID.randomUUID();
    private final static UUID BATCH_ORDER_ID = UUID.randomUUID();
    private BatchOrderStore batchOrderStore;
    private BatchStore batchStore;
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private JobEventLinkedQueueListener eventListener;
    private final ArgumentCaptor<BatchProcessingFinishedEvent> eventCaptor = forClass(BatchProcessingFinishedEvent.class);

    @BeforeEach
    void beforeEach() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        batchOrderStore = new InMemoryBatchOrderStore();
        batchStore = new InMemoryBatchStore();
        eventListener = new JobEventLinkedQueueListener(batchOrderStore, batchStore, eventPublisher);
    }

    @Test
    void shouldCompletedBatchWhenBothJobCompleted() {
        // given
        final UUID firstJob = UUID.randomUUID();
        final UUID secondJob = UUID.randomUUID();

        eventListener.addQueueForBatch(BATCH_ID, 2);
        batchStore.save(BATCH_ID, Batch.builder()
                                       .batchId(BATCH_ID)
                                       .batchOrderId(BATCH_ORDER_ID)
                                       .jobProgressList(List.of(
                JobProgress.builder().jobId(firstJob).jobState(JobState.INITIAL).build(),
                JobProgress.builder().jobId(secondJob).jobState(JobState.INITIAL).build()
        )).build());

        batchOrderStore.save(BATCH_ORDER_ID, BatchOrder.builder().batchOrderId(BATCH_ORDER_ID).batchOrderState(
                ProcessingState.PROCESSING).build());

        // when
        eventListener.handleJobProcessingFinishedEvent(
                new JobProcessingFinishedEvent(firstJob.toString(), JobState.COMPLETED.name(), "", Optional.of(BATCH_ID)));
        eventListener.handleJobProcessingFinishedEvent(
                new JobProcessingFinishedEvent(secondJob.toString(), JobState.COMPLETED.name(), "", Optional.of(BATCH_ID)));

        // then
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().batchId()).isEqualTo(BATCH_ID);
        assertThat(eventCaptor.getValue().batchState()).isEqualTo(ProcessingState.COMPLETED);
    }

    @Test
    void shouldPublishFinishedEventOnStatePartial() {
        // given
        final UUID firstJob = UUID.randomUUID();
        final UUID secondJob = UUID.randomUUID();

        eventListener.addQueueForBatch(BATCH_ID, 2);
        batchStore.save(BATCH_ID, Batch.builder()
                                       .batchId(BATCH_ID)
                                       .batchOrderId(BATCH_ORDER_ID)
                                       .jobProgressList(List.of(
                                               JobProgress.builder().jobId(firstJob).jobState(JobState.INITIAL).build(),
                                               JobProgress.builder().jobId(secondJob).jobState(JobState.INITIAL).build()
                                       )).build());

        batchOrderStore.save(BATCH_ORDER_ID,
                BatchOrder.builder().batchOrderId(BATCH_ORDER_ID).batchOrderState(ProcessingState.PROCESSING).build());

        // when
        eventListener.handleJobProcessingFinishedEvent(
                new JobProcessingFinishedEvent(firstJob.toString(), JobState.COMPLETED.name(), "",
                        Optional.of(BATCH_ID)));
        eventListener.handleJobProcessingFinishedEvent(
                new JobProcessingFinishedEvent(secondJob.toString(), JobState.CANCELED.name(), "",
                        Optional.of(BATCH_ID)));

        // then
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().batchId()).isEqualTo(BATCH_ID);
        assertThat(eventCaptor.getValue().batchState()).isEqualTo(ProcessingState.PARTIAL);
    }

    @ParameterizedTest
    @MethodSource("jobStates")
    void shouldCalculateCorrectBatchState(ProcessingState expected, List<JobState> states) {
        final ProcessingState processingState = eventListener.calculateProcessingState(states);
        assertThat(processingState).isEqualTo(expected);
    }

    private static Stream<Arguments> jobStates() {
        return Stream.of(
                Arguments.of(ProcessingState.COMPLETED, List.of(JobState.COMPLETED)),

                Arguments.of(ProcessingState.PARTIAL, List.of(JobState.CANCELED)),
                Arguments.of(ProcessingState.PARTIAL, List.of(JobState.ERROR)),
                Arguments.of(ProcessingState.PARTIAL, List.of(JobState.COMPLETED, JobState.CANCELED)),
                Arguments.of(ProcessingState.PARTIAL, List.of(JobState.COMPLETED, JobState.ERROR)),
                Arguments.of(ProcessingState.PARTIAL, List.of(JobState.CANCELED, JobState.ERROR)),
                Arguments.of(ProcessingState.PARTIAL, List.of(JobState.COMPLETED, JobState.CANCELED, JobState.ERROR)),

                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.UNSAVED)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.INITIAL)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.RUNNING)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.TRANSFERS_FINISHED)),

                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.UNSAVED, JobState.INITIAL)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.UNSAVED, JobState.RUNNING)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.UNSAVED, JobState.TRANSFERS_FINISHED)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.UNSAVED, JobState.COMPLETED)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.UNSAVED, JobState.CANCELED)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.UNSAVED, JobState.ERROR)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.INITIAL, JobState.RUNNING)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.INITIAL, JobState.TRANSFERS_FINISHED)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.INITIAL, JobState.COMPLETED)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.INITIAL, JobState.CANCELED)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.INITIAL, JobState.ERROR)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.RUNNING, JobState.TRANSFERS_FINISHED)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.RUNNING, JobState.COMPLETED)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.RUNNING, JobState.CANCELED)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.RUNNING, JobState.ERROR)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.TRANSFERS_FINISHED, JobState.COMPLETED)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.TRANSFERS_FINISHED, JobState.CANCELED)),
                Arguments.of(ProcessingState.PROCESSING, List.of(JobState.TRANSFERS_FINISHED, JobState.ERROR))
        );
    }
}