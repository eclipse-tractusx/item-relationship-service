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

import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;

/**
 * List of Job and relationship to parts
 */
@Schema(description = "Container for a job with item graph.")
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
@SuppressWarnings("PMD.ShortClassName")
public class Jobs {

    @Schema(description = "Executable unit with meta information and item graph result.", implementation = Job.class)
    private Job job;

    @ArraySchema(arraySchema = @Schema(description = "Relationships between parent and child items."), maxItems = Integer.MAX_VALUE)
    @Singular
    private List<Relationship> relationships;

    @ArraySchema(arraySchema = @Schema(description = "AAS shells."), maxItems = Integer.MAX_VALUE)
    private List<AssetAdministrationShellDescriptor> shells;

    @ArraySchema(arraySchema = @Schema(description = "Collection of not resolvable endpoints as tombstones. Including cause of error and endpoint URL."), maxItems = Integer.MAX_VALUE)
    @Singular
    private List<Tombstone> tombstones;

    @ArraySchema(arraySchema = @Schema(description = "Collection of requested Submodels"), maxItems = Integer.MAX_VALUE)
    @Singular
    private List<Submodel> submodels;

    @ArraySchema(arraySchema = @Schema(description = "Collection of bpn mappings"), maxItems = Integer.MAX_VALUE)
    @Singular
    private Set<Bpn> bpns;

}
