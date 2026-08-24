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
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import org.eclipse.tractusx.irs.recursive.util.RecursivePatternStore;

/**
 * Represents a local grant that authorizes a specific recursive chain opening.
 * Chain opening grants are created externally (e.g. by PURIS after chain opening approval)
 * and stored in the IRS-local grant persistency.
 *
 * <p>The grant acts as a technical gate: even if the BOM yields many potential
 * suppliers, only those in {@code allowedBpnlSet} may be queried for this opening.</p>
 *
 * <p>A grant is uniquely identified by {@code openingId + globalAssetId + requesterBpn + useCase}
 * ({@link RecursiveChainOpeningGrantKey}) - a node may hold several grants for the same opening,
 * one per requested material and requesting partner.</p>
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class RecursiveChainOpeningGrant {

    /**
     * Identifies the chain opening this grant belongs to. Key component.
     */
    @NotBlank
    @Pattern(regexp = RecursivePatternStore.SAFE_SINGLE_LINE_STRING,
             message = "openingId must not contain control or line separator characters")
    private String openingId;

    /**
     * The recursive use case. Key component.
     */
    @NotNull
    @Schema(description = "Supported recursive use case.")
    private RecursiveUseCase useCase;

    /**
     * The material this grant authorizes - the customer material requested at this node.
     * Key component.
     */
    @NotBlank
    @Pattern(regexp = RecursivePatternStore.GLOBAL_ASSET_ID_STRING,
             message = "globalAssetId must be a valid UUID or URN UUID")
    private String globalAssetId;

    /**
     * BPN of the partner whose requests this grant authorizes. Key component.
     */
    @NotBlank
    @Pattern(regexp = RecursivePatternStore.BPNL_STRING,
             message = "requesterBpn must be a valid BPNL")
    private String requesterBpn;

    /**
     * Set of BPNLs that may be queried recursively under this grant.
     */
    private Set<@Pattern(regexp = RecursivePatternStore.BPNL_STRING,
            message = "allowedBpnlSet must contain valid BPNLs") String> allowedBpnlSet;


    private ZonedDateTime validFrom;

    private ZonedDateTime validTo;

    /**
     * Timestamp when this grant was first stored locally.
     */
    private ZonedDateTime createdAt;

    /**
     * Timestamp when this grant was last replaced locally.
     */
    private ZonedDateTime updatedAt;
}
