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

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorCompletionService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.JobProgress;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.BatchOrder;
import org.eclipse.tractusx.irs.connector.batch.BatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.eclipse.tractusx.irs.connector.batch.InMemoryBatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.InMemoryBatchStore;
import org.eclipse.tractusx.irs.ess.service.EssService;
import org.eclipse.tractusx.irs.services.events.BatchOrderProcessingFinishedEvent;
import org.eclipse.tractusx.irs.services.events.BatchOrderRegisteredEvent;
import org.eclipse.tractusx.irs.services.events.BatchProcessingFinishedEvent;
import org.eclipse.tractusx.irs.services.timeouts.TimeoutSchedulerBatchProcessingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

class BatchOrderEventListenerTest {

    private final UUID BATCH_ORDER_ID = UUID.randomUUID();
    private final UUID FIRST_BATCH_ID = UUID.randomUUID();
    private final UUID SECOND_BATCH_ID = UUID.randomUUID();
    private BatchOrderStore batchOrderStore;
    private BatchStore batchStore;
    private final IrsItemGraphQueryService irsItemGraphQueryService = mock(IrsItemGraphQueryService.class);
    private final EssService essService = mock(EssService.class);
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    private final TimeoutSchedulerBatchProcessingService timeoutScheduler = mock(
            TimeoutSchedulerBatchProcessingService.class);
    private final ExecutorCompletionServiceFactory executorCompletionServiceFactory = mock(
            ExecutorCompletionServiceFactory.class);

    private BatchOrderEventListener eventListener;

    @BeforeEach
    void beforeEach() {
        batchOrderStore = new InMemoryBatchOrderStore();
        batchStore = new InMemoryBatchStore();
        eventListener = new BatchOrderEventListener(batchOrderStore, batchStore, irsItemGraphQueryService, essService,
                applicationEventPublisher, timeoutScheduler, executorCompletionServiceFactory);
    }

    @Test
    void shouldStartFirstBatch_regularJob() throws InterruptedException {
        // given
        final int numberOfJobs = 10;
        final int timeout = 60;

        final BatchOrder batchOrder = BatchOrder.builder()
                                                .batchOrderId(BATCH_ORDER_ID)
                                                .batchOrderState(ProcessingState.INITIALIZED)
                                                .collectAspects(Boolean.TRUE)
                                                .timeout(timeout)
                                                .jobTimeout(timeout)
                                                .lookupBPNs(Boolean.TRUE)
                                                .auditContractNegotiation(Boolean.TRUE)
                                                .jobType(BatchOrder.JobType.REGULAR)
                                                .batchIds(List.of(FIRST_BATCH_ID, SECOND_BATCH_ID))
                                                .build();
        final Batch firstBatch = Batch.builder()
                                      .batchId(FIRST_BATCH_ID)
                                      .batchState(ProcessingState.PARTIAL)
                                      .batchNumber(1)
                                      .batchOrderId(BATCH_ORDER_ID)
                                      .jobProgressList(createJobProgressList())
                                      .build();
        final Batch secondBatch = Batch.builder()
                                       .batchId(SECOND_BATCH_ID)
                                       .batchState(ProcessingState.PARTIAL)
                                       .batchNumber(2)
                                       .batchOrderId(BATCH_ORDER_ID)
                                       .build();

        given(irsItemGraphQueryService.registerItemJob(any(), any())).willReturn(
                JobHandle.builder().id(UUID.randomUUID()).build());

        ExecutorCompletionService executorCompletionService = mock(ExecutorCompletionService.class);
        given(executorCompletionService.submit(any())).willReturn(
                CompletableFuture.completedFuture(JobProgress.builder().build()));

        when(executorCompletionServiceFactory.create()).thenReturn(executorCompletionService);

        batchOrderStore.save(BATCH_ORDER_ID, batchOrder);
        batchStore.save(FIRST_BATCH_ID, firstBatch);
        batchStore.save(SECOND_BATCH_ID, secondBatch);
        // when
        eventListener.handleBatchOrderRegisteredEvent(new BatchOrderRegisteredEvent(BATCH_ORDER_ID));
        // then
        ArgumentCaptor<Callable<JobProgress>> callableCaptor = ArgumentCaptor.forClass(Callable.class);
        verify(executorCompletionService, times(numberOfJobs)).submit(callableCaptor.capture());

        callableCaptor.getAllValues().forEach(callable -> {
            try {
                callable.call();
            } catch (Exception e) {
                Assertions.fail("Callable execution failed: " + e.getMessage());
            }
        });

        verify(irsItemGraphQueryService, times(numberOfJobs)).registerItemJob(any(), eq(FIRST_BATCH_ID));
        verify(timeoutScheduler).registerBatchTimeout(FIRST_BATCH_ID, timeout);
        verify(timeoutScheduler).registerJobsTimeout(anyList(), eq(timeout));
    }

