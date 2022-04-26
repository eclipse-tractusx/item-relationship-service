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
import lombok.ToString;
import net.catenax.irs.dto.NodeType;

/**
 * Abstract Item Relationship Aspect Model
 */
@AllArgsConstructor
@ToString
@Getter
@SuppressWarnings("PMD.AbstractClassWithoutAnyMethod")
public abstract class AbstractItemRelationshipAspect implements Aspect {

    private final String catenaXId;
    private final NodeType nodeType;
}
