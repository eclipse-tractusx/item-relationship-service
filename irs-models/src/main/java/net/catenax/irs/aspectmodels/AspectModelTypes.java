//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aspectmodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Enum for the different types of aspect models
 */
@ExcludeFromCodeCoverageGeneratedReport
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AspectModelTypes {
    SERIAL_PART_TYPIZATION("serialPartTypization"),
    ASSEMBLY_PART_RELATIONSHIP("assemblyPartRelationship");

    private final String value;

    AspectModelTypes(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
