package net.catenax.prs.mappers;

import net.catenax.prs.entities.PartAspectEntity;
import net.catenax.prs.entities.PartAspectEntityKey;
import net.catenax.prs.entities.PartIdEntityPart;
import net.catenax.prs.testing.UpdateRequestMother;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PartAspectUpdateRequestToEntityMapperTests {
    UpdateRequestMother generate = new UpdateRequestMother();
    PartAspectUpdateRequestToEntityMapper sut = new PartAspectUpdateRequestToEntityMapper();


    @Test
    void toAspects() {
        //arrange
        var input = generate.partAspectUpdate();
        var eventTimestamp = Instant.now();
        var expectedEntities = input.getAspects().stream()
                .map(updateEvent -> PartAspectEntity.builder()
                        .key(PartAspectEntityKey.builder()
                                .partId(PartIdEntityPart.builder()
                                        .objectIDManufacturer(input.getPart().getObjectIDManufacturer())
                                        .oneIDManufacturer(input.getPart().getOneIDManufacturer())
                                        .build())
                                .name(updateEvent.getName())
                                .build())
                        .url(updateEvent.getUrl())
                        .effectTime(input.getEffectTime())
                        .lastModifiedTime(eventTimestamp)
                        .build()).collect(Collectors.toList());

        //act
        var output = sut.toAspects(input, eventTimestamp);

        //assert
        assertThat(output).usingFieldByFieldElementComparator()
                .containsExactlyElementsOf(expectedEntities);
    }
}
