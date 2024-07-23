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

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.eclipse.tractusx.irs.common.persistence.BlobPersistence.DEFAULT_BLOB_NAME;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.policy.AcceptedPoliciesProvider;
import org.eclipse.tractusx.irs.edc.client.policy.AcceptedPolicy;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcTransformer;
import org.eclipse.tractusx.irs.policystore.config.DefaultAcceptedPoliciesConfig;
import org.eclipse.tractusx.irs.policystore.exceptions.PolicyStoreException;
import org.eclipse.tractusx.irs.policystore.exceptions.ResourceDoesNotExistException;
import org.eclipse.tractusx.irs.policystore.models.CreatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.models.UpdatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.persistence.PolicyPersistence;
import org.eclipse.tractusx.irs.policystore.validators.PolicyValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service to manage stored policies in IRS.
 */
@Service
@Slf4j
@SuppressWarnings({ "PMD.ExcessiveImports",
                    "PMD.TooManyMethods",
                    "PMD.GodClass"
})
public class PolicyStoreService implements AcceptedPoliciesProvider {

    private static final TypeReference<List<Policy>> LIST_OF_POLICIES_TYPE = new TypeReference<>() {
    };

    private final List<Policy> allowedPoliciesFromConfig;

    private final PolicyPersistence persistence;

    private final EdcTransformer edcTransformer;

    private final Clock clock;

    /**
     * Under this "virtual" BPN custom default policies are stored
     */
    /* package */ static final String BPN_DEFAULT = "default";

    public PolicyStoreService(final DefaultAcceptedPoliciesConfig defaultAcceptedPoliciesConfig,
            final PolicyPersistence persistence, final EdcTransformer edcTransformer, final Clock clock) {

        this.clock = clock;

        this.allowedPoliciesFromConfig = createDefaultPolicyFromConfig(defaultAcceptedPoliciesConfig);
        this.persistence = persistence;
        this.edcTransformer = edcTransformer;
    }

    /**
     * Registers a policy.
     *
     * @param request the {@link CreatePolicyRequest}
     * @return the registered policy
     */
    public Policy registerPolicy(final CreatePolicyRequest request) {

        final Policy registeredPolicy;
        final JsonObject policyJson = request.payload();

        final Policy policy = edcTransformer.transformToIrsPolicy(policyJson);
        policy.setValidUntil(request.validUntil());

        registeredPolicy = doRegisterPolicy(policy,
                request.businessPartnerNumber() == null ? BPN_DEFAULT : request.businessPartnerNumber());

        return registeredPolicy;
    }

