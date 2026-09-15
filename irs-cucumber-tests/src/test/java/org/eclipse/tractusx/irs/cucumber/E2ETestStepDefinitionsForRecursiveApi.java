/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
import static org.awaitility.Awaitility.await;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelper.givenAuthentication;
import static org.hamcrest.Matchers.equalTo;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.eclipse.tractusx.irs.cucumber.AuthenticationProperties.AuthenticationPropertiesBuilder;
import org.hamcrest.Matchers;
import org.springframework.http.HttpStatus;

/**
 * Step definitions for the recursive IRS API.
 * <p>
 * Only the root instance is addressed. Everything below it is reached over EDC notifications inside
 * the dataspace and is therefore not visible to this test.
 */
public class E2ETestStepDefinitionsForRecursiveApi {

    private static final String JOBS_PATH = "/irs/recursive/jobs";
    private static final String GRANTS_PATH = "/irs/recursive/chain-openings/grants";
    private static final String POLICIES_PATH = "/irs/policies";
    private static final String GRANT_VALID_FROM = "2026-01-01T00:00:00Z";
    private static final String GRANT_VALID_TO = "2099-12-31T23:59:59Z";

    private final AuthenticationPropertiesBuilder authenticationPropertiesBuilder;

    private RecursiveChainEnvironment chain;
    private boolean grantModified;
    private Map<String, Object> jobRequest;
    private String jobId;
    private JsonPath jobStatus;

    public E2ETestStepDefinitionsForRecursiveApi() {
        this.authenticationPropertiesBuilder = AuthenticationProperties.builder();
    }

    @BeforeAll
    public static void configureLogging() {
        E2ETestHelper.configureRequestAndResponseLogging();
    }

    /**
     * Restores the grant of the root instance after every scenario. Scenarios that narrow or delete
     * it would otherwise leave the environment behind for the next run, and an aborted run would
     * leave it behind for good.
     */
    @After
    public void restoreChainOpeningGrant() {
        if (chain != null && grantModified) {
            putGrant(chain.getRootAllowedBpnls());
            grantModified = false;
        }
    }

    @Given("the recursive IRS chain of the test environment")
    public void theRecursiveChain() throws PropertyNotFoundException {
        this.chain = RecursiveChainEnvironment.fromEnvironment();
        this.authenticationPropertiesBuilder.uri(this.chain.getRootUrl());
    }

    @Given("the admin user api key -- recursive")
    public void theAdminUser() throws PropertyNotFoundException {
        final String adminUserApiKey = "ADMIN_USER_API_KEY";
        final String apiKey = System.getenv(adminUserApiKey);
        if (apiKey == null) {
            throw new PropertyNotFoundException("Environment Variable missing: " + adminUserApiKey);
        }
        this.authenticationPropertiesBuilder.apiKey(apiKey);
    }

