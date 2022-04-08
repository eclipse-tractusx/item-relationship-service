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
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * SubmodelDescriptor
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
@AllArgsConstructor
@ExcludeFromCodeCoverageGeneratedReport
@JsonDeserialize(builder = SubmodelDescriptor.SubmodelDescriptorBuilder.class)
public class SubmodelDescriptor extends BaseDescriptor {

   @Schema(implementation = SemanticId.class)
   private SemanticId semanticId;
   /**
    * User to build SubmodelDescriptor
    */
   @Schema(description = "User to build async fetched items")
   @JsonPOJOBuilder(withPrefix = "with")
   public static class SubmodelDescriptorBuilder {
   }
}
