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
package org.eclipse.tractusx.irs.cucumber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.cleanupPolicy;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.cleanupPolicyIdsByPrefix;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.extractPoliciesForBpn;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.extractPolicyIdsForBpn;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.fetchAllPolicies;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.fetchAllPoliciesSuccessfully;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.fetchPoliciesForBpn;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.fetchPoliciesForBusinessPartnerNumbers;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.getExpectedBpnToPolicyIdsMapping;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.getPolicyTemplate;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.registerPolicyForBpn;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.updatePolicies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;
import org.eclipse.tractusx.irs.cucumber.AuthenticationProperties.AuthenticationPropertiesBuilder;
import org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.BpnToPolicyId;
import org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.PolicyAttributes;
import org.hamcrest.Matchers;
import org.springframework.http.HttpStatus;

/**
 * Step definitions for Policy Store API.
 */
public class E2ETestStepDefinitionsForPolicyStoreApi {

    // TODO(#573): Cucumber Tests: Allow conflict-free parallel execution

    private final AuthenticationPropertiesBuilder authenticationPropertiesBuilder;

    private PolicyAttributes policyAttributes;

    private Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap;

    private ValidatableResponse getAllPoliciesResponse;
    private ValidatableResponse getPolicyForBpnlsResponse;
    private ValidatableResponse createPoliciesResponse;
    private ValidatableResponse updatePoliciesResponse;
    private ValidatableResponse deletePoliciesResponse;

    public E2ETestStepDefinitionsForPolicyStoreApi() {
        this.authenticationPropertiesBuilder = AuthenticationProperties.builder();
    }

    // TODO(#571): Find a way to re-use ""the IRS URL {string}" and ""the admin user api key" in all step def classes

    @BeforeAll
    public static void configureLogging() {
        E2ETestHelper.configureRequestAndResponseLogging();
    }

    @Given("the IRS URL {string} -- policystore")
    public void theIRSURL(String irsUrl) {
        this.authenticationPropertiesBuilder.uri(irsUrl);
    }

    @Given("the admin user api key -- policystore")
    public void theAdminUser() throws PropertyNotFoundException {
        final String adminUserApiKey = "ADMIN_USER_API_KEY";
        final String apiKey = System.getenv(adminUserApiKey);
        if (apiKey != null) {
            this.authenticationPropertiesBuilder.apiKey(apiKey);
        } else {
            throw new PropertyNotFoundException("Environment Variable missing: " + adminUserApiKey);
        }
    }

    @Given("no policies with prefix {string} exist")
    public void cleanupPoliciesWithPrefix(final String policyIdPrefix) {
        cleanupPolicyIdsByPrefix(authenticationPropertiesBuilder, policyIdPrefix);
    }

    @When("I fetch all policies")
    public void iFetchAllPolicies() {
        this.getAllPoliciesResponse = fetchAllPolicies(authenticationPropertiesBuilder);
        this.bpnToPoliciesMap = getBpnToPoliciesMap(this.getAllPoliciesResponse);
    }

    @SuppressWarnings("unchecked")
    private Map<String, ArrayList<LinkedHashMap<String, ?>>> getBpnToPoliciesMap(final ValidatableResponse response) {
        if (response.extract().statusCode() == HttpStatus.OK.value()) {
            return response.extract().body().as(Map.class);
        } else {
            return null;
        }
    }

    @When("I successfully fetch all policies")
    public void iFetchAllPoliciesSuccessfully() {
        this.bpnToPoliciesMap = fetchAllPoliciesSuccessfully(this.authenticationPropertiesBuilder);
    }

    @Then("the fetch all policies response should have HTTP status {int}")
    public void theFetchAllPoliciesPoliciesResponseShouldHaveStatus(final int httpStatus) {
        this.getAllPoliciesResponse.statusCode(httpStatus);
    }

    @When("I fetch policies for BPN {string}")
    public void iFetchPoliciesForBpn(final String bpn) {
        this.bpnToPoliciesMap = fetchPoliciesForBpn(this.authenticationPropertiesBuilder, bpn);
    }

    @When("I fetch policies for BPNs:")
    public void iFetchPoliciesForBpn(final List<String> businessPartnerNumbers) {
        this.getPolicyForBpnlsResponse = fetchPoliciesForBusinessPartnerNumbers(this.authenticationPropertiesBuilder,
                businessPartnerNumbers);
        this.bpnToPoliciesMap = getBpnToPoliciesMap(this.getPolicyForBpnlsResponse);
    }

