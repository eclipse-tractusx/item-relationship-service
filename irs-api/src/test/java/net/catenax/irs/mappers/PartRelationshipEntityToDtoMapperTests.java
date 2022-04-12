package net.catenax.irs.mappers;

import net.catenax.irs.entities.EntitiesMother;
import net.catenax.irs.testing.DtoMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartRelationshipEntityToDtoMapperTests {

    EntitiesMother generate = new EntitiesMother();
    DtoMother generateDto = new DtoMother();
    @Mock
    ChildItemEntityPartToDtoMapper idMapper;
    @InjectMocks
    PartRelationshipEntityToDtoMapper sut;

    @Test
    void toPartRelationshipsWithInfos() {
        // Arrange
        var parent = generate.job();
        var child = generate.job();
        var parentDto = generateDto.job();
        var childDto = generateDto.job();

        var input = generate.partRelationship(parent, child);

        when(idMapper.toJob(parent)).thenReturn(parentDto);
        when(idMapper.toJob(child)).thenReturn(childDto);

        // Act
        var output = sut.toRelationship(input);

        // Assert
        assertThat(output.getParentItem()).isEqualTo(parentDto);
        assertThat(output.getChildItem()).isEqualTo(childDto);
    }
}
