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

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;
import static org.eclipse.tractusx.irs.common.ApiConstants.FORBIDDEN_DESC;
import static org.eclipse.tractusx.irs.common.ApiConstants.UNAUTHORIZED_DESC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.UUID;

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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.IrsApplication;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.PageResult;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.connector.job.IrsTimer;
import org.eclipse.tractusx.irs.dtos.ErrorResponse;
import org.eclipse.tractusx.irs.semanticshub.AspectModels;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.eclipse.tractusx.irs.services.SemanticHubService;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
                    "PMD.ExcessiveImports",
                    "PMD.ShortVariable"
})
public class IrsController {

    private final IrsItemGraphQueryService itemJobService;
    private final SemanticHubService semanticHubService;

    @Operation(operationId = "registerJobForGlobalAssetId",
               summary = "Register an IRS job to retrieve an item graph for given {globalAssetId}.",
               security = @SecurityRequirement(name = "api_key"), tags = { "Item Relationship Service" },
               description = "Register an IRS job to retrieve an item graph for given {globalAssetId}.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Returns id of registered job.",
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
                            @ApiResponse(responseCode = "401", description = UNAUTHORIZED_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-401"))
                                         }),
                            @ApiResponse(responseCode = "403", description = FORBIDDEN_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-403"))
                                         }),
    })
    @IrsTimer("registerjob")
    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    public JobHandle registerJobForGlobalAssetId(final @Valid @RequestBody RegisterJob request) {
        return itemJobService.registerItemJob(request);
    }

    @Operation(description = "Return job with optional item graph result for requested id.",
               operationId = "getJobForJobId", summary = "Return job with optional item graph result for requested id.",
               security = @SecurityRequirement(name = "api_key"), tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
                                         description = "Return job with item graph for the requested id.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = Jobs.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/complete-job-result"))
                                         }),
                            @ApiResponse(responseCode = "206",
                                         description = "Return job with current processed item graph for the requested id.",
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
                            @ApiResponse(responseCode = "401", description = UNAUTHORIZED_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-401"))
                                         }),
                            @ApiResponse(responseCode = "403", description = FORBIDDEN_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-403"))
                                         }),
                            @ApiResponse(responseCode = "404", description = "Job with the requested jobId not found.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-404"))
                                         }),
    })
    @IrsTimer("getjob")
    @GetMapping("/jobs/{id}")
    @PreAuthorize("hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    public ResponseEntity<Jobs> getJobById(
            @Parameter(description = "Id of the job.", schema = @Schema(implementation = UUID.class), name = "id",
                       example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @Valid @PathVariable final UUID id, @Parameter(
            description = "\\<true\\> Return job with current processed item graph. \\<false\\> Return job with item graph if job is in state COMPLETED, otherwise job.") @Schema(
            implementation = Boolean.class, defaultValue = "true") @RequestParam(value = "returnUncompletedJob",
                                                                                 required = false) final boolean returnUncompletedJob) {
        final Jobs job = itemJobService.getJobForJobId(id, returnUncompletedJob);
        if (job.getJob().getState().equals(JobState.RUNNING)) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(job);
        }
        return ResponseEntity.ok(job);
    }

    @Operation(description = "Cancel job for requested jobId.", operationId = "cancelJobByJobId",
               summary = "Cancel job for requested jobId.", security = @SecurityRequirement(name = "api_key"),
               tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Job with requested jobId canceled.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = Job.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/canceled-job-response"))
                                         }),
                            @ApiResponse(responseCode = "400", description = "Cancel job failed.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-400"))
                                         }),
                            @ApiResponse(responseCode = "401", description = UNAUTHORIZED_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-401"))
                                         }),
                            @ApiResponse(responseCode = "403", description = FORBIDDEN_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-403"))
                                         }),
                            @ApiResponse(responseCode = "404", description = "Job for requested jobId not found.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-404"))
                                         }),
    })
    @IrsTimer("canceljob")
    @PutMapping("/jobs/{id}")
    @PreAuthorize("hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    public Job cancelJobByJobId(
            @Parameter(description = "Id of the job.", schema = @Schema(implementation = UUID.class), name = "id",
                       example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @Valid @PathVariable final UUID id) {

        return this.itemJobService.cancelJobById(id);
    }

    @Operation(description = "Returns paginated jobs with state and execution times.",
               operationId = "getJobsByJobStates", summary = "Returns paginated jobs with state and execution times.",
               security = @SecurityRequirement(name = "api_key"), tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
                                         description = "Paginated list of jobs with state and execution times for requested job states.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = PageResult.class))
                                         }),
                            @ApiResponse(responseCode = "400",
                                         description = "Return jobs for requested job states failed.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-400"))
                                         }),
                            @ApiResponse(responseCode = "401", description = UNAUTHORIZED_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-401"))
                                         }),
                            @ApiResponse(responseCode = "403", description = FORBIDDEN_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-403"))
                                         }),
    })
    @IrsTimer("getjobbystate")
    @GetMapping("/jobs")
    @PageableAsQueryParam
    @PreAuthorize("hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    public PageResult getJobsByState(
            @Valid @ParameterObject @Parameter(description = "Requested job states.", in = QUERY,
                                               explode = Explode.FALSE,
                                               array = @ArraySchema(schema = @Schema(implementation = JobState.class),
                                                                    maxItems = Integer.MAX_VALUE)) @RequestParam(
                    value = "states", required = false, defaultValue = "") final List<JobState> states,
            @Parameter(hidden = true) @ParameterObject final Pageable pageable) {
        return itemJobService.getJobsByState(states, pageable);
    }

    @Operation(operationId = "getAllAspectModels",
               summary = "Get all available aspect models from semantic hub or local models.",
               security = @SecurityRequirement(name = "api_key"), tags = { "Aspect Models" },
               description = "Get all available aspect models from semantic hub or local models.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Returns all available aspect models.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = AspectModels.class),
                                                              examples = { @ExampleObject(name = "complete",
                                                                                          ref = "#/components/examples/aspect-models-list")
                                                              })
                                         }),
                            @ApiResponse(responseCode = "401", description = UNAUTHORIZED_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-401"))
                                         }),
                            @ApiResponse(responseCode = "403", description = FORBIDDEN_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-403"))
                                         }),
    })
    @GetMapping("/aspectmodels")
    @PreAuthorize("hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    public AspectModels getAllAvailableAspectModels() throws SchemaNotFoundException {
        return semanticHubService.getAllAspectModels();
    }
}
