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
package org.eclipse.tractusx.irs.cucumber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.POLICY_TEMPLATE;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.cleanupPolicy;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.extractPoliciesForBpn;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.extractPolicyIdsForBpn;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.fetchAllPolicies;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.fetchPoliciesForBpn;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.fetchPoliciesForBusinessPartnerNumbers;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.fetchPolicyIdsByPrefix;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.getExpectedBpnToPolicyIdsMapping;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.registerPolicyForBpn;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.updatePolicies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.cucumber.AuthenticationProperties.AuthenticationPropertiesBuilder;
import org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.BpnToPolicyId;
import org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.CreatePoliciesResponse;
import org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.PolicyAttributes;

/**
 * Step definitions for Policy Store API.
 */
public class E2ETestStepDefinitionsForPolicyStoreApi {

    private final AuthenticationPropertiesBuilder authenticationPropertiesBuilder;

    private PolicyAttributes policyAttributes;

    private Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap;

    public E2ETestStepDefinitionsForPolicyStoreApi() {
        authenticationPropertiesBuilder = AuthenticationProperties.builder();
    }

    // TODO how to reuse this from E2ETestStepDefinitions
    @Given("the IRS URL {string} -- policystore")
    public void theIRSURL(String irsUrl) {
        authenticationPropertiesBuilder.uri(irsUrl);
    }

    // TODO how to reuse this from E2ETestStepDefinitions
    @And("the admin user api key -- policystore")
    public void theAdminUser() throws PropertyNotFoundException {
        final String adminUserApiKey = "ADMIN_USER_API_KEY";
        String apiKey = System.getenv(adminUserApiKey);
        if (apiKey != null) {
            authenticationPropertiesBuilder.apiKey(apiKey);
        } else {
            throw new PropertyNotFoundException("Environment Variable missing: " + adminUserApiKey);
        }
    }

    @Given("no policies with prefix {string} exist")
    public void cleanupPoliciesWithPrefix(final String policyIdPrefix) {
        fetchPolicyIdsByPrefix(authenticationPropertiesBuilder, policyIdPrefix).forEach(
                policyId -> cleanupPolicy(authenticationPropertiesBuilder, policyId));
    }

    @And("I fetch all policies")
    public void iFetchAllPolicies() {
        this.bpnToPoliciesMap = fetchAllPolicies(authenticationPropertiesBuilder);
    }

    @And("I fetch policies for BPN {string}")
    public void iFetchPoliciesForBpn(final String bpn) {
        this.bpnToPoliciesMap = fetchPoliciesForBpn(authenticationPropertiesBuilder, bpn);
    }

    @And("I fetch policies for BPNs:")
    public void iFetchPoliciesForBpn(final List<String> businessPartnerNumbers) {
        this.bpnToPoliciesMap = fetchPoliciesForBusinessPartnerNumbers(authenticationPropertiesBuilder,
                businessPartnerNumbers);
    }

    @Then("the BPN {string} should have the following policies:")
    public void theBpnShouldHaveTheFollowingPolicies(final String bpn, final List<String> policyIds) {
        final List<String> policyIdsForBpn = extractPolicyIdsForBpn(bpnToPoliciesMap, bpn).toList();
        assertThat(policyIdsForBpn).containsAll(policyIds);
    }

    @Then("the BPNs should be associated with policies as follows:")
    public void theBpnShouldHaveTheFollowingPolicies(final List<BpnToPolicyId> bpnToPolicyIdTable) {

        final HashMap<String, HashSet<String>> expectedBpnToPolicyIdsMapping = getExpectedBpnToPolicyIdsMapping(
                bpnToPolicyIdTable);

        expectedBpnToPolicyIdsMapping.forEach((bpn, expectedPolicies) -> {
            final List<String> policyIdsForBpn = extractPolicyIdsForBpn(bpnToPoliciesMap, bpn).toList();
            assertThat(policyIdsForBpn).as("BPN '%s' should be associated with the expected policies", bpn)
                                       .containsAll(expectedPolicies);
        });
    }

    @When("I delete the following policies:")
    public void iDeleteTheFollowingPolicies(final List<String> policyIds) {
        for (final String policyId : policyIds) {
            cleanupPolicy(authenticationPropertiesBuilder, policyId);
        }
    }

    @Then("the BPN {string} should have {int} policies having policyId starting with {string}")
    public void theBpnShouldHavePolicyIdsStartingWith(final String bpn, final int numPolicies, final String prefix) {
        final List<String> policyIdsForBpn = extractPolicyIdsForBpn(bpnToPoliciesMap, bpn).filter(startingWith(prefix))
                                                                                          .toList();
        assertThat(policyIdsForBpn).hasSize(numPolicies);
    }

