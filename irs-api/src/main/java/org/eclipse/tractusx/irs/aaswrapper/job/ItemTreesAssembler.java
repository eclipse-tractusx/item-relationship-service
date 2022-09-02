//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.aaswrapper.job;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.Bpn;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;

/**
 * Assembles multiple partial item graphs into one overall item graph.
 */
@Slf4j
@RequiredArgsConstructor
public class ItemTreesAssembler {

    /**
     * Assembles multiple partial item graphs into one overall item graph.
     *
     * @param partialGraph partial item graph.
     * @return An item graph containing all the items from {@code partialGraph}, with deduplication.
     */
    /* package */ ItemContainer retrieveItemGraph(final Stream<ItemContainer> partialGraph) {
        final var relationships = new LinkedHashSet<Relationship>();
        final var numberOfPartialTrees = new AtomicInteger();
        final ArrayList<Tombstone> tombstones = new ArrayList<>();
        final ArrayList<AssetAdministrationShellDescriptor> shells = new ArrayList<>();
        final ArrayList<Submodel> submodels = new ArrayList<>();
        final Set<Bpn> bpns = new HashSet<>();

        partialGraph.forEachOrdered(itemGraph -> {
            relationships.addAll(itemGraph.getRelationships());
            numberOfPartialTrees.incrementAndGet();
            tombstones.addAll(itemGraph.getTombstones());
            shells.addAll(itemGraph.getShells());
            submodels.addAll(itemGraph.getSubmodels());
            bpns.addAll(itemGraph.getBpns());
        });

        log.info("Assembled item graph from {} partial graphs", numberOfPartialTrees);

        return ItemContainer.builder()
                            .relationships(relationships)
                            .tombstones(tombstones)
                            .shells(shells)
                            .submodels(submodels)
                            .bpns(bpns)
                            .build();
    }
}
