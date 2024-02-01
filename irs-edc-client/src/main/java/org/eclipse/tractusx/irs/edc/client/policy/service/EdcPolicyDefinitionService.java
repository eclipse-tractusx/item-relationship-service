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
package org.eclipse.tractusx.irs.edc.client.policy.service;

import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_ODRL;

import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.edc.client.asset.model.OdrlContext;
import org.eclipse.tractusx.irs.edc.client.contract.model.EdcOperator;
import org.eclipse.tractusx.irs.edc.client.policy.model.EdcCreatePolicyDefinitionRequest;
import org.eclipse.tractusx.irs.edc.client.policy.model.EdcPolicy;
import org.eclipse.tractusx.irs.edc.client.policy.model.EdcPolicyPermission;
import org.eclipse.tractusx.irs.edc.client.policy.model.EdcPolicyPermissionConstraint;
import org.eclipse.tractusx.irs.edc.client.policy.model.EdcPolicyPermissionConstraintExpression;
import org.eclipse.tractusx.irs.edc.client.policy.model.exception.CreateEdcPolicyDefinitionException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service providing Edc Policies creation
 */

@Slf4j
public class EdcPolicyDefinitionService {

    private static final String USE_ACTION = "USE";
    private static final String POLICY_TYPE = "Policy";
    private static final String POLICY_DEFINITION_TYPE = "PolicyDefinitionRequestDto";
    private static final String ATOMIC_CONSTRAINT = "AtomicConstraint";
    private static final String CONSTRAINT = "Constraint";
    private static final String OPERATOR_PREFIX = "odrl:";

    private static final String POLICY_DEFINITION_PATH = "/management/v2/policydefinitions";

    public String createAccessPolicy(String policyName, RestTemplate restTemplate) {
        String accessPolicyId = UUID.randomUUID().toString();
        final EdcCreatePolicyDefinitionRequest request = createPolicyDefinition(policyName, accessPolicyId);

        final ResponseEntity<String> createPolicyDefinitionResponse;
        try {
            createPolicyDefinitionResponse = restTemplate.postForEntity(POLICY_DEFINITION_PATH, request, String.class);
        } catch (RestClientException e) {
            log.error("Failed to create EDC notification asset policy. Reason: ", e);

            throw new CreateEdcPolicyDefinitionException(e);
        }

        HttpStatusCode responseCode = createPolicyDefinitionResponse.getStatusCode();

        if (responseCode.value() == 409) {
            log.info("Notification asset policy definition already exists in the EDC");

            throw new CreateEdcPolicyDefinitionException("Asset policy definition already exists in the EDC");
        }

        if (responseCode.value() == 200) {
            return request.getPolicyDefinitionId();
        }

        throw new CreateEdcPolicyDefinitionException("Failed to create EDC policy definition for asset");
    }

    public EdcCreatePolicyDefinitionRequest createPolicyDefinition(String policyName, String accessPolicyId) {
        EdcPolicyPermissionConstraintExpression constraint = EdcPolicyPermissionConstraintExpression.builder()
                                                                                                    .leftOperand(
                                                                                                            "PURPOSE")
                                                                                                    .rightOperand(
                                                                                                            policyName)
                                                                                                    .operator(
                                                                                                            new EdcOperator(
                                                                                                                    OPERATOR_PREFIX
                                                                                                                            + "eq"))
                                                                                                    .type(CONSTRAINT)
                                                                                                    .build();

        EdcPolicyPermissionConstraint edcPolicyPermissionConstraint = EdcPolicyPermissionConstraint.builder()
                                                                                                   .orExpressions(
                                                                                                           List.of(constraint))
                                                                                                   .type(ATOMIC_CONSTRAINT)
                                                                                                   .build();

        EdcPolicyPermission odrlPermissions = EdcPolicyPermission.builder()
                                                                 .action(USE_ACTION)
                                                                 .edcPolicyPermissionConstraints(
                                                                         edcPolicyPermissionConstraint)
                                                                 .build();

        EdcPolicy edcPolicy = EdcPolicy.builder().odrlPermissions(List.of(odrlPermissions)).type(POLICY_TYPE).build();

        OdrlContext odrlContext = OdrlContext.builder().odrl(NAMESPACE_ODRL).build();

        return EdcCreatePolicyDefinitionRequest.builder()
                                               .policyDefinitionId(accessPolicyId)
                                               .policy(edcPolicy)
                                               .odrlContext(odrlContext)
                                               .type(POLICY_DEFINITION_TYPE)
                                               .build();
    }

    public void deleteAccessPolicy(String accessPolicyId, RestTemplate restTemplate) {
        String deleteUri = UriComponentsBuilder.fromPath(POLICY_DEFINITION_PATH)
                                               .pathSegment("{accessPolicyId}")
                                               .buildAndExpand(accessPolicyId)
                                               .toUriString();

        try {
            restTemplate.delete(deleteUri);
        } catch (RestClientException e) {
            log.error("Failed to delete EDC notification asset policy {}. Reason: ", accessPolicyId, e);
        }
    }
}
