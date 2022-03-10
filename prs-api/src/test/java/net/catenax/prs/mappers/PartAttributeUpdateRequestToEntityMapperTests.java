package net.catenax.prs.mappers;

import net.catenax.prs.testing.UpdateRequestMother;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PartAttributeUpdateRequestToEntityMapperTests {
    UpdateRequestMother generate = new UpdateRequestMother();
    PartAttributeUpdateRequestToEntityMapper sut = new PartAttributeUpdateRequestToEntityMapper();

    @Test
    void toAttribute() {
        var input = generate.partAttributeUpdate();
        var output = sut.toAttribute(input);

        assertThat(output.getEffectTime()).isEqualTo(input.getEffectTime());
        assertThat(output.getValue()).isEqualTo(input.getValue());
        assertThat(output.getLastModifiedTime()).isNotNull();
        assertThat(output.getKey().getAttribute()).isEqualTo(input.getName());
        assertThat(output.getKey().getPartId().getObjectIDManufacturer()).isEqualTo(input.getPart().getObjectIDManufacturer());
        assertThat(output.getKey().getPartId().getOneIDManufacturer()).isEqualTo(input.getPart().getOneIDManufacturer());
    }
}
