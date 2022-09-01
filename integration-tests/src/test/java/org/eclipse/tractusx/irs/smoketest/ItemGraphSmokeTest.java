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
    private static final List<AspectType> ASPECTS = List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP);
    private static RequestSpecification authenticationRequest;

    private static RegisterJob registerJob() {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setGlobalAssetId(GLOBAL_ASSET_ID);
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
                connectionProperties.getAccessToken());

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

        final UUID createdJobId = createdJobResponse.getJobId();
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
        assertThat(partialJob.getJobId()).isNotNull();
        assertThat(partialJob.getJobState()).isIn(JobState.COMPLETED, JobState.RUNNING, JobState.TRANSFERS_FINISHED);

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
                                   .getJobState()
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
        assertThat(completedJobs.getJob().getJobState()).isEqualTo(JobState.COMPLETED);
        assertThat(completedJobs.getRelationships().size()).isNotNegative();
        assertThat(completedJobs.getShells()).isNotEmpty();
        assertThat(completedJobs.getTombstones().size()).isNotNegative();
        assertThat(completedJobs.getBpns()).isNotEmpty();

        final AssetAdministrationShellDescriptor assDescriptor = completedJobs.getShells().get(0);
        final List<SubmodelDescriptor> submodelDescriptors = assDescriptor.getSubmodelDescriptors();
        assertThat(submodelDescriptors).isNotEmpty();
    }

}
