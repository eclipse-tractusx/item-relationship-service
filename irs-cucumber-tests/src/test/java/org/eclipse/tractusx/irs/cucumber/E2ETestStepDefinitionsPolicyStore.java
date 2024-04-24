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

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
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
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.cucumber.PolicyTestHelper.BpnToPolicyId;
import org.eclipse.tractusx.irs.policystore.models.CreatePoliciesResponse;
import org.eclipse.tractusx.irs.policystore.models.CreatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.models.UpdatePolicyRequest;
import org.springframework.http.HttpStatus;

public class E2ETestStepDefinitionsPolicyStore {

    public static final String URL_IRS_POLICIES = "/irs/policies";

    public static final String QUERYPARAM_BUSINESS_PARTNER_NUMBERS = "businessPartnerNumbers";

    private final AuthenticationProperties.AuthenticationPropertiesBuilder authenticationPropertiesBuilder;

    private Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap;

    public E2ETestStepDefinitionsPolicyStore() {
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
        fetchPolicyIdsByPrefix(policyIdPrefix).forEach(this::cleanupPolicy);
    }

    @And("I fetch all policies")
    public void iFetchAllPolicies() {
        this.bpnToPoliciesMap = fetchAllPolicies();
    }

    @And("I fetch policies for BPN {string}")
    public void iFetchPoliciesForBpn(final String bpn) {
        this.bpnToPoliciesMap = fetchPoliciesForBpn(bpn);
    }

    @And("I fetch policies for BPNs:")
    public void iFetchPoliciesForBpn(final List<String> businessPartnerNumbers) {
        this.bpnToPoliciesMap = fetchPoliciesForBusinessPartnerNumbers(businessPartnerNumbers);
    }

    @Then("the BPN {string} should have the following policies:")
    public void theBpnShouldHaveTheFollowingPolicies(final String bpn, final List<String> policyIds) {
        final List<String> policyIdsForBpn = PolicyTestHelper.extractPolicyIdsForBpn(bpnToPoliciesMap, bpn).toList();
        assertThat(policyIdsForBpn).containsAll(policyIds);
    }

    @Then("the BPNs should be associated with policies as follows:")
    public void theBpnShouldHaveTheFollowingPolicies(final List<BpnToPolicyId> bpnToPolicyIdTable) {
        HashMap<String, HashSet<String>> expectedBpnToPolicyIdsMapping = PolicyTestHelper.getExpectedBpnToPolicyIdsMapping(
                bpnToPolicyIdTable);

        expectedBpnToPolicyIdsMapping.forEach((bpn, expectedPolicies) -> {
            final List<String> policyIdsForBpn = PolicyTestHelper.extractPolicyIdsForBpn(bpnToPoliciesMap, bpn)
                                                                 .toList();
            assertThat(policyIdsForBpn).as("BPN '%s' should be associated with the expected policies", bpn)
                                       .containsAll(expectedPolicies);
        });
    }

    @When("I delete the following policies:")
    public void iDeleteTheFollowingPolicies(final List<String> policyIds) {
        for (final String policyId : policyIds) {
            cleanupPolicy(policyId);
        }
    }

    @Then("the BPN {string} should have {int} policies having policyId starting with {string}")
    public void theBpnShouldHaveTheFollowingPolicies(final String bpn, final int numPolicies, final String prefix) {
        final List<String> policyIdsForBpn = PolicyTestHelper.extractPolicyIdsForBpn(bpnToPoliciesMap, bpn)
                                                             .filter(startingWith(prefix))
                                                             .toList();
        assertThat(policyIdsForBpn).hasSize(numPolicies);
    }

    private static Predicate<String> startingWith(final String prefix) {
        return s -> StringUtils.startsWith(s, prefix);
    }

    @When("I perform update policy {string}, BPN {string}, validUntil {string}")
    public void iPerformUpdate(final String policyId, final String bpn, final String validUntil) {
        updatePolicies(List.of(policyId), List.of(bpn), validUntil);
    }

    @When("I add policyId {string} to given BPNs using validUntil {string}:")
    public void iAddPolicyIdToGivenBpns(final String policyId, final String validUntil,
            List<String> businessPartnerNumbers) {
        updatePolicies(List.of(policyId), businessPartnerNumbers, validUntil);
    }

    @Then("the BPN {string} should have a policy with policyId {string} and validUntil {string}")
    @SuppressWarnings({ "rawtypes",
                        "unchecked"
    })
    public void theBpnShouldHaveTheExpectedPolicyWithValidUntil(final String bpn, final String policyId,
            final String validUntil) {

        final List<LinkedHashMap> policies = PolicyTestHelper.extractPoliciesForBpn(bpnToPoliciesMap, bpn).toList();
        final List<LinkedHashMap> policiesFiltered = policies.stream()
                                                             .filter(p -> p.get("policyId").equals(policyId))
                                                             .toList();
        assertThat(policiesFiltered).hasSize(1);
        assertThat(policiesFiltered.get(0)).containsEntry("policyId", policyId) //
                                           .containsEntry("validUntil", validUntil);
    }

