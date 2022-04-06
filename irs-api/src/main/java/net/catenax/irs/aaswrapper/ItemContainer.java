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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import net.catenax.irs.aspectmodels.serialparttypization.SerialPartTypization;

/**
 * Container class to store item data
 */
@Getter
public class ItemContainer {

    private final List<SerialPartTypization> serialPartTypizations = new ArrayList<>();

    private final List<AssemblyPartRelationship> assemblyPartRelationships = new ArrayList<>();

    public void add(final SerialPartTypization typization, final AssemblyPartRelationship relationship) {
        serialPartTypizations.add(typization);
        assemblyPartRelationships.add(relationship);
    }

    public void addAll(final Collection<SerialPartTypization> typizations,
            final Collection<AssemblyPartRelationship> relationships) {
        serialPartTypizations.addAll(typizations);
        assemblyPartRelationships.addAll(relationships);
    }
}
