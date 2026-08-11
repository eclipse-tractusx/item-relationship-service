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
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** A material node in the aggregated recursive result tree. */
@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecursiveChildItem {

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String materialNumber;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String materialName;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private RecursiveQuantity quantity;

    @Builder.Default
    private List<RecursiveAspectItem> items = List.of();

    @Builder.Default
    private List<RecursiveTombstone> tombstones = List.of();

    @ArraySchema(schema = @Schema(implementation = RecursiveChildItem.class))
    @Builder.Default
    private List<RecursiveChildItem> childItems = List.of();
}
