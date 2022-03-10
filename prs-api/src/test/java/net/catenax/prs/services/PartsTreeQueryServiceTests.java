package net.catenax.prs.services;

import com.github.javafaker.Faker;
import net.catenax.prs.configuration.PrsConfiguration;
import net.catenax.prs.dtos.PartRelationshipsWithInfos;
import net.catenax.prs.entities.EntitiesMother;
import net.catenax.prs.entities.PartAttributeEntity;
import net.catenax.prs.entities.PartIdEntityPart;
import net.catenax.prs.entities.PartRelationshipEntity;
import net.catenax.prs.exceptions.MaxDepthTooLargeException;
import net.catenax.prs.mappers.PartRelationshipEntityListToDtoMapper;
import net.catenax.prs.repositories.PartAspectRepository;
import net.catenax.prs.repositories.PartAttributeRepository;
import net.catenax.prs.repositories.PartRelationshipRepository;
import net.catenax.prs.requests.PartsTreeByObjectIdRequest;
import net.catenax.prs.requests.RequestMother;
import net.catenax.prs.testing.DtoMother;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PartsTreeQueryServiceTests {

    @Mock
    PartRelationshipRepository relationshipRepository;
    @Mock
    PartAspectRepository aspectRepository;
    @Mock
    PartAttributeRepository attributeRepository;
    @Mock
    PartRelationshipEntityListToDtoMapper mapper;
    @Spy
    PrsConfiguration configuration = new PrsConfiguration();
    @InjectMocks
    PartsTreeQueryService sut;

    Faker faker = new Faker();
    int maxDepth = faker.number().numberBetween(10, 20);

    EntitiesMother generate = new EntitiesMother();
    PartIdEntityPart car1 = generate.partId();
    PartIdEntityPart gearbox1 = generate.partId();
    PartIdEntityPart gearwheel1 = generate.partId();

    RequestMother generateRequest = new RequestMother();
    PartIdEntityPart requestId = generate.partId();
    PartsTreeByObjectIdRequest request = generateRequest.byObjectId(requestId);

    Set<PartIdEntityPart> allPartIds = Set.of(requestId, car1, gearbox1, gearwheel1);
    PartRelationshipEntity car1_gearbox1 = generate.partRelationship(car1, gearbox1);
    PartRelationshipEntity gearbox1_gearwheel1 = generate.partRelationship(gearbox1, gearwheel1);

    /**
     * Collection of relationships to be returned by mock {@link PartRelationshipRepository}.
     */
    List<PartRelationshipEntity> relationships = List.of(car1_gearbox1, gearbox1_gearwheel1);
    /**
     * Collection of attributes to be returned by mock {@link PartAttributeRepository}.
     * {@literal partTypeName} attribute is returned only for {@link #gearwheel1} and {@link #car1},
     * but not for {@link #gearbox1}. Expected service behavior on missing {@literal partTypeName} attribute
     * is to return a {@literal null} value for that field.
     */
    List<PartAttributeEntity> attributes = List.of(generate.partTypeNameAttribute(gearwheel1), generate.partTypeNameAttribute(car1));

    DtoMother generateDto = new DtoMother();

    /**
     * DTO to be returned by mock {@link PartRelationshipEntityListToDtoMapper}.
     */
    PartRelationshipsWithInfos resultDto = generateDto.partRelationshipsWithInfos();

    @Test
    @DisplayName("When repository returns no match, service returns no match")
    public void getPartsTreeWithNoMatch() {
        when(relationshipRepository
            .getPartsTree(request.getOneIDManufacturer(), request.getObjectIDManufacturer(), Integer.MAX_VALUE))
            .thenReturn(emptyList());

        when(mapper
                .toPartRelationshipsWithInfos(emptyList(), Set.of(requestId), emptyList(), emptyList()))
                .thenReturn(resultDto);

        // Act
        var response = sut.getPartsTree(request);

        // Assert
        assertThat(response).isSameAs(resultDto);
        verifyNoInteractions(aspectRepository);
    }

    @Test
    @DisplayName("When relationships found, service returns them")
    public void getPartsTreeWithMatches() {
        // Arrange
        setUpCollaborators(maxDepth);

        when(mapper
                .toPartRelationshipsWithInfos(relationships, allPartIds, attributes, emptyList()))
                .thenReturn(resultDto);

        // Act
        var response = sut.getPartsTree(request);

        // Assert
        assertThat(response).isSameAs(resultDto);
        verifyNoInteractions(aspectRepository);
    }

    @Test
    @DisplayName("When aspects found, service passes them on to the mapper to enrich the results")
    public void getPartsTreeWithAspect() {
        // Arrange
        var aspect = faker.lorem().word();
        request = request.toBuilder().aspect(aspect).build();

        setUpCollaborators(maxDepth);

        var aspects = List.of(generate.partAspect(car1), generate.partAspect(gearbox1));
        when(aspectRepository
                .findAllBy(allPartIds, aspect))
                .thenReturn(aspects);

        when(mapper
                .toPartRelationshipsWithInfos(relationships, allPartIds, attributes, aspects))
                .thenReturn(resultDto);

        // Act
        var response = sut.getPartsTree(request);

        // Assert
        assertThat(response).isSameAs(resultDto);
    }

    @Test
    @DisplayName("When depth <= maxDepth is passed, service passes depth to repository")
    public void getPartsTreeWithDepthLessThanOrEqualMax() {
        var depth = faker.number().numberBetween(1, maxDepth);
        verifyGetPartsTreeDepthPassedToRepository(depth, depth);
    }

    @Test
    @DisplayName("When depth > maxDepth is passed, service passes maxDepth to repository")
    public void getPartsTreeWithDepthGreaterThanMax() {
        // Arrange
        var depth = faker.number().numberBetween(maxDepth + 1, maxDepth + 10);

        request = request.toBuilder().depth(depth).build();
        configuration.setPartsTreeMaxDepth(maxDepth);

        // Act
        assertThatExceptionOfType(MaxDepthTooLargeException.class)
                .isThrownBy(() -> sut.getPartsTree(request));
    }

    /**
     *
     * Verify that {@link PartsTreeQueryService#getPartsTree(PartsTreeByObjectIdRequest)}
     * passes on the requested depth to the repository if it is less than {@literal maxDepth},
     * otherwise passes on {@literal maxDepth} instead.
     * @param requestDepth depth requested in the service request.
     * @param expectedDepth actual depth expected to be passed to the repository.
     */
    private void verifyGetPartsTreeDepthPassedToRepository(int requestDepth, int expectedDepth) {
        // Arrange
        request = request.toBuilder().depth(requestDepth).build();

        // Pass the expected depth to the mock, so that Mockito will fail with an unstubbed invocation
        // if a different depth is passed by service.
        setUpCollaborators(expectedDepth);

        when(mapper
                .toPartRelationshipsWithInfos(relationships, allPartIds, attributes, emptyList()))
                .thenReturn(resultDto);

        // Act
        var response = sut.getPartsTree(request);

        // Assert
        assertThat(response).isSameAs(resultDto);
        verifyNoInteractions(aspectRepository);
    }

    private void setUpCollaborators(int maxDepth) {
        configuration.setPartsTreeMaxDepth(maxDepth);

        when(relationshipRepository
                .getPartsTree(request.getOneIDManufacturer(), request.getObjectIDManufacturer(), maxDepth))
                .thenReturn(relationships);

        when(attributeRepository
                .findAllBy(allPartIds, PrsConfiguration.PART_TYPE_NAME_ATTRIBUTE))
                .thenReturn(attributes);
    }
}
