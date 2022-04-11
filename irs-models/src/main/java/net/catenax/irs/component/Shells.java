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
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Collections of AAS shells
 */
@Value
@Builder(toBuilder = true)
@ExcludeFromCodeCoverageGeneratedReport
@JsonDeserialize(builder = Shells.ShellsBuilder.class)
@AllArgsConstructor
@SuppressWarnings("PMD.AvoidFieldNameMatchingTypeName")
public class Shells {


    /**
     * Shells
     */
    @Schema(description = "Collections of AAS shells")
    @Singular
    private List<Shell> shells;

    /**
     * User to build Shells
     */
    @Schema(description = "User to build shells items")
    @JsonPOJOBuilder(withPrefix = "with")
    public static class ShellsBuilder {
    }

}