    @Given("the IRS policy store of the root instance holds accepted policies")
    public void thePolicyStoreHoldsAcceptedPolicies() {
        // A redeploy empties the policy store, and an expired policy is as good as none. Whether a
        // policy actually matches an offer is decided during the EDC contract negotiation, so this
        // checks what can be checked here: at least one policy that is still valid.
        final Map<String, List<Map<String, Object>>> policiesByBpn = givenAuthentication(
                authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                .get(POLICIES_PATH)
                                                .then()
                                                .statusCode(HttpStatus.OK.value())
                                                .extract()
                                                .jsonPath()
                                                .getMap("");

        final List<String> validUntil = policiesByBpn.values()
                                                     .stream()
                                                     .flatMap(List::stream)
                                                     .map(policy -> (String) policy.get("validUntil"))
                                                     .filter(Objects::nonNull)
                                                     .toList();

        assertThat(validUntil).as("policies in the store of the root instance").isNotEmpty();
        assertThat(validUntil).as("at least one policy that is still valid")
                              .anyMatch(until -> OffsetDateTime.parse(until).isAfter(OffsetDateTime.now()));
    }

    @Given("a chain opening grant at the root instance allowing the granted partners")
    public void aChainOpeningGrantForTheGrantedPartners() {
        putGrant(chain.getRootAllowedBpnls());
    }

    @Given("a chain opening grant at the root instance allowing only {int} of the granted partners")
    public void aChainOpeningGrantAllowingOnlySome(final int partnerCount) {
        final List<String> allowed = chain.getRootAllowedBpnls();
        assertThat(partnerCount).as("configured partners of the chain")
                                .isBetween(0, allowed.size());
        grantModified = true;
        putGrant(allowed.subList(0, partnerCount));
    }

    @Given("no chain opening grant at the root instance")
    public void noChainOpeningGrant() {
        grantModified = true;
        givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                            .queryParam("openingId", chain.getOpeningId())
                                                            .queryParam("globalAssetId",
                                                                    chain.getRootGlobalAssetId())
                                                            .queryParam("requesterBpn", chain.getRootBpnl())
                                                            .queryParam("useCase", chain.getUseCase())
                                                            .delete(GRANTS_PATH)
                                                            .then()
                                                            .statusCode(
                                                                    Matchers.oneOf(HttpStatus.NO_CONTENT.value(),
                                                                            HttpStatus.NOT_FOUND.value()));
    }

    @Given("I register a recursive IRS job for the configured root material")
    public void iRegisterARecursiveJob() {
        this.jobRequest = new HashMap<>();
        this.jobRequest.put("openingId", chain.getOpeningId());
        this.jobRequest.put("useCase", chain.getUseCase());
        this.jobRequest.put("globalAssetId", chain.getRootGlobalAssetId());
        // requesterBpn is deliberately omitted: for a root job the service replaces it with its own
        // recursive.localBpnl. bomLifecycle and aspects are left out as well and are asserted below,
        // which keeps the defaults of the use case under test.
    }

    @Given("ttl {string} -- recursive")
    public void ttl(final String ttl) {
        this.jobRequest.put("ttl", ttl);
    }

    @When("I get the recursive job-id")
    public void iGetTheRecursiveJobId() {
        this.jobId = givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                                         .body(jobRequest)
                                                                         .post(JOBS_PATH)
                                                                         .then()
                                                                         .statusCode(HttpStatus.CREATED.value())
                                                                         .extract()
                                                                         .path("jobId");

        assertThat(this.jobId).isNotBlank();
    }

    @Then("the job registration is rejected with HTTP status {int} and code {string}")
    public void theJobRegistrationIsRejectedWith(final int statusCode, final String errorCode) {
        givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                            .body(jobRequest)
                                                            .post(JOBS_PATH)
                                                            .then()
                                                            .statusCode(statusCode)
                                                            .body("code", equalTo(errorCode));
    }

    @Then("I check, if the recursive job has state {string} within {int} minutes")
    public void iCheckIfTheJobHasStateWithinMinutes(final String state, final int maxWaitTime) {
        await().atMost(maxWaitTime, TimeUnit.MINUTES)
               .with()
               .pollInterval(Duration.ofSeconds(5L))
               .until(() -> state.equals(fetchJobStatus().getString("job.state")));

        this.jobStatus = fetchJobStatus();
    }

    @Then("I check, if the result status is {string}")
    public void iCheckIfTheResultStatusIs(final String resultStatus) {
        assertThat(jobStatus.getString("result.resultStatus")).isEqualTo(resultStatus);
    }

    @Then("I check, if the result contains {int} direct child items")
    public void iCheckIfTheResultContainsDirectChildItems(final int childItems) {
        assertThat(jobStatus.getList("result.childItems")).hasSize(childItems);
    }

    @Then("I check, if the material tree contains {int} child items in total")
    public void iCheckIfTheTreeContainsChildItemsInTotal(final int childItems) {
        assertThat(countChildItems(jobStatus.getList("result.childItems"))).isEqualTo(childItems);
    }

    @Then("I check, if the result was collected for bom lifecycle {string}")
    public void iCheckIfTheResultUsedBomLifecycle(final String bomLifecycle) {
        assertThat(jobStatus.getString("result.bomLifecycle")).isEqualTo(bomLifecycle);
    }

    @Then("I check, if the result was collected for {int} aspects")
    public void iCheckIfTheResultRequestedAspects(final int aspects) {
        assertThat(jobStatus.getList("result.requestedAspects")).hasSize(aspects);
    }

    @Then("I check, if the result exposes no partner identities")
    public void iCheckIfTheResultExposesNoPartnerIdentities() {
        final String result = jobStatus.prettify();
        assertThat(result).doesNotContain("bpnl").doesNotContain("Bpnl").doesNotContain("BPNL");
    }

    @Then("I check, if the material tree is {int} levels deep")
    public void iCheckIfTheTreeIsLevelsDeep(final int levels) {
        assertThat(depthOf(jobStatus.getList("result.childItems"))).as("levels below the root material")
                                                                   .isEqualTo(levels);
    }

    @Then("I check, if every material carries a quantity with unit {string}")
    public void iCheckIfEveryMaterialCarriesQuantity(final String unit) {
        forEachMaterial(material -> {
            @SuppressWarnings("unchecked")
            final Map<String, Object> quantity = (Map<String, Object>) material.get("quantity");
            assertThat(quantity).as("quantity of material %s", material.get("materialNumber")).isNotNull();
            assertThat(quantity.get("value")).as("quantity value").isNotNull();
            assertThat(quantity.get("unit")).as("quantity unit").isEqualTo(unit);
        });
    }

    @Then("I check, if every material carries {int} aspects")
    public void iCheckIfEveryMaterialCarriesAspects(final int aspects) {
        forEachMaterial(material -> {
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> items = (List<Map<String, Object>>) material.get("items");
            assertThat(items).as("aspects of material %s", material.get("materialNumber")).hasSize(aspects);
            assertThat(items).allSatisfy(item -> assertThat(item.get("aspect")).isNotNull());
        });
    }

    @Then("I check, if the result contains the material numbers:")
    public void iCheckIfTheResultContainsMaterialNumbers(final List<String> materialNumbers) {
        final List<String> found = new ArrayList<>();
        forEachMaterial(material -> found.add((String) material.get("materialNumber")));
        assertThat(found).as("material numbers in the whole tree").containsAll(materialNumbers);
    }

    @Then("I check, if the result contains a tombstone with reason {string}")
    public void iCheckIfTheResultContainsTombstoneReason(final String reason) {
        final List<String> reasons = new ArrayList<>();
        collectTombstoneReasons(jobStatus.getList("result.tombstones"), jobStatus.getList("result.childItems"),
                reasons);
        assertThat(reasons).as("tombstone reasons in the whole result").contains(reason);
    }

    @Then("I check, if the result contains no tombstones")
    public void iCheckIfTheResultContainsNoTombstones() {
        final List<String> reasons = new ArrayList<>();
        collectTombstoneReasons(jobStatus.getList("result.tombstones"), jobStatus.getList("result.childItems"),
                reasons);
        assertThat(reasons).as("tombstone reasons in the whole result").isEmpty();
    }

    // PUT replaces the stored grant for this key. A grant left over from an earlier run may have
    // expired or carry a different allow-list, so it is overwritten instead of reused.
    private void putGrant(final List<String> allowedBpnls) {
        final Map<String, Object> grant = new HashMap<>();
        grant.put("openingId", chain.getOpeningId());
        grant.put("useCase", chain.getUseCase());
        grant.put("globalAssetId", chain.getRootGlobalAssetId());
        grant.put("requesterBpn", chain.getRootBpnl());
        grant.put("allowedBpnlSet", allowedBpnls);
        grant.put("validFrom", GRANT_VALID_FROM);
        grant.put("validTo", GRANT_VALID_TO);

        givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                            .body(grant)
                                                            .put(GRANTS_PATH)
                                                            .then()
                                                            .statusCode(HttpStatus.OK.value());
    }

    @SuppressWarnings("unchecked")
    private void forEachMaterial(final Consumer<Map<String, Object>> check) {
        final List<Map<String, Object>> roots = jobStatus.getList("result.childItems");
        assertThat(roots).as("materials below the root").isNotEmpty();
        final Deque<Map<String, Object>> pending = new ArrayDeque<>(roots);
        while (!pending.isEmpty()) {
            final Map<String, Object> material = pending.pop();
            check.accept(material);
            final List<Map<String, Object>> children = (List<Map<String, Object>>) material.get("childItems");
            if (children != null) {
                pending.addAll(children);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private int depthOf(final List<Map<String, Object>> childItems) {
        if (childItems == null || childItems.isEmpty()) {
            return 0;
        }
        return 1 + childItems.stream()
                             .mapToInt(child -> depthOf((List<Map<String, Object>>) child.get("childItems")))
                             .max()
                             .orElse(0);
    }

    @SuppressWarnings("unchecked")
    private void collectTombstoneReasons(final List<Map<String, Object>> tombstones,
            final List<Map<String, Object>> childItems, final List<String> reasons) {
        if (tombstones != null) {
            tombstones.stream().map(tombstone -> (String) tombstone.get("reason")).forEach(reasons::add);
        }
        if (childItems != null) {
            for (final Map<String, Object> childItem : childItems) {
                collectTombstoneReasons((List<Map<String, Object>>) childItem.get("tombstones"),
                        (List<Map<String, Object>>) childItem.get("childItems"), reasons);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private int countChildItems(final List<Map<String, Object>> childItems) {
        if (childItems == null) {
            return 0;
        }
        int count = childItems.size();
        for (final Map<String, Object> childItem : childItems) {
            count += countChildItems((List<Map<String, Object>>) childItem.get("childItems"));
        }
        return count;
    }

    private JsonPath fetchJobStatus() {
        return givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                                   .get(JOBS_PATH + "/" + jobId)
                                                                   .then()
                                                                   .statusCode(HttpStatus.OK.value())
                                                                   .extract()
                                                                   .jsonPath();
    }
}
