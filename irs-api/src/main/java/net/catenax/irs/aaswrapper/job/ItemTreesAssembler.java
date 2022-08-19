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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.Bpn;
import net.catenax.irs.component.Submodel;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;

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
        final var relationships = new LinkedHashSet<AssemblyPartRelationshipDTO>();
        final var numberOfPartialTrees = new AtomicInteger();
        final ArrayList<Tombstone> tombstones = new ArrayList<>();
        final ArrayList<AssetAdministrationShellDescriptor> shells = new ArrayList<>();
        final ArrayList<Submodel> submodels = new ArrayList<>();
        final Set<Bpn> bpns = new HashSet<>();

        partialGraph.forEachOrdered(itemGraph -> {
            relationships.addAll(itemGraph.getAssemblyPartRelationships());
            numberOfPartialTrees.incrementAndGet();
            tombstones.addAll(itemGraph.getTombstones());
            shells.addAll(itemGraph.getShells());
            submodels.addAll(itemGraph.getSubmodels());
            bpns.addAll(itemGraph.getBpns());
        });

        log.info("Assembled item graph from {} partial graphs", numberOfPartialTrees);

        return ItemContainer.builder()
                            .assemblyPartRelationships(relationships)
                            .tombstones(tombstones)
                            .shells(shells)
                            .submodels(submodels)
                            .bpns(bpns)
                            .build();
    }
}
