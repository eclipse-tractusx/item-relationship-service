//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.testing;

import net.catenax.irs.component.*;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;

import java.util.UUID;


/**
 * Object Mother to generate fake DTO data for testing.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
public class DtoMother {

    private static final BomLifecycle GEARBOXBOMLIFECYCLE = BomLifecycle.AS_BUILT;
    private static final AspectType GEARBOXASPECT = AspectType.MATERIAL_ASPECT;
    private static final Integer GEARBOXDEPTH = 6;
    private static final Direction GEARBOXDIRECTION = Direction.DOWNWARD;
    private static final Integer GEARBOXQUEUE = 1;
    private static final Integer GEARBOXRUNNING = 2;
    private static final Integer GEARBOXCOMPLETE = 3;
    private static final Integer GEARBOXFAILED = 4;
    private static final Integer GEARBOXLOST = 5;

    private final Job car2 = generateJob(UUID.randomUUID(), UUID.randomUUID().toString());
    private final AsyncFetchedItems gearboxAsynchFetchedItems = getGearboxAsynchFetchedItems(GEARBOXQUEUE, GEARBOXRUNNING, GEARBOXCOMPLETE, GEARBOXFAILED, GEARBOXLOST);
    private final Summary gearboxSummary = getGearboxSummary(gearboxAsynchFetchedItems);
    private final QueryParameter gearboxQueryParameter = getGearboxQueryParameter(GEARBOXBOMLIFECYCLE, GEARBOXASPECT, GEARBOXDEPTH, GEARBOXDIRECTION);
    //Job car1 = job(car2, gearboxSummary, gearboxQueryParameter);


    /**
     * Base mother object that generates test data.
     */
    private final transient BaseDtoMother base = new BaseDtoMother();

    /**
     * Generate a {@link Relationship} linking two random parts.
     *
     * @return a {@link Relationship} linking two random parts.
     */
    public Relationship relationship() {
        return base.relationship(job(car2, gearboxSummary, gearboxQueryParameter), job(car2, gearboxSummary, gearboxQueryParameter));
    }

    /**
     * Generate a {@link Job} containing provided data.
     *
     * @param job       part identifier.
     * @param summary part type name.
     * @param queryParameter optional aspect to be included in the result. May be {@literal null}.
     * @return a {@link Job}.
     */
    public Job job(final Job job, final Summary summary, final QueryParameter queryParameter) {
        return base.job(job, summary, queryParameter);
    }

    public Summary summary(final AsyncFetchedItems asyncFetchedItems) {
        return base.summary(asyncFetchedItems);
    }

    public QueryParameter queryParameter(final BomLifecycle bomLifecycle, final AspectType aspects, final Integer depth, final Direction direction) {
        return base.queryParameter(bomLifecycle, aspects, depth, direction);
    }

    /**
     * Generate a {@link Job} containing random data.
     *
     * @return a {@link Job} containing random data.
     */
    public Job job() {
        return job();
    }

    public Summary getGearboxSummary(final AsyncFetchedItems asyncFetchedItems) {
        return Summary.builder()
                .asyncFetchedItems(asyncFetchedItems)
                .build();
    }

    public QueryParameter getGearboxQueryParameter(final BomLifecycle bomLifecycle, final AspectType aspect, final Integer depth, final Direction direction) {
        return QueryParameter.builder()
                .bomLifecycle(bomLifecycle)
                .aspect(aspect)
                .depth(depth)
                .direction(direction)
                .build();
    }

    public AsyncFetchedItems getGearboxAsynchFetchedItems(final Integer queue, final Integer running, final Integer complete, final Integer failed, final Integer lost) {
        return AsyncFetchedItems.builder()
                .queue(queue)
                .running(running)
                .complete(complete)
                .failed(failed)
                .lost(lost)
                .build();
    }

    public Job generateJob(final UUID jobId, final String action) {
        return Job.builder()
                .jobId(jobId)
                .action(action)
                .build();
    }
}
