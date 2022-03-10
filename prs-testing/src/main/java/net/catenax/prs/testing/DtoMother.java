//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.testing;

import com.github.javafaker.Faker;
import net.catenax.prs.dtos.Aspect;
import net.catenax.prs.dtos.PartId;
import net.catenax.prs.dtos.PartInfo;
import net.catenax.prs.dtos.PartRelationship;
import net.catenax.prs.dtos.PartRelationshipsWithInfos;

import java.util.List;

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
     * Generate a {@link PartRelationshipsWithInfos} containing random data.
     *
     * @return a {@link PartRelationshipsWithInfos} containing random dta.
     */
    public PartRelationshipsWithInfos partRelationshipsWithInfos() {
        return base.partRelationshipsWithInfos(List.of(partRelationship()), List.of(partInfo()));
    }

    /**
     * Generate a {@link PartRelationship} linking two random parts.
     *
     * @return a {@link PartRelationship} linking two random parts.
     */
    public PartRelationship partRelationship() {
        return base.partRelationship(partId(), partId());
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
     * Generate a {@link Aspect}
     * with random values for {@link Aspect#getName()}
     * and {@link Aspect#getUrl()}.
     * <p>
     * Example: {@code Aspect(name=nihil, url=https://www.lincoln-smith.co)}.
     *
     * @return a {@link Aspect} with random data.
     */
    public Aspect partAspect() {
        return base.partAspect(faker.lorem().word(), "https://" + faker.internet().url());
    }

    /**
     * Generate a {@link PartInfo} containing provided data.
     *
     * @param partId       part identifier.
     * @param partTypeName part type name.
     * @param aspectOrNull optional aspect to be included in the result. May be {@literal null}.
     * @return a {@link PartInfo} containing the provided {@code partId} and optionally {@code aspect}.
     */
    public PartInfo partInfo(final PartId partId, final String partTypeName, final Aspect aspectOrNull) {
        return base.partInfo(partId, partTypeName, aspectOrNull);
    }

    /**
     * Generate a {@link PartInfo} containing random data.
     *
     * @return a {@link PartInfo} containing random data.
     */
    public PartInfo partInfo() {
        return partInfo(partId(), faker.commerce().productName(), partAspect());
    }
}
