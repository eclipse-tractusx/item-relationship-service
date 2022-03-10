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
import net.catenax.prs.dtos.PartAttribute;
import net.catenax.prs.dtos.PartLifecycleStage;
import net.catenax.prs.dtos.events.PartAspectsUpdateRequest;
import net.catenax.prs.dtos.events.PartAttributeUpdateRequest;
import net.catenax.prs.dtos.events.PartRelationshipUpdate;
import net.catenax.prs.dtos.events.PartRelationshipsUpdateRequest;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.DAYS;

/**
 * Object Mother to generate fake DTO data for testing.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class UpdateRequestMother {
    /**
     * JavaFaker instance used to generate random data.
     */
    private final transient Faker faker = new Faker();
    /**
     * Object Mother to generate core DTO data for testing.
     */
    private final transient DtoMother generate = new DtoMother();

    /**
     * Generate a {@link PartRelationshipsUpdateRequest} containing a single relationship update
     * with random data.
     *
     * @return never returns {@literal null}.
     */
    public PartRelationshipsUpdateRequest partRelationshipUpdateList() {
        return partRelationshipUpdateList(partRelationshipUpdate());
    }

    /**
     * Generate a {@link PartRelationshipsUpdateRequest} containing provided relationship updates.
     *
     * @param relationships part relationships to include in the generated request.
     * @return never returns {@literal null}.
     */
    public PartRelationshipsUpdateRequest partRelationshipUpdateList(final PartRelationshipUpdate... relationships) {
        return PartRelationshipsUpdateRequest.builder()
                .withRelationships(Arrays.asList(relationships))
                .build();
    }

    /**
     * Generate a {@link PartRelationshipUpdate} containing random data,
     * with an effect time in the past.
     *
     * @return never returns {@literal null}.
     */
    public PartRelationshipUpdate partRelationshipUpdate() {
        return PartRelationshipUpdate.builder()
                .withRelationship(generate.partRelationship())
                .withRemove(false)
                .withStage(faker.options().option(PartLifecycleStage.class))
                .withEffectTime(faker.date().past(100, DAYS).toInstant())
                .build();
    }

    /**
     * Generate a {@link PartAspectsUpdateRequest} containing random data.
     *
     * @return never returns {@literal null}.
     */
    public PartAspectsUpdateRequest partAspectUpdate() {
        return PartAspectsUpdateRequest.builder()
                .withPart(generate.partId())
                .withAspects(singletonList(generate.partAspect()))
                .withRemove(false)
                .withEffectTime(faker.date().past(100, DAYS).toInstant())
                .build();
    }

    /**
     * Generate a {@link PartAttributeUpdateRequest} containing random data.
     *
     * @return never returns {@literal null}.
     */
    public PartAttributeUpdateRequest partAttributeUpdate() {
        return PartAttributeUpdateRequest.builder()
                .withPart(generate.partId())
                .withName(faker.options().option(PartAttribute.class).name())
                .withValue(faker.commerce().productName())
                .withEffectTime(faker.date().past(100, DAYS).toInstant())
                .build();
    }
}
