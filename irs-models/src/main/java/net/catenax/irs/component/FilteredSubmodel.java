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
import lombok.Builder;
import lombok.Value;

/**
 * Use to filter submodel to specific child items
 */
@Schema(description = "Use to filter submodel to specific child items")
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = FilteredSubmodel.FilteredSubmodelBuilder.class)
public class FilteredSubmodel {

    private String catenaXId;

    private List<ChildItem> childParts;

    /**
     * Builder for FilteredSubmodel class
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class FilteredSubmodelBuilder {
    }
}
