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

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Global unique identifier for asset
 */


@Schema(description = "Represents a CatenaX id in the format urn:uuid:<uuid>")
@Value
@Builder
@JsonDeserialize(builder = GlobalAssetIdentification.GlobalAssetIdBuilder.class)
@ExcludeFromCodeCoverageGeneratedReport
public class GlobalAssetIdentification {

    @Valid
    @Schema(description = "Global unique C-X identifier", example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0", minLength = 45, maxLength = 45)
    private String globalAssetId;

    /**
     * Builder for GlobalAssetIdBuilder class
     */
    @JsonPOJOBuilder(withPrefix = "with")
    public static class GlobalAssetIdBuilder {
    }
}
