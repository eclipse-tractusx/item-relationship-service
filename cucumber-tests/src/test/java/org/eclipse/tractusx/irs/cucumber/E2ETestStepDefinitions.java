/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.cucumber;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class E2ETestStepDefinitions {
    private RegisterJob.RegisterJobBuilder registerJobBuilder;
    private UUID jobId;
    private Jobs completedJob;
    private ObjectMapper objectMapper;
    private AuthenticationProperties authProperties;

    @Before
    public void setup() {
        registerJobBuilder = RegisterJob.builder();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Given("the environment is {string}")
    public void theEnvironmentIs(String environment) {
        final String clientId = System.getenv("KEYCLOAK_CLIENT_ID");
        final String clientSecret = System.getenv("KEYCLOAK_CLIENT_SECRET");
        final String keycloakUrl = System.getenv("KEYCLOAK_HOST");
        final String irsUri;
        if (environment.equals("DEV")) {
            irsUri = System.getenv("IRS_DEV");
        } else if (environment.equals("INT")) {
            irsUri = System.getenv("IRS_INT");
        } else {
            throw new PendingException(String.format("No implementation for environment: '%s'", environment));
        }
        authProperties = new AuthenticationProperties(irsUri, clientId, clientSecret, keycloakUrl, "client_credentials",
                "access_token");
    }

    @Given("I register an IRS job for globalAssetId {string}")
    public void iRegisterAnIRSJobForGlobalAssetId(String globalAssetId) {
        registerJobBuilder.globalAssetId(globalAssetId);
    }

    @And("collectAspects {string}")
    public void collectAspects(String collectAspects) {
        registerJobBuilder.collectAspects(Boolean.parseBoolean(collectAspects));
    }

    @And("depth {int}")
    public void depth(int depth) {
        registerJobBuilder.depth(depth);
    }

    @And("direction {string}")
    public void direction(String direction) {
        registerJobBuilder.direction(Direction.fromValue(direction));
    }

    @And("bomLifecycle {string}")
    public void bomlifecycle(String bomLifecycle) {
        registerJobBuilder.bomLifecycle(BomLifecycle.fromValue(bomLifecycle));
    }

    @And("aspects :")
    public void aspects(List<String> aspects) {
        final ArrayList<AspectType> parsedAspects = new ArrayList<>();
        aspects.forEach(s -> parsedAspects.add(AspectType.fromValue(s)));
        registerJobBuilder.aspects(parsedAspects);
    }

    @When("I get the job-id")
    public void iGetTheJobId() {
        final RegisterJob job = registerJobBuilder.build();

        final JobHandle createdJobResponse = given().spec(authProperties.getNewAuthenticationRequestSpecification())
                                                    .contentType(ContentType.JSON)
                                                    .body(job)
                                                    .when()
                                                    .post("/irs/jobs")
                                                    .then()
                                                    .statusCode(HttpStatus.CREATED.value())
                                                    .extract()
                                                    .as(JobHandle.class);

        assertThat(createdJobResponse.getJobId()).isNotNull();
        jobId = createdJobResponse.getJobId();
    }

    @Then("I check, if the job has status {string} within {int} minutes")
    public void iCheckIfTheJobHasStatusWithinMinutes(String status, int maxWaitTime) {
        await().atMost(maxWaitTime, TimeUnit.MINUTES)
               .with()
               .pollInterval(Duration.ofSeconds(5L))
               .until(() -> given().spec(authProperties.getNewAuthenticationRequestSpecification())
                                   .contentType(ContentType.JSON)
                                   .queryParam("returnUncompletedJob", false)
                                   .get("/irs/jobs/" + jobId)
                                   .as(Jobs.class)
                                   .getJob()
                                   .getState()
                                   .equals(JobState.value(status)));

        completedJob = given().spec(authProperties.getNewAuthenticationRequestSpecification())
                              .contentType(ContentType.JSON)
                              .queryParam("returnUncompletedJob", true)
                              .get("/irs/jobs/" + jobId)
                              .as(Jobs.class);
    }

    @And("I check, if number of {string} equals to {string}")
    public void iCheckIfNumberOfEqualsTo(String valueType, String arg1) {
        if ("tombstones".equals(valueType)) {
            assertThat(completedJob.getTombstones()).hasSize(
                    completedJob.getJob().getSummary().getAsyncFetchedItems().getFailed());
        } else {
            throw new PendingException();
        }
    }

    @And("I check, if summary contains {int} completed and {int} failed items")
    public void iCheckIfSummaryContainsCompletedAndFailedItems(int completed, int failed) {
        assertThat(completedJob.getJob().getSummary().getAsyncFetchedItems().getCompleted()).isEqualTo(completed);
        assertThat(completedJob.getJob().getSummary().getAsyncFetchedItems().getFailed()).isEqualTo(failed);
    }

    @And("I check, if {string} are equal to {string}")
    public void iCheckIfAreAsExpected(String valueType, String fileName) throws IOException {
        if ("relationships".equals(valueType)) {
            final List<Relationship> actualRelationships = completedJob.getRelationships();
            final List<Relationship> expectedRelationships = getExpectedRelationships(fileName);
            assertThat(actualRelationships).hasSameSizeAs(expectedRelationships)
                                           .containsAll(expectedRelationships);
        } else if ("submodels".equals(valueType)) {
            final List<Submodel> actualSubmodels = completedJob.getSubmodels();
            final List<Submodel> expectedSubmodels = getExpectedSubmodels(fileName);
            assertThat(actualSubmodels).hasSameSizeAs(expectedSubmodels)
                                       .usingRecursiveFieldByFieldElementComparatorIgnoringFields("identification")
                                       .containsAll(expectedSubmodels);
        }
    }

    private List<Submodel> getExpectedSubmodels(final String fileName) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("expected-files/" + fileName).getFile());
        assertThat(file).exists();
        final Jobs expectedJob = objectMapper.readValue(file, Jobs.class);
        return expectedJob.getSubmodels();
    }

    private List<Relationship> getExpectedRelationships(final String fileName) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("expected-files/" + fileName).getFile());
        assertThat(file).exists();
        final Jobs expectedJob = objectMapper.readValue(file, Jobs.class);
        return expectedJob.getRelationships();
    }

    @And("I check, if submodels contains BPNL number {string} exactly {int} times")
    public void iCheckIfSubmodelsContainsBPNLNumberExactlyTimes(String bpnNumber, int numberOfOccurrence) {
        assertThat((int) completedJob.getSubmodels()
                                     .stream()
                                     .filter(submodel -> submodel.getPayload().toString().contains(bpnNumber))
                                     .count()).isEqualTo(numberOfOccurrence);
    }

    @And("I check, if submodels contains BPNL number {string} at least {int} times")
    public void iCheckIfSubmodelsContainsBPNLNumberAtLeastTimes(String bpnlNumber, int numberOfOccurrence) {
        assertThat((int) completedJob.getSubmodels()
                                     .stream()
                                     .filter(submodel -> submodel.getPayload().toString().contains(bpnlNumber))
                                     .count()).isGreaterThanOrEqualTo(numberOfOccurrence);
    }

    @And("{string} are empty")
    public void areEmpty(String valueType) {
        if ("tombstones".equals(valueType)) {
            assertThat(completedJob.getTombstones()).isEmpty();
        } else if ("submodels".equals(valueType)) {
            assertThat(completedJob.getSubmodels()).isEmpty();
        } else if ("relationships".equals(valueType)) {
            assertThat(completedJob.getRelationships()).isEmpty();
        } else if ("shells".equals(valueType)) {
            assertThat(completedJob.getShells()).isEmpty();
        } else if ("bpns".equals(valueType)) {
            assertThat(completedJob.getBpns()).isEmpty();
        } else {
            throw new PendingException();
        }
    }

    @After("@INTEGRATION_TEST")
    public void addJobIdToResult(Scenario scenario) {
        scenario.attach(jobId.toString(), MediaType.TEXT_PLAIN_VALUE, "jobId");
    }


}
