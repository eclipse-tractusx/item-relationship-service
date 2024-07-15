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
import static org.awaitility.Awaitility.await;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelper.getExpectedFile;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelper.givenAuthentication;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelper.objectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.DataTableType;
import io.cucumber.java.PendingException;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.eclipse.tractusx.irs.component.BatchOrderCreated;
import org.eclipse.tractusx.irs.component.BatchOrderResponse;
import org.eclipse.tractusx.irs.component.BatchResponse;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.JobStatusResult;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterBatchOrder;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.enums.BatchStrategy;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Step definitions for Job API.
 */
public class E2ETestStepDefinitionsForJobApi {

    private final RegisterJob.RegisterJobBuilder registerJobBuilder;

    private final RegisterBatchOrder.RegisterBatchOrderBuilder registerBatchOrderBuilder;

    private UUID jobId;
    private UUID orderId;
    private UUID batchId;

    private Jobs completedJob;

    private BatchOrderResponse batchOrderResponse;
    private BatchResponse batchResponse;

    private final AuthenticationProperties.AuthenticationPropertiesBuilder authenticationPropertiesBuilder;

    public E2ETestStepDefinitionsForJobApi() {
        registerJobBuilder = RegisterJob.builder();
        registerBatchOrderBuilder = RegisterBatchOrder.builder();
        authenticationPropertiesBuilder = AuthenticationProperties.builder();
    }

    @BeforeAll
    public static void configureLogging() {
        E2ETestHelper.configureRequestAndResponseLogging();
    }

    @DataTableType
    public PartChainIdentificationKey definePartChainIdentificationKey(Map<String, String> entry) {
        return new PartChainIdentificationKey(entry.get("globalAssetId"), entry.get("bpn"));
    }

    @Given("the IRS URL {string}")
    public void theIRSURL(String irsUrl) {
        authenticationPropertiesBuilder.uri(irsUrl);
    }

    @And("the regular user api key")
    public void theRegularUser() throws PropertyNotFoundException {
        final String regularUserApiKey = "REGULAR_USER_API_KEY";
        final String apiKey = System.getenv(regularUserApiKey);
        if (apiKey != null) {
            authenticationPropertiesBuilder.apiKey(apiKey);
        } else {
            throw new PropertyNotFoundException("Environment Variable missing: " + regularUserApiKey);
        }
    }

    @And("the admin user api key")
    public void theAdminUser() throws PropertyNotFoundException {
        final String adminUserApiKey = "ADMIN_USER_API_KEY";
        final String apiKey = System.getenv(adminUserApiKey);
        if (apiKey != null) {
            authenticationPropertiesBuilder.apiKey(apiKey);
        } else {
            throw new PropertyNotFoundException("Environment Variable missing: " + adminUserApiKey);
        }
    }

    @Given("I register an IRS job for globalAssetId {string}")
    public void iRegisterAnIRSJobForGlobalAssetId(String globalAssetId) {
        registerJobBuilder.key(PartChainIdentificationKey.builder().globalAssetId(globalAssetId).build());
    }

    @Given("I register an IRS job for globalAssetId {string} and BPN {string}")
    public void iRegisterAnIRSJobForGlobalAssetIdAndBpn(String globalAssetId, String bpn) {
        registerJobBuilder.key(PartChainIdentificationKey.builder().globalAssetId(globalAssetId).bpn(bpn).build());
    }

    @Given("I register an IRS batch job for globalAssetIds and BPNs:")
    public void iRegisterAnIRSBatchForGlobalAssetIdsAndBpns(List<PartChainIdentificationKey> keys) {
        registerBatchOrderBuilder.keys(Set.copyOf(keys));
    }

    @Given("I register an IRS batch job for globalAssetIds and BPN:")
    public void iRegisterAnIRSBatchForGlobalAssetIds(Map<String, String> keys) {
        final Set<PartChainIdentificationKey> keySet = new HashSet<>();
        keys.forEach((globalAssetId, bpn) -> keySet.add(
                PartChainIdentificationKey.builder().globalAssetId(globalAssetId).bpn(bpn).build()));
        registerBatchOrderBuilder.keys(keySet);
    }

    @And("collectAspects {string}")
    public void collectAspects(String collectAspects) {
        registerJobBuilder.collectAspects(Boolean.parseBoolean(collectAspects));
        registerBatchOrderBuilder.collectAspects(Boolean.parseBoolean(collectAspects));
    }

