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

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.IrsApplication;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.JobStatusResult;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.connector.job.IrsTimer;
import org.eclipse.tractusx.irs.dtos.ErrorResponse;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Application REST controller.
 */
@Slf4j
@RestController
@RequestMapping(IrsApplication.API_PREFIX)
@RequiredArgsConstructor
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals",
                    "PMD.ExcessiveImports"
})
public class IrsController {

    private final IrsItemGraphQueryService itemJobService;

    @Operation(operationId = "registerJobForGlobalAssetId",
               summary = "Register an IRS job to retrieve an item graph for given {globalAssetId}.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Item Relationship Service" },
               description = "Register an IRS job to retrieve an item graph for given {globalAssetId}.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Returns jobId of registered job.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = JobHandle.class),
                                                              examples = { @ExampleObject(name = "complete",
                                                                                          ref = "#/components/examples/job-handle")
                                                              })
                                         }),
                            @ApiResponse(responseCode = "400", description = "Job registration failed.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-400"))
                                         }),
    })
    @IrsTimer("registerjob")
    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    public JobHandle registerJobForGlobalAssetId(final @Valid @RequestBody RegisterJob request) {
        return itemJobService.registerItemJob(request);
    }

    @Operation(description = "Return job with optional item graph result for requested jobId.",
               operationId = "getJobForJobId",
               summary = "Return job with optional item graph result for requested jobId.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
                                         description = "Return job with item graph for the requested jobId.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = Jobs.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/complete-job-result"))
                                         }),
                            @ApiResponse(responseCode = "206",
                                         description = "Return job with current processed item graph for the requested jobId.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = Jobs.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/partial-job-result"))
                                         }),
                            @ApiResponse(responseCode = "400", description = "Return job failed.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-400"))
                                         }),
                            @ApiResponse(responseCode = "404", description = "Job with the requested jobId not found.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-404"))
                                         }),
    })
    @IrsTimer("getjob")
    @GetMapping("/jobs/{jobId}")
    public Jobs getJobById(
            @Parameter(description = "JobId of the job.", schema = @Schema(implementation = UUID.class), name = "jobId",
                       example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @Size(min = IrsAppConstants.JOB_ID_SIZE,
                                                                               max = IrsAppConstants.JOB_ID_SIZE) @Valid @PathVariable final UUID jobId,
            @Parameter(
                    description = "<true> Return job with current processed item graph. <false> Return job with item graph if job is in state <COMPLETED>, otherwise job.") @Schema(
                    implementation = Boolean.class, defaultValue = "true") @RequestParam(value = "returnUncompletedJob",
                                                                                         required = false) final boolean returnUncompletedJob) {
        return itemJobService.getJobForJobId(jobId, returnUncompletedJob);
    }

    @Operation(description = "Cancel job for requested jobId.", operationId = "cancelJobByJobId",
               summary = "Cancel job for requested jobId.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Job with requested jobId canceled."),
                            @ApiResponse(responseCode = "400", description = "Cancel job failed.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-400"))
                                         }),
                            @ApiResponse(responseCode = "404", description = "Job for requested jobId not found.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-404"))
                                         }),
    })
    @IrsTimer("canceljob")
    @PutMapping("/jobs/{jobId}")
    public Job cancelJobByJobId(
            @Parameter(description = "JobId of the job.", schema = @Schema(implementation = UUID.class), name = "jobId",
                       example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @Size(min = IrsAppConstants.JOB_ID_SIZE,
                                                                               max = IrsAppConstants.JOB_ID_SIZE) @Valid @PathVariable final UUID jobId) {

        return this.itemJobService.cancelJobById(jobId);
    }

    @Operation(description = "Returns jobIds for requested job states.", operationId = "getJobIdsByJobStates",
               summary = "Returns jobIds for requested job states.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
                                         description = "List of job ids and status for requested job states.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(
                                                 schema = @Schema(implementation = JobStatusResult.class), maxItems = Integer.MAX_VALUE),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/complete-job-list-processing-state"))
                                         }),
                            @ApiResponse(responseCode = "400", description = "Returns jobIds for requested job states failed.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-400"))
                                         }),
                            @ApiResponse(responseCode = "404", description = "Job with the requested state not found.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-404"))
                                         }),
    })
    @IrsTimer("getjobbystate")
    @GetMapping("/jobs")
    public List<JobStatusResult> getJobsByJobState(
            @Valid @ParameterObject @Parameter(description = "Requested job states.", in = QUERY,
                                               explode = Explode.FALSE, array = @ArraySchema(
                    schema = @Schema(implementation = JobState.class), maxItems = Integer.MAX_VALUE)) @RequestParam(value = "jobStates",
                                                                                      required = false,
                                                                                      defaultValue = "") final List<JobState> jobStates) {
        return itemJobService.getJobsByJobState(jobStates);
    }

}
