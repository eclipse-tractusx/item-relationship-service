/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
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
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.eclipse.tractusx.irs.connector.batch.JobProgress;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.springframework.stereotype.Service;

/**
 * Execute proper cancel jobs that are taking to long in Batch Process.
 */
@Service
@RequiredArgsConstructor
public class CancelBatchProcessingService {

    private final IrsItemGraphQueryService irsItemGraphQueryService;
    private final BatchStore batchStore;

    public void cancelNotFinishedJobs(List<UUID> jobIds) {
        jobIds.forEach(jobId -> {
            final JobState jobState = irsItemGraphQueryService.getJobForJobId(jobId, false).getJob().getState();
            if (isNotCompleted(jobState)) {
                irsItemGraphQueryService.cancelJobById(jobId);
            }
        });
    }

    public void cancelNotFinishedJobsInBatch(UUID batchId) {
        batchStore.find(batchId).ifPresent(batch -> {
            if (isBatchNotCompleted(batch.getBatchState())) {
                cancelNotFinishedJobs(batch.getJobProgressList().stream().map(JobProgress::getJobId).toList());
            }
        });
    }

    private boolean isNotCompleted(JobState jobState) {
        return JobState.RUNNING.equals(jobState) || JobState.INITIAL.equals(jobState) || JobState.UNSAVED.equals(jobState);
    }

    private boolean isBatchNotCompleted(ProcessingState processingState) {
        return ProcessingState.PROCESSING.equals(processingState) || ProcessingState.INITIALIZED.equals(processingState);
    }

}