    @Then("the fetch policies for BPN response should have HTTP status {int}")
    public void theFetchPoliciesForBpnResponseShouldHaveStatus(final int httpStatus) {
        this.getPolicyForBpnlsResponse.statusCode(httpStatus);
    }

    @Then("the BPN {string} should have the following policies:")
    public void theBpnShouldHaveTheFollowingPolicies(final String bpn, final List<String> policyIds) {
        final List<String> policyIdsForBpn = extractPolicyIdsForBpn(this.bpnToPoliciesMap, bpn).toList();
        assertThat(policyIdsForBpn).containsAll(policyIds);
    }

    @Then("the BPNs should be associated with policies as follows:")
    public void theBpnShouldHaveTheFollowingPolicies(final List<BpnToPolicyId> bpnToPolicyIdTable) {

        final HashMap<String, HashSet<String>> expectedBpnToPolicyIdsMapping = getExpectedBpnToPolicyIdsMapping(
                bpnToPolicyIdTable);

        expectedBpnToPolicyIdsMapping.forEach((bpn, expectedPolicies) -> {
            final List<String> policyIdsForBpn = extractPolicyIdsForBpn(this.bpnToPoliciesMap, bpn).toList();
            assertThat(policyIdsForBpn).as("BPN '%s' should be associated with the expected policies", bpn)
                                       .containsAll(expectedPolicies);
        });
    }

    @When("I delete the following policies:")
    public void iDeleteTheFollowingPolicies(final List<String> policyIds) {
        for (final String policyId : policyIds) {
            cleanupPolicy(this.authenticationPropertiesBuilder, policyId);
        }
    }

    @When("I delete the policy {string}")
    public void iDeletePolicyWithPolicyId(final String policyId) {
        this.deletePoliciesResponse = E2ETestHelperForPolicyStoreApi.deletePolicy(this.authenticationPropertiesBuilder,
                policyId);
    }

    @Then("the delete policy response should have HTTP status {int}")
    public void theDeletePolicyResponseShouldHaveStatus(final int httpStatus) {
        this.deletePoliciesResponse.statusCode(httpStatus);
    }

    @Then("the BPN {string} should have {int} policies having policyId starting with {string}")
    public void theBpnShouldHavePolicyIdsStartingWith(final String bpn, final int numPolicies, final String prefix) {
        final List<String> policyIdsForBpn = extractPolicyIdsForBpn(this.bpnToPoliciesMap, bpn).filter(
                E2ETestHelper.startingWith(prefix)).toList();
        assertThat(policyIdsForBpn).hasSize(numPolicies);
    }

    @Then("the BPN {string} should have no policies with policyId {string}")
    public void theBpnShouldNotHavePolicyId(final String bpn, final String policyId) {
        final List<String> policyIdsForBpn = extractPolicyIdsForBpn(this.bpnToPoliciesMap, bpn).filter(policyId::equals)
                                                                                               .toList();
        assertThat(policyIdsForBpn).isEmpty();
    }

    @When("I update policy {string}, BPN {string}, validUntil {string}")
    public void iPerformUpdatePolicy(final String policyId, final String bpn, final String validUntil) {
        this.updatePoliciesResponse = updatePolicies(this.authenticationPropertiesBuilder, List.of(policyId),
                List.of(bpn), validUntil);
    }

    @When("I add policyId {string} to given BPNs using validUntil {string}:")
    @When("I update policy with policyId {string} and given BPNs using validUntil {string}:")
    public void iUpdatePolicyBpns(final String policyId, final String validUntil, List<String> businessPartnerNumbers) {
        this.updatePoliciesResponse = updatePolicies(this.authenticationPropertiesBuilder, List.of(policyId),
                businessPartnerNumbers, validUntil);
        this.updatePoliciesResponse.statusCode(HttpStatus.OK.value());
    }

    @Then("the BPN {string} should have a policy with policyId {string} and validUntil {string}")
    @SuppressWarnings({ "rawtypes",
                        "unchecked"
    })
    public void theBpnShouldHaveTheExpectedPolicyWithValidUntil(final String bpn, final String policyId,
            final String validUntil) {

        final List<LinkedHashMap> policies = extractPoliciesForBpn(this.bpnToPoliciesMap, bpn).toList();
        final List<LinkedHashMap> policiesFiltered = policies.stream()
                                                             .filter(p -> p.get("policyId").equals(policyId))
                                                             .toList();
        assertThat(policiesFiltered).hasSize(1);
        assertThat(policiesFiltered.get(0)).containsEntry("policyId", policyId) //
                                           .containsEntry("validUntil", validUntil);
    }

