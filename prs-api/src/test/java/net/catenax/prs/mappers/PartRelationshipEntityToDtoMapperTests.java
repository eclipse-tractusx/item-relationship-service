package net.catenax.prs.mappers;

import net.catenax.prs.entities.EntitiesMother;
import net.catenax.prs.testing.DtoMother;
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
    PartIdEntityPartToDtoMapper idMapper;
    @InjectMocks
    PartRelationshipEntityToDtoMapper sut;

    @Test
    void toPartRelationshipsWithInfos() {
        // Arrange
        var parent = generate.partId();
        var child = generate.partId();
        var parentDto = generateDto.partId();
        var childDto = generateDto.partId();

        var input = generate.partRelationship(parent, child);

        when(idMapper.toPartId(parent)).thenReturn(parentDto);
        when(idMapper.toPartId(child)).thenReturn(childDto);

        // Act
        var output = sut.toPartRelationship(input);

        // Assert
        assertThat(output.getParent()).isEqualTo(parentDto);
        assertThat(output.getChild()).isEqualTo(childDto);
    }
}
