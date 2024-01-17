/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.smoketest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.configuration.SmokeTestConfiguration;
import org.eclipse.tractusx.irs.configuration.SmokeTestConnectionProperties;
import org.eclipse.tractusx.irs.configuration.SmokeTestCredentialsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { SmokeTestConfiguration.class })
class ItemGraphSmokeTest {

    @Autowired
    private SmokeTestConnectionProperties connectionProperties;

    @Autowired
    private SmokeTestCredentialsProperties credentialsProperties;

    private static final String GLOBAL_ASSET_ID = "urn:uuid:5e3e9060-ba73-4d5d-a6c8-dfd5123f4d99";
    private static final int TREE_DEPTH = 2;
    private static final List<String> ASPECTS = List.of(AspectType.SINGLE_LEVEL_BOM_AS_BUILT.toString());
    private static RequestSpecification authenticationRequest;

    private static RegisterJob registerJob() {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setKey(PartChainIdentificationKey.builder().globalAssetId(GLOBAL_ASSET_ID).build());
        registerJob.setDepth(TREE_DEPTH);
        registerJob.setAspects(ASPECTS);
        registerJob.setBomLifecycle(BomLifecycle.AS_BUILT);

        return registerJob;
    }

    private static String obtainAccessToken(final String grantType, final String clientId, final String clientSecret,
            final String accessToken) {
        final Map<String, String> oauth2Payload = new HashMap<>();
        oauth2Payload.put("grant_type", grantType);
        oauth2Payload.put("client_id", clientId);
        oauth2Payload.put("client_secret", clientSecret);

        return given().params(oauth2Payload).post(accessToken).then().extract().jsonPath().getString("access_token");
    }

    @BeforeEach
    void setUp() {
        final String accessToken = obtainAccessToken(credentialsProperties.getAuthorizationGrantType(),
                credentialsProperties.getClientId(), credentialsProperties.getClientSecret(),
                connectionProperties.getAccessTokenUri());

        final RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.addHeader("Authorization", "Bearer " + accessToken);
        builder.setBaseUri(connectionProperties.getBaseUri());

        authenticationRequest = builder.build();
    }

    @Test
    void shouldCreateAndCompleteJob() {
        // Integration test Scenario 2 STEP 1
        final JobHandle createdJobResponse = given().spec(authenticationRequest)
                                                    .contentType("application/json")
                                                    .body(registerJob())
                                                    .when()
                                                    .post("/irs/jobs")
                                                    .then()
                                                    .statusCode(HttpStatus.CREATED.value())
                                                    .extract()
                                                    .as(JobHandle.class);

        assertThat(createdJobResponse).isNotNull();

        final UUID createdJobId = createdJobResponse.getId();
        assertThat(createdJobId).isNotNull();

        // Integration test Scenario 2 STEP 2
        final Jobs getPartialJobs = given().spec(authenticationRequest)
                                           .contentType("application/json")
                                           .queryParam("returnUncompletedJob", true)
                                           .get("/irs/jobs/" + createdJobId)
                                           .then()
                                           .assertThat()
                                           .statusCode(HttpStatus.OK.value())
                                           .extract()
                                           .as(Jobs.class);

        assertThat(getPartialJobs).isNotNull();

        final Job partialJob = getPartialJobs.getJob();
        assertThat(partialJob).isNotNull();
        assertThat(partialJob.getId()).isNotNull();
        assertThat(partialJob.getState()).isIn(JobState.COMPLETED, JobState.RUNNING, JobState.TRANSFERS_FINISHED);

        final GlobalAssetIdentification globalAsset = partialJob.getGlobalAssetId();
        assertThat(globalAsset.getGlobalAssetId()).isEqualTo(GLOBAL_ASSET_ID);

        final List<Relationship> relationships = getPartialJobs.getRelationships();
        assertThat(relationships).isEmpty();
        assertThat(getPartialJobs.getShells().size()).isNotNegative();
        assertThat(getPartialJobs.getTombstones()).isEmpty();

        // Integration test Scenario 2 STEP 3
        await().atMost(80, TimeUnit.SECONDS)
               .until(() -> given().spec(authenticationRequest)
                                   .contentType("application/json")
                                   .queryParam("returnUncompletedJob", true)
                                   .get("/irs/jobs/" + createdJobId)
                                   .as(Jobs.class)
                                   .getJob()
                                   .getState()
                                   .equals(JobState.COMPLETED));

        final Response pollResponse = given().spec(authenticationRequest)
                                             .contentType("application/json")
                                             .queryParam("returnUncompletedJob", true)
                                             .get("/irs/jobs/" + createdJobId);

        final Jobs completedJobs = pollResponse.then()
                                               .assertThat()
                                               .statusCode(HttpStatus.OK.value())
                                               .extract()
                                               .as(Jobs.class);

        assertThat(completedJobs).isNotNull();
        assertThat(completedJobs.getJob()).isNotNull();
        assertThat(completedJobs.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(completedJobs.getRelationships().size()).isNotNegative();
        assertThat(completedJobs.getShells()).isNotEmpty();
        assertThat(completedJobs.getTombstones().size()).isNotNegative();
        assertThat(completedJobs.getBpns()).isNotEmpty();

        final AssetAdministrationShellDescriptor assDescriptor = completedJobs.getShells().get(0);
        final List<SubmodelDescriptor> submodelDescriptors = assDescriptor.getSubmodelDescriptors();
        assertThat(submodelDescriptors).isNotEmpty();
    }

}
