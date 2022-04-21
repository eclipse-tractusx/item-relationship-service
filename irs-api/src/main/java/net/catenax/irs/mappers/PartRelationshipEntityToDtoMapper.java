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
import net.catenax.irs.component.Job;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.entities.JobEntityPart;
import net.catenax.irs.entities.PartRelationshipEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper from {@link PartRelationshipEntity} entity to {@link Job} DTO.
 */
@Component
@RequiredArgsConstructor
public class PartRelationshipEntityToDtoMapper {
    /**
     * Mapper from {@link JobEntityPart} entity to {@link Job} DTO.
     */
    private final ChildItemEntityPartToDtoMapper idMapper;

    /**
     * Map a {@link JobEntityPart} entity into a {@link Job} DTO.
     *
     * @param source entity to map. Must not be {@literal null}.
     * @return DTO containing data from the entity. Guaranteed to be not {@literal null}.
     */
    public Relationship toRelationship(final PartRelationshipEntity source) {
        return Relationship.builder()
                .parentItem(idMapper.toJob(source.getKey().getParentId()))
                .childItem(idMapper.toJob(source.getKey().getChildId()))
                .build();
    }
}
