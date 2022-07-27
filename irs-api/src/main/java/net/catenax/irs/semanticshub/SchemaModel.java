//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.semanticshub;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Common Schema released models
 */
@AllArgsConstructor
@Getter
enum SchemaModel {
//    AssemblyPartRelationship("urn:bamm:com.catenax.assembly_part_relationship:1.0.0#AssemblyPartRelationship"), FIXME currently returning 500 status
    SerialPartTypization("urn:bamm:io.catenax.serial_part_typization:1.0.0#SerialPartTypization"),
    ;

    private final String urn;

}
