/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.controllers;

import static java.lang.String.format;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithDepthAndAspect;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithGlobalAssetIdAndDepth;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithoutDepthAndAspect;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.JobStatusResult;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.configuration.SecurityConfiguration;
import org.eclipse.tractusx.irs.exceptions.EntityNotFoundException;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IrsItemGraphQueryService service;

    private static Stream<RegisterJob> corruptedJobs() {
        return Stream.of(registerJobWithDepthAndAspect(110, null),
                registerJobWithGlobalAssetIdAndDepth("invalidGlobalAssetId", 0, null, false),
                registerJobWithGlobalAssetIdAndDepth("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5\n\rdf6", 0, null,
                        false));
    }

    @Test
    @WithMockUser(authorities = "view_irs")
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
    @WithMockUser(authorities = "view_irs")
    void shouldReturnBadRequestWhenRegisterJobBodyNotValid(final RegisterJob registerJob) throws Exception {
        this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(registerJob)))
                    .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void getJobsByJobState() throws Exception {
        final JobStatusResult returnedJob = JobStatusResult.builder()
                                                           .jobId(UUID.randomUUID())
                                                           .jobState(JobState.COMPLETED)
                                                           .startedOn(ZonedDateTime.now(ZoneId.of("UTC")))
                                                           .jobCompleted(ZonedDateTime.now(ZoneId.of("UTC")))
                                                           .build();

        final String returnJobAsString = objectMapper.writeValueAsString(returnedJob);

        when(service.getJobsByJobState(any())).thenReturn(List.of(returnedJob));

        this.mockMvc.perform(get("/irs/jobs"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(returnJobAsString)))
                    .andExpect(content().string(containsString(returnedJob.getJobId().toString())))
                    .andExpect(content().string(containsString(returnedJob.getJobState().toString())))
                    .andExpect(content().string(containsString(returnedJob.getStartedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")))))
                    .andExpect(content().string(containsString(returnedJob.getJobCompleted().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")))));
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void cancelJobById() throws Exception {
        final Job canceledJob = Job.builder().jobId(jobId).jobState(JobState.CANCELED).build();

        when(this.service.cancelJobById(jobId)).thenReturn(canceledJob);

        this.mockMvc.perform(put("/irs/jobs/" + jobId)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void cancelJobById_throwEntityNotFoundException() throws Exception {
        given(this.service.cancelJobById(jobId)).willThrow(
                new EntityNotFoundException("No job exists with id " + jobId));

        this.mockMvc.perform(put("/irs/jobs/" + jobId))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntityNotFoundException));
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void getJobWithMalformedIdShouldReturnBadRequest() throws Exception {
        final String jobIdMalformed = UUID.randomUUID() + "MALFORMED";

        this.mockMvc.perform(get("/irs/jobs/" + jobIdMalformed)).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void shouldReturnBadRequestWhenRegisterJobWithMalformedAspectJson() throws Exception {
        final String requestBody = "{ \"aspects\": [ \"MALFORMED\" ], \"globalAssetId\": \"urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6\" }";

        this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                    .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "view_irs")
    void shouldReturnBadRequestWhenCancelingAlreadyCompletedJob() throws Exception {
        given(this.service.cancelJobById(jobId)).willThrow(new IllegalStateException(
                format("Cannot transition from state %s to %s", JobState.COMPLETED, JobState.CANCELED)));

        this.mockMvc.perform(put("/irs/jobs/" + jobId))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalStateException));
    }

}