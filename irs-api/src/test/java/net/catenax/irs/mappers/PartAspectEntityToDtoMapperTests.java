package net.catenax.irs.mappers;

import net.catenax.irs.entities.EntitiesMother;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PartAspectEntityToDtoMapperTests {
    EntitiesMother generate = new EntitiesMother();
    PartAspectEntityToDtoMapper sut = new PartAspectEntityToDtoMapper();

    @Test
    void toAspect() {
        var input = generate.partAspect(generate.partId());
        var output = sut.toAspect(input);
        assertThat(output.getName()).isEqualTo(input.getKey().getName());
        assertThat(output.getUrl()).isEqualTo(input.getUrl());
    }
}
