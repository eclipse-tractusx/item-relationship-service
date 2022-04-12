package net.catenax.irs.entities;

import com.github.javafaker.Faker;
import net.catenax.irs.component.AsyncFetchedItems;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;
import net.catenax.irs.dtos.ItemLifecycleStage;

import java.util.Collection;
import java.util.UUID;
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
    public PartRelationshipEntity partRelationship(Job parentId, Job childId) {
        return PartRelationshipEntity.builder()
                .key(partRelationshipKey(parentId, childId))
                .uploadDateTime(now())
                .partRelationshipListId(randomUUID())
                .build();
    }

    public PartRelationshipEntity partRelationship() {
        return partRelationship(job(), job());
    }

    /**
     * Generate a {@link PartRelationshipEntityKey} linking two parts,
     * with a random {@link PartRelationshipEntity#getPartRelationshipListId()}.
     *
     * @param parentId parent in the relationship.
     * @param childId  child in the relationship.
     * @return a {@link PartRelationshipEntityKey} linking {@code parentId} to {@code childId}.
     */
    public PartRelationshipEntityKey partRelationshipKey(Job parentId, Job childId) {
        return PartRelationshipEntityKey.builder()
                .childId(childId)
                .parentId(parentId)
                .effectTime(faker.date().past(faker.number().randomDigitNotZero(), TimeUnit.DAYS).toInstant())
                .lifeCycleStage(ItemLifecycleStage.BUILD)
                .removed(false)
                .build();
    }

    /**
     * Generate a {@link JobEntityPart} linking two parts,
     * with random values for {@link JobEntityPart}
     * and {@link JobEntityPart}.
     * <p>
     * Example: {@code JobEntityPart(oneIDManufacturer=Stiedemann Inc, objectIDManufacturer=ypiu9wzwuka1ov03)}.
     *
     * @return a {@link JobEntityPart} with random identifiers.
     */
    public Job job() {
        return Job.builder()
                .jobId(UUID.randomUUID())
                .owner(UUID.randomUUID().toString())
                .build();
    }

    public AsyncFetchedItems asyncFetchedItems() {
      return AsyncFetchedItems.builder()
              .running(1)
              .build();
    }

    public SummaryAttributeEntity summary(AsyncFetchedItems asyncFetchedItems) {
        return SummaryAttributeEntity.builder()
                .asyncFetchedItems(asyncFetchedItems)
                .build();
    }

    public QueryParameterEntityPart queryParameter(BomLifecycle bomLifecycle, Collection<AspectType> aspects, Integer depth, Direction direction) {
        return QueryParameterEntityPart.builder()
                .bomLifecycle(bomLifecycle)
                .aspects(aspects)
                .depth(depth)
                .direction(direction)
                .build();
    }
}
