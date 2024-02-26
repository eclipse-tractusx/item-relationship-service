/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.edc.client.contract.service;

import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiation;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.contract.model.EdcContractAgreementListWrapper;
import org.eclipse.tractusx.irs.edc.client.contract.model.exception.ContractAgreementException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


/**
 * EdcContractAgreementService used for contract agreements and contract agreement negotiation details
 */
@Slf4j
@RequiredArgsConstructor
public class EdcContractAgreementService {

    private final EdcConfiguration config;
    private final RestTemplate restTemplate;

    public List<ContractAgreement> getContractAgreements(final String... contractAgreementIds) {

        final QuerySpec querySpec = buildQuerySpec(contractAgreementIds);
        final ResponseEntity<EdcContractAgreementListWrapper> edcContractAgreementListResponseEntity = restTemplate.postForEntity(
                config.getControlplane().getEndpoint().getContractAgreements(), querySpec,
                EdcContractAgreementListWrapper.class);

        if (edcContractAgreementListResponseEntity.getBody() != null) {
            return edcContractAgreementListResponseEntity.getBody().getContractAgreementList();
        } else {
            throw new ContractAgreementException(
                    "Empty message body on edc response: " + edcContractAgreementListResponseEntity);
        }

    }

    public ContractNegotiation getContractAgreementNegotiation(final String contractAgreementId) {
        final ResponseEntity<ContractNegotiation> contractNegotiationResponseEntity = restTemplate.getForEntity(
                config.getControlplane().getEndpoint().getContractAgreements() + "/" + contractAgreementId
                        + "/negotiation", ContractNegotiation.class);
        return contractNegotiationResponseEntity.getBody();
    }

    private QuerySpec buildQuerySpec(final String... contractAgreementIds) {

        final List<Criterion> criterionList = Arrays.stream(contractAgreementIds)
                                                    .map(id -> Criterion.Builder.newInstance()
                                                                                .operandLeft(
                                                                                        "https://w3id.org/edc/v0.0.1/ns/assetId")
                                                                                .operator("=")
                                                                                .operandRight(id)
                                                                                .build())
                                                    .toList();
        return QuerySpec.Builder.newInstance().filter(criterionList).build();
    }

}
