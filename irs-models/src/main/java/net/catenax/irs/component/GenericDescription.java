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
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * GenericDescription
 */
@Schema(description = "")
@Value
@Builder
@JsonDeserialize(builder = GenericDescription.GenericDescriptionBuilder.class)
@ExcludeFromCodeCoverageGeneratedReport
public class GenericDescription {
    @Schema(description = "Identification string")
    private String identification;

    @Schema(description = "Identification short form")
    private String idShort;

    @Schema(description = "Key value pair for specific asset id")
    private Map<String, String> specificAssetId;

    @Schema(description = "Description")
    private List<Description> descriptions;



}
