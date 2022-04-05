//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.dtos.ItemLifecycleStage;

/***
 * API type for the view of the items tree to be returned by a query.
 *
 * @see ItemLifecycleStage
 */
@ExcludeFromCodeCoverageGeneratedReport
@Schema(description = "View defining which data of the PartsTree is retrieved.")
public enum BomLifecycle {
    @Schema(description = "The view of the ItemsTree as the vehicle was assembled.") asBuilt
    //,@Schema(description = "The view of the PartsTree that accounts for all updates during the vehicle lifecycle.") asMaintained
    //,@Schema(description = "The view of the PartsTree that accounts for all updates during the vehicle lifecycle.") asPlanned
    //,@Schema(description = "The view of the PartsTree that accounts for all updates during the vehicle lifecycle.") asDesigned
    //,@Schema(description = "The view of the PartsTree that accounts for all updates during the vehicle lifecycle.") asOrdered
    //,@Schema(description = "The view of the PartsTree that accounts for all updates during the vehicle lifecycle.") asRecycled
}
