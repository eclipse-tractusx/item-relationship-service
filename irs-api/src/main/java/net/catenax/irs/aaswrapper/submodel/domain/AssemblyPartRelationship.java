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
import net.catenax.irs.aaswrapper.Aspect;

/**
 * AssemblyPartRelationship
 */
@Data
class AssemblyPartRelationship implements Aspect {

    /**
     * catenaXId
     */
    private String catenaXId;

    /**
     * childParts
     */
    private Set<ChildData> childParts;

    @Override
    public Aspect getAspect() {
        return null;
    }
}
