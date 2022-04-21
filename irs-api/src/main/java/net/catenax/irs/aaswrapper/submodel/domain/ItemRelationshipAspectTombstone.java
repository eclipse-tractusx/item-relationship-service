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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.dto.NodeType;
import net.catenax.irs.dto.ProcessingError;

/**
 * Item Relationship Tombstone
 */
@Getter
@ToString
@JsonDeserialize(builder = ItemRelationshipAspectTombstone.ItemRelationshipAspectTombstoneBuilder.class)
public class ItemRelationshipAspectTombstone extends AbstractItemRelationshipAspect implements Tombstone {
    private final ProcessingError processingError;
    private final String endpointURL;

    @Builder(setterPrefix = "with")
    public ItemRelationshipAspectTombstone(final String catenaXId, final ProcessingError processingError,
            final String endpointURL) {
        super(catenaXId, NodeType.TOMBSTONE);
        this.processingError = processingError;
        this.endpointURL = endpointURL;
    }

    @Builder(setterPrefix = "with")
    private ItemRelationshipAspectTombstone(final String catenaXId, final ProcessingError processingError,
            final String endpointURL, final NodeType nodeType) {
        super(catenaXId, nodeType);
        this.processingError = processingError;
        this.endpointURL = endpointURL;
    }
}
