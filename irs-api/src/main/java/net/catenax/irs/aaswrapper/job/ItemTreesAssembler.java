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

import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.registry.domain.AasShellTombstone;
import net.catenax.irs.aaswrapper.submodel.domain.ItemRelationshipAspectTombstone;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;

/**
 * Assembles multiple partial parts trees into one overall parts tree.
 */
@Slf4j
@RequiredArgsConstructor
public class ItemTreesAssembler {

    /**
     * Assembles multiple partial parts trees into one overall parts tree.
     *
     * @param partialTrees partial parts trees.
     * @return A parts tree containing all the items from {@code partialTrees}, with deduplication.
     */
    /* package */ ItemContainer retrievePartsTrees(final Stream<ItemContainer> partialTrees) {
        final var relationships = new LinkedHashSet<AssemblyPartRelationshipDTO>();
        final var numberOfPartialTrees = new AtomicInteger();
        final LinkedHashSet<ItemRelationshipAspectTombstone> aspectTombstones = new LinkedHashSet<>();
        final LinkedHashSet<AasShellTombstone> shellTombstones = new LinkedHashSet<>();

        partialTrees.forEachOrdered(partialTree -> {
            relationships.addAll(partialTree.getAssemblyPartRelationships());
            numberOfPartialTrees.incrementAndGet();
            aspectTombstones.addAll(partialTree.getItemRelationshipAspectTombstones());
            shellTombstones.addAll(partialTree.getAasShellTombstones());
        });

        log.info("Assembled item tree from {} partial trees", numberOfPartialTrees);

        final var result = new ItemContainer();
        result.addAllRelationships(relationships);
        result.addAllAspectTombstones(aspectTombstones);
        result.addAllShellTombstones(shellTombstones);
        return result;
    }
}
