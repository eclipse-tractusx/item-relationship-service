/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import org.eclipse.tractusx.irs.component.AsyncFetchedItems;
import org.eclipse.tractusx.irs.component.JobErrorDetails;
import org.eclipse.tractusx.irs.component.enums.JobState;

/**
 * Job envelope of the recursive result, shaped like the iterative {@code Job} so a consumer of the
 * classic IRS job result can read the same field names. Only fields that carry a real value are
 * present; topology fields (relationships/shells/bpns) are intentionally absent in the recursive
 * privacy model.
 */
@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("PMD.ShortVariable")
public class RecursiveJobView {

    @NonNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    private String globalAssetId;
    private JobState state;
    private ZonedDateTime createdOn;
    private ZonedDateTime completedOn;
    private ZonedDateTime lastModifiedOn;

    /**
     * Child-response counters projected onto the iterative running/completed/failed shape.
     * Counts only this node's DIRECT children - the requester already knows them from its own
     * BOM, so the counters reveal no deeper chain structure (anonymity boundary holds).
     */
    private AsyncFetchedItems asyncFetchedItems;

    /** Terminal failure reason and timestamp, like the iterative {@code job.exception}; only set for failed jobs. */
    private JobErrorDetails exception;

    /** How the job was started (opening, use case, lifecycle, aspects, deadlines). */
    private RecursiveJobParameter parameter;
}
