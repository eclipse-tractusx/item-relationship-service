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
import net.catenax.irs.component.Jobs;
import net.catenax.irs.dtos.ChildItemInfo;
import net.catenax.irs.dtos.PartId;
import net.catenax.irs.dtos.PartRelationship;
import net.catenax.irs.dtos.PartRelationshipsWithInfos;
import net.catenax.irs.entities.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mapper from entities to {@link PartRelationshipsWithInfos} DTO.
 */
@Component
@RequiredArgsConstructor
public class PartRelationshipEntityListToDtoMapper {
    /**
     * Mapper from {@link PartRelationshipEntity} entity to {@link PartRelationship} DTO.
     */
    private final PartRelationshipEntityToDtoMapper relationshipMapper;
    /**
     * Mapper from {@link JobEntityPart} entity to {@link PartId} DTO.
     */
    private final ChildItemEntityPartToDtoMapper idMapper;

    /**
     * Map entities into a {@link PartRelationshipsWithInfos} DTO.
     *
     * @param source     collection of {@link PartRelationshipEntity}
     *                   to be mapped into {@link PartRelationshipsWithInfos#getRelationships()}.
     * @param jobs collection of {@link JobEntityPart}
     *                   to be mapped into {@link PartRelationshipsWithInfos#getPartInfos()}.
     * @param summary collection of {@link SummaryAttributeEntity}
     *                   to be mapped into {@link ChildItemInfo#getPartTypeName()}.
     * @param queryParameter    collection of {@link PartAspectEntity}
     *                   to be mapped into {@link ChildItemInfo#getAspects()}.
     * @return DTO containing data from the entities. Guaranteed to be not {@literal null}.
     */
    public Jobs toPartRelationshipsWithInfos(final Collection<PartRelationshipEntity> source, final Collection<Job> jobs, final SummaryAttributeEntity summary, final QueryParameterEntityPart queryParameter) {

        return Jobs.builder()
                .relationships(mapToList(source, relationshipMapper::toRelationship))
                .build();
    }

    private static <S, T> List<T> mapToList(final Collection<S> source, final Function<S, T> map) {
        return source.stream().map(map).collect(Collectors.toList());
    }
}
