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

import java.util.Set;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * AssemblyPartRelationship
 */
@Data
@Jacksonized
class AssemblyPartRelationship {

    private String catenaXId;
    private Set<ChildData> childParts;

}
