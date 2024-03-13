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
package org.eclipse.tractusx.irs.policystore.services;

import static org.eclipse.tractusx.irs.common.persistence.BlobPersistence.DEFAULT_BLOB_NAME;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.edc.client.policy.AcceptedPoliciesProvider;
import org.eclipse.tractusx.irs.edc.client.policy.AcceptedPolicy;
import org.eclipse.tractusx.irs.edc.client.policy.Constraint;
import org.eclipse.tractusx.irs.edc.client.policy.Constraints;
import org.eclipse.tractusx.irs.edc.client.policy.Operator;
import org.eclipse.tractusx.irs.edc.client.policy.OperatorType;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyType;
import org.eclipse.tractusx.irs.policystore.config.DefaultAcceptedPoliciesConfig;
import org.eclipse.tractusx.irs.policystore.exceptions.PolicyStoreException;
import org.eclipse.tractusx.irs.policystore.models.UpdatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.persistence.PolicyPersistence;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service to manage stored policies in IRS.
 */
@Service
@Slf4j
public class PolicyStoreService implements AcceptedPoliciesProvider {

    public static final int DEFAULT_POLICY_LIFETIME_YEARS = 5;
    private final List<Policy> allowedPoliciesFromConfig;
    private final PolicyPersistence persistence;
    private final Clock clock;
    private static final String MISSING_REQUEST_FIELD_MESSAGE = "Request does not contain all required fields. Missing: %s";

    public PolicyStoreService(final DefaultAcceptedPoliciesConfig defaultAcceptedPoliciesConfig,
            final PolicyPersistence persistence, final Clock clock) {
        this.allowedPoliciesFromConfig = createDefaultPolicyFromConfig(defaultAcceptedPoliciesConfig);
        this.persistence = persistence;
        this.clock = clock;
    }

    public void registerPolicy(final Policy policy, final List<String> businessPartnersNumbers) {
        validatePolicy(policy);
        policy.setCreatedOn(OffsetDateTime.now(clock));
        log.info("Registering new policy with id {}, valid until {}", policy.getPolicyId(), policy.getValidUntil());
        try {
            persistence.save(businessPartnersNumbers, policy);
        } catch (final PolicyStoreException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Checks whether policy from register policy request has all required fields
     *
     * @param policy policy to register
     */
    private void validatePolicy(final Policy policy) {
        if (policy.getPermissions() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format(MISSING_REQUEST_FIELD_MESSAGE, "odrl:permission"));
        }
        if (policy.getPermissions().stream().anyMatch(p -> p.getConstraint() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format(MISSING_REQUEST_FIELD_MESSAGE, "odrl:constraint"));
        }
    }

    public List<Policy> getStoredPolicies(final List<String> bpns) {
        log.info("Reading all stored polices for BPN {}", bpns);
        final List<Policy> storedPolicies = new LinkedList<>();
        bpns.forEach(bpn -> storedPolicies.addAll(persistence.readAll(bpn)));

        if (storedPolicies.isEmpty()) {
            log.info("Policy store is empty, returning default values from config");
            return allowedPoliciesFromConfig;
        } else {
            return storedPolicies;
        }
    }

    public Map<String, List<Policy>> getAllStoredPolicies() {
        final Map<String, List<Policy>> bpnToPolicies = persistence.readAll();
        if (bpnToPolicies.isEmpty()) {
            return Map.of("", allowedPoliciesFromConfig);
        }
        return bpnToPolicies;
    }

    public void deletePolicy(final String policyId) {
        try {
            log.info("Getting all policies to find correct bpn number");
            final List<String> bpnsContainingPolicyId = getAllStoredPolicies().entrySet()
                                                                              .stream()
                                                                              .filter(entry -> entry.getValue()
                                                                                                    .stream()
                                                                                                    .anyMatch(
                                                                                                            policy -> policy.getPolicyId()
                                                                                                                            .equals(policyId)))
                                                                              .map(Map.Entry::getKey)
                                                                              .toList();

            log.info("Deleting policy with id {}", policyId);
            bpnsContainingPolicyId.forEach(bpn -> persistence.delete(bpn, policyId));
        } catch (final PolicyStoreException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }

    }

    public void updatePolicies(final UpdatePolicyRequest request) {
        for (final String policyId : request.policiesIds()) {
            updatePolicy(policyId, request.validUntil(), request.businessPartnerNumbers());
        }
    }

    private void updatePolicy(final String policyId, final OffsetDateTime validUntil, final List<String> bpns) {
        try {
            log.info("Updating policy with id {}", policyId);
            final List<String> bpnsContainingPolicyId = getAllStoredPolicies().entrySet()
                                                                              .stream()
                                                                              .filter(entry -> entry.getValue()
                                                                                                    .stream()
                                                                                                    .anyMatch(
                                                                                                            policy -> policy.getPolicyId()
                                                                                                                            .equals(policyId)))
                                                                              .map(Map.Entry::getKey)
                                                                              .toList();

            final Policy policyToUpdate = getStoredPolicies(bpnsContainingPolicyId).stream()
                                                                                   .filter(policy -> policy.getPolicyId()
                                                                                                           .equals(policyId))
                                                                                   .findAny()
                                                                                   .orElseThrow(
                                                                                           () -> new PolicyStoreException(
                                                                                                   "Policy with id '"
                                                                                                           + policyId
                                                                                                           + "' doesn't exists!"));

            policyToUpdate.update(validUntil);
            bpnsContainingPolicyId.forEach(bpn -> persistence.delete(bpn, policyId));
            persistence.save(bpns, policyToUpdate);
        } catch (final PolicyStoreException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @Override
    public List<AcceptedPolicy> getAcceptedPolicies(final List<String> bpns) {
        if (bpns == null) {
            return getAllStoredPolicies().values()
                                         .stream()
                                         .flatMap(Collection::stream)
                                         .map(this::toAcceptedPolicy)
                                         .toList();
        }

        final ArrayList<Policy> policies = new ArrayList<>();
        policies.addAll(getStoredPolicies(bpns));
        policies.addAll(getStoredPolicies(List.of(DEFAULT_BLOB_NAME)));

        final TreeSet<Policy> result = new TreeSet<>(Comparator.comparing(Policy::getPolicyId));
        result.addAll(policies);

        return result.stream().map(this::toAcceptedPolicy).toList();
    }

    private AcceptedPolicy toAcceptedPolicy(final Policy policy) {
        return new AcceptedPolicy(policy, policy.getValidUntil());
    }

    private List<Policy> createDefaultPolicyFromConfig(
            final DefaultAcceptedPoliciesConfig defaultAcceptedPoliciesConfig) {
        final List<Constraint> constraints = new ArrayList<>();
        defaultAcceptedPoliciesConfig.getAcceptedPolicies()
                                     .forEach(acceptedPolicy -> constraints.add(
                                             new Constraint(acceptedPolicy.getLeftOperand(),
                                                     new Operator(OperatorType.fromValue(acceptedPolicy.getOperator())),
                                                     acceptedPolicy.getRightOperand())));
        final Policy policy = new Policy("default-policy", OffsetDateTime.now(),
                OffsetDateTime.now().plusYears(DEFAULT_POLICY_LIFETIME_YEARS),
                List.of(new Permission(PolicyType.USE, new Constraints(constraints, constraints))));

        return List.of(policy);
    }

}
