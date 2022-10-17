/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.aaswrapper.submodel.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.dto.RelationshipAspect;
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

        final BomLifecycle lifecycleContext = jobData.getBomLifecycle();
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
            final BomLifecycle lifecycleContext) {
        submodelParts.removeIf(isNotLifecycleContext(lifecycleContext));
    }

    private boolean shouldFilterByLifecycleContext(final BomLifecycle lifecycleContext) {
        return lifecycleContext != null;
    }

    private Predicate<ChildData> isNotLifecycleContext(final BomLifecycle lifecycleContext) {
        return childData -> !childData.getLifecycleContext().getValue().equals(lifecycleContext.getLifecycleContextCharacteristicValue());
    }
}
