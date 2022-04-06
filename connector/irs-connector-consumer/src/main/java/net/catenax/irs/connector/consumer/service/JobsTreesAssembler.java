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
import net.catenax.irs.dtos.version02.Job;
import net.catenax.irs.dtos.version02.Jobs;
import net.catenax.irs.dtos.version02.Relationship;
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
@SuppressWarnings("PMD.GuardLogStatement") // Monitor doesn't offer guard statements
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
            relationships[0] = partialJob.getRelationship();
            shells[0] = Optional.of(partialJob.getShells().get());
            numberOfPartialJobs.incrementAndGet();
        });

        monitor.info(format("Assembled parts tree from %s partial trees", numberOfPartialJobs));

        var result = new Jobs(null, null, null);
        result.toBuilder().withJob(job[0]);
        result.toBuilder().withRelationship(relationships[0]);
        result.toBuilder().withShells(shells[0]);
        return result;
    }
}
