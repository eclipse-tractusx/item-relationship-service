/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

/**
 * Check and validate Policy in Catalog fetch from EDC providers.
 */
@Slf4j
@Service("irsEdcClientPolicyCheckerService")
@RequiredArgsConstructor
public class PolicyCheckerService {

    private final AcceptedPoliciesProvider policyStore;
    private final ConstraintCheckerService constraintCheckerService;

    public boolean isValid(final Policy policy) {
        if (getValidStoredPolicyIds().contains("*")) {
            return true;
        }

        return policy.getPermissions().stream().allMatch(permission -> isValid(permission, getValidStoredPolicies()));
    }

    private boolean isValid(final Permission permission, final List<AcceptedPolicy> validStoredPolicies) {
        return validStoredPolicies.stream().anyMatch(acceptedPolicy ->
                constraintCheckerService.hasAllConstraint(acceptedPolicy.policy(), permission.getConstraints()));
    }

    private List<String> getValidStoredPolicyIds() {
        return policyStore.getAcceptedPolicies()
                          .stream()
                          .filter(p -> p.validUntil().isAfter(OffsetDateTime.now()))
                          .map(acceptedPolicy -> acceptedPolicy.policy().getPolicyId())
                          .flatMap(this::addEncodedVersion)
                          .toList();
    }

    private List<AcceptedPolicy> getValidStoredPolicies() {
        return policyStore.getAcceptedPolicies()
                          .stream()
                          .filter(p -> p.validUntil().isAfter(OffsetDateTime.now()))
                          .toList();
    }

    private Stream<String> addEncodedVersion(final String original) {
        return Stream.of(original, UriUtils.encode(original, StandardCharsets.UTF_8));
    }

}
