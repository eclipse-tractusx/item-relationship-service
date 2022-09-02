/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.component;

import java.util.List;
import java.util.Set;

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

    @Schema(description = "Relationships between parent and child items.")
    @Singular
    private List<Relationship> relationships;

    @Schema(description = "AAS shells.")
    private List<AssetAdministrationShellDescriptor> shells;

    @Schema(description = "Collection of not resolvable endpoints as tombstones. Including cause of error and endpoint URL.")
    @Singular
    private List<Tombstone> tombstones;

    @Schema(description = "Collection of requested Submodels")
    @Singular
    private List<Submodel> submodels;

    @Schema(description = "Collection of bpn mappings")
    @Singular
    private Set<Bpn> bpns;

}
