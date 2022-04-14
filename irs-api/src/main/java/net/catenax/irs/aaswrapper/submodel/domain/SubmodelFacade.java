//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import net.catenax.irs.dto.NodeType;
import net.catenax.irs.dto.ProcessingError;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for submodel domain
 */
@RequiredArgsConstructor
@Service
public class SubmodelFacade {

    private final SubmodelClient submodelClient;

    /**
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @param catenaXId               The catenaXId shell of the descriptor
     * @return The Aspect Model for the given submodel
     */
    public AbstractItemRelationshipAspect getAssemblyPartRelationshipSubmodel(final String submodelEndpointAddress,
            final String catenaXId) {
        AbstractItemRelationshipAspect result;
        try {
            final Aspect submodelResponse = submodelClient.getSubmodel(submodelEndpointAddress, catenaXId,
                    AssemblyPartRelationship.class);
            if (submodelResponse instanceof AssemblyPartRelationship) {
                final AssemblyPartRelationship submodel = (AssemblyPartRelationship) submodelResponse;
                final Set<ChildDataDTO> childParts = new HashSet<>();
                submodel.getChildParts()
                        .forEach(childData -> childParts.add(ChildDataDTO.builder()
                                                                         .withChildCatenaXId(
                                                                                 childData.getChildCatenaXId())
                                                                         .withLifecycleContext(
                                                                                 childData.getLifecycleContext()
                                                                                          .getValue())
                                                                         .build()));
                final AssemblyPartRelationshipDTO relationshipDTO = AssemblyPartRelationshipDTO.builder()
                                                                                               .withCatenaXId(
                                                                                                       submodel.getCatenaXId())
                                                                                               .withChildParts(
                                                                                                       childParts)
                                                                                               .build();
                final NodeType nodeType = relationshipDTO.getChildParts().isEmpty() ? NodeType.LEAF : NodeType.NODE;
                result = new ItemRelationshipAspect(catenaXId, nodeType, relationshipDTO);
            } else {
                result = getResponseTombStoneForResponse(catenaXId, submodelEndpointAddress,
                        "The returned Aspect Model did not match the provided class.", Optional.empty(), 1);
            }
        } catch (SubmodelClientException e) {
            result = getResponseTombStoneForResponse(catenaXId, submodelEndpointAddress, e.getMessage(), Optional.of(e),
                    1);
        }
        return result;

    }

    private AbstractItemRelationshipAspect getResponseTombStoneForResponse(final String catenaXId,
            final String endpointUrl, final String errorDetail, final Optional<Exception> exception,
            final int retryCounter) {
        final ProcessingError processingError = new ProcessingError(exception, errorDetail, retryCounter,
                Instant.now());
        return new ItemRelationshipAspectTombstone(catenaXId, processingError, endpointUrl);
    }
}
