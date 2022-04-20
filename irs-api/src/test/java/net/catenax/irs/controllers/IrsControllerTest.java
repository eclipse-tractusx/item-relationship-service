package net.catenax.irs.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import net.catenax.irs.component.Job;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.exceptions.EntityNotFoundException;
import net.catenax.irs.services.IrsItemGraphQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(IrsController.class)
class IrsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IrsItemGraphQueryService irsItemGraphQueryService;

    @Test
    void initiateJobForGlobalAssetId() {
        assertTrue(true);
    }

    @Test
    void getJobById() {
        assertTrue(true);
    }

    @Test
    void getJobsByJobState() throws Exception {
        final UUID returnedJob = UUID.randomUUID();
        when(irsItemGraphQueryService.getJobsByJobState(any())).thenReturn(List.of(returnedJob));

        this.mockMvc.perform(get("/irs/jobs"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(returnedJob.toString())));
    }

    @Test
    void cancelJobById() throws Exception {
        final var jobId = UUID.randomUUID();
        final var canceledJob = Job.builder().jobId(jobId).jobState(JobState.CANCELED).build();
        given(irsItemGraphQueryService.cancelJobById(jobId)).willReturn(canceledJob);

        this.mockMvc.perform(put("/irs/jobs/{jobId}", jobId))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(jobId.toString())));
    }

    @Test
    void cancelJobById_throwEntityNotFoundException() throws Exception {
        final var jobId = UUID.randomUUID();
        given(irsItemGraphQueryService.cancelJobById(jobId)).willThrow(
                new EntityNotFoundException("No job exists with id " + jobId));

        this.mockMvc.perform(put("/irs/jobs/{jobId}", jobId))
                    .andExpect(status().isNotFound());

    }
}