/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.component.enums;

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
    @Schema(description = "The tree is traversed in upward direction.") UPWARD(DirectionConstants.UPWARD),
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
        public static final String UPWARD = "upward";
        public static final String DOWNWARD = "downward";
    }
}
