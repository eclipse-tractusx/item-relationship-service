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

/**
 * Provide descriptions to request parts
 */
@Schema(description = "Provide descriptions to request parts.")
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = Description.DescriptionBuilder.class)
public class Description {

    public static final int LANGUAGE_MAX_LENGTH = 3;
    public static final int DESCRIPTION_MAX_LENGTH = 4000;

    @Schema(description = "Language used for description.", example = "en", implementation = String.class,
            minLength = 0, maxLength = LANGUAGE_MAX_LENGTH)
    private String language;

    @Schema(description = "Description text.", example = "The shell for a vehicle", implementation = String.class,
            minLength = 0, maxLength = DESCRIPTION_MAX_LENGTH)
    private String text;

    /**
     * Builder for Description class
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class DescriptionBuilder {
    }
}