    @And("depth {int}")
    public void depth(int depth) {
        registerJobBuilder.depth(depth);
        registerBatchOrderBuilder.depth(depth);
    }

    @And("direction {string}")
    public void direction(String direction) {
        registerJobBuilder.direction(Direction.fromValue(direction));
        registerBatchOrderBuilder.direction(Direction.fromValue(direction));
    }

    @And("bomLifecycle {string}")
    public void bomlifecycle(String bomLifecycle) {
        registerJobBuilder.bomLifecycle(BomLifecycle.fromValue(bomLifecycle));
        registerBatchOrderBuilder.bomLifecycle(BomLifecycle.fromValue(bomLifecycle));
    }

    @And("aspects :")
    public void aspects(List<String> aspects) {
        registerJobBuilder.aspects(aspects);
        registerBatchOrderBuilder.aspects(aspects);
    }

    @And("callbackUrl {string}")
    public void callbackUrl(String callbackUrl) {
        registerJobBuilder.callbackUrl(callbackUrl);
        registerBatchOrderBuilder.callbackUrl(callbackUrl);
    }

    @And("batchStrategy {string}")
    public void batchStrategy(String batchStrategy) {
        registerBatchOrderBuilder.batchStrategy(BatchStrategy.valueOf(batchStrategy));
    }

    @And("batchSize {int}")
    public void batchSize(int batchSize) {
        registerBatchOrderBuilder.batchSize(batchSize);
    }

    @And("jobTimeout {int}")
    public void jobTimeout(int jobTimeout) {
        registerBatchOrderBuilder.jobTimeout(jobTimeout);
    }

    @And("timeout {int}")
    public void timeout(int timeout) {
        registerBatchOrderBuilder.timeout(timeout);
    }

    @When("I get the job-id")
    public void iGetTheJobId() {
        final RegisterJob job = registerJobBuilder.build();

        final JobHandle createdJobResponse = //
                givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                                    .body(job)
                                                                    .when()
                                                                    .post("/irs/jobs")
                                                                    .then()
                                                                    .statusCode(HttpStatus.CREATED.value())
                                                                    .extract()
                                                                    .as(JobHandle.class);

        assertThat(createdJobResponse.getId()).isNotNull();
        jobId = createdJobResponse.getId();
    }

    @When("I get the order-id")
    public void iGetTheOrderId() {
        final RegisterBatchOrder order = registerBatchOrderBuilder.build();

        final BatchOrderCreated createdOrderResponse = //
                givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                                    .body(order)
                                                                    .when()
                                                                    .post("/irs/orders")
                                                                    .then()
                                                                    .statusCode(HttpStatus.CREATED.value())
                                                                    .extract()
                                                                    .as(BatchOrderCreated.class);

        assertThat(createdOrderResponse.id()).isNotNull();
        orderId = createdOrderResponse.id();
    }

    @When("I get the batch-id of {string} batch")
    public void iGetTheBatchIdOfBatch(String which) {
        final UUID foundBatchId = switch (which) {
            case "first" -> batchOrderResponse.getBatches()
                                              .stream()
                                              .filter(batch -> batch.getBatchNumber().equals(1))
                                              .findFirst()
                                              .map(BatchOrderResponse.BatchResponse::getBatchId)
                                              .orElseThrow();
            case "last" -> batchOrderResponse.getBatches()
                                             .stream()
                                             .filter(batch -> batch.getBatchNumber()
                                                                   .equals(batchOrderResponse.getBatches().size()))
                                             .findFirst()
                                             .map(BatchOrderResponse.BatchResponse::getBatchId)
                                             .orElseThrow();
            case "any" -> batchOrderResponse.getBatches()
                                            .stream()
                                            .findAny()
                                            .map(BatchOrderResponse.BatchResponse::getBatchId)
                                            .orElseThrow();
            default -> throw new PendingException(String.format("Type: '%s' not supported.", which));
        };

        assertThat(foundBatchId).isNotNull();
        batchId = foundBatchId;
    }

    @When("I get the {string} job-id from batch")
    public void iGetTheJobIdFromBatch(String which) {
        final UUID foundJobId = switch (which) {
            case "first" -> batchResponse.getJobs().get(0).getId();
            case "any" -> batchResponse.getJobs().stream().findAny().map(JobStatusResult::getId).orElseThrow();
            default -> throw new PendingException(String.format("Type: '%s' not supported.", which));
        };

        assertThat(foundJobId).isNotNull();
        jobId = foundJobId;
    }

