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
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Provide descriptions to request parts
 */
@Schema(description = "Provide descriptions to request parts")
@Value
@Builder
@JsonDeserialize(builder = Description.DescriptionBuilder.class)
@ExcludeFromCodeCoverageGeneratedReport
public class Description {

    @Schema(description = "language used for description", example = "en", implementation = String.class, minLength = 0, maxLength = 3)
    private String language;

    @Schema(description = "description text", example = "The shell for a vehicle", implementation = String.class, minLength = 0, maxLength = 4000)
    private String text;

    /**
     * Builder for Description class
     */
    @JsonPOJOBuilder(withPrefix = "with")
    public static class DescriptionBuilder {
    }
}
