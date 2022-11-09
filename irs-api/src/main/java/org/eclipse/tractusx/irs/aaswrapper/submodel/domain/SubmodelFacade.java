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

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.Relationship;
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
     * @param traversalAspectType aspect type for traversal informations
     * @return The Aspect Model for the given submodel
     */
    public List<Relationship> getRelationships(final String submodelEndpointAddress, final RelationshipAspect traversalAspectType) {
        final RelationshipSubmodel relationshipSubmodel = this.submodelClient.getSubmodel(submodelEndpointAddress, traversalAspectType.getSubmodelClazz());

        return relationshipSubmodel.asRelationships();
    }

    /**
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @return The Aspect Model as JSON-String for the given submodel
     */
    public String getSubmodelRawPayload(final String submodelEndpointAddress) {
        final String submodel = this.submodelClient.getSubmodel(submodelEndpointAddress);
        log.info("Retrieved Submodel as raw string from endpoint: '{}'", submodelEndpointAddress);
        return submodel;
    }

}
