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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Global unique identifier for asset
 */

@Schema(description = "Represents a CatenaX id in the format urn:uuid:<uuid>.")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = ToStringSerializer.class)
@JsonDeserialize(builder = GlobalAssetIdentification.GlobalAssetIdBuilder.class)
@ExcludeFromCodeCoverageGeneratedReport
public class GlobalAssetIdentification {

    private static final int GLOBAL_ASSET_ID_LENGTH = 45;

    @Valid
    @Schema(description = "Global unique C-X identifier.", example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
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
    public static final class GlobalAssetIdBuilder {
        private final GlobalAssetIdentification globalAssetId;

        private GlobalAssetIdBuilder() {
            globalAssetId = new GlobalAssetIdentification();
        }

        @JsonCreator
        public static GlobalAssetIdBuilder instance() {
            return new GlobalAssetIdBuilder();
        }

        public GlobalAssetIdBuilder globalAssetId(String globalAssetId) {
            this.globalAssetId.globalAssetId = globalAssetId;
            return this;
        }

        public GlobalAssetIdentification build() {
            /*if (StringUtils.isEmpty(globalAssetId.globalAssetId)) {
                throw new IllegalArgumentException("Global Asset identifier cannot be null");
            }*/
            return globalAssetId;
        }
    }

}
