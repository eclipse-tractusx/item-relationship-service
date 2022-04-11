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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.IrsApplication;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.JobHandleCollection;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.dtos.ErrorResponse;
import net.catenax.irs.requests.IrsPartsTreeRequest;
import net.catenax.irs.services.IrsPartsTreeQueryService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Application REST controller.
 */
@Tag(name = "Item Relationship Service")
@Slf4j
@RestController
@RequestMapping(IrsApplication.API_PREFIX)
@RequiredArgsConstructor
@ExcludeFromCodeCoverageGeneratedReport
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals",
                    "PMD.ExcessiveImports"})
public class IrsController {

    private final IrsPartsTreeQueryService itemJobService;

    @Operation(operationId = "getBomLifecycleByGlobalAssetId", summary = "Registers and starts a AAS crawler job for given {globalAssetId}",
            tags = { "Item Relationship Service" }, description = "Registers and starts a AAS crawler job for given {globalAssetId}")
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
            description = "job id response for successful job registration",
            content = { @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = JobHandle.class),
                  examples = {@ExampleObject(name="complete", ref = "#/components/examples/job-handle")})
            }),
        @ApiResponse(responseCode = "400",
                description = "Processing of job failed",
                content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class),
                      examples = @ExampleObject(name="complete", ref = "#/components/examples/error-response"))
                }),
    })
    @PostMapping("/items/{globalAssetId}")
    public JobHandle getBomLifecycleByGlobalAssetId(final @Valid @ParameterObject IrsPartsTreeRequest request) {
        return itemJobService.registerItemJob(request);
    }

    @Operation(operationId = "getBOMForJobId", summary = "Get a BOM partial or complete for a given jobId.",
            tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
            description = "complete job result with bom tree, livecycle tree representation with the starting point of the given jobId",
            content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Jobs.class),
                  examples = @ExampleObject(name="complete", ref = "#/components/examples/complete-job-result"))
            }),
            @ApiResponse(responseCode = "201",
                description = "job details for given jobId - job is in running state and has not completed.",
                content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = Jobs.class),
                      examples = @ExampleObject(name="complete", ref = "#/components/examples/job-result-without-uncompleted-result-tree"))
            }),
            @ApiResponse(responseCode = "206",
                  description = "uncompleted livecycle tree representation with the starting point of the given jobId",
                  content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = Jobs.class),
                        examples = @ExampleObject(name="complete", ref = "#/components/examples/partial-job-result"))
            }),
            @ApiResponse(responseCode = "404", description = "processing of job was canceled",
                  content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = Jobs.class),
                        examples = @ExampleObject(name="complete", ref = "#/components/examples/canceled-job-result"))
            }),
            @ApiResponse(responseCode = "417", description = "Processing of job failed",
                  content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = Jobs.class),
                        examples = @ExampleObject(name="complete", ref = "#/components/examples/failed-job-result"))
            }),
    })
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Jobs> getBOMForJobId(
            final @Valid @Parameter(description = "Id of the job in processing.", schema = @Schema(implementation = UUID.class), name = "jobId", example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @PathVariable @Size(min = IrsApiConstants.JOB_ID_SIZE,
                    max = IrsApiConstants.JOB_ID_SIZE) UUID jobId, final @Parameter(description = "If true, the endpoint returns the uncompleted results of the bom tree.", schema = @Schema(implementation = Boolean.class, defaultValue = "true", required = false)) boolean returnUncompletedResultTree)  {
        return new ResponseEntity<>(Jobs.builder().build(), HttpStatus.OK);
    }

    @Operation(operationId = "cancelJobForJobId", summary = "Cancel job execution for a given jobId.",
            tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Job with {jobId} was canceled"),
                            @ApiResponse(responseCode = "400",
                                    description = "Bad request. JobId must be a string in UUID format."),
                            @ApiResponse(responseCode = "401",
                                    description = "Authorization information is missing or invalid."),
                            @ApiResponse(responseCode = "404",
                                    description = "A job with the specified jobId was not found."),
                            @ApiResponse(responseCode = "500", description = "Unexpected error.")
    })
    @PutMapping("/jobs/{jobId}/cancel")
    public ResponseEntity<?> cancelJobForJobId(final @Valid @Parameter(description = "Id of the job in processing.", schema = @Schema(implementation = UUID.class), name = "jobId", example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @PathVariable UUID jobId) {

        return new ResponseEntity<>(Jobs.builder().build(), HttpStatus.OK);
    }

    @Operation(operationId = "getJobsByProcessingState",
          summary = "List of jobs (globalAssetIds) for a certain processing state",
          tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "list of jobs with given processingState",
          content = { @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = JobHandleCollection.class),
                examples = @ExampleObject(name="complete", ref = "#/components/examples/complete-job-list-processing-state"))
          }),
          @ApiResponse(responseCode = "400", description = "Bad Request",
                content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ErrorResponse.class),
                      examples = @ExampleObject(name="complete", ref = "#/components/examples/error-response"))
          }),
          @ApiResponse(responseCode = "404", description = "No process found with this state",
                content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ErrorResponse.class),
                      examples = @ExampleObject(name="complete", ref = "#/components/examples/error-response"))
          }),

    })
    @GetMapping("/jobs/{processingState}")
    public ResponseEntity<Jobs> getJobsByProcessingState(final @Valid @Parameter(description = "List of jobs (globalAssetIds) for a certain processing state", schema = @Schema(implementation = JobState.class)) @PathVariable JobState processingState) {
        return new ResponseEntity<>(Jobs.builder().build(), HttpStatus.OK);
    }
}