    @When("a policy with policyId {string} is registered for BPN {string} and validUntil {string}")
    public void iRegisterAPolicy(final String policyId, final String bpn, final String validUntil) throws IOException {
        final String policyJson = getPolicyTemplate().formatted(policyId);
        this.createPoliciesResponse = registerPolicyForBpn(this.authenticationPropertiesBuilder, policyJson, bpn,
                validUntil);
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

    @Given("the policy should have policyId {string}")
    public void policyShouldHavePolicyId(final String policyId) {
        this.policyAttributes.setPolicyId(policyId);
    }

    @Given("the policy should be associated to BPN {string}")
    public void policyShouldBeAssociatedToBpn(final String bpn) {
        if (this.policyAttributes.getBpnls() == null) {
            this.policyAttributes.setBpnls(new ArrayList<>());
        }
        this.policyAttributes.getBpnls().add(bpn);
    }

    @Given("the policy should be associated to the following BPNs:")
    public void policyShouldBeAssociatedToBpn(final List<String> bpnls) {
        this.policyAttributes.setBpnls(bpnls);
    }

    @Given("the policy should have validUntil {string}")
    public void policyShouldHaveValidUntil(final String validUntil) {
        this.policyAttributes.setValidUntil(validUntil);
    }

    @Given("the policy should have no validUntil")
    public void policyShouldHaveNoValidUntil() {
        this.policyAttributes.setValidUntil(null);
    }

    @When("I register the policy")
    public void iTryToRegisterThePolicy() throws IOException {

        this.createPoliciesResponse = null;
        this.updatePoliciesResponse = null;

        // 'POST policies' only supports one BPN, therefore if we want to associate a policy with multiple BPNs
        // we first need to create it via POST for the first BPN ...
        final String policyJson = getPolicyTemplate().formatted(this.policyAttributes.getPolicyId());
        this.createPoliciesResponse = registerPolicyForBpn(this.authenticationPropertiesBuilder, policyJson,
                this.policyAttributes.getBpnls().get(0), this.policyAttributes.getValidUntil());

        if (this.policyAttributes.getBpnls().size() > 1) {
            // ... and then add it via 'UPDATE policies' to all BPNs to which it should be associated
            // (note that this also update the validUntil).
            this.updatePoliciesResponse = updatePolicies(this.authenticationPropertiesBuilder,
                    List.of(this.policyAttributes.getPolicyId()), this.policyAttributes.getBpnls(),
                    this.policyAttributes.getValidUntil());
        }

    }

    @When("I update the policy")
    public void iUpdateThePolicy() {
        this.updatePoliciesResponse = updatePolicies(this.authenticationPropertiesBuilder,
                List.of(this.policyAttributes.getPolicyId()), this.policyAttributes.getBpnls(),
                this.policyAttributes.getValidUntil());
    }

    @Then("the create policy response should have HTTP status {int} and policyId {string}")
    public void theCreatePolicyResponseShouldHaveStatus(final int httpStatus, final String policyId) {
        this.createPoliciesResponse.statusCode(httpStatus);
        this.createPoliciesResponse.body("policyId", Matchers.equalTo(policyId));
    }

    @Then("the create policy response should have HTTP status {int}")
    public void theCreatePolicyResponseShouldHaveStatus(final int httpStatus) {
        this.createPoliciesResponse.statusCode(httpStatus);
    }

    @Then("the create policy response should have message containing {string}")
    public void theCreatePolicyResponseShouldHaveMessageContaining(final String string) {
        final ValidatableResponse validatableResponse = this.createPoliciesResponse;
        assertThatResponseHasMessageContaining(validatableResponse, string);
    }

    @Then("the delete policy response should have message containing {string}")
    public void thedeletePolicyResponseShouldHaveMessageContaining(final String string) {
        assertThatResponseHasMessageContaining(this.deletePoliciesResponse, string);
    }

    @Then("the update policy response should have message containing {string}")
    public void theUpdatePolicyResponseShouldHaveMessageContaining(final String string) {
        assertThatResponseHasMessageContaining(this.updatePoliciesResponse, string);
    }

    private static void assertThatResponseHasMessageContaining(final ValidatableResponse validatableResponse,
            final String string) {
        validatableResponse.body("messages", Matchers.hasItem(Matchers.containsString(string)));
    }

    @Then("the update policy response should have HTTP status {int}")
    public void theUpdatePolicyResponseShouldHaveStatus(final int httpStatus) {
        this.updatePoliciesResponse.statusCode(httpStatus);
    }
}
