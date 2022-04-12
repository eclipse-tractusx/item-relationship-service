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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Relationship
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
@AllArgsConstructor
@ExcludeFromCodeCoverageGeneratedReport
public class Relationship {

    @Schema(implementation = GlobalAssetIdentification.class)
    private GlobalAssetIdentification catenaXId;

    private Job childItem;

    private Job parentItem;

}