    @Given("a policy with policyId {string} for BPN {string} and validUntil {string}")
    public void iRegisterAPolicy(final String policyId, final String bpn, final String validUntil) {
        final String policyJson = PolicyTestHelper.policyTemplate.formatted(policyId);
        final CreatePoliciesResponse response = registerPolicyForBpn(policyJson, bpn, validUntil);
        assertThat(response.policyId()).isEqualTo(policyId);
    }

    @Given("I register a policy to be create")
    public void aPolicyToBeCreate() {

    }

    private void cleanupPolicy(final String policyId) {

        final int status = givenAuthentication().pathParam("policyId", policyId)
                                                .when()
                                                .delete(URL_IRS_POLICIES + "/{policyId}")
                                                .then()
                                                .log()
                                                .all()
                                                .extract()
                                                .statusCode();

        assertThat(List.of(HttpStatus.OK.value(), HttpStatus.NOT_FOUND.value())).describedAs(
                "Should either return status 200 OK or 404 NOT_FOUND").contains(status);
    }

    private List<String> fetchPolicyIdsByPrefix(final String policyIdPrefix) {
        final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap = fetchAllPolicies();
        return PolicyTestHelper.extractPolicyIdsStartingWith(bpnToPoliciesMap, policyIdPrefix).toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, ArrayList<LinkedHashMap<String, ?>>> fetchAllPolicies() {

        return givenAuthentication().when()
                                    .get(URL_IRS_POLICIES)
                                    .then()
                                    .log()
                                    .all()
                                    .statusCode(HttpStatus.OK.value())
                                    .extract()
                                    .body()
                                    .as(Map.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, ArrayList<LinkedHashMap<String, ?>>> fetchPoliciesForBpn(final String bpn) {

        return givenAuthentication().queryParam(QUERYPARAM_BUSINESS_PARTNER_NUMBERS, bpn)
                                    .when()
                                    .get(URL_IRS_POLICIES)
                                    .then()
                                    .log()
                                    .all()
                                    .statusCode(HttpStatus.OK.value())
                                    .extract()
                                    .body()
                                    .as(Map.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, ArrayList<LinkedHashMap<String, ?>>> fetchPoliciesForBusinessPartnerNumbers(
            final List<String> businessPartnerNumbers) {

        return givenAuthentication().queryParam(QUERYPARAM_BUSINESS_PARTNER_NUMBERS, businessPartnerNumbers)
                                    .when()
                                    .get(URL_IRS_POLICIES)
                                    .then()
                                    .log()
                                    .all()
                                    .statusCode(HttpStatus.OK.value())
                                    .extract()
                                    .body()
                                    .as(Map.class);
    }

    private CreatePoliciesResponse registerPolicyForBpn(final String policyJson, final String bpn,
            final String validUntil) {

        final var createPolicyRequest = CreatePolicyRequest.builder()
                                                           .validUntil(OffsetDateTime.parse(validUntil))
                                                           .businessPartnerNumber(bpn)
                                                           .payload(PolicyTestHelper.jsonFromString(policyJson))
                                                           .build();
        return givenAuthentication().contentType(ContentType.JSON)
                                    .body(createPolicyRequest)
                                    .when()
                                    .post(URL_IRS_POLICIES)
                                    .then()
                                    .log()
                                    .all()
                                    .statusCode(HttpStatus.CREATED.value())
                                    .extract()
                                    .as(CreatePoliciesResponse.class);
    }

    private void updatePolicies(final List<String> policyIds, final List<String> businessPartnerNumbers,
            final String validUntil) {

        final var updatePolicyRequest = UpdatePolicyRequest.builder()
                                                           .policyIds(policyIds)
                                                           .businessPartnerNumbers(businessPartnerNumbers)
                                                           .validUntil(OffsetDateTime.parse(validUntil))
                                                           .build();
        givenAuthentication().contentType(ContentType.JSON)
                             .body(updatePolicyRequest)
                             .when()
                             .put(URL_IRS_POLICIES)
                             .then()
                             .log()
                             .all()
                             .statusCode(HttpStatus.OK.value());
    }

    private RequestSpecification givenAuthentication() {
        final AuthenticationProperties authProperties = authenticationPropertiesBuilder.build();
        return given().log().all().spec(authProperties.getNewAuthenticationRequestSpecification());
    }

}
