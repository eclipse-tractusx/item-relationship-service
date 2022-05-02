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
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 * An AAS shell.
 */
@Value
@Builder(toBuilder = true)
@Schema(description = "")
@AllArgsConstructor
@JsonDeserialize(builder = Shell.ShellBuilder.class)
public class Shell {

    @Schema(implementation = String.class)
    private String identification;

    @Schema(implementation = String.class)
    private String idShort;

    @Schema()
    @Singular
    private Map<String, String> specificAssetIds;

    @Schema()
    @Singular
    private List<Description> descriptions;

    @Schema()
    @Singular
    private List<GlobalAssetIdentification> globalAssetIds;

    @Schema()
    @Singular
    private List<SubmodelDescriptor> submodelDescriptors;

    /**
     * User to build Shell
     */
    @Schema(description = "User to build shell items")
    @JsonPOJOBuilder(withPrefix = "")
    public static class ShellBuilder {
    }
}
