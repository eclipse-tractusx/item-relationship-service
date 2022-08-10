package net.catenax.irs.controllers;

import static net.catenax.irs.util.TestMother.registerJobWithDepthAndAspect;
import static net.catenax.irs.util.TestMother.registerJobWithGlobalAssetIdAndDepth;
import static net.catenax.irs.util.TestMother.registerJobWithoutDepthAndAspect;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.JobStatusResult;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.configuration.SecurityConfiguration;
import net.catenax.irs.exceptions.EntityNotFoundException;
import net.catenax.irs.services.IrsItemGraphQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(IrsController.class)
@Import(SecurityConfiguration.class)
class IrsControllerTest {

    private final UUID jobId = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IrsItemGraphQueryService service;

    private static Stream<RegisterJob> corruptedJobs() {
        return Stream.of(registerJobWithDepthAndAspect(110, null),
                registerJobWithGlobalAssetIdAndDepth("invalidGlobalAssetId", 0, null, false),
                registerJobWithGlobalAssetIdAndDepth("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5\n\rdf6", 0, null,
                        false));
    }

    @Test
    @WithMockUser
    void initiateJobForGlobalAssetId() throws Exception {
        final UUID returnedJob = UUID.randomUUID();
        when(service.registerItemJob(any())).thenReturn(JobHandle.builder().jobId(returnedJob).build());

        this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(
                                                      registerJobWithoutDepthAndAspect())))
                    .andExpect(status().isCreated())
                    .andExpect(content().string(containsString(returnedJob.toString())));
    }

    @ParameterizedTest
    @MethodSource("corruptedJobs")
    @WithMockUser
    void shouldReturnBadRequestWhenRegisterJobBodyNotValid(final RegisterJob registerJob) throws Exception {
        this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(registerJob)))
                    .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getJobsByJobState() throws Exception {
        UUID jobId = UUID.randomUUID();
        final JobStatusResult returnedJob = JobStatusResult.builder().jobId(jobId).status(JobState.RUNNING).build();

        String returnJobAsString = new ObjectMapper().writeValueAsString(returnedJob);

        when(service.getJobsByJobState(any())).thenReturn(List.of(returnedJob));

        this.mockMvc.perform(get("/irs/jobs"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(returnJobAsString)))
                    .andExpect(content().string(containsString(returnedJob.getJobId().toString())))
                    .andExpect(content().string(containsString(returnedJob.getStatus().toString())));
    }

    @Test
    @WithMockUser
    void cancelJobById() throws Exception {
        final Job canceledJob = Job.builder().jobId(jobId).jobState(JobState.CANCELED).build();

        when(this.service.cancelJobById(jobId)).thenReturn(canceledJob);

        this.mockMvc.perform(put("/irs/jobs/" + jobId)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void cancelJobById_throwEntityNotFoundException() throws Exception {
        given(this.service.cancelJobById(jobId)).willThrow(
                new EntityNotFoundException("No job exists with id " + jobId));

        this.mockMvc.perform(put("/irs/jobs/" + jobId))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntityNotFoundException));
    }

    @Test
    @WithMockUser
    void getJobWithMalformedIdShouldReturnBadRequest() throws Exception {
        final String jobIdMalformed = UUID.randomUUID() + "MALFORMED";

        this.mockMvc.perform(get("/irs/jobs/" + jobIdMalformed)).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenRegisterJobWithMalformedAspectJson() throws Exception {
        final String requestBody = "{ \"aspects\": [ \"MALFORMED\" ], \"globalAssetId\": \"urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6\" }";

        this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                    .andExpect(status().isBadRequest());
    }

}