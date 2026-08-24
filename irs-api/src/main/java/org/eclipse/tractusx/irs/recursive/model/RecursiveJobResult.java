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
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;

/**
 * Externally visible recursive business result.
 */
@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecursiveJobResult {

    @Schema(description = "Business outcome of the aggregation: COMPLETE (no tombstones), PARTIAL "
            + "(usable material tree plus tombstones) or FAILED (no usable material tree). Independent of "
            + "job.state, which reflects the technical run - a COMPLETED job can carry a FAILED "
            + "result when the chain delivered no usable data.")
    private RecursiveResultStatus resultStatus;
    private RecursiveUseCase useCase;
    private BomLifecycle bomLifecycle;
    private List<String> requestedAspects;

    @Builder.Default
    private List<@Valid RecursiveChildItem> childItems = List.of();

    @Schema(description = "Sanitized root-level or otherwise unassignable failures. Failures that belong "
            + "to a material node are exposed in that child item's tombstones. Identical tombstones within "
            + "the same scope are merged with an occurrences counter.")
    @Builder.Default
    private List<@Valid RecursiveTombstone> tombstones = List.of();
}
