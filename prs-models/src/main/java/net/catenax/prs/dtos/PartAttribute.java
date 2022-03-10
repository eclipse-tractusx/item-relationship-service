//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import net.catenax.prs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/***
 * API type for the part attributes.
 */
@ExcludeFromCodeCoverageGeneratedReport
@Schema(description = "Part attributes.")
public enum PartAttribute {
    @Schema(description = "Type of material, (sub)component/part or vehicle")
    PART_TYPE_NAME,
}
