package net.catenax.irs.mappers;

import net.catenax.irs.component.AsyncFetchedItems;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.QueryParameter;
import net.catenax.irs.component.Summary;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;
import net.catenax.irs.entities.EntitiesMother;
import net.catenax.irs.testing.DtoMother;
import org.junit.jupiter.api.Disabled;
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
    AsyncFetchedItems gearboxAsynchFetchedItems = gearboxAsynchFetchedItems(GEARBOX_QUEUE, GEARBOX_RUNNING, GEARBOX_COMPLETE, GEARBOX_FAILED, GEARBOX_LOST);
    Summary gearboxSummary = gearboxSummary(gearboxAsynchFetchedItems);
    QueryParameter gearboxQueryParameter = gearboxQueryParameter(GEARBOX_BOMLIFECYCLE, GEARBOX_ASPECT, GEARBOX_DEPTH, GEARBOX_DIRECTION);


    @Disabled
    @Test
    void toPartRelationshipsWithInfos() {
        // Arrange
        var parent = generate.job();
        var child = generate.job();
        var parentDto = generateDto.job(car1, gearboxSummary, gearboxQueryParameter);
        var childDto = generateDto.job(car1, gearboxSummary, gearboxQueryParameter);

        var input = generate.partRelationship(parent, child);

        when(idMapper.toJob(parent)).thenReturn(parentDto);
        when(idMapper.toJob(child)).thenReturn(childDto);

        // Act
        var output = sut.toRelationship(input);

        // Assert
        assertThat(output.getParentItem()).isEqualTo(parentDto);
        assertThat(output.getChildItem()).isEqualTo(childDto);
    }

    public Summary gearboxSummary(final AsyncFetchedItems asyncFetchedItems) {
        return Summary.builder()
                .asyncFetchedItems(asyncFetchedItems)
                .build();
    }

    public QueryParameter gearboxQueryParameter(final BomLifecycle bomLifecycle, final AspectType aspect, final Integer depth, final Direction direction) {
        return QueryParameter.builder()
                .bomLifecycle(bomLifecycle)
                .aspect(aspect)
                .depth(depth)
                .direction(direction)
                .build();
    }

    public AsyncFetchedItems gearboxAsynchFetchedItems(final Integer queue, final Integer running, final Integer complete, final Integer failed, final Integer lost) {
        return AsyncFetchedItems.builder()
                .queue(queue)
                .running(running)
                .complete(complete)
                .failed(failed)
                .lost(lost)
                .build();
    }
}
