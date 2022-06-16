package net.catenax.irs.smoketest;

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
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.configuration.SmokeTestConfiguration;
import net.catenax.irs.configuration.SmokeTestConnectionProperties;
import net.catenax.irs.configuration.SmokeTestCredentialsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { SmokeTestConfiguration.class })
public class ItemGraphSmokeTest {

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

    private static String obtainAccessToken(final String grantType, final String clientId, final String clientSecret, final String accessToken) {
        final Map<String, String> oauth2Payload = new HashMap<String, String>();
        oauth2Payload.put("grant_type", grantType);
        oauth2Payload.put("client_id", clientId);
        oauth2Payload.put("client_secret", clientSecret);

        return given().params(oauth2Payload)
                      .post(accessToken)
                      .then()
                      .extract()
                      .jsonPath()
                      .getString("access_token");
    }

    @BeforeEach
    void setUp() {
        final String accessToken =
                obtainAccessToken(credentialsProperties.getAuthorizationGrantType(),
                        credentialsProperties.getClientId(),
                        credentialsProperties.getClientSecret(),
                        connectionProperties.getAccessToken());

        final RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.addHeader("Authorization", "Bearer " + accessToken);
        builder.setBaseUri(connectionProperties.getBaseUri());

        authenticationRequest = builder.build();
    }

    @Test
    public void shouldCreateAndCompleteJob() {
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
        assertThat(relationships.size()).isEqualTo(0);
        assertThat(getPartialJobs.getShells().size()).isGreaterThanOrEqualTo(0);
        assertThat(getPartialJobs.getTombstones().size()).isEqualTo(0);

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
        assertThat(completedJobs.getRelationships().size()).isGreaterThanOrEqualTo(0);
        assertThat(completedJobs.getShells().size()).isGreaterThan(0);
        assertThat(completedJobs.getTombstones().size()).isGreaterThanOrEqualTo(0);

        final AssetAdministrationShellDescriptor assDescriptor = completedJobs.getShells().get(0);
        final List<SubmodelDescriptor> submodelDescriptors = assDescriptor.getSubmodelDescriptors();
        assertThat(submodelDescriptors.size()).isGreaterThan(0);
    }

}
