package net.catenax.irs.smoketest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.JobState;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles(profiles = { "test" })
public class ItemGraphIntegrationTest {

    private static final String GLOBAL_ASSET_ID = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
    private static final int TREE_DEPTH = 5;
    private static final List<AspectType> ASPECTS = List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP);

    private static RegisterJob registerJob() {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setGlobalAssetId(GLOBAL_ASSET_ID);
        registerJob.setDepth(TREE_DEPTH);
        registerJob.setAspects(ASPECTS);
        registerJob.setBomLifecycle(BomLifecycle.AS_BUILT);

        return registerJob;
    }

    @Test
    void initiateJobForGlobalAssetId() throws JsonProcessingException, InterruptedException {
        // Integration test Scenario 2 STEP 1
        final JobHandle responsePost = given().contentType("application/json")
                                              .body(new ObjectMapper().writeValueAsString(registerJob()))
                                              .when()
                                              .post("http://localhost:8081/irs/jobs")
                                              .then()
                                              .statusCode(HttpStatus.CREATED.value())
                                              .extract()
                                              .as(JobHandle.class);

        assertThat(responsePost).isNotNull();
        System.out.println("responsePost: " + responsePost);

        final UUID jobId = responsePost.getJobId();
        assertThat(jobId).isNotNull();

        // Integration test Scenario 2 STEP 2
        final boolean returnUncompletedJob = true;
        final Jobs responseGet = given().body(returnUncompletedJob)
                                        .get("http://localhost:8081/irs/jobs/" + jobId)
                                        .then()
                                        .assertThat()
                                        .statusCode(HttpStatus.OK.value())
                                        .extract()
                                        .as(Jobs.class);

        assertThat(responseGet).isNotNull();
        System.out.println("responseGet: " + responseGet);

        final Job job = responseGet.getJob();
        assertThat(job).isNotNull();
        assertThat(job.getJobId()).isNotNull();
        assertThat(job.getJobState()).isEqualTo(JobState.COMPLETED);

        final GlobalAssetIdentification globalAsset = job.getGlobalAssetId();
        assertThat(globalAsset.getGlobalAssetId()).isEqualTo(GLOBAL_ASSET_ID);

        final List<Relationship> relationships = responseGet.getRelationships();
        assertThat(relationships).isNotEmpty();
        assertThat(responseGet.getShells()).isNull();
        assertThat(responseGet.getTombstones()).isNotEmpty();

        // Integration test Scenario 2 STEP 3
        final Response responseGetPoll = given().body(returnUncompletedJob)
                                                .get("http://localhost:8081/irs/jobs/" + jobId);


        TimeUnit.SECONDS.sleep(10);

        final Jobs jobs = responseGetPoll.then()
                                         .assertThat()
                                         .statusCode(HttpStatus.OK.value())
                                         .extract()
                                         .as(Jobs.class);

        assertThat(jobs).isNotNull();
        System.out.println("jobs: " + jobs);

        assertThat(jobs.getJob()).isNotNull();
        assertThat(jobs.getRelationships()).isNotEmpty();
        assertThat(jobs.getShells()).isNull();
        assertThat(jobs.getTombstones()).isNotEmpty();
    }

    @Test
    public void testRegisterItemJob() throws JsonProcessingException {
        final String responsePost = given().contentType("application/json")
                                           .body(new ObjectMapper().writeValueAsString(registerJob()))
                                           .when()
                                           .post("http://localhost:8081/irs/jobs")
                                           .then()
                                           .statusCode(HttpStatus.CREATED.value())
                                           .extract()
                                           .asString();

        assertThat(responsePost).isNotNull();

        final JsonNode jsonNode = new ObjectMapper().readTree(responsePost);
        String jobId = jsonNode.get("jobId").asText();
        final JobHandle replyJobHandle = JobHandle.builder().jobId(UUID.fromString(jobId)).build();
        assertThat(replyJobHandle.getJobId()).isNotNull();
    }

}
