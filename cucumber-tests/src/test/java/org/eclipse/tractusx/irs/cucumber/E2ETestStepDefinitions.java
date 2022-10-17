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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.springframework.http.HttpStatus;

public class E2ETestStepDefinitions {
    private RegisterJob.RegisterJobBuilder registerJobBuilder;
    private UUID jobId;
    private Jobs completedJob;
    private RequestSpecification authenticationRequest;
    private ObjectMapper objectMapper;
    private String uri;

    @Before
    public void setup() {
        registerJobBuilder = RegisterJob.builder();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private String obtainAccessToken() {
        final Map<String, String> oauth2Payload = new HashMap<>();
        oauth2Payload.put("grant_type", System.getenv("GRANT_TYPE"));
        oauth2Payload.put("client_id", System.getenv("CLIENT_ID"));
        oauth2Payload.put("client_secret", System.getenv("CLIENT_SECRET"));

        return given().params(oauth2Payload)
                      .post(System.getenv("TOKEN_URL"))
                      .then()
                      .extract()
                      .jsonPath()
                      .getString("access_token");
    }

    private RequestSpecification getAuthenticationRequestWithNewToken() {
        final String accessToken = obtainAccessToken();
        final RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.addHeader("Authorization", "Bearer " + accessToken);
        builder.setBaseUri(uri);

        return builder.build();
    }

    @Given("I have authorization with Keycloak to {string}")
    public void iHaveAuthorizationWithKeycloakTo(String environment) {
        final String accessToken = obtainAccessToken();
        final RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.addHeader("Authorization", "Bearer " + accessToken);
        if ("DEV".equals(environment)) {
            uri = "https://irs.dev.demo.catena-x.net";
        } else if ("INT".equals(environment)) {
            uri = "https://irs.int.demo.catena-x.net";
        } else {
            throw new PendingException(String.format("No implementation for environment: '%s'", environment));
        }
        builder.setBaseUri(uri);

        authenticationRequest = builder.build();
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

    @And("aspects :")
    public void aspects(List<String> aspects) {
        System.out.println(aspects);
        final ArrayList<AspectType> parsedAspects = new ArrayList<>();
        aspects.forEach(s -> parsedAspects.add(AspectType.fromValue(s)));
        registerJobBuilder.aspects(parsedAspects);
    }

    @When("I get the job-id")
    public void iGetTheJobId() {
        final RegisterJob job = registerJobBuilder.build();
        System.out.println(job);

        final JobHandle createdJobResponse = given().spec(authenticationRequest)
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
               .until(() -> given().spec(getAuthenticationRequestWithNewToken())
                                   .contentType(ContentType.JSON)
                                   .queryParam("returnUncompletedJob", false)
                                   .get("/irs/jobs/" + jobId)
                                   .as(Jobs.class)
                                   .getJob()
                                   .getJobState()
                                   .equals(JobState.value(status)));

        completedJob = given().spec(getAuthenticationRequestWithNewToken())
                              .contentType(ContentType.JSON)
                              .queryParam("returnUncompletedJob", true)
                              .get("/irs/jobs/" + jobId)
                              .as(Jobs.class);
    }

    @And("I check, if number of {string} equals to {string}")
    public void iCheckIfNumberOfEqualsTo(String valueType, String arg1) {
        if ("tombstones".equals(valueType)) {
            assertThat(completedJob.getTombstones().size()).isEqualTo(
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

    @And("I check, if {string} are as expected")
    public void iCheckIfAreAsExpected(String valueType) throws IOException {
        if ("submodels".equals(valueType)) {
            assertThat(objectMapper.writeValueAsString(completedJob.getSubmodels())).isEqualToIgnoringWhitespace(
                    getExpectedAsString(valueType));
        } else if ("relationships".equals(valueType)) {
            assertThat(objectMapper.writeValueAsString(completedJob.getRelationships())).isEqualToIgnoringWhitespace(
                    getExpectedAsString(valueType));
        }
    }

    private String getExpectedAsString(final String valueType) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource(valueType + ".json").getFile());
        assertThat(file.exists()).isTrue();
        final LinkedHashMap values = objectMapper.readValue(file, LinkedHashMap.class);
        final LinkedHashMap values2 = (LinkedHashMap) values.get("values");
        return objectMapper.writeValueAsString(values2.get(valueType));
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
}
