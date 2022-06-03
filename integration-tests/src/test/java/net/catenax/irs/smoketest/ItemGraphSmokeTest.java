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
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {SmokeTestConfiguration.class})
public class ItemGraphSmokeTest {

    @Autowired
    private SmokeTestConnectionProperties connectionProperties;

    @Autowired
    private SmokeTestCredentialsProperties credentialsProperties;

    private static final String GLOBAL_ASSET_ID = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
    private static final int TREE_DEPTH = 5;
    private static final List<AspectType> ASPECTS = List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP);
    private static RequestSpecification requestSpecification;

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

        requestSpecification = builder.build();
    }

    @Test
    void shouldCreateAndCompleteJob() {
        // Integration test Scenario 2 STEP 1
        final JobHandle responsePost = given().spec(requestSpecification)
                                              .contentType("application/json")
                                              .body(registerJob())
                                              .when()
                                              .post("/irs/jobs")
                                              .then()
                                              .statusCode(HttpStatus.CREATED.value())
                                              .extract()
                                              .as(JobHandle.class);

        assertThat(responsePost).isNotNull();

        final UUID jobId = responsePost.getJobId();
        assertThat(jobId).isNotNull();

        // Integration test Scenario 2 STEP 2
        final Jobs responseGet = given().spec(requestSpecification)
                                        .contentType("application/json")
                                        .queryParam("returnUncompletedJob", true)
                                        .get("/irs/jobs/" + jobId)
                                        .then()
                                        .assertThat()
                                        .statusCode(HttpStatus.OK.value())
                                        .extract()
                                        .as(Jobs.class);

        assertThat(responseGet).isNotNull();

        final Job job = responseGet.getJob();
        assertThat(job).isNotNull();
        assertThat(job.getJobId()).isNotNull();
        assertThat(job.getJobState()).isIn(JobState.COMPLETED, JobState.RUNNING, JobState.TRANSFERS_FINISHED);

        final GlobalAssetIdentification globalAsset = job.getGlobalAssetId();
        assertThat(globalAsset.getGlobalAssetId()).isEqualTo(GLOBAL_ASSET_ID);

        final List<Relationship> relationships = responseGet.getRelationships();
        assertThat(relationships).isNotEmpty();
        assertThat(responseGet.getShells()).isNull();
        assertThat(responseGet.getTombstones()).isNotEmpty();

        // Integration test Scenario 2 STEP 3
        final Response responseGetPoll = given().spec(requestSpecification)
                                                .contentType("application/json")
                                                .queryParam("returnUncompletedJob", true)
                                                .get("/irs/jobs/" + jobId);

        await().atMost(30, TimeUnit.SECONDS)
               .until(() -> responseGetPoll.as(Jobs.class).getJob().getJobState().equals(JobState.COMPLETED));

        final Jobs jobs = responseGetPoll.then()
                                         .assertThat()
                                         .statusCode(HttpStatus.OK.value())
                                         .extract()
                                         .as(Jobs.class);

        assertThat(jobs).isNotNull();

        assertThat(jobs.getJob()).isNotNull();
        assertThat(jobs.getRelationships()).isNotEmpty();
        assertThat(jobs.getShells()).isNull();
        assertThat(jobs.getTombstones()).isNotEmpty();
    }

}
