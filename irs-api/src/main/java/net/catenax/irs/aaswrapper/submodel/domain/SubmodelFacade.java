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
import java.util.function.Predicate;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;
import net.catenax.irs.dto.JobParameter;
import org.apache.commons.lang3.StringUtils;
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
     * @param jobData                 relevant job data values
     * @return The Aspect Model for the given submodel
     */
    @Retry(name = "submodelRetryer")
    public AssemblyPartRelationshipDTO getSubmodel(final String submodelEndpointAddress, final JobParameter jobData) {
        final AssemblyPartRelationship submodel = this.submodelClient.getSubmodel(submodelEndpointAddress,
                AssemblyPartRelationship.class);

        final Set<ChildData> submodelParts = submodel.getChildParts();

        final String lifecycleContext = jobData.getBomLifecycle();
        if (shouldFilterByLifecycleContext(lifecycleContext)) {
            filterSubmodelPartsByLifecycleContext(submodelParts, lifecycleContext);
        }

        return buildAssemblyPartRelationshipResponse(submodelParts, submodel.getCatenaXId());
    }

    private AssemblyPartRelationshipDTO buildAssemblyPartRelationshipResponse(final Set<ChildData> submodelParts,
            final String catenaXId) {
        final Set<ChildDataDTO> childParts = new HashSet<>();
        submodelParts.forEach(childData -> childParts.add(ChildDataDTO.builder()
                                                                      .childCatenaXId(childData.getChildCatenaXId())
                                                                      .lifecycleContext(childData.getLifecycleContext()
                                                                                                 .getValue())
                                                                      .build()));

        return AssemblyPartRelationshipDTO.builder().catenaXId(catenaXId).childParts(childParts).build();
    }

    private void filterSubmodelPartsByLifecycleContext(final Set<ChildData> submodelParts,
            final String lifecycleContext) {
        submodelParts.removeIf(isNotLifecycleContext(lifecycleContext));
    }

    private boolean shouldFilterByLifecycleContext(final String lifecycleContext) {
        return StringUtils.isNotBlank(lifecycleContext);
    }

    private Predicate<ChildData> isNotLifecycleContext(final String lifecycleContext) {
        return childData -> !childData.getLifecycleContext().getValue().equals(lifecycleContext);
    }

}
