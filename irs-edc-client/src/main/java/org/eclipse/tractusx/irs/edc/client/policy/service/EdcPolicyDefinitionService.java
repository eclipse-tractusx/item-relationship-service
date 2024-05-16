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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.asset.model.OdrlContext;
import org.eclipse.tractusx.irs.edc.client.contract.model.EdcOperator;
import org.eclipse.tractusx.irs.edc.client.policy.model.EdcCreatePolicyDefinitionRequest;
import org.eclipse.tractusx.irs.edc.client.policy.model.EdcPolicy;
import org.eclipse.tractusx.irs.edc.client.policy.model.EdcPolicyPermission;
import org.eclipse.tractusx.irs.edc.client.policy.model.EdcPolicyPermissionConstraint;
import org.eclipse.tractusx.irs.edc.client.policy.model.EdcPolicyPermissionConstraintExpression;
import org.eclipse.tractusx.irs.edc.client.policy.model.exception.CreateEdcPolicyDefinitionException;
import org.eclipse.tractusx.irs.edc.client.policy.model.exception.DeleteEdcPolicyDefinitionException;
import org.eclipse.tractusx.irs.edc.client.policy.model.exception.EdcPolicyDefinitionAlreadyExists;
import org.eclipse.tractusx.irs.edc.client.policy.model.exception.GetEdcPolicyDefinitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service providing Edc Policies creation
 */

@Slf4j
@RequiredArgsConstructor
public class EdcPolicyDefinitionService {

    private static final String POLICY_TYPE = "Policy";
    private static final String POLICY_DEFINITION_TYPE = "PolicyDefinitionRequestDto";
    private static final String ATOMIC_CONSTRAINT = "AtomicConstraint";
    private static final String CONSTRAINT = "Constraint";
    private static final String OPERATOR_PREFIX = "odrl:";
    private static final String USE_ACTION = "use";

    private final EdcConfiguration config;
    private final RestTemplate restTemplate;

    public String createAccessPolicy(final String policyName) throws CreateEdcPolicyDefinitionException {
        final String accessPolicyId = UUID.randomUUID().toString();

        return createAccessPolicy(policyName, accessPolicyId);
    }

    public String createAccessPolicy(final String policyName, final String policyId)
            throws CreateEdcPolicyDefinitionException {
        final EdcCreatePolicyDefinitionRequest request = createPolicyDefinition(policyName, policyId);

        return createAccessPolicy(request);
    }

    public String createAccessPolicy(final EdcCreatePolicyDefinitionRequest policyRequest)
            throws CreateEdcPolicyDefinitionException {
        final ResponseEntity<String> createPolicyDefinitionResponse;
        try {
            createPolicyDefinitionResponse = restTemplate.postForEntity(
                    config.getControlplane().getEndpoint().getPolicyDefinition(), policyRequest, String.class);
        } catch (HttpClientErrorException e) {
            log.error("Failed to create EDC policy definition. Reason: ", e);
            if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
                throw new EdcPolicyDefinitionAlreadyExists("Policy definition already exists in the EDC", e);
            }
            throw new CreateEdcPolicyDefinitionException(e);
        }

        final HttpStatusCode responseCode = createPolicyDefinitionResponse.getStatusCode();

        if (responseCode.value() == HttpStatus.OK.value()) {
            return policyRequest.getPolicyDefinitionId();
        }

        throw new CreateEdcPolicyDefinitionException("Failed to create EDC policy definition for asset");
    }

    public boolean policyDefinitionExists(final String policyName) throws GetEdcPolicyDefinitionException {

        try {
            final ResponseEntity<String> getPolicyDefinitionRequest = restTemplate.getForEntity(
                    config.getControlplane().getEndpoint().getPolicyDefinition() + "/" + policyName, String.class);

            final HttpStatusCode responseCode = getPolicyDefinitionRequest.getStatusCode();

            if (responseCode.value() == HttpStatus.OK.value()) {
                return true;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                log.info(String.format("Policy with id %s not found within the edc", policyName));
                return false;
            } else {
                throw new GetEdcPolicyDefinitionException(e);
            }
        }
        return false;
    }

    public EdcCreatePolicyDefinitionRequest createPolicyDefinition(final String policyName,
            final String accessPolicyId) {
        final EdcPolicyPermissionConstraintExpression constraint = EdcPolicyPermissionConstraintExpression.builder()
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

        final EdcPolicyPermissionConstraint edcPolicyPermissionConstraint = EdcPolicyPermissionConstraint.builder()
                                                                                                         .orExpressions(
                                                                                                                 List.of(constraint))
                                                                                                         .type(ATOMIC_CONSTRAINT)
                                                                                                         .build();

        final EdcPolicyPermission odrlPermissions = EdcPolicyPermission.builder()
                                                                       .action(USE_ACTION)
                                                                       .edcPolicyPermissionConstraints(
                                                                               edcPolicyPermissionConstraint)
                                                                       .build();

        final EdcPolicy edcPolicy = EdcPolicy.builder()
                                             .odrlPermissions(List.of(odrlPermissions))
                                             .type(POLICY_TYPE)
                                             .build();

        final OdrlContext odrlContext = OdrlContext.builder().odrl(NAMESPACE_ODRL).build();

        return EdcCreatePolicyDefinitionRequest.builder()
                                               .policyDefinitionId(accessPolicyId)
                                               .policy(edcPolicy)
                                               .odrlContext(odrlContext)
                                               .type(POLICY_DEFINITION_TYPE)
                                               .build();
    }

    public void deleteAccessPolicy(final String accessPolicyId) throws DeleteEdcPolicyDefinitionException {
        final String deleteUri = UriComponentsBuilder.fromPath(
                                                             config.getControlplane().getEndpoint().getPolicyDefinition())
                                                     .pathSegment("{accessPolicyId}")
                                                     .buildAndExpand(accessPolicyId)
                                                     .toUriString();

        try {
            restTemplate.delete(deleteUri);
        } catch (RestClientException e) {
            log.error("Failed to delete EDC notification asset policy {}. Reason: ", accessPolicyId, e);
            throw new DeleteEdcPolicyDefinitionException(e);
        }
    }
}
