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
package org.eclipse.tractusx.irs.services.timeouts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.eclipse.tractusx.irs.connector.batch.InMemoryBatchStore;
import org.eclipse.tractusx.irs.connector.batch.JobProgress;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CancelBatchProcessingServiceTest {

    private final IrsItemGraphQueryService irsItemGraphQueryService = mock(IrsItemGraphQueryService.class);
    private final BatchStore batchStore = new InMemoryBatchStore();
    private final CancelBatchProcessingService cancelBatchProcessingService = new CancelBatchProcessingService(irsItemGraphQueryService, batchStore);

    @Test
    void shouldCancelOnlyNotCompletedJob() {
        // given
        UUID firstJobId = UUID.randomUUID();
        UUID secondJobId = UUID.randomUUID();
        UUID runningJobId = UUID.randomUUID();

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);

        given(irsItemGraphQueryService.getJobForJobId(firstJobId, false)).willReturn(
                jobInState(JobState.COMPLETED)
        );
        given(irsItemGraphQueryService.getJobForJobId(secondJobId, false)).willReturn(
                jobInState(JobState.ERROR)
        );
        given(irsItemGraphQueryService.getJobForJobId(runningJobId, false)).willReturn(
                jobInState(JobState.RUNNING)
        );

        // when
        cancelBatchProcessingService.cancelNotFinishedJobs(List.of(firstJobId, secondJobId, runningJobId));

        // then
        verify(irsItemGraphQueryService).cancelJobById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(runningJobId);
    }

    @Test
    void shouldCancelOnlyNotCompletedJobInBatch() {
        // given
        UUID firstJobId = UUID.randomUUID();
        UUID secondJobId = UUID.randomUUID();
        UUID runningJobId = UUID.randomUUID();
        UUID batchId = UUID.randomUUID();

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);

        given(irsItemGraphQueryService.getJobForJobId(firstJobId, false)).willReturn(
                jobInState(JobState.COMPLETED)
        );
        given(irsItemGraphQueryService.getJobForJobId(secondJobId, false)).willReturn(
                jobInState(JobState.ERROR)
        );
        given(irsItemGraphQueryService.getJobForJobId(runningJobId, false)).willReturn(
                jobInState(JobState.RUNNING)
        );

        batchStore.save(batchId, createBatch(batchId, UUID.randomUUID(), ProcessingState.PROCESSING,
                List.of(firstJobId, secondJobId, runningJobId)));

        // when
        cancelBatchProcessingService.cancelNotFinishedJobsInBatch(batchId);

        // then
        verify(irsItemGraphQueryService).cancelJobById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(runningJobId);
    }

    @Test
    void shouldCancelOnlyNotCompletedJobInBatchOrder() {
        // given
        UUID firstJobId = UUID.randomUUID();
        UUID secondJobId = UUID.randomUUID();
        UUID runningJobId = UUID.randomUUID();
        UUID batchId = UUID.randomUUID();
        UUID batchOrderId = UUID.randomUUID();

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);

        given(irsItemGraphQueryService.getJobForJobId(firstJobId, false)).willReturn(
                jobInState(JobState.COMPLETED)
        );
        given(irsItemGraphQueryService.getJobForJobId(secondJobId, false)).willReturn(
                jobInState(JobState.ERROR)
        );
        given(irsItemGraphQueryService.getJobForJobId(runningJobId, false)).willReturn(
                jobInState(JobState.RUNNING)
        );

        batchStore.save(batchId, createBatch(batchId, batchOrderId, ProcessingState.PROCESSING,
                List.of(firstJobId, secondJobId, runningJobId)));

        // when
        cancelBatchProcessingService.cancelNotFinishedJobsInBatchOrder(batchOrderId);

        // then
        verify(irsItemGraphQueryService).cancelJobById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(runningJobId);
    }

    private static Jobs jobInState(final JobState completed) {
        return Jobs.builder().job(Job.builder().state(completed).build()).build();
    }

    private Batch createBatch(final UUID batchId, final UUID batchOrderId, final ProcessingState state, final List<UUID> jobIds) {
        return Batch.builder()
                    .batchId(batchId)
                    .batchOrderId(batchOrderId)
                    .batchState(state)
                    .jobProgressList(
                jobIds.stream().map(uuid -> JobProgress.builder().jobId(uuid).build()).toList()
        ).build();
    }

}