    @Test
    void shouldStartFirstBatch_essJob() throws InterruptedException {
        // given
        final int numberOfJobs = 10;
        final int timeout = 60;

        final BatchOrder batchOrder = BatchOrder.builder()
                                                .batchOrderId(BATCH_ORDER_ID)
                                                .batchOrderState(ProcessingState.INITIALIZED)
                                                .collectAspects(Boolean.TRUE)
                                                .timeout(timeout)
                                                .jobTimeout(timeout)
                                                .lookupBPNs(Boolean.TRUE)
                                                .jobType(BatchOrder.JobType.ESS)
                                                .batchIds(List.of(FIRST_BATCH_ID, SECOND_BATCH_ID))
                                                .build();
        final Batch firstBatch = Batch.builder()
                                      .batchId(FIRST_BATCH_ID)
                                      .batchState(ProcessingState.PARTIAL)
                                      .batchNumber(1)
                                      .batchOrderId(BATCH_ORDER_ID)
                                      .jobProgressList(createJobProgressList())
                                      .build();
        final Batch secondBatch = Batch.builder()
                                       .batchId(SECOND_BATCH_ID)
                                       .batchState(ProcessingState.PARTIAL)
                                       .batchNumber(2)
                                       .batchOrderId(BATCH_ORDER_ID)
                                       .build();

        given(essService.startIrsJob(any(), any())).willReturn(JobHandle.builder().id(UUID.randomUUID()).build());

        ExecutorCompletionService executorCompletionService = mock(ExecutorCompletionService.class);
        given(executorCompletionService.submit(any())).willReturn(
                CompletableFuture.completedFuture(JobProgress.builder().build()));

        when(executorCompletionServiceFactory.create()).thenReturn(executorCompletionService);

        batchOrderStore.save(BATCH_ORDER_ID, batchOrder);
        batchStore.save(FIRST_BATCH_ID, firstBatch);
        batchStore.save(SECOND_BATCH_ID, secondBatch);
        // when
        eventListener.handleBatchOrderRegisteredEvent(new BatchOrderRegisteredEvent(BATCH_ORDER_ID));
        // then
        ArgumentCaptor<Callable<JobProgress>> callableCaptor = ArgumentCaptor.forClass(Callable.class);
        verify(executorCompletionService, times(numberOfJobs)).submit(callableCaptor.capture());

        callableCaptor.getAllValues().forEach(callable -> {
            try {
                callable.call();
            } catch (Exception e) {
                Assertions.fail("Callable execution failed: " + e.getMessage());
            }
        });

        verify(essService, times(numberOfJobs)).startIrsJob(any(), eq(FIRST_BATCH_ID));
        verify(timeoutScheduler).registerBatchTimeout(FIRST_BATCH_ID, timeout);
        verify(timeoutScheduler).registerJobsTimeout(anyList(), eq(timeout));
    }

    @Test
    void shouldStartNextBatchWhenPreviousFinished() throws InterruptedException {
        // given
        final int numberOfJobs = 10;
        final int timeout = 60;
        final BatchOrder batchOrder = BatchOrder.builder()
                                                .batchOrderId(BATCH_ORDER_ID)
                                                .batchOrderState(ProcessingState.INITIALIZED)
                                                .collectAspects(Boolean.TRUE)
                                                .timeout(timeout)
                                                .jobTimeout(timeout)
                                                .lookupBPNs(Boolean.TRUE)
                                                .jobType(BatchOrder.JobType.ESS)
                                                .batchIds(List.of(FIRST_BATCH_ID, SECOND_BATCH_ID))
                                                .build();
        final Batch firstBatch = Batch.builder()
                                      .batchId(FIRST_BATCH_ID)
                                      .batchState(ProcessingState.PARTIAL)
                                      .batchNumber(1)
                                      .batchOrderId(BATCH_ORDER_ID)
                                      .build();
        final Batch secondBatch = Batch.builder()
                                       .batchId(SECOND_BATCH_ID)
                                       .batchState(ProcessingState.INITIALIZED)
                                       .batchNumber(2)
                                       .batchOrderId(BATCH_ORDER_ID)
                                       .jobProgressList(createJobProgressList())
                                       .build();

        given(essService.startIrsJob(any(), any())).willReturn(JobHandle.builder().id(UUID.randomUUID()).build());

        ExecutorCompletionService executorCompletionService = mock(ExecutorCompletionService.class);
        given(executorCompletionService.submit(any())).willReturn(
                CompletableFuture.completedFuture(JobProgress.builder().build()));

        when(executorCompletionServiceFactory.create()).thenReturn(executorCompletionService);

        batchOrderStore.save(BATCH_ORDER_ID, batchOrder);
        batchStore.save(FIRST_BATCH_ID, firstBatch);
        batchStore.save(SECOND_BATCH_ID, secondBatch);
        // when
        eventListener.handleBatchProcessingFinishedEvent(
                new BatchProcessingFinishedEvent(BATCH_ORDER_ID, FIRST_BATCH_ID, ProcessingState.PARTIAL,
                        ProcessingState.COMPLETED, 1, ""));
        // then
        ArgumentCaptor<Callable<JobProgress>> callableCaptor = ArgumentCaptor.forClass(Callable.class);
        verify(executorCompletionService, times(numberOfJobs)).submit(callableCaptor.capture());

        callableCaptor.getAllValues().forEach(callable -> {
            try {
                callable.call();
            } catch (Exception e) {
                Assertions.fail("Callable execution failed: " + e.getMessage());
            }
        });

        verify(essService, times(numberOfJobs)).startIrsJob(any(), eq(SECOND_BATCH_ID));
        verify(timeoutScheduler).registerBatchTimeout(SECOND_BATCH_ID, timeout);
        verify(timeoutScheduler).registerJobsTimeout(anyList(), eq(timeout));
    }

