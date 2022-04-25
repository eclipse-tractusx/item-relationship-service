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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import net.catenax.irs.component.AsyncFetchedItems;
import net.catenax.irs.component.ChildItem;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.MeasurementUnit;
import net.catenax.irs.component.Quantity;
import net.catenax.irs.component.QueryParameter;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.Summary;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;

/**
 * Base object mother class to create DTOs for testing.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
public class BaseDtoMother {

    private static final Instant EXAMPLE_INSTANT = Instant.parse("2022-02-03T14:48:54.709Z");

    /**
     * Generate a {@link Jobs} containing provided data.
     *
     * @param relationships list of {@link Relationship}.
     * @param job           list of {@link Job}.
     * @return a {@link Jobs} containing
     * the provided {@code relationships} and {@code partInfos}.
     */
    public Jobs jobs(final List<Relationship> relationships, final Job job) {
        return Jobs.builder().relationships(relationships).job(job).build();
    }

    /**
     * Generate a {@link Relationship} linking two parts.
     *
     * @param childItem  child in the relationship.
     * @param parentItem child in the relationship.
     * @return a {@link Relationship} linking {@code parentId} to {@code childItem}.
     */
    public Relationship relationship(final ChildItem childItem, final ChildItem parentItem) {
        return Relationship.builder().childItem(childItem).parentItem(parentItem).build();
    }

    /**
     * Generate a {@link Job} from oneId and objectId.
     *
     * @param jobId  one id of the manufacturer.
     * @param action part serial number.
     *               <p>
     *               Example: {@code Job(oneIDManufacturer=Stiedemann Inc, objectId=ypiu9wzwuka1ov03)}.
     * @return a {@link Job} with random identifiers.
     */
    public Job job(final UUID jobId, final String action) {
        return Job.builder().jobId(jobId).action(action).build();
    }

    public Summary summary(final AsyncFetchedItems asyncFetchedItems) {
        return Summary.builder().asyncFetchedItems(asyncFetchedItems).build();
    }

    public QueryParameter queryParameter(final BomLifecycle bomLifecycle, final AspectType aspect, final Integer depth,
            final Direction direction) {
        return QueryParameter.builder()
                             .bomLifecycle(bomLifecycle)
                             .aspect(aspect)
                             .depth(depth)
                             .direction(direction)
                             .build();
    }

    public AsyncFetchedItems asynchFetchedItems(final Integer queue, final Integer running, final Integer complete,
            final Integer failed) {
        return AsyncFetchedItems.builder().queue(queue).running(running).complete(complete).failed(failed).build();
    }

    /**
     * Generate a {@link Job} containing provided data.
     *
     * @param job            part identifier.
     * @param summary        part type name.
     * @param queryParameter optional aspect to be included in the result. May be {@literal null}.
     * @return a {@link Job}.
     */
    public Job job(final Job job, final Summary summary, final QueryParameter queryParameter) {
        return Job.builder()
                  .jobId(job.getJobId())
                  .globalAssetId(job.getGlobalAssetId())
                  .jobState(job.getJobState())
                  .summary(summary)
                  .queryParameter(queryParameter)
                  .build();
    }

    public ChildItem childItem(final String childCatenaxId, final int quantity) {
        return ChildItem.builder()
                        .quantity(createQuantity(quantity))
                        .childCatenaXId(GlobalAssetIdentification.builder().globalAssetId(childCatenaxId).build())
                        .lastModifiedOn(EXAMPLE_INSTANT)
                        .assembledOn(EXAMPLE_INSTANT)
                        .lifecycleContext(BomLifecycle.AS_BUILT)
                        .build();

    }

    private Quantity createQuantity(final int quantity) {
        return Quantity.builder()
                       .quantityNumber(quantity)
                       .measurementUnit(MeasurementUnit.builder().lexicalValue("pc").build())
                       .build();
    }
}
