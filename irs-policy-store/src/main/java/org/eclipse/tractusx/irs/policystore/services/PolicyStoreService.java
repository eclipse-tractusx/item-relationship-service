/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.policystore.services;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.edc.client.policy.AcceptedPoliciesProvider;
import org.eclipse.tractusx.irs.edc.client.policy.AcceptedPolicy;
import org.eclipse.tractusx.irs.policystore.exceptions.PolicyStoreException;
import org.eclipse.tractusx.irs.policystore.models.CreatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.models.Policy;
import org.eclipse.tractusx.irs.policystore.models.UpdatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.persistence.PolicyPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service to manage stored policies in IRS.
 */
@Service
@Slf4j
public class PolicyStoreService implements AcceptedPoliciesProvider {

    public static final int DEFAULT_POLICY_LIFETIME_YEARS = 100;
    private final String apiAllowedBpn;
    private final Clock clock;
    private final List<Policy> allowedPoliciesFromConfig;
    private final PolicyPersistence persistence;

    public PolicyStoreService(@Value("${apiAllowedBpn:}") final String apiAllowedBpn,
            @Value("${edc.catalog.policies.allowedNames}") final List<String> allowedPolicies,
            final PolicyPersistence persistence, final Clock clock) {
        this.apiAllowedBpn = apiAllowedBpn;
        this.allowedPoliciesFromConfig = allowedPolicies.stream()
                                                        .map(p -> new Policy(p, OffsetDateTime.now(),
                                                                OffsetDateTime.now().plusYears(
                                                                        DEFAULT_POLICY_LIFETIME_YEARS),
                                                                Collections.emptyList()))
                                                        .toList();
        this.persistence = persistence;
        this.clock = clock;
    }

    public void registerPolicy(final CreatePolicyRequest request) {
        log.info("Registering new policy with id {}, valid until {}", request.policyId(), request.validUntil());
        try {
            persistence.save(apiAllowedBpn, new Policy(request.policyId(), OffsetDateTime.now(clock), request.validUntil(), request.permissions()));
        } catch (final PolicyStoreException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    public List<Policy> getStoredPolicies() {
        log.info("Reading all stored polices for BPN {}", apiAllowedBpn);
        final var storedPolicies = persistence.readAll(apiAllowedBpn);
        if (storedPolicies.isEmpty()) {
            log.info("Policy store is empty, returning default values from config");
            return allowedPoliciesFromConfig;
        } else {
            return storedPolicies;
        }
    }

    public void deletePolicy(final String policyId) {
        try {
            log.info("Deleting policy with id {}", policyId);
            persistence.delete(apiAllowedBpn, policyId);
        } catch (final PolicyStoreException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }

    }

    public void updatePolicy(final String policyId, final UpdatePolicyRequest request) {
        try {
            log.info("Updating policy with id {}", policyId);
            persistence.update(apiAllowedBpn, policyId, request.validUntil());
        } catch (final PolicyStoreException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @Override
    public List<AcceptedPolicy> getAcceptedPolicies() {
        return getStoredPolicies().stream().map(this::toAcceptedPolicy).toList();
    }

    private AcceptedPolicy toAcceptedPolicy(final Policy policy) {
        return new AcceptedPolicy(policy.getPolicyId(), policy.getValidUntil());
    }

}
