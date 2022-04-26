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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.annotations.ValueOfEnum;
import net.catenax.irs.controllers.ApiErrorsConstants;
import net.catenax.irs.dtos.ItemsTreeView;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Optional;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;
import static net.catenax.irs.dtos.ValidationConstants.INPUT_FIELD_MAX_LENGTH;
import static net.catenax.irs.dtos.ValidationConstants.INPUT_FIELD_MIN_LENGTH;

/**
 * Base for {@code getPartsTreeBy*} parameter objects.
 */
@RequiredArgsConstructor
@SuppressWarnings({"PMD.CommentRequired", "PMD.AbstractClassWithoutAbstractMethod"})
abstract class PartsTreeRequestBase {
    @NotBlank(message = ApiErrorsConstants.ITEM_VIEW_NOT_NULL)
    @ValueOfEnum(enumClass = ItemsTreeView.class, message = ApiErrorsConstants.ITEM_VIEW_MUST_MATCH_ENUM)
    @Parameter(description = "PartsTree View to retrieve", in = QUERY, required = true, schema = @Schema(implementation = ItemsTreeView.class))
    protected final String view;

    @Pattern(regexp = "^(?!\\s*$).+", message = ApiErrorsConstants.NOT_BLANK)
    @Size(min = INPUT_FIELD_MIN_LENGTH, max = INPUT_FIELD_MAX_LENGTH)
    @Parameter(description = "AspectType information to add to the returned tree.", in = QUERY, example = "CE", schema = @Schema(implementation = String.class))
    protected final String aspect;

    @Min(value = 1, message = ApiErrorsConstants.ITEM_MIN_DEPTH)
    @Parameter(description = "Max depth of the returned tree, if empty max depth is returned.", in = QUERY, schema = @Schema(implementation = Integer.class, minimum = "1"))
    protected final Integer depth;

    public ItemsTreeView getView() {
        return ItemsTreeView.valueOf(view);
    }

    public Optional<String> getAspect() {
        return Optional.ofNullable(aspect);
    }

    public Optional<Integer> getDepth() {
        return Optional.ofNullable(depth);
    }
}
