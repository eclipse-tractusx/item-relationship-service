//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.component.enums;

import lombok.Getter;

/**
 * Node Type Enum
 */
@Getter
public enum NodeType {
    ROOT("Root Node of the tree - the initial C-X ID"),
    NODE("Node of the tree with children - further AssemblyPartRelationShip aspects"),
    LEAF("Leaf node of the tree - No further AssemblyPartRelationShip aspects"),
    TOMBSTONE("Exceptional state - transient exception");

    private final String description;

    NodeType(final String description) {
        this.description = description;
    }
}
