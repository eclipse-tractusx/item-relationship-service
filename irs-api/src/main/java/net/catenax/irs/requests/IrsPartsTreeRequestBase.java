//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.requests;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

import java.util.List;
import java.util.Optional;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.annotations.ValueOfEnum;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;
import net.catenax.irs.controllers.ApiErrorsConstants;

/**
 * Base class for IrsPartsTreeRequest
 */
@RequiredArgsConstructor
public abstract class IrsPartsTreeRequestBase {

    private static final long MIN_TREE_DEPTH = 1;
    private static final long MAX_TREE_DEPTH = 100;

    @NotBlank(message = ApiErrorsConstants.INVALID_ARGUMENTS)
    @ValueOfEnum(enumClass = BomLifecycle.class, message = ApiErrorsConstants.ITEM_VIEW_MUST_MATCH_ENUM)
    @Parameter(
            description = "Unique identifier of a single, unique (sub)component/part/batch,  given by its globalAssetId/ digital twin id.",
            in = QUERY, example = "asBuilt", schema = @Schema(implementation = BomLifecycle.class))
    protected final String bomLifecycle;

    @ValueOfEnum(enumClass = AspectType.class, message = ApiErrorsConstants.ITEM_VIEW_MUST_MATCH_ENUM)
    @Parameter(description = "AspectType information to add to the returned tree", in = QUERY,
            example = "SerialPartTypization", explode = Explode.FALSE, schema = @Schema(implementation = AspectType.class))
    protected final List<String> aspects;

    @Min(value = MIN_TREE_DEPTH, message = ApiErrorsConstants.ITEM_MIN_DEPTH)
    @Max(value = MAX_TREE_DEPTH, message = ApiErrorsConstants.ITEM_MAX_DEPTH)
    @Parameter(description = "Max depth of the returned tree, if empty max depth is returned", in = QUERY,
            schema = @Schema(implementation = Integer.class, minimum = "1", maximum = "100"))
    protected final Integer depth;

    @ValueOfEnum(enumClass = Direction.class, message = ApiErrorsConstants.ITEM_VIEW_MUST_MATCH_ENUM)
    @Parameter(description = "Direction in which the tree shall be traversed", in = QUERY, example = "downward",
            schema = @Schema(implementation = Direction.class))
    protected final String direction;

    public String getBomLifecycle() {
        return bomLifecycle;
    }

    public Optional<List<String>> getAspects() {
        return Optional.ofNullable(aspects);
    }

    public Optional<Integer> getDepth() {
        return Optional.ofNullable(depth);
    }

    public Optional<String> getDirection() {
        return Optional.ofNullable(direction);
    }
}
