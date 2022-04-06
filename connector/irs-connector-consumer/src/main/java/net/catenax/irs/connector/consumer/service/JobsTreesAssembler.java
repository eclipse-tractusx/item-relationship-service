//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.consumer.service;


import lombok.RequiredArgsConstructor;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.Shells;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Assembles multiple partial parts trees into one overall parts tree.
 */
@RequiredArgsConstructor
@SuppressWarnings({ "PMD.UseShortArrayInitializer" })
public class JobsTreesAssembler {

    /**
     * Logger.
     */
    private final Monitor monitor;

    /**
     * Assembles multiple partial parts trees into one overall parts tree.
     *
     * @param partialJobs partial jobs trees.
     * @return A parts tree containing all the items from {@code partialTrees}, with deduplication.
     */
    /* package */ Jobs retrieveJobsTrees(final Stream<Jobs> partialJobs) {
        final Job[] job = {null};
        final Relationship[] relationships = {new Relationship(null, null, null)};
        final Optional<List<Shells>>[] shells = new Optional[]{Optional.empty()};
        final var numberOfPartialJobs = new AtomicInteger();

        partialJobs.forEachOrdered(partialJob -> {
            job[0] = partialJob.getJob();
            // TODO (mschlach): provide solution for a list of relationships
            relationships[0] = partialJob.getRelationships().get(0);
            shells[0] = Optional.of(partialJob.getShells().get());
            numberOfPartialJobs.incrementAndGet();
        });

        monitor.info(format("Assembled parts tree from %s partial trees", numberOfPartialJobs));

        final var result = new Jobs(null, null, null);
        result.toBuilder().job(job[0]);
        result.toBuilder().relationship(relationships[0]);
        result.toBuilder().shells(shells[0]);
        return result;
    }
}
