//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.dto;

/**
 * Supported submodel types
 */
public enum SubmodelType {
    ASSEMBLY_PART_RELATIONSHIP("urn:bamm:com.catenax.assembly_part_relationship:1.0.0"),
    SERIAL_PART_TYPIZATION("urn:bamm:com.catenax.serial_part_typization:1.0.0");

    private final String value;

    SubmodelType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
