/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.controllers;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.tractusx.irs.util.TestMother.registerBatchOrder;
import static org.eclipse.tractusx.irs.util.TestMother.registerJob;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithDepthAndAspect;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithUrl;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithoutDepthAndAspect;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
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
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.tractusx.irs.ControllerTest;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.JobStatusResult;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.PageResult;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.configuration.security.SecurityConfiguration;
import org.eclipse.tractusx.irs.semanticshub.AspectModel;
import org.eclipse.tractusx.irs.semanticshub.AspectModels;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.eclipse.tractusx.irs.services.SemanticHubService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(IrsController.class)
@Import(SecurityConfiguration.class)
class IrsControllerTest extends ControllerTest {

    private final UUID jobId = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IrsItemGraphQueryService service;
    @MockBean
    private SemanticHubService semanticHubService;

    private static Stream<RegisterJob> corruptedJobs() {
        return Stream.of(registerJobWithDepthAndAspect(110, null),
                registerJob("invalidGlobalAssetId", 0, null, false, false, Direction.DOWNWARD),
                registerJob("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5\n\rdf6", 0, null, false, false,
                        Direction.DOWNWARD));
    }

    @Test
    void initiateJobForGlobalAssetId() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final UUID returnedJob = UUID.randomUUID();
        when(service.registerItemJob(any())).thenReturn(JobHandle.builder().id(returnedJob).build());


