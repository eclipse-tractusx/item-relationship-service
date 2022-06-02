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
 * Assembles multiple partial parts trees into one overall parts tree.
 */
@Slf4j
@RequiredArgsConstructor
public class ItemTreesAssembler {

    /**
     * Assembles multiple partial item trees into one overall item tree.
     *
     * @param itemTrees partial item trees.
     * @return An item tree containing all the items from {@code itemTrees}, with deduplication.
     */
    /* package */ ItemContainer retrieveItemTrees(final Stream<ItemContainer> itemTrees) {
        final var relationships = new LinkedHashSet<AssemblyPartRelationshipDTO>();
        final var numberOfPartialTrees = new AtomicInteger();
        final ArrayList<Tombstone> tombstones = new ArrayList<>();

        itemTrees.forEachOrdered(itemTree -> {
            relationships.addAll(itemTree.getAssemblyPartRelationships());
            numberOfPartialTrees.incrementAndGet();
            tombstones.addAll(itemTree.getTombstones());
        });

        log.info("Assembled item tree from {} partial trees", numberOfPartialTrees);

        return ItemContainer.builder().assemblyPartRelationships(relationships).tombstones(tombstones).build();
    }
}
