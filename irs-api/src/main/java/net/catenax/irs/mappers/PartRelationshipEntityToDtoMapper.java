//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.mappers;

import lombok.RequiredArgsConstructor;
import net.catenax.irs.dtos.PartId;
import net.catenax.irs.dtos.PartRelationship;
import net.catenax.irs.entities.PartIdEntityPart;
import net.catenax.irs.entities.PartRelationshipEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper from {@link PartRelationshipEntity} entity to {@link PartRelationship} DTO.
 */
@Component
@RequiredArgsConstructor
public class PartRelationshipEntityToDtoMapper {
    /**
     * Mapper from {@link PartIdEntityPart} entity to {@link PartId} DTO.
     */
    private final PartIdEntityPartToDtoMapper idMapper;

    /**
     * Map a {@link PartIdEntityPart} entity into a {@link PartId} DTO.
     *
     * @param source entity to map. Must not be {@literal null}.
     * @return DTO containing data from the entity. Guaranteed to be not {@literal null}.
     */
    public PartRelationship toPartRelationship(final PartRelationshipEntity source) {
        return PartRelationship.builder()
                .withParent(idMapper.toPartId(source.getKey().getParentId()))
                .withChild(idMapper.toPartId(source.getKey().getChildId()))
                .build();
    }
}
