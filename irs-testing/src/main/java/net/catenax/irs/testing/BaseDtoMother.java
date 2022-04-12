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
import net.catenax.irs.dtos.*;

import java.util.List;
import java.util.UUID;

/**
 * Base object mother class to create DTOs for testing.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
public class BaseDtoMother {

    /**
     * Generate a {@link PartRelationshipsWithInfos} containing provided data.
     *
     * @param relationships list of {@link PartRelationship}.
     * @param job     list of {@link ChildItemInfo}.
     * @return a {@link PartRelationshipsWithInfos} containing
     * the provided {@code relationships} and {@code partInfos}.
     */
    public Jobs jobs(final List<Relationship> relationships, final Job job) {
        return Jobs.builder()
                .relationships(relationships)
                .job(job)
                .build();
    }

    /**
     * Generate a {@link PartRelationship} linking two parts.
     *
     * @param childItem  child in the relationship.
     * @param parentItem  child in the relationship.
     * @return a {@link PartRelationship} linking {@code parentId} to {@code childItem}.
     */
    public Relationship relationship(final Job childItem, final Job parentItem) {
        return Relationship.builder()
                .childItem(childItem)
                .parentItem(parentItem)
                .build();
    }

    /**
     * Generate a {@link PartId} from oneId and objectId.
     *
     * @param jobId one id of the manufacturer.
     * @param action part serial number.
     * <p>
     * Example: {@code PartId(oneIDManufacturer=Stiedemann Inc, objectId=ypiu9wzwuka1ov03)}.
     *
     * @return a {@link Job} with random identifiers.
     */
    public Job job(final UUID jobId, final String action) {
        return Job.builder()
                .jobId(jobId)
                .action(action)
                .build();
    }

    public Summary summary(final AsyncFetchedItems asyncFetchedItems) {
        return Summary.builder()
                .asyncFetchedItems(asyncFetchedItems)
                .build();
    }

    public QueryParameter queryParameter(final BomLifecycle bomLifecycle, final AspectType aspect, final Integer depth, final Direction direction) {
        return QueryParameter.builder()
                .bomLifecycle(bomLifecycle)
                .aspect(aspect)
                .depth(depth)
                .direction(direction)
                .build();
    }

    /**
     * Generate a {@link PartId} from oneId and objectId.
     *
     * @param oneId one id of the manufacturer.
     * @param objectId part serial number.
     * <p>
     * Example: {@code PartId(oneIDManufacturer=Stiedemann Inc, objectId=ypiu9wzwuka1ov03)}.
     *
     * @return a {@link PartId} with random identifiers.
     */
    public PartId partId(final String oneId, final String objectId) {
        return PartId.builder()
                .withOneIDManufacturer(oneId)
                .withObjectIDManufacturer(objectId)
                .build();
    }

    public AsyncFetchedItems asynchFetchedItems(final Integer queue, final Integer running, final Integer complete, final Integer failed, final Integer lost) {
        return AsyncFetchedItems.builder()
                .queue(queue)
                .running(running)
                .complete(complete)
                .failed(failed)
                .lost(lost)
                .build();
    }

    /**
     * Generate a {@link Aspect} from aspect name and url.
     *
     * @param aspectName name of the aspect.
     * @param url url location of the aspect.
     *
     * Example: {@code Aspect(name=nihil, url=www.lincoln-smith.co)}.
     *
     * @return a {@link Aspect} with random data.
     */
    public Aspect partAspect(final String aspectName, final String url) {
        return Aspect.builder()
                .withName(aspectName)
                .withUrl(url)
                .build();
    }

    /**
     * Generate a {@link ChildItemInfo} containing provided data.
     *
     * @param job       part identifier.
     * @param summary part type name.
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
}
