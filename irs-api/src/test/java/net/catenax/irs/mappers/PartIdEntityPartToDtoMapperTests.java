package net.catenax.irs.mappers;

import net.catenax.irs.entities.EntitiesMother;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PartIdEntityPartToDtoMapperTests {

    EntitiesMother generate = new EntitiesMother();
    ChildItemEntityPartToDtoMapper sut = new ChildItemEntityPartToDtoMapper();

    @Test
    void toPartId() {
        var input = generate.job();
        var output = sut.toJob(input);
        assertThat(output.getJobId()).isEqualTo(input.getJobId());
        assertThat(output.getOwner()).isEqualTo(input.getOwner());
    }
}
