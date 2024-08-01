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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.eclipse.tractusx.irs.connector.batch.JobProgress;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.springframework.stereotype.Service;

/**
 * Execute proper cancel jobs that are taking to long in Batch Process.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CancelBatchProcessingService {

    private final IrsItemGraphQueryService irsItemGraphQueryService;
    private final BatchStore batchStore;

    public void cancelNotFinishedJobs(final List<UUID> jobIds) {
        log.info("Start scheduled timeout process for jobIds: {}", jobIds.toString());
        jobIds.forEach(jobId -> {
            final JobState jobState = irsItemGraphQueryService.getJobForJobId(jobId, false).getJob().getState();
            if (isNotCompleted(jobState)) {
                log.info("Not completed job detected. Canceling job with jobId: {}", jobId);
                irsItemGraphQueryService.cancelJobById(jobId);
            }
        });
    }

    public void cancelNotFinishedJobsInBatch(final UUID batchId) {
        log.info("Start scheduled timeout process for batchId: {}", batchId.toString());
        batchStore.find(batchId).ifPresent(batch -> {
            if (isBatchNotCompleted(batch.getBatchState())) {
                final List<UUID> jobIds = batch.getJobProgressList()
                                               .stream()
                                               .map(JobProgress::getJobId)
                                               .filter(Objects::nonNull)
                                               .toList();
                cancelNotFinishedJobs(jobIds);
            }
        });
    }

    public void cancelNotFinishedJobsInBatchOrder(final UUID batchOrderId) {
        log.info("Canceling processing of jobs in order with id: {}", batchOrderId.toString());
        final List<Batch> batches = batchStore.findAll()
                                              .stream()
                                              .filter(batch -> batch.getBatchOrderId().equals(batchOrderId))
                                              .toList();
        batches.forEach(batch -> {
            if (isBatchNotCompleted(batch.getBatchState())) {
                final List<UUID> jobIds = batch.getJobProgressList()
                                             .stream()
                                             .map(JobProgress::getJobId)
                                             .filter(Objects::nonNull)
                                             .toList();
                cancelNotFinishedJobs(jobIds);
            }
        });
    }

    private boolean isNotCompleted(final JobState jobState) {
        return JobState.RUNNING.equals(jobState) || JobState.INITIAL.equals(jobState) || JobState.UNSAVED.equals(jobState);
    }

    private boolean isBatchNotCompleted(final ProcessingState processingState) {
        return ProcessingState.PROCESSING.equals(processingState) || ProcessingState.INITIALIZED.equals(processingState);
    }

}
