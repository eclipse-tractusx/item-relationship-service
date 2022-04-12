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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * A query parameter to retrieve job data.
 */
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = QueryParameter.QueryParameterBuilder.class)
@AllArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
@ExcludeFromCodeCoverageGeneratedReport
public class QueryParameter {

    /**
     * jobId
     */
    @NotNull
    @Schema(description = "Bom lifecycle for query parameter", implementation = BomLifecycle.class)
    private final BomLifecycle bomLifecycle;

    /**
     * globalAssetId
     */
    @NotNull
    @Schema(description = "Aspect type for query parameter", implementation = AspectType.class)
    private final Collection<AspectType> aspect;

    @NotNull
    @Schema()
    private final Number depth;

    @NotNull
    @Schema(description = "Direction query parameter", implementation = Direction.class)
    private final Direction direction;

    /**
     * Builder class
     */
    @JsonPOJOBuilder(withPrefix = "with")
    public static class QueryParameterBuilder {
    }


}

