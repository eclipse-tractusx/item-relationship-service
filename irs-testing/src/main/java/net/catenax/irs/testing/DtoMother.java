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

import com.github.javafaker.Faker;
import net.catenax.irs.component.*;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;
import net.catenax.irs.dtos.ChildItemInfo;
import net.catenax.irs.dtos.PartId;
import net.catenax.irs.dtos.PartRelationship;

import java.util.Collection;

/**
 * Object Mother to generate fake DTO data for testing.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
public class DtoMother {
    /**
     * JavaFaker instance used to generate random data.
     */
    private final transient Faker faker = new Faker();

    /**
     * Base mother object that generates test data.
     */
    private final transient BaseDtoMother base = new BaseDtoMother();

    /**
     * Generate a {@link PartRelationship} linking two random parts.
     *
     * @return a {@link PartRelationship} linking two random parts.
     */
    public Relationship relationship() {
        return base.relationship(job(), job());
    }

    /**
     * Generate a {@link PartId}
     * with random values for {@link PartId#getOneIDManufacturer()}
     * and {@link PartId#getObjectIDManufacturer()}.
     * <p>
     * Example: {@code PartId(oneIDManufacturer=Stiedemann Inc, objectIDManufacturer=ypiu9wzwuka1ov03)}.
     *
     * @return a {@link PartId} with random identifiers.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public PartId partId() {
        return base.partId(faker.company().name(), faker.lorem().characters(10, 20));
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
        return base.job(job, summary, queryParameter);
    }

    public Summary summary(final AsyncFetchedItems asyncFetchedItems) {
        return base.summary(asyncFetchedItems);
    }

    public QueryParameter queryParameter(final BomLifecycle bomLifecycle, final Collection<AspectType> aspects, final Integer depth, final Direction direction) {
        return base.queryParameter(bomLifecycle, aspects, depth, direction);
    }

    /**
     * Generate a {@link ChildItemInfo} containing random data.
     *
     * @return a {@link ChildItemInfo} containing random data.
     */
    public Job job() {
        return job();
    }
}
