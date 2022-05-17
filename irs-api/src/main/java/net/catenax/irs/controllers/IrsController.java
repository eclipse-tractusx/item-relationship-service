//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.controllers;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.IrsApplication;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.dtos.ErrorResponse;
import net.catenax.irs.services.IrsItemGraphQueryService;
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
@ExcludeFromCodeCoverageGeneratedReport
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals",
                    "PMD.ExcessiveImports"
})
public class IrsController {

    private final IrsItemGraphQueryService itemJobService;

    @Operation(operationId = "initiateJobForGlobalAssetId",
               summary = "Registers and starts a item relationship crawler job for given {globalAssetId}.",
               tags = { "Item Relationship Service" },
               description = "Registers and starts a item relationship crawler job for given {globalAssetId}.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201",
                                         description = "Job id response for successful job registration.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = JobHandle.class),
                                                              examples = { @ExampleObject(name = "complete",
                                                                                          ref = "#/components/examples/job-handle")
                                                              })
                                         }),
                            @ApiResponse(responseCode = "400", description = "Processing of job failed.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/error-response"))
                                         }),
    })
    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    public JobHandle initiateJobForGlobalAssetId(final @Valid @RequestBody RegisterJob request) {
        return itemJobService.registerItemJob(request);
    }

    @Operation(operationId = "getJobForJobId", summary = "Get a BOM partial or complete for a given jobId.",
               tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
                                         description = "Completed job result with the root node, lifecycle tree representation with the starting point of the given jobId.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = Jobs.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/complete-job-result"))
                                         }),
                            @ApiResponse(responseCode = "206",
                                         description = "Uncompleted lifecycle tree representation with the starting point of the given jobId.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = Jobs.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/partial-job-result"))
                                         }),
                            @ApiResponse(responseCode = "404",
                                         description = "A job with the specified jobId was not found.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/error-response"))
                                         }),
    })
    @GetMapping("/jobs/{jobId}")
    public Jobs getJobById(
            @Parameter(description = "ID of the job.", schema = @Schema(implementation = UUID.class), name = "jobId",
                       example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @Size(min = IrsApiConstants.JOB_ID_SIZE,
                                                                               max = IrsApiConstants.JOB_ID_SIZE) @Valid @PathVariable final UUID jobId,
            @Parameter(
                    description = "If true, the endpoint returns the current state of the fetched bom tree.") @Schema(
                    implementation = Boolean.class, defaultValue = "true") @RequestParam(value = "returnUncompletedJob",
                                                                                         required = false) final boolean returnUncompletedJob) {
        return itemJobService.getJobForJobId(jobId, returnUncompletedJob);
    }

    @Operation(operationId = "cancelJobById", summary = "Cancel job execution for a given jobId.",
               tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Job with {jobId} was canceled."),
                            @ApiResponse(responseCode = "404",
                                         description = "A job with the specified jobId was not found.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/error-response"))
                                         }),
    })
    @PutMapping("/jobs/{jobId}")
    public Job cancelJobById(
            @Parameter(description = "ID of the job.", schema = @Schema(implementation = UUID.class), name = "jobId",
                       example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @Size(min = IrsApiConstants.JOB_ID_SIZE,
                                                                               max = IrsApiConstants.JOB_ID_SIZE) @Valid @PathVariable final UUID jobId) {

        return this.itemJobService.cancelJobById(jobId);
    }

    @Operation(operationId = "getJobsByJobState", summary = "List of jobs for a certain job states.",
               tags = { "Item Relationship Service" })
    @ApiResponses(
            value = { @ApiResponse(responseCode = "200", description = "List of job ids for requested job states.",
                                   content = { @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(
                                           schema = @Schema(implementation = UUID.class)),
                                                        examples = @ExampleObject(name = "complete",
                                                                                  ref = "#/components/examples/complete-job-list-processing-state"))
                                   }),
                      @ApiResponse(responseCode = "404", description = "No process found with this state.",
                                   content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                        schema = @Schema(implementation = ErrorResponse.class),
                                                        examples = @ExampleObject(name = "complete",
                                                                                  ref = "#/components/examples/error-response"))
                                   }),
            })
    @GetMapping("/jobs")
    public List<UUID> getJobsByJobState(
            @Valid @ParameterObject @Parameter(description = "Requested job states.", in = QUERY,
                                               explode = Explode.FALSE, array = @ArraySchema(
                    schema = @Schema(implementation = JobState.class))) @RequestParam(value = "jobStates",
                                                                                      required = false,
                                                                                      defaultValue = "") final List<JobState> jobStates) {
        return itemJobService.getJobsByJobState(jobStates);
    }
}
