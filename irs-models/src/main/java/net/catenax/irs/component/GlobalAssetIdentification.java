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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Global unique identifier for asset
 */

@Schema(description = "CATENA-X global asset id in the format urn:uuid:<uuid>.")
@Value
@Builder(toBuilder = true)
@JsonSerialize(using = ToStringSerializer.class)
@JsonDeserialize(builder = GlobalAssetIdentification.GlobalAssetIdentificationBuilder.class)
@ExcludeFromCodeCoverageGeneratedReport
public class GlobalAssetIdentification {

    private static final int GLOBAL_ASSET_ID_LENGTH = 45;

    @Valid
    @Schema(description = "CATENA-X global asset id in the format urn:uuid:<uuid>.", example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
            minLength = GLOBAL_ASSET_ID_LENGTH, maxLength = GLOBAL_ASSET_ID_LENGTH)
    private String globalAssetId;

    @Override
    public String toString() {
        return globalAssetId;
    }

    /**
     * Builder for GlobalAssetIdBuilder class
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class GlobalAssetIdentificationBuilder {
    }

}
