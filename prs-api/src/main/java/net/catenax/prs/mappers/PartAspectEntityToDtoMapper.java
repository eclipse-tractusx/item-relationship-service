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

import lombok.RequiredArgsConstructor;
import net.catenax.prs.dtos.Aspect;
import net.catenax.prs.entities.PartAspectEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper from {@link PartAspectEntity} entity to {@link Aspect} DTO.
 */
@Component
@RequiredArgsConstructor
public class PartAspectEntityToDtoMapper {
    /**
     * Map a {@link PartAspectEntity} entity into an {@link Aspect} DTO.
     *
     * @param source entity to map. Must not be {@literal null}.
     * @return DTO containing data from the entity. Guaranteed to be not {@literal null}.
     */
    public Aspect toAspect(final PartAspectEntity source) {
        return Aspect.builder()
                .withName(source.getKey().getName())
                .withUrl(source.getUrl())
                .build();
    }
}
