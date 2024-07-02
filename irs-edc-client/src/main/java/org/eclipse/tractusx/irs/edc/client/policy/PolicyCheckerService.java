/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
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
package org.eclipse.tractusx.irs.edc.client.policy;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.springframework.stereotype.Service;

/**
 * Check and validate Policy in Catalog fetch from EDC providers.
 */
@Slf4j
@Service("irsEdcClientPolicyCheckerService")
@RequiredArgsConstructor
public class PolicyCheckerService {

    private final AcceptedPoliciesProvider policyStore;
    private final ConstraintCheckerService constraintCheckerService;

    public boolean isValid(final Policy policy, final String bpn) {
        final List<AcceptedPolicy> validStoredPolicies = getValidStoredPolicies(bpn);
        return policy.getPermissions()
                     .stream()
                     .allMatch(permission -> hasValidConstraints(permission, validStoredPolicies));
    }

    private boolean hasValidConstraints(final Permission permission, final List<AcceptedPolicy> validStoredPolicies) {
        return validStoredPolicies.stream()
                                  .anyMatch(acceptedPolicy -> constraintCheckerService.hasAllConstraint(
                                          acceptedPolicy.policy(), permission.getConstraints()));
    }

    public boolean isExpired(final Policy policy, final String bpn) {
        return policy.getPermissions()
                     .stream()
                     .allMatch(permission -> hasExpiredConstraint(permission, getValidStoredPolicies(bpn)));
    }

    private boolean hasExpiredConstraint(final Permission permission, final List<AcceptedPolicy> validStoredPolicies) {
        return validStoredPolicies.stream()
                                  .filter(acceptedPolicy -> constraintCheckerService.hasAllConstraint(
                                          acceptedPolicy.policy(), permission.getConstraints()))
                                  .allMatch(
                                          acceptedPolicy -> acceptedPolicy.validUntil().isBefore(OffsetDateTime.now()));
    }

    public List<AcceptedPolicy> getValidStoredPolicies(final String bpn) {
        return policyStore.getAcceptedPolicies(bpn).stream().toList();
    }

}
