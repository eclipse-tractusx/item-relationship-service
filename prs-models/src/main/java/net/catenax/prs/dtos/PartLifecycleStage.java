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
 * API type for the stage at which an event occurred, determining which events are used
 * to generate a particular {@link PartsTreeView}.
 *
 * @see PartsTreeView
 */
@ExcludeFromCodeCoverageGeneratedReport
@Schema(description = "Stage defining whether changes apply to the AS_BUILT or AS_MAINTAINED BOM views.")
@SuppressWarnings("PMD.CommentRequired")
public enum PartLifecycleStage {
    @Schema(description = "The time the part is built.")
    BUILD,

    @Schema(description = "The time after the part is built.")
    MAINTENANCE
}