        this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(
                                                      registerJobWithoutDepthAndAspect())))
                    .andExpect(status().isCreated())
                    .andExpect(content().string(containsString(returnedJob.toString())));
    }

    @Test
    void shouldReturnUnauthorizedStatusWhenAuthenticationIsMissing() {
        when(authenticationService.getAuthentication(any(HttpServletRequest.class)))
                .thenThrow(new BadCredentialsException("Wrong ApiKey"));

        assertThatThrownBy(() -> this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON)
                                                                       .content(new ObjectMapper().writeValueAsString(
                                                                               registerJobWithoutDepthAndAspect())))
        ).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldReturnForbiddenStatusWhenRequiredAuthorityIsMissing() throws Exception {
        authenticateWith("view_irs_wrong_authority");

        this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(
                                                      registerJobWithoutDepthAndAspect())))
                    .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("corruptedJobs")
    void shouldReturnBadRequestWhenRegisterJobBodyNotValid(final RegisterJob registerJob) throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(registerJob)))
                    .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRegisterJobHasWrongCallbackUrl() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(
                                                      registerJobWithUrl("hhh://example.com"))))
                    .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAcceptCorrectCallbackUrl() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final UUID returnedJob = UUID.randomUUID();
        when(service.registerItemJob(any())).thenReturn(JobHandle.builder().id(returnedJob).build());

        this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(
                                                      registerJobWithUrl("https://example.com"))))
                    .andExpect(status().isCreated());
    }

    @Test
    void getJobsByState() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final JobStatusResult returnedJob = JobStatusResult.builder()
                                                           .id(UUID.randomUUID())
                                                           .state(JobState.COMPLETED)
                                                           .startedOn(ZonedDateTime.now(ZoneId.of("UTC")))
                                                           .completedOn(ZonedDateTime.now(ZoneId.of("UTC")))
                                                           .build();

        final String returnJobAsString = objectMapper.writeValueAsString(returnedJob);

        when(service.getJobsByState(any(), any())).thenReturn(
                new PageResult(new PagedListHolder<>(List.of(returnedJob))));

        this.mockMvc.perform(get("/irs/jobs"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(returnJobAsString)))
                    .andExpect(content().string(containsString(returnedJob.getId().toString())))
                    .andExpect(content().string(containsString(returnedJob.getState().toString())))
                    .andExpect(content().string(containsString(returnedJob.getStartedOn()
                                                                          .format(DateTimeFormatter.ofPattern(
                                                                                  "yyyy-MM-dd'T'HH:mm:ss.SSS")))))
                    .andExpect(content().string(containsString(returnedJob.getCompletedOn()
                                                                          .format(DateTimeFormatter.ofPattern(
                                                                                  "yyyy-MM-dd'T'HH:mm:ss.SSS")))));
    }

    @Test
    void cancelJobById() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final Job canceledJob = Job.builder().id(jobId).state(JobState.CANCELED).build();
        when(this.service.cancelJobById(jobId)).thenReturn(canceledJob);

        this.mockMvc.perform(put("/irs/jobs/" + jobId)).andExpect(status().isOk());
    }

    @Test
    void cancelJobById_throwEntityNotFoundException() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        given(this.service.cancelJobById(jobId)).willThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No job exists with id " + jobId));

        this.mockMvc.perform(put("/irs/jobs/" + jobId))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
    }

    @Test
    void getJobWithMalformedIdShouldReturnBadRequest() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final String jobIdMalformed = UUID.randomUUID() + "MALFORMED";

        this.mockMvc.perform(get("/irs/jobs/" + jobIdMalformed)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRegisterJobWithMalformedAspectJson() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        when(service.registerItemJob(any())).thenThrow(IllegalArgumentException.class);
        final String requestBody = "{ \"aspects\": [ \"MALFORMED\" ], \"globalAssetId\": \"urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6\" }";

        this.mockMvc.perform(post("/irs/jobs").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                    .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCancelingAlreadyCompletedJob() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        given(this.service.cancelJobById(jobId)).willThrow(new IllegalStateException(
                format("Cannot transition from state %s to %s", JobState.COMPLETED, JobState.CANCELED)));

        this.mockMvc.perform(put("/irs/jobs/" + jobId))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalStateException));
    }

    @Test
    void shouldReturnAspectModels() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final AspectModel assemblyPartRelationship = AspectModel.builder()
                                                                .name("AssemblyPartRelationship")
                                                                .urn("urn:bamm:io.catenax.assembly_part_relationship:1.1.1#AssemblyPartRelationship")
                                                                .version("1.1.1")
                                                                .status("RELEASED")
                                                                .type("BAMM")
                                                                .build();

        final AspectModels aspectModels = AspectModels.builder()
                                                      .lastUpdated("2023-02-13T08:18:11.990659500Z")
                                                      .models(List.of(assemblyPartRelationship))
                                                      .build();

        given(this.semanticHubService.getAllAspectModels()).willReturn(aspectModels);
        final String aspectModelResponseAsString = objectMapper.writeValueAsString(aspectModels);

        this.mockMvc.perform(get("/irs/aspectmodels"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(aspectModelResponseAsString)));
    }

    @Test
    void shouldReturnForbiddenStatusForAspectModelsWhenRequiredAuthorityIsMissing() throws Exception {
        authenticateWith("view_irs_wrong_authority");

        this.mockMvc.perform(get("/irs/aspectmodels").contentType(MediaType.APPLICATION_JSON)
                                                     .content(new ObjectMapper().writeValueAsString(
                                                             registerJobWithoutDepthAndAspect())))
                    .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnPartialWhenJobCompleted() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final Jobs runningJob = Jobs.builder().job(Job.builder().id(jobId).state(JobState.RUNNING).build()).build();
        final boolean shouldIncludePartial = Boolean.TRUE;
        final boolean shouldNotIncludePartial = Boolean.FALSE;

        when(this.service.getJobForJobId(eq(jobId), anyBoolean())).thenReturn(runningJob);

        this.mockMvc.perform(get("/irs/jobs/" + jobId).queryParam("returnUncompletedJob", String.valueOf(shouldIncludePartial))).andExpect(status().isPartialContent());
        this.mockMvc.perform(get("/irs/jobs/" + jobId).queryParam("returnUncompletedJob", String.valueOf(shouldNotIncludePartial))).andExpect(status().isPartialContent());
    }

    @Test
    void shouldReturnOkWhenJobCompleted() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        final Jobs completedJob = Jobs.builder().job(Job.builder().id(jobId).state(JobState.COMPLETED).build()).build();
        final boolean shouldIncludePartial = Boolean.TRUE;
        final boolean shouldNotIncludePartial = Boolean.FALSE;

        when(this.service.getJobForJobId(eq(jobId), anyBoolean())).thenReturn(completedJob);

        this.mockMvc.perform(get("/irs/jobs/" + jobId).queryParam("returnUncompletedJob", String.valueOf(shouldIncludePartial))).andExpect(status().isOk());
        this.mockMvc.perform(get("/irs/jobs/" + jobId).queryParam("returnUncompletedJob", String.valueOf(shouldNotIncludePartial))).andExpect(status().isOk());
    }

}