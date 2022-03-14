package net.catenax.irs.mappers;

import net.catenax.irs.entities.PartIdEntityPart;
import net.catenax.irs.entities.PartRelationshipEntity;
import net.catenax.irs.entities.PartRelationshipEntityKey;
import net.catenax.irs.testing.UpdateRequestMother;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PartRelationshipUpdateRequestToEntityMapperTests {
    UpdateRequestMother generate = new UpdateRequestMother();
    PartRelationshipUpdateRequestToEntityMapper sut = new PartRelationshipUpdateRequestToEntityMapper();


    @Test
    void toRelationships() {
        //arrange
        var input = generate.partRelationshipUpdateList();
        var relationshipUpdateId = UUID.randomUUID();
        var eventTimestamp = Instant.now();
        var expectedEntities = input.getRelationships().stream()
                .map(updateEvent -> PartRelationshipEntity.builder()
                        .partRelationshipListId(relationshipUpdateId)
                        .key(PartRelationshipEntityKey.builder()
                                .removed(updateEvent.isRemove())
                                .lifeCycleStage(updateEvent.getStage())
                                .effectTime(updateEvent.getEffectTime())
                                .childId(PartIdEntityPart.builder()
                                        .objectIDManufacturer(updateEvent.getRelationship().getChild().getObjectIDManufacturer())
                                        .oneIDManufacturer(updateEvent.getRelationship().getChild().getOneIDManufacturer())
                                        .build())
                                .parentId(PartIdEntityPart.builder()
                                        .objectIDManufacturer(updateEvent.getRelationship().getParent().getObjectIDManufacturer())
                                        .oneIDManufacturer(updateEvent.getRelationship().getParent().getOneIDManufacturer())
                                        .build())
                                .build())
                        .uploadDateTime(eventTimestamp)
                        .build()).collect(Collectors.toList());

        //act
        var output = sut.toRelationships(input, relationshipUpdateId, eventTimestamp);

        //assert
        assertThat(output).usingFieldByFieldElementComparator()
                .containsExactlyElementsOf(expectedEntities);
    }
}
