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

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.stream.Stream;

/**
 * Direction indicator
 */
@Schema(description = "Direction in which the tree shall be traversed.")
public enum Direction {
    //@Schema(description = "The tree is traversed in upward direction.") UPWARD("upward"),
    @Schema(description = "The tree is traversed in downward direction.") DOWNWARD(DirectionConstants.DOWNWARD);

    private final String value;

    Direction(final String value) {
        this.value = value;
    }

    /**
     * of as a substitute/alias for valueOf handling the default value
     *
     * @param value see {@link #value}
     * @return the corresponding Direction
     */
    public static Direction value(final String value) {
        return Direction.valueOf(value);
    }

    @JsonCreator
    public static Direction fromValue(final String value) {
        return Stream.of(Direction.values()).filter(direction -> direction.value.equals(value)).findFirst().orElseThrow();
    }

    /**
     * @return convert Direction to string value
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Constants for directions
     */
    public static class DirectionConstants {
        public static final String DOWNWARD = "downward";
    }
}