    @Then("the BPN {string} should have no policies with policyId {string}")
    public void theBpnShouldNotHavePolicyId(final String bpn, final String policyId) {
        final List<String> policyIdsForBpn = extractPolicyIdsForBpn(bpnToPoliciesMap, bpn).filter(policyId::equals)
                                                                                          .toList();
        assertThat(policyIdsForBpn).isEmpty();
    }

    private static Predicate<String> startingWith(final String prefix) {
        return s -> StringUtils.startsWith(s, prefix);
    }

    @When("I update policy {string}, BPN {string}, validUntil {string}")
    public void iPerformUpdatePolicy(final String policyId, final String bpn, final String validUntil) {
        updatePolicies(authenticationPropertiesBuilder, List.of(policyId), List.of(bpn), validUntil);
    }

    @When("I add policyId {string} to given BPNs using validUntil {string}:")
    @When("I update policy with policyId {string} and given BPNs using validUntil {string}:")
    public void iUpdatePolicyBpns(final String policyId, final String validUntil, List<String> businessPartnerNumbers) {
        updatePolicies(authenticationPropertiesBuilder, List.of(policyId), businessPartnerNumbers, validUntil);
    }

    @Then("the BPN {string} should have a policy with policyId {string} and validUntil {string}")
    @SuppressWarnings({ "rawtypes",
                        "unchecked"
    })
    public void theBpnShouldHaveTheExpectedPolicyWithValidUntil(final String bpn, final String policyId,
            final String validUntil) {

        final List<LinkedHashMap> policies = extractPoliciesForBpn(bpnToPoliciesMap, bpn).toList();
        final List<LinkedHashMap> policiesFiltered = policies.stream()
                                                             .filter(p -> p.get("policyId").equals(policyId))
                                                             .toList();
        assertThat(policiesFiltered).hasSize(1);
        assertThat(policiesFiltered.get(0)).containsEntry("policyId", policyId) //
                                           .containsEntry("validUntil", validUntil);
    }

    @Given("a policy with policyId {string} is registered for BPN {string} and validUntil {string}")
    public void iRegisterAPolicy(final String policyId, final String bpn, final String validUntil) {
        final String policyJson = POLICY_TEMPLATE.formatted(policyId);
        final CreatePoliciesResponse response = registerPolicyForBpn(authenticationPropertiesBuilder, policyJson, bpn,
                validUntil);
        assertThat(response.policyId()).isEqualTo(policyId);
    }

    @Given("I want to register a policy")
    @Given("I want to update a policy")
    public void iWantToRegisterAPolicy() {
        this.policyAttributes = new PolicyAttributes();
    }

    @Given("I want to register a policy with policyId {string}")
    @Given("I want to update the policy with policyId {string}")
    public void iWantToRegisterAPolicyWithPolicyId(final String policyId) {
        iWantToRegisterAPolicy();
        policyShouldHavePolicyId(policyId);
    }

    @And("the policy should have policyId {string}")
    public void policyShouldHavePolicyId(final String policyId) {
        this.policyAttributes.setPolicyId(policyId);
    }

    @And("the policy should be associated to BPN {string}")
    public void policyShouldBeAssociatedToBpn(final String bpn) {
        if (this.policyAttributes.getBpn() == null) {
            this.policyAttributes.setBpn(new ArrayList<>());
        }
        this.policyAttributes.getBpn().add(bpn);
    }

    @And("the policy should be associated to the following BPNs:")
    public void policyShouldBeAssociatedToBpn(final List<String> bpnls) {
        this.policyAttributes.setBpn(bpnls);
    }

    @And("the policy should have validUntil {string}")
    public void policyShouldHaveValidUntil(final String validUntil) {
        this.policyAttributes.setValidUntil(validUntil);
    }

    @When("I register the policy")
    public void iRegisterThePolicy() {

        // 'POST policies' only supports one BPN, therefore if we want to associate a policy with multiple BPNs
        // we first need to create it via POST for the first BPN ...
        iRegisterAPolicy(policyAttributes.getPolicyId(), policyAttributes.getBpn().get(0),
                policyAttributes.getValidUntil());

        if (policyAttributes.getBpn().size() > 1) {
            // ... and then add it via 'UPDATE policies' to all BPNs to which it should be associated
            // (note that this also update the validUntil).
            updatePolicies(authenticationPropertiesBuilder, List.of(policyAttributes.getPolicyId()),
                    policyAttributes.getBpn(), policyAttributes.getValidUntil());
        }
    }

    @When("I update the policy")
    public void iUpdateThePolicy() {
        updatePolicies(authenticationPropertiesBuilder, List.of(policyAttributes.getPolicyId()),
                policyAttributes.getBpn(), policyAttributes.getValidUntil());
    }

}
