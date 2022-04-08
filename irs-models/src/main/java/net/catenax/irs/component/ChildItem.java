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

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.component.enums.BomLifecycle;

/*** API type for ChildItem name/url entry. */
@Schema(description = "Describe child item of a global asset ")
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = ChildItem.ChildItemBuilder.class)
@AllArgsConstructor
@ExcludeFromCodeCoverageGeneratedReport
public class ChildItem {

    @Schema(description = "Quantity component", implementation = Quantity.class)
    private Quantity quantity;

    @Schema(description = "Unique identifier of a single, unique (sub)component/part/batch,given by its globalAssetId/ digital twin id",
            implementation = BomLifecycle.class)
    private String lifecycleContext;

    @Schema(description = "Datetime of assembly", implementation = Instant.class)
    private Instant assembledOn;

    @Schema(description = "Last date and time part was modified", implementation = Instant.class)
    private Instant lastModifiedOn;

    @Schema(description = "CatenaX child Id", implementation = Instant.class)
    private String childCatenaXId;

    /**
     * Builder for ChildItem class
     */
    @Schema(description = "Builder to to build child items")
    @JsonPOJOBuilder(withPrefix = "with")
    public static class ChildItemBuilder {
    }
}
