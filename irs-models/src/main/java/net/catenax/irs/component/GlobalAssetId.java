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
 * Global unique identifier for asset
 */
@Value
@Builder
@JsonDeserialize(builder = GlobalAssetId.GlobalAssetIdBuilder.class)
public class GlobalAssetId {

    @Schema(description = "Global unique identifier")
    private String globalAssetId;

    /**
     * Builder for GlobalAssetIdBuilder class
     */
    @JsonPOJOBuilder(withPrefix = "with")
    public static class GlobalAssetIdBuilder {
    }
}
