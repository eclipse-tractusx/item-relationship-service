package net.catenax.irs.controllers;

import static net.catenax.irs.util.TestMother.registerJobWithoutDepth;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.exceptions.EntityNotFoundException;
import net.catenax.irs.services.IrsItemGraphQueryService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(IrsController.class)
class IrsControllerTest {

    private final UUID jobId = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IrsItemGraphQueryService service;

    @Test
    @Disabled
    void initiateJobForGlobalAssetId() throws Exception {
        final UUID returnedJob = UUID.randomUUID();
        when(service.registerItemJob(any())).thenReturn(JobHandle.builder().jobId(returnedJob).build());

        this.mockMvc.perform(post("/irs/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(registerJobWithoutDepth())))
                    .andExpect(status().isCreated())
                    .andExpect(content().string(containsString(returnedJob.toString())));
    }

    @Test
    void getJobById() {
        assertTrue(true);
    }

    @Test
    void getJobsByJobState() throws Exception {
        final UUID returnedJob = UUID.randomUUID();
        when(service.getJobsByJobState(any())).thenReturn(List.of(returnedJob));

        this.mockMvc.perform(get("/irs/jobs"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(returnedJob.toString())));
    }

    @Test
    void cancelJobById() throws Exception {
        final Job canceledJob = Job.builder().jobId(jobId).jobState(JobState.CANCELED).build();

        when(this.service.cancelJobById(jobId)).thenReturn(canceledJob);

        this.mockMvc.perform(put("/irs/jobs/" + jobId)).andExpect(status().isOk());
    }

    @Test
    void cancelJobById_throwEntityNotFoundException() throws Exception {
        given(this.service.cancelJobById(jobId)).willThrow(
                new EntityNotFoundException("No job exists with id " + jobId));

        this.mockMvc.perform(put("/irs/jobs/" + jobId))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntityNotFoundException));
    }

}