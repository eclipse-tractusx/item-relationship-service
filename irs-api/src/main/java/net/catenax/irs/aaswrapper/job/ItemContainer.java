//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import net.catenax.irs.aaswrapper.submodel.domain.ItemRelationshipAspectTombstone;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;

/**
 * Container class to store item data
 */
@Getter
public class ItemContainer {

    private final List<AssemblyPartRelationshipDTO> assemblyPartRelationships = new ArrayList<>();

    private final List<ItemRelationshipAspectTombstone> itemRelationshipAspectTombstones = new ArrayList<>();

    public void add(final AssemblyPartRelationshipDTO relationship) {
        assemblyPartRelationships.add(relationship);
    }

    public void addAll(final Collection<AssemblyPartRelationshipDTO> relationships) {
        assemblyPartRelationships.addAll(relationships);
    }

    public void addItemRelationshipAspectTombstone(final ItemRelationshipAspectTombstone tombstone) {
        itemRelationshipAspectTombstones.add(tombstone);
    }
}
