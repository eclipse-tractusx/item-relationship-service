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
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import org.apache.commons.lang3.tuple.Pair;

/**
 * An AAS shell.
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
@Schema(description = "")
@ExcludeFromCodeCoverageGeneratedReport
public class Shell extends BaseDescriptor{



    @Schema(implementation = Map.class)
    private Map<String, String> specificAssetIds;

    @Schema(implementation = Description.class)
    private List<Description> descriptions;

    @Schema(implementation = GlobalAssetIdentification.class)
    private List<GlobalAssetIdentification> globalAssetId;

    @Schema(implementation = SubmodelDescriptor.class)
    private List<SubmodelDescriptor> submodelDescriptors;
}
