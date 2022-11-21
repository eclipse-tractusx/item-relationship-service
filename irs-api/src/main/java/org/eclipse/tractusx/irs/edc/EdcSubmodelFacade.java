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
package org.eclipse.tractusx.irs.edc;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.RelationshipAspect;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.RelationshipSubmodel;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.edc.model.NegotiationResponse;
import org.eclipse.tractusx.irs.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.services.AsyncPollingService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 * Public API facade for EDC domain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EdcSubmodelFacade {

    public static final int MAXIMUM_TASK_RUNTIME_MINUTES = 10;
    private final ContractNegotiationService contractNegotiationService;
    private final EdcDataPlaneClient edcDataPlaneClient;
    private final EndpointDataReferenceStorage endpointDataReferenceStorage;
    private final JsonUtil jsonUtil;
    private final AsyncPollingService pollingService;

    public CompletableFuture<List<Relationship>> getRelationships(final String submodelEndpointAddress,
            final RelationshipAspect traversalAspectType) throws EdcClientException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("Get EDC Submodel task for relationships, endpoint " + submodelEndpointAddress);

        final String submodel = "/submodel";
        final int indexOfUrn = findIndexOf(submodelEndpointAddress, "/urn");
        final int indexOfSubModel = findIndexOf(submodelEndpointAddress, submodel);

        if (indexOfUrn == -1 || indexOfSubModel == -1) {
            throw new EdcClientException(
                    "Cannot rewrite endpoint address, malformed format: " + submodelEndpointAddress);
        }

        final String providerConnectorUrl = submodelEndpointAddress.substring(0, indexOfUrn);
        final String target = submodelEndpointAddress.substring(indexOfUrn + 1, indexOfSubModel);
        log.info("Starting contract negotiation with providerConnectorUrl {} and target {}", providerConnectorUrl,
                target);
        final NegotiationResponse negotiationResponse = contractNegotiationService.negotiate(providerConnectorUrl,
                target);

        return startSubmodelDataRetrieval(traversalAspectType, submodel, negotiationResponse.getContractAgreementId(),
                stopWatch);
    }

    private CompletableFuture<List<Relationship>> startSubmodelDataRetrieval(
            final RelationshipAspect traversalAspectType, final String submodel, final String contractAgreementId,
            final StopWatch stopWatch) {

        return pollingService.<List<Relationship>>createJob()
                             .action(() -> retrieveSubmodelData(traversalAspectType, submodel, contractAgreementId,
                                     stopWatch))
                             .timeToLive(Duration.ofMinutes(MAXIMUM_TASK_RUNTIME_MINUTES))
                             .description("waiting for submodel retrieval")
                             .build()
                             .schedule();

    }

    private Optional<List<Relationship>> retrieveSubmodelData(final RelationshipAspect traversalAspectType,
            final String submodel, final String contractAgreementId, final StopWatch stopWatch) {
        log.info("Retrieving dataReference from storage for contractAgreementId {}", contractAgreementId);
        final Optional<EndpointDataReference> dataReference = endpointDataReferenceStorage.get(contractAgreementId);

        if (dataReference.isPresent()) {
            final EndpointDataReference ref = dataReference.get();
            log.info("Retrieving data from EDC data plane with dataReference {}:{}", ref.getAuthKey(),
                    ref.getAuthCode());
            final String data = edcDataPlaneClient.getData(ref, submodel);

            final RelationshipSubmodel relationshipSubmodel = jsonUtil.fromString(data,
                    traversalAspectType.getSubmodelClazz());

            final List<Relationship> relationships = relationshipSubmodel.asRelationships();

            stopWatch.stop();
            log.info("EDC Task '{}' took {} ms", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());

            return Optional.of(relationships);
        }
        return Optional.empty();
    }

    private int findIndexOf(final String endpointAddress, final String str) {
        return endpointAddress.indexOf(str);
    }

}
