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

import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_EDC;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.edc.client.asset.model.EdcContext;
import org.eclipse.tractusx.irs.edc.client.contract.model.EdcContractDefinitionCriteria;
import org.eclipse.tractusx.irs.edc.client.contract.model.EdcCreateContractDefinitionRequest;
import org.eclipse.tractusx.irs.edc.client.contract.model.exception.CreateEdcContractDefinitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * EdcContractDefinitionService used for contract creation
 */

@Slf4j
public class EdcContractDefinitionService {

    private static final String ASSET_SELECTOR_ID = "https://w3id.org/edc/v0.0.1/ns/id";
    private static final String ASSET_SELECTOR_EQUALITY_OPERATOR = "=";
    private static final String ASSET_SELECTOR_TYPE = "CriterionDto";
    private static final String CONTRACT_DEFINITION_TYPE = "ContractDefinition";

    private static final String CONTRACT_DEFINITION_PATH = "/management/v2/contractdefinitions";

    public String createContractDefinition(String assetId, String policyId, RestTemplate restTemplate) {
        EdcCreateContractDefinitionRequest createContractDefinitionRequest = createContractDefinitionRequest(assetId,
                policyId);
        final ResponseEntity<String> createContractDefinitionResponse;
        try {
            createContractDefinitionResponse = restTemplate.postForEntity(CONTRACT_DEFINITION_PATH,
                    createContractDefinitionRequest, String.class);

            HttpStatusCode responseCode = createContractDefinitionResponse.getStatusCode();

            if (responseCode.value() == HttpStatus.CONFLICT.value()) {
                log.info("{} asset contract definition already exists in the EDC", assetId);

                throw new CreateEdcContractDefinitionException("Asset contract definition already exists in the EDC");
            }

            if (responseCode.value() == HttpStatus.OK.value()) {
                return policyId;
            }

            throw new CreateEdcContractDefinitionException(
                    "Failed to create EDC contract definition for %s notification asset id".formatted(assetId));
        } catch (RestClientException e) {
            log.error(
                    "Failed to create edc contract definition for {} notification asset and {} policy definition id. Reason: ",
                    assetId, policyId, e);

            throw new CreateEdcContractDefinitionException(e);
        }

    }

    public EdcCreateContractDefinitionRequest createContractDefinitionRequest(final String assetId,
            final String accessPolicyId) {
        EdcContractDefinitionCriteria edcContractDefinitionCriteria = EdcContractDefinitionCriteria.builder()
                                                                                                   .type(ASSET_SELECTOR_TYPE)
                                                                                                   .operandLeft(
                                                                                                           ASSET_SELECTOR_ID)
                                                                                                   .operandRight(
                                                                                                           assetId)
                                                                                                   .operator(
                                                                                                           ASSET_SELECTOR_EQUALITY_OPERATOR)
                                                                                                   .build();

        EdcContext edcContext = EdcContext.builder().edc(NAMESPACE_EDC).build();
        return EdcCreateContractDefinitionRequest.builder()
                                                 .contractPolicyId(accessPolicyId)
                                                 .edcContext(edcContext)
                                                 .type(CONTRACT_DEFINITION_TYPE)
                                                 .accessPolicyId(accessPolicyId)
                                                 .id(accessPolicyId)
                                                 .assetsSelector(edcContractDefinitionCriteria)
                                                 .build();
    }
}
