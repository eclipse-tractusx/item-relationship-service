//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;

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

}
