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
import lombok.ToString;
import net.catenax.irs.dto.NodeType;
import net.catenax.irs.dto.ProcessingError;

/**
 * Item Relationship Tombstone
 */
@Getter
@ToString
public class ItemRelationshipAspectTombstone extends AbstractItemRelationshipAspect {
    private final ProcessingError processingError;
    private final String endpointURL;

    public ItemRelationshipAspectTombstone(final String catenaXId, final ProcessingError processingError,
            final String endpointURL) {
        super(catenaXId, NodeType.TOMBSTONE);
        this.processingError = processingError;
        this.endpointURL = endpointURL;
    }
}