    @Test
    void shouldPublishBatchOrderProcessingFinishedEventWhenAllBatchesCompleted() {
        // given
        final int timeout = 60;
        final BatchOrder batchOrder = BatchOrder.builder()
                                                .batchOrderId(BATCH_ORDER_ID)
                                                .batchOrderState(ProcessingState.PARTIAL)
                                                .collectAspects(Boolean.TRUE)
                                                .timeout(timeout)
                                                .jobTimeout(timeout)
                                                .lookupBPNs(Boolean.TRUE)
                                                .jobType(BatchOrder.JobType.REGULAR)
                                                .batchIds(List.of(FIRST_BATCH_ID, SECOND_BATCH_ID))
                                                .build();
        final Batch firstBatch = Batch.builder()
                                      .batchId(FIRST_BATCH_ID)
                                      .batchState(ProcessingState.PARTIAL)
                                      .batchNumber(1)
                                      .batchState(ProcessingState.COMPLETED)
                                      .batchOrderId(BATCH_ORDER_ID)
                                      .build();
        final Batch secondBatch = Batch.builder()
                                       .batchId(SECOND_BATCH_ID)
                                       .batchState(ProcessingState.INITIALIZED)
                                       .batchNumber(2)
                                       .batchState(ProcessingState.COMPLETED)
                                       .batchOrderId(BATCH_ORDER_ID)
                                       .jobProgressList(createJobProgressList())
                                       .build();

        batchOrderStore.save(BATCH_ORDER_ID, batchOrder);
        batchStore.save(FIRST_BATCH_ID, firstBatch);
        batchStore.save(SECOND_BATCH_ID, secondBatch);
        // when
        eventListener.handleBatchProcessingFinishedEvent(
                new BatchProcessingFinishedEvent(BATCH_ORDER_ID, SECOND_BATCH_ID, ProcessingState.PARTIAL,
                        ProcessingState.COMPLETED, 2, ""));
        // then
        verify(applicationEventPublisher).publishEvent(any(BatchOrderProcessingFinishedEvent.class));
    }

    @Test
    void shouldNotStartBatchWhenJobTypeIsUnknown() throws InterruptedException {
        // given
        final int numberOfJobs = 10;
        final int timeout = 60;

        final BatchOrder batchOrder = BatchOrder.builder()
                                                .batchOrderId(BATCH_ORDER_ID)
                                                .batchOrderState(ProcessingState.INITIALIZED)
                                                .collectAspects(Boolean.TRUE)
                                                .timeout(timeout)
                                                .jobTimeout(timeout)
                                                .lookupBPNs(Boolean.TRUE)
                                                .jobType(null)
                                                .batchIds(List.of(FIRST_BATCH_ID))
                                                .build();
        final Batch firstBatch = Batch.builder()
                                      .batchId(FIRST_BATCH_ID)
                                      .batchState(ProcessingState.PARTIAL)
                                      .batchNumber(1)
                                      .batchOrderId(BATCH_ORDER_ID)
                                      .jobProgressList(createJobProgressList())
                                      .build();

        given(irsItemGraphQueryService.registerItemJob(any(), any())).willReturn(
                JobHandle.builder().id(UUID.randomUUID()).build());

        ExecutorCompletionService executorCompletionService = mock(ExecutorCompletionService.class);
        given(executorCompletionService.submit(any())).willReturn(CompletableFuture.completedFuture(JobProgress.builder().build()));

        when(executorCompletionServiceFactory.create()).thenReturn(executorCompletionService);

        batchOrderStore.save(BATCH_ORDER_ID, batchOrder);
        batchStore.save(FIRST_BATCH_ID, firstBatch);
        // when
        eventListener.handleBatchOrderRegisteredEvent(new BatchOrderRegisteredEvent(BATCH_ORDER_ID));
        // then
        ArgumentCaptor<Callable<JobProgress>> callableCaptor = ArgumentCaptor.forClass(Callable.class);
        verify(executorCompletionService, times(numberOfJobs)).submit(callableCaptor.capture());

        callableCaptor.getAllValues().forEach(callable -> {
            IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                    callable::call);
            Assertions.assertEquals(illegalArgumentException.getMessage(), "Unsupported job type: null");
        });
    }

    private List<JobProgress> createJobProgressList() {
        return IntStream.range(0, 10)
                        .boxed()
                        .map(i -> JobProgress.builder()
                                             .identificationKey(PartChainIdentificationKey.builder()
                                                                                          .globalAssetId(i.toString())
                                                                                          .bpn("BPN" + i)
                                                                                          .build())
                                             .build())
                        .collect(Collectors.toList());
    }

}