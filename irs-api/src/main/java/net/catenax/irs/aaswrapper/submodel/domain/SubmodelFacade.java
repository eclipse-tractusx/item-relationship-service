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

import java.util.HashSet;
import java.util.Set;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for submodel domain
 */
@Service
@RequiredArgsConstructor
public class SubmodelFacade {

    private final SubmodelClient submodelClient;

    /**
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @return The Aspect Model for the given submodel
     */
    @Retry(name = "submodelRetryer")
    public AssemblyPartRelationshipDTO getSubmodel(final String submodelEndpointAddress) {
        final AssemblyPartRelationship submodel = this.submodelClient.getSubmodel(submodelEndpointAddress, AssemblyPartRelationship.class);

        final Set<ChildDataDTO> childParts = new HashSet<>();
        submodel.getChildParts()
                .forEach(childData -> childParts.add(ChildDataDTO.builder()
                                                                 .withChildCatenaXId(childData.getChildCatenaXId())
                                                                 .withLifecycleContext(
                                                                         childData.getLifecycleContext().getValue())
                                                                 .build()));

        return AssemblyPartRelationshipDTO.builder()
                                          .withCatenaXId(submodel.getCatenaXId())
                                          .withChildParts(childParts)
                                          .build();
    }

}
