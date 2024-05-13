/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.eclipse.tractusx.irs.util.TestMother.registerJob;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithDepthAndAspect;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithUrl;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithoutDepthAndAspect;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.tractusx.irs.ControllerTest;
import org.eclipse.tractusx.irs.TestConfig;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.JobStatusResult;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.PageResult;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.semanticshub.AspectModel;
import org.eclipse.tractusx.irs.semanticshub.AspectModels;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.eclipse.tractusx.irs.services.SemanticHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = { "digitalTwinRegistry.type=central" })
@ActiveProfiles(profiles = { "test",
                             "local"
})
@Import(TestConfig.class)
@ExtendWith({ MockitoExtension.class,
              SpringExtension.class
})
class IrsControllerTest extends ControllerTest {

    private final UUID jobId = UUID.randomUUID();

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IrsItemGraphQueryService service;
    @MockBean
    private SemanticHubService semanticHubService;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    private static Stream<RegisterJob> corruptedJobs() {
        return Stream.of(registerJobWithDepthAndAspect(110, null),
                registerJob("invalidGlobalAssetId", 0, null, false, false, Direction.DOWNWARD),
                registerJob("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5\n\rdf6", 0, null, false, false,
                        Direction.DOWNWARD));
    }

    @Test
    void initiateJobForGlobalAssetId() {
        authenticateWith(IrsRoles.VIEW_IRS);

        final UUID returnedJob = UUID.randomUUID();
        Mockito.when(service.registerItemJob(any())).thenReturn(JobHandle.builder().id(returnedJob).build());

        given().port(port)
               .contentType(ContentType.JSON)
               .body(registerJobWithoutDepthAndAspect())
               .post("/irs/jobs")
               .then()
               .statusCode(CREATED.value())
               .body("id", is(returnedJob.toString()));
    }

