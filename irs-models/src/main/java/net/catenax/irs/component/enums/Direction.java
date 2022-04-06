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

/**
 * Direction indicator
 */
@Schema(description = "Direction in which the tree shall be traversed")
public enum Direction {
    @Schema(description = "The tree is traversed in upward direction.") UPWARD,
    @Schema(description = "The tree is traversed in downward direction.")  DOWNWARD;
}
