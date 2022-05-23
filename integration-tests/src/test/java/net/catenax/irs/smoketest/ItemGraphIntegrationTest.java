package net.catenax.irs.smoketest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles(profiles = { "test" })
public class ItemGraphIntegrationTest {

    private static final String GLOBAL_ASSET_ID = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
    private static final int TREE_DEPTH = 5;
    private static final List<AspectType> ASPECTS = List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP);


    @Test
    void initiateJobForGlobalAssetId() throws JsonProcessingException {
        // Integration test Scenario 2 STEP 1
        final JobHandle responsePost = given().body(new ObjectMapper().writeValueAsString(registerJob()))
                                              .when()
                                              .post("http://localhost:8081/irs/jobs")
                                              .then()
                                              .assertThat()
                                              .statusCode(HttpStatus.OK.value())
                                              .extract()
                                              .as(JobHandle.class);

        assertThat(responsePost.getJobId()).isNotNull();

/*        final ResultActions resultInitiateJobForGlobalAssetId = this.mvc.perform(post("/irs/jobs")
                                                                            .contentType(MediaType.APPLICATION_JSON)
                                                                            .content(new ObjectMapper().writeValueAsString(registerJob())))
                                                                        .andExpect(status().isCreated());*/

        // Deserialize response
/*        final MvcResult result1 = resultInitiateJobForGlobalAssetId.andReturn();
        String contentAsString1 = result1.getResponse().getContentAsString();

        final JobHandle returnedJob = objectMapper.readValue(contentAsString1, JobHandle.class);*/

        // jobId UUID for registered job processing globalAssetId
/*        final UUID jobId = returnedJob.getJobId();

        assertThat(returnedJob).isNotNull();
        assertThat(jobId).isNotNull();*/

        // Integration test Scenario 2 STEP 2
/*        final ResultActions resultGetJobById = this.mvc.perform(get("/irs/jobs/" + jobId)
                                                           .contentType(MediaType.APPLICATION_JSON))
                                                       .andExpect(status().is(206));*/

        // Deserialize response
/*        final MvcResult result2 = resultGetJobById.andReturn();
        String contentAsString2 = result2.getResponse().getContentAsString();

        final Jobs partialJob = objectMapper.readValue(contentAsString2, Jobs.class);

        assertThat(partialJob).isNotNull();
        assertThat(partialJob.getRelationships()).isEmpty();
        assertThat(partialJob.getShells()).isEmpty();
        assertThat(partialJob.getTombstones()).isEmpty();

        final Job job = this.getJob(partialJob);
        assertThat(job).isNotNull();
        assertThat(job.getGlobalAssetId().getGlobalAssetId()).isEqualTo(GLOBAL_ASSET_ID);
        assertThat(job.getJobState()).isEqualTo(JobState.RUNNING);*/

        // Integration test Scenario 2 STEP 3

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


/*        get("http://localhost:8080//irs/jobs")
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", is(1));*/
    }

    private static RegisterJob registerJob() {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setGlobalAssetId(GLOBAL_ASSET_ID);
        registerJob.setDepth(TREE_DEPTH);
        registerJob.setAspects(null);
        registerJob.setBomLifecycle(null);

        return registerJob;
    }

    private Job getJob(final Jobs jobs) {
        return jobs.getJob();
    }
}
