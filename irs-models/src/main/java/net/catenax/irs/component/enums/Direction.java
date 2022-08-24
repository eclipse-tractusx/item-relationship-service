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

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Direction indicator
 */
@JsonSerialize(using = ToStringSerializer.class)
@Schema(description = "Item graph traversal direction.")
public enum Direction {
    //@Schema(description = "The tree is traversed in upward direction.") UPWARD("upward"),
    @Schema(description = "The tree is traversed in downward direction.") DOWNWARD(DirectionConstants.DOWNWARD);

    private final String name;

    Direction(final String name) {
        this.name = name;
    }

    /**
     * of as a substitute/alias for valueOf handling the default value
     *
     * @param value see {@link #name}
     * @return the corresponding Direction
     */
    public static Direction value(final String value) {
        return Direction.valueOf(value);
    }

    @JsonCreator
    public static Direction fromValue(final String value) {
        return Stream.of(Direction.values())
                     .filter(direction -> direction.name.equals(value))
                     .findFirst()
                     .orElseThrow();
    }

    /**
     * @return convert Direction to string value
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Constants for directions
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class DirectionConstants {
        public static final String DOWNWARD = "downward";
    }
}
