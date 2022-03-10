package net.catenax.prs.mappers;

import net.catenax.prs.entities.EntitiesMother;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PartIdEntityPartToDtoMapperTests {

    EntitiesMother generate = new EntitiesMother();
    PartIdEntityPartToDtoMapper sut = new PartIdEntityPartToDtoMapper();

    @Test
    void toPartId() {
        var input = generate.partId();
        var output = sut.toPartId(input);
        assertThat(output.getOneIDManufacturer()).isEqualTo(input.getOneIDManufacturer());
        assertThat(output.getObjectIDManufacturer()).isEqualTo(input.getObjectIDManufacturer());
    }
}
