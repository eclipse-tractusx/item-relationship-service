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

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.annotations.ValueOfEnum;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;
import net.catenax.irs.connector.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.controllers.ApiErrorsConstants;


/**
 * Base class for IrsPartsTreeRequest
 */
@RequiredArgsConstructor
@SuppressWarnings({ "PMD.AbstractClassWithoutAbstractMethod", "PMD.DataClass" })
@ExcludeFromCodeCoverageGeneratedReport
public abstract class IrsPartsTreeRequestBase implements Serializable {

    private static final long MIN_TREE_DEPTH = 1;
    private static final long MAX_TREE_DEPTH = 100;
    public static final String DEFAULT_DEPTH = "1";

    @NotBlank(message = ApiErrorsConstants.INVALID_ARGUMENTS)
    @ValueOfEnum(enumClass = BomLifecycle.class, message = ApiErrorsConstants.ITEM_VIEW_MUST_MATCH_ENUM)
    @Parameter(
            description = "BoM Lifecycle of the result tree.",
            in = QUERY, example = "asBuilt", schema = @Schema(implementation = BomLifecycle.class))
    protected final String bomLifecycle;

    @ValueOfEnum(enumClass = AspectType.class, message = ApiErrorsConstants.ITEM_VIEW_MUST_MATCH_ENUM)
    @Parameter(description = "AspectType information to add to the returned tree.", in = QUERY,
            example = "SerialPartTypization", explode = Explode.FALSE,
            array = @ArraySchema(schema = @Schema(implementation = AspectType.class, defaultValue = AspectType.AspectTypesConstants.SERIAL_PART_TYPIZATION)))
    protected final List<AspectType> aspects;

    @Min(value = MIN_TREE_DEPTH, message = ApiErrorsConstants.ITEM_MIN_DEPTH)
    @Max(value = MAX_TREE_DEPTH, message = ApiErrorsConstants.ITEM_MAX_DEPTH)
    @Parameter(description = "Max depth of the returned tree, if empty max depth is returned.", in = QUERY, example = "1")
    @Schema(implementation = Integer.class, minimum = "1", maximum = "100", defaultValue = DEFAULT_DEPTH)
    protected final int depth;

    @ValueOfEnum(enumClass = Direction.class, message = ApiErrorsConstants.ITEM_VIEW_MUST_MATCH_ENUM)
    @Parameter(description = "Direction in which the tree shall be traversed.", in = QUERY, example = "downward")
    @Schema(implementation = Direction.class, defaultValue = Direction.DirectionConstants.DOWNWARD1)
    protected final String direction;

    public String getBomLifecycle() {
        return bomLifecycle;
    }

    public Optional<List<AspectType>> getAspects() {
        return Optional.ofNullable(aspects);
    }

    public Optional<Integer> getDepth() {
        return Optional.ofNullable(depth);
    }

    public Optional<String> getDirection() {
        return Optional.ofNullable(direction);
    }
}
