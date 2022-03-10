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
import net.catenax.prs.dtos.PartId;
import net.catenax.prs.entities.PartIdEntityPart;
import org.springframework.stereotype.Component;

/**
 * Mapper from {@link PartIdEntityPart} entity to {@link PartId} DTO.
 */
@Component
@RequiredArgsConstructor
public class PartIdEntityPartToDtoMapper {
    /**
     * Map a {@link PartIdEntityPart} entity into a {@link PartId} DTO.
     *
     * @param source entity to map. Must not be {@literal null}.
     * @return DTO containing data from the entity. Guaranteed to be not {@literal null}.
     */
    public PartId toPartId(final PartIdEntityPart source) {
        return PartId.builder()
                .withOneIDManufacturer(source.getOneIDManufacturer())
                .withObjectIDManufacturer(source.getObjectIDManufacturer())
                .build();
    }
}
