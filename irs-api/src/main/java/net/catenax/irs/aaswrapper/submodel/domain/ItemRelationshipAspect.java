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
import net.catenax.irs.dto.NodeType;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;

/**
 * Item Relationship Aspect Model
 */
@Getter
public class ItemRelationshipAspect extends AbstractItemRelationshipAspect {
    private final AssemblyPartRelationshipDTO assemblyPartRelationship;

    public ItemRelationshipAspect(final String catenaXId, final NodeType nodeType,
            final AssemblyPartRelationshipDTO assemblyPartRelationship) {
        super(catenaXId, nodeType);
        this.assemblyPartRelationship = assemblyPartRelationship;
    }
}