    @Then("I check, if the job has status {string} within {int} minutes")
    public void iCheckIfTheJobHasStatusWithinMinutes(String status, int maxWaitTime) {
        await().atMost(maxWaitTime, TimeUnit.MINUTES)
               .with()
               .pollInterval(Duration.ofSeconds(5L))
               .until(() -> givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                                                .queryParam("returnUncompletedJob",
                                                                                        false)
                                                                                .get("/irs/jobs/" + jobId)
                                                                                .as(Jobs.class)
                                                                                .getJob()
                                                                                .getState()
                                                                                .equals(JobState.value(status)));

        completedJob = givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                                           .queryParam("returnUncompletedJob", true)
                                                                           .get("/irs/jobs/" + jobId)
                                                                           .as(Jobs.class);
    }

    @Then("I check, if the order contains {int} batches")
    public void iCheckIfTheOrderContainsBatches(int batchesSize) {
        batchOrderResponse = givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                                                 .get("/irs/orders/" + orderId)
                                                                                 .as(BatchOrderResponse.class);

        assertThat(batchOrderResponse.getBatches()).hasSize(batchesSize);
    }

    @Then("I check, if the batch contains {int} jobs")
    public void iCheckIfTheBatchContainsJobs(int jobSize) {
        batchResponse = givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                                            .get("/irs/orders/" + orderId + "/batches/"
                                                                                    + batchId)
                                                                            .as(BatchResponse.class);

        assertThat(batchResponse.getJobsInBatchChecksum()).isEqualTo(jobSize);
    }

    @Then("I check, if job parameter are set with aspects:")
    public void iCheckIfJobParameterAreSetWithAspects(List<String> aspects) {
        completedJob = givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                                           .queryParam("returnUncompletedJob", true)
                                                                           .get("/irs/jobs/" + jobId)
                                                                           .as(Jobs.class);

        assertThat(completedJob.getJob().getParameter().getAspects()).containsAll(aspects);
    }

    @And("I check, if number of {string} equals to {string}")
    public void iCheckIfNumberOfEqualsTo(String valueType, String summaryType) {
        int nrOfValueType = switch (valueType) {
            case "tombstones" -> completedJob.getTombstones().size();
            case "submodels" -> completedJob.getSubmodels().size();
            case "shells" -> completedJob.getShells().size();
            case "relationships" -> completedJob.getRelationships().size();
            case "bpns" -> completedJob.getBpns().size();
            default -> throw new PendingException(String.format("Type: '%s' not supported.", valueType));
        };
        int nrOfItemsInSummary = switch (summaryType) {
            case "summary/asyncFetchedItems/running" ->
                    completedJob.getJob().getSummary().getAsyncFetchedItems().getRunning();
            case "summary/asyncFetchedItems/completed" ->
                    completedJob.getJob().getSummary().getAsyncFetchedItems().getCompleted();
            case "summary/asyncFetchedItems/failed" ->
                    completedJob.getJob().getSummary().getAsyncFetchedItems().getFailed();
            default -> throw new PendingException(String.format("Type: '%s' not supported.", summaryType));
        };
        assertThat(nrOfValueType).isEqualTo(nrOfItemsInSummary);
    }

    @And("I check, if {string} contains {int} completed and {int} failed items")
    public void iCheckIfSummaryContainsCompletedAndFailedItems(String summary, int completed, int failed) {
        if ("summary".equals(summary)) {
            assertThat(completedJob.getJob().getSummary().getAsyncFetchedItems().getCompleted()).isEqualTo(completed);
            assertThat(completedJob.getJob().getSummary().getAsyncFetchedItems().getFailed()).isEqualTo(failed);
        }
    }

