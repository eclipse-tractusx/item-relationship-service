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

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.util.RecursivePatternStore;

/**
 * Request DTO for starting a recursive IRS job.
 *
 * <p>{@code openingId}, {@code useCase} and {@code globalAssetId} are required. Lifecycle, aspects and TTL use
 * their configured PURIS defaults when omitted. The notification message ID used for idempotency and response
 * correlation is not part of this public request.</p>
 *
 * <p>The supported aspect bundle and lifecycle are defined by {@link RecursiveUseCase}. Unsupported selections
 * are rejected at every hop.</p>
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class RecursiveJobRequest {

    /**
     * Chain opening identifier, identical across the whole recursive chain.
     */
    @Schema(description = "Chain opening identifier, identical across the whole recursive chain.")
    @NotBlank
    @Pattern(regexp = RecursivePatternStore.SAFE_SINGLE_LINE_STRING,
             message = "openingId must not contain control or line separator characters")
    private String openingId;

    @NotNull
    @Schema(description = "Recursive use case. Only the configured PURIS anonymized bundle is supported.",
            example = "PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE")
    private RecursiveUseCase useCase;

    /**
     * Traversal root. Required - the recursive path identifies the root only by its global asset id.
     */
    @NotBlank
    @Pattern(regexp = RecursivePatternStore.GLOBAL_ASSET_ID_STRING,
             message = "globalAssetId must be a valid UUID or URN UUID")
    private String globalAssetId;

    /**
     * Lifecycle used for the relationship traversal. This selects the traversal submodel automatically.
     */
    @Schema(description = "BOM lifecycle used for traversal. PURIS supports only asPlanned.",
            defaultValue = "asPlanned", allowableValues = "asPlanned")
    private BomLifecycle bomLifecycle;

    /**
     * Payload aspects to collect during the recursive traversal.
     */
    @Schema(description = "Semantic IDs of the payload aspects to collect. Optional - when omitted, "
            + "all three anonymized PURIS aspects are collected. An explicit list must be a subset of "
            + "the configured use-case bundle. Unknown or bundle-external aspects are rejected at every hop.")
    private List<@Pattern(regexp = RecursivePatternStore.SAFE_SINGLE_LINE_STRING,
            message = "aspects must not contain control or line separator characters") String> aspects;

    /**
     * Business Partner Number of the requesting party - also the BPN the response is returned to.
     * Auto-filled from {@code irs.recursive.localBpnl} if null/blank.
     */
    @Pattern(regexp = RecursivePatternStore.OPTIONAL_BPNL_STRING,
             message = "requesterBpn must be blank or a valid BPNL")
    private String requesterBpn;

    /**
     * Time-to-live as ISO 8601 duration.
     * Defaults to configured {@code irs.recursive.timeout.defaultJobTtl} if null.
     */
    @Schema(description = "Job time-to-live as ISO 8601 duration, e.g. PT30M. Defaults to "
            + "irs.recursive.timeout.defaultJobTtl and is capped at maxJobTtl. Each hop passes the "
            + "remaining budget minus a safety buffer to the child as the child response deadline.")
    @Pattern(regexp = RecursivePatternStore.OPTIONAL_SAFE_SINGLE_LINE_STRING,
             message = "ttl must not contain control or line separator characters")
    private String ttl;
}
