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

import java.util.Collection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Collections of AAS shells
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
@ExcludeFromCodeCoverageGeneratedReport
@SuppressWarnings("PMD.AvoidFieldNameMatchingTypeName")
public class Shells {


    @Schema(description = "Collections of AAS shells")
    /**
     * Shells
     */
    private Collection<Shell> shells;
}
