package net.catenax.prs.entities;

import com.github.javafaker.Faker;
import net.catenax.prs.configuration.PrsConfiguration;
import net.catenax.prs.dtos.PartLifecycleStage;

import java.util.concurrent.TimeUnit;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;

/**
 * Object Mother to generate fake domain data for testing.
 * <p>
 * Static methods are not used so that state can be later introduced to generate more complex scenarios.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">https://martinfowler.com/bliki/ObjectMother.html</a>
 */
public class EntitiesMother {
    /**
     * JavaFaker instance used to generate random data.
     */
    private final Faker faker = new Faker();

    /**
     * Generate a {@link PartRelationshipEntity} linking two parts,
     * with a {@link PartRelationshipEntity#getUploadDateTime()} equal to the current time
     * and a random {@link PartRelationshipEntity#getPartRelationshipListId()}.
     *
     * @param parentId parent in the relationship.
     * @param childId  child in the relationship.
     * @return a {@link PartRelationshipEntity} linking {@code parentId} to {@code childId}.
     */
    public PartRelationshipEntity partRelationship(PartIdEntityPart parentId, PartIdEntityPart childId) {
        return PartRelationshipEntity.builder()
                .key(partRelationshipKey(parentId, childId))
                .uploadDateTime(now())
                .partRelationshipListId(randomUUID())
                .build();
    }

    public PartRelationshipEntity partRelationship() {
        return partRelationship(partId(), partId());
    }

    /**
     * Generate a {@link PartRelationshipEntityKey} linking two parts,
     * with a random {@link PartRelationshipEntity#getPartRelationshipListId()}.
     *
     * @param parentId parent in the relationship.
     * @param childId  child in the relationship.
     * @return a {@link PartRelationshipEntityKey} linking {@code parentId} to {@code childId}.
     */
    public PartRelationshipEntityKey partRelationshipKey(PartIdEntityPart parentId, PartIdEntityPart childId) {
        return PartRelationshipEntityKey.builder()
                .childId(childId)
                .parentId(parentId)
                .effectTime(faker.date().past(faker.number().randomDigitNotZero(), TimeUnit.DAYS).toInstant())
                .lifeCycleStage(PartLifecycleStage.BUILD)
                .removed(false)
                .build();
    }

    /**
     * Generate a {@link PartIdEntityPart} linking two parts,
     * with random values for {@link PartIdEntityPart#getOneIDManufacturer()}
     * and {@link PartIdEntityPart#getObjectIDManufacturer()}.
     * <p>
     * Example: {@code PartIdEntityPart(oneIDManufacturer=Stiedemann Inc, objectIDManufacturer=ypiu9wzwuka1ov03)}.
     *
     * @return a {@link PartIdEntityPart} with random identifiers.
     */
    public PartIdEntityPart partId() {
        return PartIdEntityPart.builder()
                .oneIDManufacturer(faker.company().name())
                .objectIDManufacturer(faker.lorem().characters(10, 20))
                .build();
    }

    public PartAspectEntity partAspect(PartIdEntityPart id) {
        return PartAspectEntity.builder()
                .key(partAspectEntityKey(id, faker.lorem().word()))
                .url(faker.internet().url())
                .build();
    }

    public PartAttributeEntity partTypeNameAttribute(PartIdEntityPart id) {
        return PartAttributeEntity.builder()
                .key(partAttributeEntityKey(id, PrsConfiguration.PART_TYPE_NAME_ATTRIBUTE))
                .value(faker.commerce().productName())
                .build();
    }

    private PartAttributeEntityKey partAttributeEntityKey(PartIdEntityPart id, String attribute) {
        return PartAttributeEntityKey.builder()
                .partId(id)
                .attribute(attribute)
                .build();
    }

    private PartAspectEntityKey partAspectEntityKey(PartIdEntityPart id, String name) {
        return PartAspectEntityKey.builder()
                .partId(id)
                .name(name)
                .build();
    }
}
