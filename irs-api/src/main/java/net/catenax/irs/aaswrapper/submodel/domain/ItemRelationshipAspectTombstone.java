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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.ToString;
import net.catenax.irs.dto.NodeType;
import net.catenax.irs.dto.ProcessingError;

/**
 * Item Relationship Tombstone
 */
@Getter
@ToString
@JsonDeserialize(builder = ItemRelationshipAspectTombstone.ItemRelationshipAspectTombstoneBuilder.class)
public class ItemRelationshipAspectTombstone extends AbstractItemRelationshipAspect {
    private final ProcessingError processingError;
    private final String endpointURL;

    public ItemRelationshipAspectTombstone(final String catenaXId, final ProcessingError processingError,
            final String endpointURL) {
        super(catenaXId, NodeType.TOMBSTONE);
        this.processingError = processingError;
        this.endpointURL = endpointURL;
    }

    /**
     * Custom ItemRelationshipAspectTombstoneBuilder including the catenaXId
     */
    @JsonIgnoreProperties(value = "nodeType")
    public static class ItemRelationshipAspectTombstoneBuilder {
        String catenaXId;
        String endpointURL;
        ProcessingError processingError;

        public ItemRelationshipAspectTombstoneBuilder withCatenaXId(String catenaXId) {
            this.catenaXId = catenaXId;
            return this;
        }

        public ItemRelationshipAspectTombstoneBuilder withEndpointURL(String endpointURL) {
            this.endpointURL = endpointURL;
            return this;
        }

        public ItemRelationshipAspectTombstoneBuilder withProcessingError(ProcessingError processingError) {
            this.processingError = processingError;
            return this;
        }

        public ItemRelationshipAspectTombstone build() {
            return new ItemRelationshipAspectTombstone(catenaXId, processingError, endpointURL);
        }
    }
}
