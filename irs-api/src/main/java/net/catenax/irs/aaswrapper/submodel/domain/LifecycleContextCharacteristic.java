//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import lombok.Getter;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * LifecycleContextCharacteristic
 */
@Getter
@ExcludeFromCodeCoverageGeneratedReport
enum LifecycleContextCharacteristic {
    ASREQUIRED("AsRequired"),
    ASDESIGNED("AsDesigned"),
    ASPLANNED("AsPlanned"),
    ASBUILT("AsBuilt"),
    ASMAINTAINED("AsMaintained"),
    ASRECYCLED("AsRecycled");

    /**
     * value
     */
    private final String value;

    LifecycleContextCharacteristic(final String value) {
        this.value = value;
    }

}
