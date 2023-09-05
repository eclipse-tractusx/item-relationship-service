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
package org.eclipse.tractusx.irs.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.common.auth.SecurityHelperService;
import org.eclipse.tractusx.irs.component.BatchOrderResponse;
import org.eclipse.tractusx.irs.component.BatchResponse;
import org.eclipse.tractusx.irs.component.JobStatusResult;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.BatchOrder;
import org.eclipse.tractusx.irs.connector.batch.BatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.eclipse.tractusx.irs.connector.job.JobStore;
import org.eclipse.tractusx.irs.connector.job.MultiTransferJob;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Read service for Batches
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QueryBatchService {

    private final BatchOrderStore batchOrderStore;
    private final BatchStore batchStore;

    private final JobStore jobStore;
    private final SecurityHelperService securityHelperService;

    public BatchOrderResponse findOrderById(final UUID batchOrderId) {
        final BatchOrder batchOrder = batchOrderStore.find(batchOrderId)
                                                     .orElseThrow(
                                                             () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                                     "Cannot find Batch Order with id: "
                                                                             + batchOrderId));

        if (!securityHelperService.isAdmin() && !batchOrder.getOwner().equals(securityHelperService.getClientIdForViewIrs())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot access Batch Order with id " + batchOrderId + " due to missing privileges.");
        }

        final List<Batch> batches = batchStore.findAll()
                                              .stream()
                                              .filter(batch -> batch.getBatchOrderId().equals(batchOrderId))
                                              .toList();

        return BatchOrderResponse.builder()
                                 .orderId(batchOrderId)
                                 .state(batchOrder.getBatchOrderState())
                                 .batchChecksum(batches.size())
                                 .batches(batches.stream().map(this::toResponse).toList())
                                 .build();
    }

    public BatchResponse findBatchById(final UUID batchOrderId, final UUID batchId) {
        final Batch batchResponse = batchStore.find(batchId)
                                              .filter(batch -> batch.getBatchOrderId().equals(batchOrderId))
                                              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                                      "Cannot find Batch with orderId: " + batchOrderId
                                                                              + " and id: " + batchId));

        if (!securityHelperService.isAdmin() && !batchResponse.getOwner().equals(securityHelperService.getClientIdForViewIrs())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot accessBatch with orderId: " + batchOrderId
                    + " and id: " + batchId + " due to missing privileges.");
        }

        final Integer totalJobs = batchStore.findAll()
                                            .stream()
                                            .filter(batch -> batch.getBatchOrderId().equals(batchOrderId))
                                            .map(batch -> batch.getJobProgressList().size())
                                            .reduce(0, Integer::sum);

        return toBatchResponse(batchResponse, totalJobs);
    }

    private BatchOrderResponse.BatchResponse toResponse(final Batch batch) {
        return BatchOrderResponse.BatchResponse.builder()
                                               .batchId(batch.getBatchId())
                                               .batchNumber(batch.getBatchNumber())
                                               .batchProcessingState(batch.getBatchState())
                                               .batchUrl(batch.getBatchUrl())
                                               .jobsInBatchChecksum(batch.getJobProgressList().size())
                                               .build();
    }

    private BatchResponse toBatchResponse(final Batch batch, final Integer totalNumberOfJobsInOrder) {
        final List<JobStatusResult> jobs = batch.getJobProgressList()
                                                .stream()
                                                .filter(jobProgress -> jobProgress.getJobId() != null)
                                                .map(jobProgress -> jobStore.find(jobProgress.getJobId().toString()))
                                                .flatMap(Optional::stream)
                                                .map(toJobStatus())
                                                .toList();

        return BatchResponse.builder()
                            .batchId(batch.getBatchId())
                            .orderId(batch.getBatchOrderId())
                            .batchNumber(batch.getBatchNumber())
                            .totalJobs(totalNumberOfJobsInOrder)
                            .startedOn(batch.getStartedOn())
                            .completedOn(batch.getCompletedOn())
                            .batchTotal(batch.getBatchTotal())
                            .jobsInBatchChecksum(
                                    Optional.ofNullable(batch.getJobProgressList()).map(List::size).orElse(0))
                            .jobs(jobs)
                            .batchProcessingState(batch.getBatchState())
                            .build();
    }

    private static Function<MultiTransferJob, JobStatusResult> toJobStatus() {
        return job -> JobStatusResult.builder()
                                     .id(job.getJobId())
                                     .state(job.getJob().getState())
                                     .startedOn(job.getJob().getStartedOn())
                                     .completedOn(job.getJob().getCompletedOn())
                                     .build();
    }

}