    @And("I check, if {string} are equal to {string}")
    public void iCheckIfAreAsExpected(String valueType, String fileName) throws IOException {
        if ("relationships".equals(valueType)) {
            final List<Relationship> actualRelationships = completedJob.getRelationships();
            final List<Relationship> expectedRelationships = getExpectedRelationships(fileName);
            assertThat(actualRelationships).hasSameSizeAs(expectedRelationships).containsAll(expectedRelationships);
        } else if ("submodels".equals(valueType)) {
            final List<Submodel> actualSubmodels = completedJob.getSubmodels();
            final List<Submodel> expectedSubmodels = getExpectedSubmodels(fileName);
            assertThat(actualSubmodels).hasSameSizeAs(expectedSubmodels)
                                       .usingRecursiveFieldByFieldElementComparatorIgnoringFields("identification",
                                               "contractAgreementId")
                                       .containsAll(expectedSubmodels);
        } else if ("tombstones".equals(valueType)) {
            final List<Tombstone> actualTombstones = completedJob.getTombstones();
            final List<Tombstone> expectedTombstones = getExpectedTombstones(fileName);
            assertThat(actualTombstones).hasSameSizeAs(expectedTombstones)
                                        .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                                                "processingError.lastAttempt", "processingError.rootCauses")
                                        .containsAll(expectedTombstones);
        }
    }

    @And("I check, if batch {int} contains {int} job")
    public void iCheckIfBatchContainsJob(int batchNumber, int jobSize) {
        final Optional<BatchOrderResponse.BatchResponse> foundBatch = batchOrderResponse.getBatches()
                                                                                        .stream()
                                                                                        .filter(batch -> batch.getBatchNumber()
                                                                                                              .equals(batchNumber))
                                                                                        .findFirst();

        assertThat(foundBatch).isPresent();
        assertThat(foundBatch.get().getJobsInBatchChecksum()).isEqualTo(jobSize);
    }

    @And("I check, if the batchNumber is {int}")
    public void iCheckIfTheBatchNumberIs(int batchNumber) {
        assertThat(batchResponse.getBatchNumber()).isEqualTo(batchNumber);
    }

    @And("batchTotal is {int}")
    public void batchTotalIs(int batchTotal) {
        assertThat(batchResponse.getBatchTotal()).isEqualTo(batchTotal);
    }

    @And("totalJobs is {int}")
    public void totalJobsIs(int totalJobs) {
        assertThat(batchResponse.getTotalJobs()).isEqualTo(totalJobs);
    }

    @And("jobsInBatchChecksum is {int}")
    public void jobsInBatchChecksumIs(int jobsInBatchChecksum) {
        assertThat(batchResponse.getJobsInBatchChecksum()).isEqualTo(jobsInBatchChecksum);
    }

    @And("collectAspects is {string}")
    public void collectAspectsIs(String collectAspects) {
        assertThat(completedJob.getJob().getParameter().isCollectAspects()).isEqualTo(Boolean.valueOf(collectAspects));
    }

    @And("depth is {int}")
    public void depthIs(int depth) {
        assertThat(completedJob.getJob().getParameter().getDepth()).isEqualTo(depth);
    }

    @And("direction is {string}")
    public void directionIs(String direction) {
        assertThat(completedJob.getJob().getParameter().getDirection()).isEqualTo(Direction.fromValue(direction));
    }

    @And("bomLifecycle is {string}")
    public void bomLifecycleIs(String bomLifecycle) {
        assertThat(completedJob.getJob().getParameter().getBomLifecycle()).isEqualTo(
                BomLifecycle.fromValue(bomLifecycle));
    }

    @And("callbackUrl is {string}")
    public void callbackUrlIs(String callbackUrl) {
        assertThat(completedJob.getJob().getParameter().getCallbackUrl()).isEqualTo(callbackUrl);
    }

    private List<Submodel> getExpectedSubmodels(final String fileName) throws IOException {
        return getExpectedJob(fileName).getSubmodels();
    }

    private List<Tombstone> getExpectedTombstones(final String fileName) throws IOException {
        return getExpectedJob(fileName).getTombstones();
    }

    private List<Relationship> getExpectedRelationships(final String fileName) throws IOException {
        return getExpectedJob(fileName).getRelationships();
    }

    private static Jobs getExpectedJob(final String fileName) throws IOException {
        final File file = getExpectedFile(fileName);
        assertThat(file).exists();
        return objectMapper.readValue(file, Jobs.class);
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
        switch (valueType) {
            case "tombstones" -> assertThat(completedJob.getTombstones()).isEmpty();
            case "submodels" -> assertThat(completedJob.getSubmodels()).isEmpty();
            case "relationships" -> assertThat(completedJob.getRelationships()).isEmpty();
            case "shells" -> assertThat(completedJob.getShells()).isEmpty();
            case "bpns" -> assertThat(completedJob.getBpns()).isEmpty();
            default -> throw new PendingException();
        }
    }

    @After("@INTEGRATION_TEST")
    public void addJobIdToResult(Scenario scenario) {
        if (jobId != null) {
            scenario.attach(jobId.toString(), MediaType.TEXT_PLAIN_VALUE, "jobId");
        }
    }

}
