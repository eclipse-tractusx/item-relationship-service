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

/**
 * SubmodelDescriptor description
 */
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonDeserialize(builder = SubmodelDescriptor.SubmodelDescriptorBuilder.class)
public class SubmodelDescriptor {

    @Schema(implementation = String.class)
    private String identification;

    @Schema(implementation = String.class)
    private String idShort;

    @Schema()
    @Singular
    private List<Description> descriptions;

    @Schema(implementation = SemanticId.class)
    private SemanticId semanticId;

    @Schema()
    @Singular
    private List<Endpoint> endpoints;

    /**
     * User to build SubmodelDescriptor
     */
    @Schema(description = "User to build async fetched items")
    @JsonPOJOBuilder(withPrefix = "with")
    public static class SubmodelDescriptorBuilder {
    }
}
