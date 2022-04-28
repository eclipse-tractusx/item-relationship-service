//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
//
package net.catenax.irs.aaswrapper.submodel.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * LifecycleContextCharacteristic
 */
@Getter
@ExcludeFromCodeCoverageGeneratedReport
enum LifecycleContextCharacteristic {
    ASREQUIRED("AsRequired"),
    /**
     * Build up the initial BoM in design phase of a new automotive product including
     * alternative partsExpected to have reserach & development part descriptions
     * instead of specific part numbers
     */
    ASDESIGNED("AsDesigned"),
    /**
     * BoM as it is used to plan manufacturing including alternative parts
     * Sourcing will most likely be based on this (besides key parts
     * which start earlier)
     */
    ASPLANNED("AsPlanned"),
    /**
     * BoM as a component is built or manufacturedDuring manufactoring of a
     * vehicle the serial numbers & batch numbers are documented (German: Verbaudokumentation)
     * This leads to one BOM per build car
     */
    ASBUILT("AsBuilt"),
    /**
     * BoM AsMaintained describes the BoM after purchase by a
     * customer and updates through maintenace.
     */
    ASMAINTAINED("AsMaintained"),
    /**
     * BoM AsRecycled describes the BoM after the recycling of the product.
     */
    ASRECYCLED("AsRecycled");

    /**
     * value
     */
    @JsonValue
    private final String value;

    LifecycleContextCharacteristic(final String value) {
        this.value = value;
    }

}