    @Test
    void shouldReturnUnauthorizedStatusWhenAuthenticationIsMissing() {
        Mockito.when(authenticationService.getAuthentication(any(HttpServletRequest.class)))
               .thenThrow(new BadCredentialsException("Wrong ApiKey"));

        given().port(port)
               .contentType(ContentType.JSON)
               .body(registerJobWithoutDepthAndAspect())
               .post("/irs/jobs")
               .then()
               .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldReturnForbiddenStatusWhenRequiredAuthorityIsMissing() {
        authenticateWith("view_irs_wrong_authority");

        given().port(port)
               .contentType(ContentType.JSON)
               .body(registerJobWithoutDepthAndAspect())
               .post("/irs/jobs")
               .then()
               .statusCode(FORBIDDEN.value());
    }

    @ParameterizedTest
    @MethodSource("corruptedJobs")
    void shouldReturnBadRequestWhenRegisterJobBodyNotValid(final RegisterJob registerJob) {
        authenticateWith(IrsRoles.VIEW_IRS);

        given().port(port)
               .contentType(ContentType.JSON)
               .body(registerJob)
               .post("/irs/jobs")
               .then()
               .statusCode(BAD_REQUEST.value());
    }

    @ParameterizedTest
    @ValueSource(strings = { "upwards",
                             "downwards"
    })
    void shouldReturnBadRequestWhenRegisterJobWithInvalidDirection(String invalidDirection) {
        authenticateWith(IrsRoles.VIEW_IRS);

        given().port(port)
               .contentType(ContentType.JSON)
               .body("""
                       {
                          "key": {
                              "globalAssetId": "urn:uuid:c6d2d642-a055-4ddf-87e3-1a3b02c689e3",
                              "bpn": "BPNL00000000BJTL"
                          },
                          "direction": "<DIRECTION>"
                       }
                       """.replace("<DIRECTION>", invalidDirection))
               .post("/irs/jobs")
               .then()
               .statusCode(BAD_REQUEST.value())
               .body("error", containsString("Unsupported direction"))
               .body("error", containsString("Must be one of: upward, downward"))
               // error message should not contain unvalidated user input for security reasons
               .body("error", not(containsString(invalidDirection)));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterJobHasWrongCallbackUrl() {
        authenticateWith(IrsRoles.VIEW_IRS);

        given().port(port)
               .contentType(ContentType.JSON)
               .body(registerJobWithUrl("hhh://example.com"))
               .post("/irs/jobs")
               .then()
               .statusCode(BAD_REQUEST.value());
    }

    @Test
    void shouldAcceptCorrectCallbackUrl() {
        authenticateWith(IrsRoles.VIEW_IRS);

        final UUID returnedJob = UUID.randomUUID();
        Mockito.when(service.registerItemJob(any())).thenReturn(JobHandle.builder().id(returnedJob).build());

        given().port(port)
               .contentType(ContentType.JSON)
               .body(registerJobWithUrl("https://example.com"))
               .post("/irs/jobs")
               .then()
               .statusCode(CREATED.value());
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

        Mockito.when(service.getJobsByState(any(), any()))
               .thenReturn(new PageResult(new PagedListHolder<>(List.of(returnedJob))));

        given().port(port)
               .get("/irs/jobs")
               .then()
               .statusCode(OK.value())
               .body(containsString(returnJobAsString))
               .body(containsString(returnedJob.getId().toString()))
               .body(containsString(returnedJob.getState().toString()))
               .body(containsString(
                       returnedJob.getStartedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"))))
               .body(containsString(
                       returnedJob.getCompletedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"))));
    }

    @Test
    void cancelJobById() {
        authenticateWith(IrsRoles.VIEW_IRS);

        final Job canceledJob = Job.builder().id(jobId).state(JobState.CANCELED).build();
        Mockito.when(this.service.cancelJobById(jobId)).thenReturn(canceledJob);

        given().port(port).put("/irs/jobs/" + jobId).then().statusCode(OK.value());
    }

    @Test
    void cancelJobById_throwEntityNotFoundException() {
        authenticateWith(IrsRoles.VIEW_IRS);

        BDDMockito.given(this.service.cancelJobById(jobId))
                  .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No job exists with id " + jobId));

        given().port(port).put("/irs/jobs/" + jobId).then().statusCode(NOT_FOUND.value());
    }

    @Test
    void getJobWithMalformedIdShouldReturnBadRequest() {
        authenticateWith(IrsRoles.VIEW_IRS);

        final String jobIdMalformed = UUID.randomUUID() + "MALFORMED";

        given().port(port).get("/irs/jobs/" + jobIdMalformed).then().statusCode(BAD_REQUEST.value());
    }

    @Test
    void shouldReturnBadRequestWhenRegisterJobWithMalformedAspectJson() {
        authenticateWith(IrsRoles.VIEW_IRS);

        Mockito.when(service.registerItemJob(any())).thenThrow(IllegalArgumentException.class);
        final String requestBody = "{ \"aspects\": [ \"MALFORMED\" ], \"globalAssetId\": \"urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6\" }";

        given().port(port)
               .contentType(ContentType.JSON)
               .body(requestBody)
               .post("/irs/jobs")
               .then()
               .statusCode(BAD_REQUEST.value());
    }

    @Test
    void shouldReturnBadRequestWhenCancelingAlreadyCompletedJob() {
        authenticateWith(IrsRoles.VIEW_IRS);

        BDDMockito.given(this.service.cancelJobById(jobId))
                  .willThrow(new IllegalStateException(
                          format("Cannot transition from state %s to %s", JobState.COMPLETED, JobState.CANCELED)));

        given().port(port).put("/irs/jobs/" + jobId).then().statusCode(BAD_REQUEST.value());
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

        BDDMockito.given(this.semanticHubService.getAllAspectModels()).willReturn(aspectModels);

        final AspectModels response = given().port(port)
                                             .get("/irs/aspectmodels")
                                             .then()
                                             .statusCode(OK.value())
                                             .and()
                                             .extract()
                                             .response()
                                             .as(AspectModels.class);

        assertEquals(aspectModels, response);
    }

    @Test
    void shouldReturnForbiddenStatusForAspectModelsWhenRequiredAuthorityIsMissing() {
        authenticateWith("view_irs_wrong_authority");

        given().port(port).get("/irs/aspectmodels").then().statusCode(FORBIDDEN.value());
    }

    @Test
    void shouldReturnPartialWhenJobCompleted() {
        authenticateWith(IrsRoles.VIEW_IRS);

        final Jobs runningJob = Jobs.builder().job(Job.builder().id(jobId).state(JobState.RUNNING).build()).build();

        Mockito.when(this.service.getJobForJobId(eq(jobId), anyBoolean())).thenReturn(runningJob);

        given().port(port)
               .queryParam("returnUncompletedJob", true)
               .get("/irs/jobs/" + jobId)
               .then()
               .statusCode(PARTIAL_CONTENT.value());
        given().port(port)
               .queryParam("returnUncompletedJob", false)
               .get("/irs/jobs/" + jobId)
               .then()
               .statusCode(PARTIAL_CONTENT.value());
    }

    @Test
    void shouldReturnOkWhenJobCompleted() {
        authenticateWith(IrsRoles.VIEW_IRS);

        final Jobs completedJob = Jobs.builder().job(Job.builder().id(jobId).state(JobState.COMPLETED).build()).build();

        Mockito.when(this.service.getJobForJobId(eq(jobId), anyBoolean())).thenReturn(completedJob);

        given().port(port)
               .queryParam("returnUncompletedJob", true)
               .get("/irs/jobs/" + jobId)
               .then()
               .statusCode(OK.value());
        given().port(port)
               .queryParam("returnUncompletedJob", false)
               .get("/irs/jobs/" + jobId)
               .then()
               .statusCode(OK.value());
    }

}