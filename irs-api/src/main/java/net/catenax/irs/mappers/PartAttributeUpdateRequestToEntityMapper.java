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

import net.catenax.irs.component.events.PartAttributeUpdateRequest;
import net.catenax.irs.entities.PartAttributeEntity;
import net.catenax.irs.entities.PartAttributeEntityKey;
import net.catenax.irs.entities.PartIdEntityPart;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper for {@link PartAttributeUpdateRequest} to {@link PartAttributeEntity} entity.
 */
@Component
public class PartAttributeUpdateRequestToEntityMapper {

    /**
     * Map a {@link PartAttributeUpdateRequest} event to {@link PartAttributeEntity} entity.
     *
     * @param event see {@link PartAttributeUpdateRequest}
     * @return {@link PartAttributeEntity} containing data from update event.
     */
    public PartAttributeEntity toAttribute(final PartAttributeUpdateRequest event) {
        final var partAttributeEntityKey = PartAttributeEntityKey.builder()
                .attribute(event.getName())
                .partId(PartIdEntityPart.builder()
                        .objectIDManufacturer(event.getPart().getObjectIDManufacturer())
                        .oneIDManufacturer(event.getPart().getOneIDManufacturer()).build())
                .build();

        return PartAttributeEntity.builder()
                .key(partAttributeEntityKey)
                .value(event.getValue())
                .effectTime(event.getEffectTime())
                .lastModifiedTime(Instant.now())
                .build();
    }
}
