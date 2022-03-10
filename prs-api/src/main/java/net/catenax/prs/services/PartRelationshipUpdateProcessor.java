//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.prs.dtos.events.PartRelationshipsUpdateRequest;
import net.catenax.prs.entities.PartRelationshipEntity;
import net.catenax.prs.mappers.PartRelationshipUpdateRequestToEntityMapper;
import net.catenax.prs.repositories.PartRelationshipRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;


/**
 * Service for processing parts tree update events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PartRelationshipUpdateProcessor {

    /**
     * Repository for retrieving {@link PartRelationshipEntity} data.
     */
    private final PartRelationshipRepository relationshipRepository;

    /**
     * Mapper for Parts relationship events to db entity.
     */
    private final PartRelationshipUpdateRequestToEntityMapper entityMapper;

    /**
     * Update {@link PartRelationshipsUpdateRequest} data into database.
     *
     * @param event Parts relationship update event from broker.
     * @param eventTimestamp Timestamp of the event.
     */
    public void process(final PartRelationshipsUpdateRequest event, final Instant eventTimestamp) {
        final var relationshipsUpdateId = UUID.randomUUID();
        entityMapper.toRelationships(event, relationshipsUpdateId, eventTimestamp)
                .forEach(partRelationshipEntity -> {
                    try {
                        persistIfNew(partRelationshipEntity);
                    } catch (DataIntegrityViolationException e) {
                        log.warn("Failed to persist entity, probably because an entity with same primary key was concurrently inserted. Trying again.", e);
                        persistIfNew(partRelationshipEntity);
                    }
                });
    }

    private void persistIfNew(final PartRelationshipEntity partRelationshipEntity) {
        if (relationshipRepository.findById(partRelationshipEntity.getKey()).isEmpty()) {
            relationshipRepository.saveAndFlush(partRelationshipEntity);
        } else {
            log.info("Ignoring duplicate entity");
        }
    }
}
