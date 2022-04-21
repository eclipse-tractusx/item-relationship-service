package net.catenax.irs.mappers;

import net.catenax.irs.component.*;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;
import net.catenax.irs.entities.*;
import net.catenax.irs.testing.DtoMother;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartRelationshipEntityListToDtoMapperTests {

    EntitiesMother generate = new EntitiesMother();
    DtoMother generateDto = new DtoMother();

    @Mock
    PartRelationshipEntityToDtoMapper relationshipMapper;
    @Mock
    ChildItemEntityPartToDtoMapper idMapper;
    @InjectMocks
    PartRelationshipEntityListToDtoMapper sut;

    BomLifecycle GEARBOX_BOMLIFECYCLE = BomLifecycle.AS_BUILT;
    AspectType GEARBOX_ASPECT = AspectType.MATERIAL_ASPECT;
    Integer GEARBOX_DEPTH = 6;
    Direction GEARBOX_DIRECTION = Direction.DOWNWARD;
    Integer GEARBOX_QUEUE = 1;
    Integer GEARBOX_RUNNING = 2;
    Integer GEARBOX_COMPLETE = 3;
    Integer GEARBOX_FAILED = 4;
    Integer GEARBOX_LOST = 5;

    Job car1 = generate.job();
    Job gearbox1 = generate.job();
    Job gearwheel1 = generate.job();
    AsyncFetchedItems gearboxAsynchFetchedItems = gearboxAsynchFetchedItems(GEARBOX_QUEUE, GEARBOX_RUNNING, GEARBOX_COMPLETE, GEARBOX_FAILED, GEARBOX_LOST);
    Summary gearboxSummary = gearboxSummary(gearboxAsynchFetchedItems);
    QueryParameter gearboxQueryParameter = gearboxQueryParameter(GEARBOX_BOMLIFECYCLE, GEARBOX_ASPECT, GEARBOX_DEPTH, GEARBOX_DIRECTION);
   AsyncFetchedItems asyncFetchedItems = generate.asyncFetchedItems();
    SummaryAttributeEntity summaryEntityPart = generate.summary(asyncFetchedItems);
    QueryParameterEntityPart queryParameterEntityPart = generate.queryParameter(BomLifecycle.AS_BUILT, new ArrayList<AspectType>(Arrays.asList(AspectType.values())), 2, Direction.DOWNWARD);
    PartRelationshipEntity car1_gearbox1 = generate.partRelationship(car1, gearbox1);
    PartRelationshipEntity gearbox1_gearwheel1 = generate.partRelationship(gearbox1, gearwheel1);
    List<PartRelationshipEntity> relations = List.of(car1_gearbox1, gearbox1_gearwheel1);
    List<Job> jobEntityParts = List.of(car1, gearbox1, gearwheel1);
    List<Relationship> relationsDto = relations.stream().map(s -> generateDto.relationship()).collect(Collectors.toList());
    List<Job> jobsDto = jobEntityParts.stream().map(s -> generateDto.job(car1, gearboxSummary, gearboxQueryParameter)).collect(Collectors.toList());
    Summary summaryDto = generateDto.summary(asyncFetchedItems);
    QueryParameter queryParameterDto = generateDto.queryParameter(BomLifecycle.AS_BUILT, AspectType.MATERIAL_ASPECT, 2, Direction.DOWNWARD);

    @Test
    void toPartRelationshipsWithInfos() {
        // Arrange
        zip(jobEntityParts, jobsDto)
                .forEach(i -> lenient().when(idMapper.toJob(i.getKey())).thenReturn(i.getValue()));
        zip(relations, relationsDto)
                .forEach(i -> when(relationshipMapper.toRelationship(i.getKey())).thenReturn(i.getValue()));

        // Act
        var output = sut.toPartRelationshipsWithInfos(relations, car1, summaryEntityPart, queryParameterEntityPart);

        // Assert
        List<Job> expectedPartInfos = List.of(
                // Case with non-missing type name and missing aspect
                generateDto.job(jobsDto.get(0), summaryDto, null),
                // Case with non-missing type name and non-missing aspect
                generateDto.job(jobsDto.get(1), summaryDto, queryParameterDto)
        );
        assertThat(output).usingRecursiveComparison()
                .isEqualTo(
                        (Jobs.builder()
                                .job(jobsDto.get(0))
                                .relationships(relationsDto)
                                .build()));
    }

    private static <L, R> Stream<Pair<L, R>> zip(List<L> left, List<R> right) {
        if (left.size() != right.size()) {
            throw new IllegalArgumentException("Sizes should match");
        }
        return IntStream.range(0, left.size())
                .mapToObj(i -> new ImmutablePair<>(left.get(i), right.get(i)));
    }

    private Summary gearboxSummary(final AsyncFetchedItems asyncFetchedItems) {
        return Summary.builder()
                .asyncFetchedItems(asyncFetchedItems)
                .build();
    }

    private QueryParameter gearboxQueryParameter(final BomLifecycle bomLifecycle, final AspectType aspect, final Integer depth, final Direction direction) {
        return QueryParameter.builder()
                .bomLifecycle(bomLifecycle)
                .aspect(aspect)
                .depth(depth)
                .direction(direction)
                .build();
    }

    private AsyncFetchedItems gearboxAsynchFetchedItems(final Integer queue, final Integer running, final Integer complete, final Integer failed, final Integer lost) {
        return AsyncFetchedItems.builder()
                .queue(queue)
                .running(running)
                .complete(complete)
                .failed(failed)
                .lost(lost)
                .build();
    }
}
