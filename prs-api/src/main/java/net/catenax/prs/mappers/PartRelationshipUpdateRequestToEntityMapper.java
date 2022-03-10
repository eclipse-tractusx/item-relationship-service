//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.mappers;

import net.catenax.prs.dtos.events.PartRelationshipsUpdateRequest;
import net.catenax.prs.entities.PartIdEntityPart;
import net.catenax.prs.entities.PartRelationshipEntity;
import net.catenax.prs.entities.PartRelationshipEntityKey;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mapper for {@link PartRelationshipsUpdateRequest} to {@link PartRelationshipEntity} entity.
 */
@Component
public class PartRelationshipUpdateRequestToEntityMapper {
    /**
     * Map a {@link PartRelationshipsUpdateRequest} event to {@link PartRelationshipEntity} entity.
     *
     * @param event see {@link PartRelationshipsUpdateRequest}
     * @param partRelationshipListId An {@link UUID} unique id for all relationships in event.
     * @param eventTimestamp Timestamp of the event.
     * @return List of {@link PartRelationshipEntity} containing data from update event.
     */
    public List<PartRelationshipEntity> toRelationships(final PartRelationshipsUpdateRequest event, final UUID partRelationshipListId, final Instant eventTimestamp) {
        final List<PartRelationshipEntity> relationshipEntityList = new ArrayList<>();

        event.getRelationships().forEach(relInEvent -> {
            final var partRelationshipEntityKey = PartRelationshipEntityKey.builder()
                    .parentId(toPartIdEntityPart(relInEvent.getRelationship().getParent().getOneIDManufacturer(),
                            relInEvent.getRelationship().getParent().getObjectIDManufacturer()))
                    .childId(toPartIdEntityPart(relInEvent.getRelationship().getChild().getOneIDManufacturer(),
                            relInEvent.getRelationship().getChild().getObjectIDManufacturer()))
                    .effectTime(relInEvent.getEffectTime())
                    .removed(relInEvent.isRemove())
                    .lifeCycleStage(relInEvent.getStage())
                    .build();

            final var relationshipEntity = PartRelationshipEntity.builder()
                    .key(partRelationshipEntityKey)
                    .uploadDateTime(eventTimestamp)
                    .partRelationshipListId(partRelationshipListId)
                    .build();

            relationshipEntityList.add(relationshipEntity);

        });

        return relationshipEntityList;
    }


    private PartIdEntityPart toPartIdEntityPart(final String oneIDManufacturer, final String objectIDManufacturer) {
        return PartIdEntityPart.builder()
                .oneIDManufacturer(oneIDManufacturer)
                .objectIDManufacturer(objectIDManufacturer).build();
    }
}
