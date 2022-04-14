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

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * as
 */
@Schema(description = "Register job request.")
@Data
@NoArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
@ExcludeFromCodeCoverageGeneratedReport
public class RegisterJob {

    private static final String MIN_TREE_DEPTH = "1";
    private static final String MAX_TREE_DEPTH = "100";
    private static final String DEFAULT_DEPTH = "1";
    private static final int UUID_SIZE = 36;

    @NotNull
    @Schema(description = "GlobalAssetId of Item from which the tree building process starts.", example = "6c311d29-5753-46d4-b32c-19b918ea93b0", implementation = String.class, minLength = UUID_SIZE, maxLength = UUID_SIZE)
    private UUID globalAssetId;

    @Schema(description = "BoM Lifecycle of the result tree.", implementation = BomLifecycle.class)
    private BomLifecycle bomLifecycle;

    @ArraySchema(schema = @Schema(implementation = AspectType.class))
    private List<AspectType> aspects;

    @Schema(implementation = Integer.class, minimum = MIN_TREE_DEPTH, maximum = MAX_TREE_DEPTH, defaultValue = DEFAULT_DEPTH, description = "Max depth of the returned tree, if empty max depth is returned.")
    private int depth;

    @Schema(implementation = Direction.class, defaultValue = Direction.DirectionConstants.DOWNWARD)
    private Direction direction;
}
