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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.util.List;

/**
 * Request body for registering new job
 */
@Schema(description = "Register job request.")
@Data
@NoArgsConstructor
@ExcludeFromCodeCoverageGeneratedReport
public class RegisterJob {

    private static final String MIN_TREE_DEPTH_DESC = "1";
    private static final String MAX_TREE_DEPTH_DESC = "100";
    private static final int MIN_TREE_DEPTH = 1;
    private static final int MAX_TREE_DEPTH = 100;
    private static final String GLOBAL_ASSET_ID_REGEX = "^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    private static final int UUID_SIZE = 36;
    private static final int URN_PREFIX_SIZE = 9;
    private static final int GLOBAL_ASSET_ID_SIZE = URN_PREFIX_SIZE + UUID_SIZE;

    @Pattern(regexp = GLOBAL_ASSET_ID_REGEX)
    @NotBlank
    @Size(min = GLOBAL_ASSET_ID_SIZE, max = GLOBAL_ASSET_ID_SIZE)
    @Schema(description = "GlobalAssetId of Item from which the tree building process starts.", example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0", implementation = String.class, minLength = GLOBAL_ASSET_ID_SIZE, maxLength = GLOBAL_ASSET_ID_SIZE)
    private String globalAssetId;

    @Schema(description = "BoM Lifecycle of the result tree.", implementation = BomLifecycle.class)
    private BomLifecycle bomLifecycle;

    @ArraySchema(schema = @Schema(implementation = AspectType.class))
    private List<AspectType> aspects;

    @Schema(implementation = Integer.class, minimum = MIN_TREE_DEPTH_DESC, maximum = MAX_TREE_DEPTH_DESC, description = "Max depth of the returned tree, if empty max depth is returned.")
    @Min(MIN_TREE_DEPTH)
    @Max(MAX_TREE_DEPTH)
    private Integer depth;

    @Schema(implementation = Direction.class, defaultValue = Direction.DirectionConstants.DOWNWARD)
    private Direction direction;

    /**
     * Returns requested depth if provided, otherwise MAX_TREE_DEPTH value
     * @return depth
     */
    public Integer getDepth() {
        return depth == null ? MAX_TREE_DEPTH : depth;
    }
}
