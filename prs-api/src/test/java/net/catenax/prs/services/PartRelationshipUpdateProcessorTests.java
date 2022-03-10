package net.catenax.prs.services;

import com.github.javafaker.Faker;
import net.catenax.prs.dtos.events.PartRelationshipsUpdateRequest;
import net.catenax.prs.entities.EntitiesMother;
import net.catenax.prs.mappers.PartRelationshipUpdateRequestToEntityMapper;
import net.catenax.prs.repositories.PartRelationshipRepository;
import net.catenax.prs.testing.UpdateRequestMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartRelationshipUpdateProcessorTests {

    @Mock
    PartRelationshipRepository relationshipRepository;
    @Mock
    PartRelationshipUpdateRequestToEntityMapper entityMapper;
    @InjectMocks
    PartRelationshipUpdateProcessor sut;

    Faker faker = new Faker();
    EntitiesMother generate = new EntitiesMother();
    UpdateRequestMother generateDto = new UpdateRequestMother();
    PartRelationshipsUpdateRequest relationshipUpdate = generateDto.partRelationshipUpdateList();
    Instant eventTimestamp = Instant.now();

    @Test
    void process() {
        // Arrange
        var entities = IntStream.range(0, faker.number().numberBetween(1, 3))
                .mapToObj(i -> generate.partRelationship())
                .collect(Collectors.toList());

        when(entityMapper
                .toRelationships(eq(relationshipUpdate), any(UUID.class), eq(eventTimestamp)))
                .thenReturn(entities);

        // Act
        sut.process(relationshipUpdate, eventTimestamp);

        // Assert
        entities.stream()
                .forEach(r -> verify(relationshipRepository).saveAndFlush(r));
    }

    @Test
    void process_OnPreexistingDuplicate_IgnoresAndContinues() {
        // Arrange
        var entities = IntStream.range(0, 3)
                .mapToObj(i -> generate.partRelationship())
                .collect(Collectors.toList());

        when(entityMapper
                .toRelationships(eq(relationshipUpdate), any(UUID.class), eq(eventTimestamp)))
                .thenReturn(entities);
        when(relationshipRepository.findById(any())).thenReturn(
                Optional.empty(),
                Optional.of(entities.get(1)),
                Optional.empty()
        );

        // Act
        sut.process(relationshipUpdate, eventTimestamp);

        // Assert
        verify(relationshipRepository, never()).saveAndFlush(entities.get(1));
        verify(relationshipRepository).saveAndFlush(entities.get(2));
    }

    @Test
    void process_OnConcurrentDuplicate_IgnoresAndContinues() {
        // Arrange
        var entities = IntStream.range(0, 3)
                .mapToObj(i -> generate.partRelationship())
                .collect(Collectors.toList());

        when(entityMapper
                .toRelationships(eq(relationshipUpdate), any(UUID.class), eq(eventTimestamp)))
                .thenReturn(entities);
        when(relationshipRepository.saveAndFlush(any()))
                .thenReturn(null)
                .thenThrow(new DataIntegrityViolationException(faker.lorem().sentence()))
                .thenReturn(null);

        // Act
        sut.process(relationshipUpdate, eventTimestamp);

        // Assert
        verify(relationshipRepository).saveAndFlush(entities.get(2));
    }
}