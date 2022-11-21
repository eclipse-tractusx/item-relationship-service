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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.edc.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.exceptions.EdcClientException;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 * Public API Facade for submodel domain
 */
@Slf4j
@Service
@AllArgsConstructor
public class SubmodelFacade {

    private final SubmodelClient submodelClient;
    private final EdcSubmodelFacade edcSubmodelFacade;

    /**
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @param traversalAspectType     aspect type for traversal information
     * @return The Aspect Model for the given submodel
     */
    public List<Relationship> getRelationships(final String submodelEndpointAddress,
            final RelationshipAspect traversalAspectType)
            throws ExecutionException, InterruptedException, EdcClientException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("Get Submodel task for relationships");
        final CompletableFuture<List<Relationship>> relationships = edcSubmodelFacade.getRelationships(
                submodelEndpointAddress, traversalAspectType);
        stopWatch.stop();
        final List<Relationship> relationshipsResponse = relationships.get();
        log.info("Task {} took {} ms for endpoint address: {}", stopWatch.getLastTaskName(),
                stopWatch.getLastTaskTimeMillis(), submodelEndpointAddress);
        return relationshipsResponse;
    }

    /**
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @return The Aspect Model as JSON-String for the given submodel
     */
    public String getSubmodelRawPayload(final String submodelEndpointAddress) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("Get Submodel raw payload task");
        final String submodel = this.submodelClient.getSubmodel(submodelEndpointAddress);
        log.info("Retrieved Submodel as raw string from endpoint: '{}'", submodelEndpointAddress);
        stopWatch.stop();
        log.info("Task {} took {} ms for endpoint address: {}", stopWatch.getLastTaskName(),
                stopWatch.getLastTaskTimeMillis(), submodelEndpointAddress);
        return submodel;
    }

}
