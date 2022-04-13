//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper;

import lombok.Getter;
import lombok.ToString;

/**
 * Item Relationship Tombstone
 */
@Getter
@ToString
public class ItemRelationshipAspectTombstone extends AbstractItemRelationshipAspect {
    private final ProcessingError processingError;

    public ItemRelationshipAspectTombstone(final String catenaXId, final ProcessingError processingError) {
        super(catenaXId, NodeType.TOMBSTONE);
        this.processingError = processingError;
    }
}
