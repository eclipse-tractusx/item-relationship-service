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
import net.catenax.irs.aaswrapper.registry.domain.AasShellTombstone;
import net.catenax.irs.aaswrapper.submodel.domain.ItemRelationshipAspectTombstone;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;

/**
 * Container class to store item data
 */
@Getter
public class ItemContainer {

    private final List<AssemblyPartRelationshipDTO> assemblyPartRelationships = new ArrayList<>();

    private final List<ItemRelationshipAspectTombstone> itemRelationshipAspectTombstones = new ArrayList<>();

    private final List<AasShellTombstone> aasShellTombstones = new ArrayList<>();

    public void addRelationship(final AssemblyPartRelationshipDTO relationship) {
        assemblyPartRelationships.add(relationship);
    }

    public void addAllRelationships(final Collection<AssemblyPartRelationshipDTO> relationships) {
        assemblyPartRelationships.addAll(relationships);
    }

    public void addAspectTombstone(final ItemRelationshipAspectTombstone tombstone) {
        itemRelationshipAspectTombstones.add(tombstone);
    }

    public void addAllAspectTombstones(final Collection<ItemRelationshipAspectTombstone> tombstones) {
        this.itemRelationshipAspectTombstones.addAll(tombstones);
    }

    public void addShellTombstone(final AasShellTombstone tombstone) {
        aasShellTombstones.add(tombstone);
    }

    public void addAllShellTombstones(final Collection<AasShellTombstone> tombstones) {
        this.aasShellTombstones.addAll(tombstones);
    }
}
