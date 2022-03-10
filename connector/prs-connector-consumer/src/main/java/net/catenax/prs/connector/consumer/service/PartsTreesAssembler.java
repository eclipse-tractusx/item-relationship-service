//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.consumer.service;


import lombok.RequiredArgsConstructor;
import net.catenax.prs.client.model.PartInfo;
import net.catenax.prs.client.model.PartRelationship;
import net.catenax.prs.client.model.PartRelationshipsWithInfos;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Assembles multiple partial parts trees into one overall parts tree.
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement") // Monitor doesn't offer guard statements
public class PartsTreesAssembler {

    /**
     * Logger.
     */
    private final Monitor monitor;

    /**
     * Assembles multiple partial parts trees into one overall parts tree.
     *
     * @param partialTrees partial parts trees.
     * @return A parts tree containing all the items from {@code partialTrees}, with deduplication.
     */
    /* package */ PartRelationshipsWithInfos retrievePartsTrees(final Stream<PartRelationshipsWithInfos> partialTrees) {
        final var relationships = new LinkedHashSet<PartRelationship>();
        final var partInfos = new LinkedHashSet<PartInfo>();
        final var numberOfPartialTrees = new AtomicInteger();

        partialTrees.forEachOrdered(partialTree -> {
            relationships.addAll(partialTree.getRelationships());
            partInfos.addAll(partialTree.getPartInfos());
            numberOfPartialTrees.incrementAndGet();
        });

        monitor.info(format("Assembled parts tree from %s partial trees", numberOfPartialTrees));

        final var result = new PartRelationshipsWithInfos();
        result.setRelationships(new ArrayList<>(relationships));
        result.setPartInfos(new ArrayList<>(partInfos));
        return result;
    }
}
