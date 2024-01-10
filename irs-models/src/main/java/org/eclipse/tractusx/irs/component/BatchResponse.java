/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;

/**
 * Batch model
 */
@Schema(description = "Batch model.")
@Value
@Builder
@AllArgsConstructor
@Jacksonized
public class BatchResponse {

    private static final int UUID_LENGTH = 36;

    @Schema(description = "Id of the order.", minLength = UUID_LENGTH,
            maxLength = UUID_LENGTH, implementation = UUID.class,
            pattern = "/^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i")
    private UUID orderId;

    @Schema(description = "Id of the batch.", minLength = UUID_LENGTH,
            maxLength = UUID_LENGTH, implementation = UUID.class,
            pattern = "/^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i")
    private UUID batchId;

    @Schema(implementation = Integer.class, description = "Sequential number begins with first (1.) batch.")
    private Integer batchNumber;

    @Schema(implementation = Integer.class, description = "The total number of batches.")
    private Integer batchTotal;

    @Schema(implementation = Integer.class, description = "Total amount of jobs inside the order.")
    private Integer totalJobs;

    @Schema(implementation = ZonedDateTime.class)
    private ZonedDateTime startedOn;

    @Schema(implementation = ZonedDateTime.class)
    private ZonedDateTime completedOn;

    @ArraySchema(arraySchema = @Schema(description = "Array of jobs."), maxItems = Integer.MAX_VALUE)
    private List<JobStatusResult> jobs;

    @Schema(implementation = Integer.class, description = "Expected number of jobs in batch.")
    private Integer jobsInBatchChecksum;

    @Schema(implementation = ProcessingState.class, description = "The state of the batch.")
    private ProcessingState batchProcessingState;

}
