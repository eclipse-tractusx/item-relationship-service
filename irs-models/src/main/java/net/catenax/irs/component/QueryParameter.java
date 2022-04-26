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

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;

/**
 * Query parameter for current irs query
 */
@Schema(description = "Query parameter for current irs query.")
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = QueryParameter.QueryParameterBuilder.class)
@AllArgsConstructor
public class QueryParameter {

    @Schema(implementation = BomLifecycle.class)
    private BomLifecycle bomLifecycle;

    @Schema(implementation = AspectType.class)
    @Singular
    private List<AspectType> aspects;

    @Schema(implementation = Integer.class)
    private Integer depth;

    @Schema(implementation = Direction.class)
    private Direction direction;


    /**
     * Builder for QueryParameter class
     */
    @Schema(description = "Builder to to build query parameters")
    @JsonPOJOBuilder(withPrefix = "with")
    public static class QueryParameterBuilder {
    }
}
