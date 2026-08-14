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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import org.eclipse.tractusx.irs.recursive.util.RecursivePatternStore;

/**
 * Externally visible recursive tombstone.
 *
 * <p>Identical failures (same scope, reason, aspects and detail) are aggregated within their
 * result-tree node: {@code occurrences} counts them and {@code errorRefs} keeps one correlation id
 * per occurrence. Placement in a {@link RecursiveChildItem} associates a failure with an anonymous
 * material node without exposing participant identities.</p>
 *
 * <p>This model is separate from the iterative IRS tombstone because recursive results must not carry
 * partner, endpoint, policy or asset identifiers across tiers.</p>
 */
@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecursiveTombstone {

    @NotNull
    private RecursiveTombstoneType type;

    @NotNull
    private RecursiveTombstoneScope scope;

    /**
     * The requested aspect semantic IDs this failure relates to.
     */
    @NotNull
    private List<@Pattern(regexp = RecursivePatternStore.SAFE_SINGLE_LINE_STRING,
            message = "aspects must not contain control or line separator characters") String> aspects;

    @NotNull
    private RecursiveTombstoneReason reason;

    @NotNull
    private Boolean retryable;

    @NotBlank
    @Schema(minLength = 1)
    @Pattern(regexp = RecursivePatternStore.SAFE_SINGLE_LINE_STRING,
             message = "detail must not contain control or line separator characters")
    private String detail;

    /**
     * Number of identical failures aggregated into this tombstone.
     */
    @NotNull
    @Min(1)
    private Integer occurrences;

    /**
     * Identity-free correlation ids, one per occurrence. They carry no partner data themselves,
     * but let a support request (customer quotes an errorRef) be mapped to the detailed internal
     * diagnostics in the logs of the node that produced it.
     */
    @NotEmpty
    @ArraySchema(minItems = 1)
    private List<@Pattern(regexp = RecursivePatternStore.UUID_STRING,
            message = "errorRefs must contain UUIDs") String> errorRefs;
}
