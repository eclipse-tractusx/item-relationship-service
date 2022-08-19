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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.dto.JobParameter;
import net.catenax.irs.dto.RelationshipAspect;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for submodel domain
 */
@Slf4j
@Service
@AllArgsConstructor
public class SubmodelFacade {

    private final SubmodelClient submodelClient;

    /**
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @param jobData                 relevant job data values
     * @return The Aspect Model for the given submodel
     */
    public List<Relationship> getRelationships(final String submodelEndpointAddress, final JobParameter jobData) {
        final AssemblyPartRelationship submodel = this.submodelClient.getSubmodel(submodelEndpointAddress,
                AssemblyPartRelationship.class);

        log.info("Submodel: {}, childParts {}", submodel.getCatenaXId(), submodel.getChildParts());

        final Set<ChildData> childParts = thereAreChildParts(submodel)
                ? new HashSet<>(submodel.getChildParts())
                : Collections.emptySet();

        final String lifecycleContext = jobData.getBomLifecycle();
        if (shouldFilterByLifecycleContext(lifecycleContext)) {
            filterSubmodelPartsByLifecycleContext(childParts, lifecycleContext);
        }

        return buildRelationships(childParts, submodel.getCatenaXId());
    }

    private List<Relationship> buildRelationships(final Set<ChildData> submodelParts, final String catenaXId) {
        return submodelParts.stream()
                            .map(childData -> childData.toRelationship(catenaXId,
                                    RelationshipAspect.AssemblyPartRelationship))
                            .collect(Collectors.toList());
    }

    /**
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @return The Aspect Model as JSON-String for the given submodel
     */
    public String getSubmodelRawPayload(final String submodelEndpointAddress) {
        final String submodel = this.submodelClient.getSubmodel(submodelEndpointAddress);
        log.info("Returning Submodel as String: '{}'", submodel);
        return submodel;
    }

    private boolean thereAreChildParts(final AssemblyPartRelationship submodel) {
        return submodel.getChildParts() != null;
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
