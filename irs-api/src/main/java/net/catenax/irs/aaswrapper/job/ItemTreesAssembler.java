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
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.Tombstone;
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

        partialGraph.forEachOrdered(itemGraph -> {
            relationships.addAll(itemGraph.getAssemblyPartRelationships());
            numberOfPartialTrees.incrementAndGet();
            tombstones.addAll(itemGraph.getTombstones());
        });

        log.info("Assembled item graph from {} partial graphs", numberOfPartialTrees);

        return ItemContainer.builder().assemblyPartRelationships(relationships).tombstones(tombstones).build();
    }
}
