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

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.component.enums.NodeType;

/**
 * Item Relationship Aspect Model
 */
@Getter
@AllArgsConstructor
public class ItemRelationshipAspect {
    private final String catenaXId;
    private final NodeType nodeType;
    private final AssemblyPartRelationshipDTO assemblyPartRelationship;
}
