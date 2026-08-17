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
import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Internal state of a recursive job.
 *
 * <p>Persisted to blob storage (MinIO / S3) via
 * {@link org.eclipse.tractusx.irs.recursive.store.BlobRecursiveJobStateStore} under the key prefix
 * {@code recursive-job:}.
 * The {@code @Jacksonized} annotation enables Jackson deserialization via the Lombok builder.</p>
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
@SuppressWarnings("PMD.TooManyFields")
public class RecursiveJobState {

    @NonNull
    private UUID jobId;
    private String openingId;
    private RecursiveUseCase useCase;
    private String globalAssetId;
    private BomLifecycle bomLifecycle;
    private List<String> aspects;
    private String requesterBpnl;
    private String receiverBpnl;
    private String messageId;
    private ZonedDateTime createdOn;
    private ZonedDateTime lastModifiedOn;
    private ZonedDateTime deadline;
    private ZonedDateTime childResponseDeadline;
    private ZonedDateTime timedOutOn;
    private RecursiveJobPhase state;

    /**
     * True for jobs started directly via POST /irs/recursive/jobs (the root / OEM call).
     * False for jobs created by handleIncomingRequest (child hops).
     * Root jobs do NOT send a parent response - there is no parent to notify.
     */
    private boolean rootJob;

    /**
     * BOM children discovered during local traversal.
     */
    private List<RecursiveBomChild> bomChildren;

    /**
     * Concrete child branches expected for this job. Each entry represents one direct BOM child.
     */
    @NonNull
    private List<RecursiveChildBranch> childBranches;

    /**
     * External reason used for the exception field of a technically failed job.
     */
    private RecursiveTombstoneReason failureReason;

    /**
     * Locally collected material metadata, anonymized aspects and node-specific tombstones.
     * Root jobs leave this empty because the PURIS result starts with the root's BOM children.
     */
    private RecursiveChildItem localNode;

    /**
     * Aggregated result visible on the local job itself.
     * For child jobs, this is the payload that is sent to the parent.
     * For root jobs, this is the final result that clients can fetch via GET /irs/recursive/jobs/{jobId}.
     */
    private RecursiveJobResult result;

    /**
     * Number of child responses expected from the concrete child branches.
     *
     * @return expected child response count
     */
    public int expectedChildResponseCount() {
        return childBranches.size();
    }
}
