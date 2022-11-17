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

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.RelationshipAspect;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.RelationshipSubmodel;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.edc.model.NegotiationResponse;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Slf4j
@Service
@RequiredArgsConstructor
public class EdcSubmodelFacade {

    private final ContractNegotiationService contractNegotiationService;
    private final EdcDataPlaneClient edcDataPlaneClient;
    private final EndpointDataReferenceStorage endpointDataReferenceStorage;
    private final JsonUtil jsonUtil;

    public List<Relationship> getRelationships(final String submodelEndpointAddress, final RelationshipAspect traversalAspectType)
            throws InterruptedException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("Get EDC Submodel task for relationships");

        final String submodel = "/submodel";
        final int indexOfUrn = findIndexOf(submodelEndpointAddress, "/urn");
        final int indexOfSubModel = findIndexOf(submodelEndpointAddress, submodel);

        if (indexOfUrn == -1 || indexOfSubModel == -1) {
            throw new IllegalArgumentException("Cannot rewrite endpoint address, malformed format: " + submodelEndpointAddress);
        }

        final String providerConnectorUrl = submodelEndpointAddress.substring(0, indexOfUrn);
        final String target = submodelEndpointAddress.substring(indexOfUrn, indexOfSubModel);

        log.info("Starting contract negotiation with providerConnectorUrl {} and target {}", providerConnectorUrl, target);
        final NegotiationResponse negotiationResponse = contractNegotiationService.negotiate(providerConnectorUrl, target);

        EndpointDataReference dataReference = null;
        // need to add timeout break
        while (dataReference == null) {
            Thread.sleep(1000);
            log.info("Retrieving dataReference from storage for contractAgreementId {}", negotiationResponse.getContractAgreementId());
            dataReference = endpointDataReferenceStorage.get(negotiationResponse.getContractAgreementId());
        }

        log.info("Retrieving data from EDC data plane with dataReference {}:{}", dataReference.getAuthKey(), dataReference.getAuthCode());
        final String data = edcDataPlaneClient.getData(dataReference, submodel);

        final RelationshipSubmodel relationshipSubmodel = jsonUtil.fromString(data, traversalAspectType.getSubmodelClazz());
        stopWatch.stop();
        log.info("EDC Task {} took {} ms for endpoint address: {}", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis(), submodelEndpointAddress);
        return relationshipSubmodel.asRelationships();
    }

    private int findIndexOf(final String endpointAddress, final String str) {
        return endpointAddress.indexOf(str);
    }

}