    /* package */ Policy doRegisterPolicy(final Policy policy, final String businessPartnersNumber) {
        PolicyValidator.validate(policy);
        policy.setCreatedOn(OffsetDateTime.now(clock));
        log.info("Registering new policy with id {}, valid until {}", policy.getPolicyId(), policy.getValidUntil());
        try {
            return persistence.save(businessPartnersNumber, policy);
        } catch (final PolicyStoreException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Finds policies by list of BPN.
     *
     * @param bpnList list of BPNs
     * @return a map that maps BPN to list of policies
     */
    public Map<String, List<Policy>> getPolicies(final List<String> bpnList) {
        if (bpnList == null) {
            return getAllStoredPolicies();
        } else {
            return bpnList.stream().map(bpn -> {
                final List<Policy> storedPolicies = getStoredPolicies(List.of(bpn));
                return new AbstractMap.SimpleEntry<>(bpn, storedPolicies);
            }).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        }
    }

    public List<Policy> getStoredPolicies(final List<String> businessPartnerNumbers) {
        log.info("Reading all stored polices for BPN {}", businessPartnerNumbers);
        final List<Policy> storedPolicies = new LinkedList<>();
        for (final String bpn : businessPartnerNumbers) {
            storedPolicies.addAll(persistence.readAll(bpn));
        }

        // Policies not associated with a BPN (default policies) should only be returned
        // if there are no policies that are registered for this BPN explicitly (see #199).
        if (storedPolicies.isEmpty()) {
            storedPolicies.addAll(persistence.readAll(DEFAULT_BLOB_NAME));
        }

        if (storedPolicies.isEmpty()) {
            log.info("Policy store is empty, returning default values from config");
            return allowedPoliciesFromConfig;
        } else {
            return storedPolicies;
        }
    }

    public List<AcceptedPolicy> getConfiguredDefaultPolicies() {
        return allowedPoliciesFromConfig.stream().map(this::toAcceptedPolicy).toList();
    }

    public Map<String, List<Policy>> getAllStoredPolicies() {
        final Map<String, List<Policy>> bpnToPolicies = persistence.readAll();
        if (containsNoDefaultPolicy(bpnToPolicies)) {
            bpnToPolicies.put(BPN_DEFAULT, allowedPoliciesFromConfig);
        }
        return bpnToPolicies;
    }

    private static boolean containsNoDefaultPolicy(final Map<String, List<Policy>> bpnToPolicies) {
        return bpnToPolicies.keySet().stream().noneMatch(key -> StringUtils.isEmpty(key) || BPN_DEFAULT.equals(key));
    }

    public void deletePolicy(final String policyId) {

        handleTrialToModifyFallbackPolicy(policyId);

        log.info("Getting all policies to find correct BPN");
        final List<String> bpnsContainingPolicyId = PolicyHelper.findBpnsByPolicyId(getAllStoredPolicies(), policyId);

        if (bpnsContainingPolicyId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Policy with id '%s' not found".formatted(policyId));
        } else {
            try {
                log.info("Deleting policy with id {}", policyId);
                bpnsContainingPolicyId.forEach(bpn -> persistence.delete(bpn, policyId));
            } catch (final ResourceDoesNotExistException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
            } catch (final PolicyStoreException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
            }
        }
    }

    public void deletePolicyForEachBpn(final String policyId, final List<String> bpnList) {

        handleTrialToModifyFallbackPolicy(policyId);

        try {
            for (final String bpn : bpnList) {
                persistence.delete(bpn, policyId);
            }
        } catch (final ResourceDoesNotExistException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (final PolicyStoreException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    private boolean isFallbackPolicyFromConfiguration(final String policyId) {
        return this.allowedPoliciesFromConfig.stream().map(Policy::getPolicyId).anyMatch(p -> p.equals(policyId));
    }

    public void updatePolicies(final UpdatePolicyRequest request) {
        for (final String policyId : request.policyIds()) {
            updatePolicy(policyId, request.validUntil(),
                    request.businessPartnerNumbers() == null ? List.of(BPN_DEFAULT) : request.businessPartnerNumbers());
        }
    }

    public void updatePolicy(final String policyId, final OffsetDateTime newValidUntil,
            final List<String> newBusinessPartnerNumbers) {

        handleTrialToModifyFallbackPolicy(policyId);

        log.info("Updating policy with id {}", policyId);

        final List<String> businessPartnerNumbersContainingPolicyId = findBusinessPartnerNumbersByPolicyId(policyId);

        final Policy policyToUpdate = findPolicy(policyId, businessPartnerNumbersContainingPolicyId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Policy with id '" + policyId + "' doesn't exists!"));

        deletePolicyForEachBpn(policyId, businessPartnerNumbersContainingPolicyId);
        updatePolicy(policyToUpdate, newValidUntil, newBusinessPartnerNumbers);
    }

    private void updatePolicy(final Policy policyToUpdate, final OffsetDateTime newValidUntil,
            final List<String> newBusinessPartnerNumbers) {
        try {
            policyToUpdate.update(newValidUntil);

            for (final String bpn : newBusinessPartnerNumbers) {
                persistence.save(bpn, policyToUpdate);
            }

        } catch (final PolicyStoreException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    private List<String> findBusinessPartnerNumbersByPolicyId(final String policyId) {
        final List<String> businessPartnerNumbersContainingPolicyId;
        try {
            final Map<String, List<Policy>> allStoredPolicies = getAllStoredPolicies();
            businessPartnerNumbersContainingPolicyId = PolicyHelper.findBpnsByPolicyId(allStoredPolicies, policyId);
        } catch (PolicyStoreException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return businessPartnerNumbersContainingPolicyId;
    }

    private Optional<Policy> findPolicy(final String policyId, final List<String> bpnsContainingPolicyId) {
        return getStoredPolicies(bpnsContainingPolicyId).stream()
                                                        .filter(PolicyHelper.havingPolicyId(policyId))
                                                        .findAny();
    }

    @Override
    public List<AcceptedPolicy> getAcceptedPolicies(final String bpn) {

        if (bpn == null) {
            return getCustomPolicyOrFallback();
        }

        final List<Policy> storedPolicies = getStoredPolicies(List.of(bpn));
        final Stream<Policy> result = sortByPolicyId(storedPolicies);
        return result.map(this::toAcceptedPolicy).toList();
    }

    /**
     * Gets either the custom default policies from the database
     * or the fallback default policies from the configuration.
     *
     * @return list of default policies
     */
    private List<AcceptedPolicy> getCustomPolicyOrFallback() {
        final List<Policy> defaultPoliciesFromDb = getAllStoredPolicies().get(BPN_DEFAULT);
        if (isNotEmpty(defaultPoliciesFromDb)) {
            return defaultPoliciesFromDb.stream().map(this::toAcceptedPolicy).toList();
        } else {
            return getConfiguredDefaultPolicies();
        }
    }

    private static Stream<Policy> sortByPolicyId(final List<Policy> policies) {
        final TreeSet<Policy> result = new TreeSet<>(Comparator.comparing(Policy::getPolicyId));
        result.addAll(policies);
        return result.stream();
    }

    private AcceptedPolicy toAcceptedPolicy(final Policy policy) {
        return new AcceptedPolicy(policy, policy.getValidUntil());
    }

    private List<Policy> createDefaultPolicyFromConfig(final DefaultAcceptedPoliciesConfig defaultPoliciesConfig) {
        return StringMapper.mapFromBase64String(defaultPoliciesConfig.getAcceptedPolicies(), LIST_OF_POLICIES_TYPE);
    }

    private void handleTrialToModifyFallbackPolicy(final String policyId) {
        if (isFallbackPolicyFromConfiguration(policyId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    ("The policy '%s' is a configured default policy which cannot be modified. "
                     + "However you can define custom default policies via the API by registering a policy "
                     + "without a business partner number. "
                     + "These will take precedence over configured default policies.").formatted(policyId));
        }
    }

}
