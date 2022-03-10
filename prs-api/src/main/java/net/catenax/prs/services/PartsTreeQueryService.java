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
import net.catenax.prs.configuration.PrsConfiguration;
import net.catenax.prs.controllers.ApiErrorsConstants;
import net.catenax.prs.dtos.PartRelationshipsWithInfos;
import net.catenax.prs.entities.PartAspectEntity;
import net.catenax.prs.entities.PartAttributeEntity;
import net.catenax.prs.entities.PartIdEntityPart;
import net.catenax.prs.entities.PartRelationshipEntity;
import net.catenax.prs.exceptions.MaxDepthTooLargeException;
import net.catenax.prs.mappers.PartRelationshipEntityListToDtoMapper;
import net.catenax.prs.repositories.PartAspectRepository;
import net.catenax.prs.repositories.PartAttributeRepository;
import net.catenax.prs.repositories.PartRelationshipRepository;
import net.catenax.prs.requests.PartsTreeByObjectIdRequest;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Service for retrieving parts tree.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PartsTreeQueryService {
    /**
     * Repository for retrieving {@link PartRelationshipEntity} data.
     */
    private final PartRelationshipRepository relationshipRepository;
    /**
     * Repository for retrieving {@link PartAspectEntity} data.
     */
    private final PartAspectRepository aspectRepository;
    /**
     * Repository for retrieving {@link PartAttributeEntity} data.
     */
    private final PartAttributeRepository attributeRepository;
    /**
     * Mapper from entities to {@link PartRelationshipsWithInfos} DTO.
     */
    private final PartRelationshipEntityListToDtoMapper mapper;
    /**
     * PRS configuration settings.
     */
    private final PrsConfiguration configuration;

    /**
     * Get a parts tree for a {@link PartsTreeByObjectIdRequest}.
     *
     * @param request Request.
     * @return PartsTree with parts info.
     */
    public PartRelationshipsWithInfos getPartsTree(final PartsTreeByObjectIdRequest request) {
        final int depth = request.getDepth().orElse(configuration.getPartsTreeMaxDepth());
        if (depth > configuration.getPartsTreeMaxDepth()) {
            throw new MaxDepthTooLargeException(MessageFormat.format(ApiErrorsConstants.PARTS_TREE_MAX_DEPTH, configuration.getPartsTreeMaxDepth()));
        }
        final var tree = relationshipRepository.getPartsTree(
                request.getOneIDManufacturer(),
                request.getObjectIDManufacturer(),
                depth);

        final var allIds = getAllIds(request, tree);

        final var typeNames = attributeRepository.findAllBy(allIds, PrsConfiguration.PART_TYPE_NAME_ATTRIBUTE);
        final var aspects = request.getAspect()
                .map(aspect -> aspectRepository.findAllBy(allIds, aspect))
                .orElseGet(Collections::emptyList);
        return mapper.toPartRelationshipsWithInfos(tree, allIds, typeNames, aspects);
    }

    private Set<PartIdEntityPart> getAllIds(final PartsTreeByObjectIdRequest request, final Collection<PartRelationshipEntity> tree) {
        final var allIds = new LinkedHashSet<PartIdEntityPart>();

        // add request root id, to ensure aspects are returned even if it has no children
        allIds.add(PartIdEntityPart.builder()
                .oneIDManufacturer(request.getOneIDManufacturer())
                .objectIDManufacturer(request.getObjectIDManufacturer())
                .build());

        // add all parent and child IDs found in relationships
        // NB: forEachOrdered guarantees non-concurrent execution
        tree.stream().map(e -> e.getKey().getParentId()).forEachOrdered(allIds::add);
        tree.stream().map(e -> e.getKey().getChildId()).forEachOrdered(allIds::add);

        return allIds;
    }
}
