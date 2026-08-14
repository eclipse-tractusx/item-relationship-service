/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobRequest;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStartResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.eclipse.tractusx.irs.recursive.service.RecursiveJobService;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for recursive IRS jobs.
 *
 * <p>Provides the public API for starting and querying recursive
 * supply chain traversals, as well as managing chain opening grants.</p>
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/irs/recursive")
@RequiredArgsConstructor
@Tag(name = "Recursive IRS", description = "Recursive supply chain traversal with grant-based access control")
@PreAuthorize("hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
public class RecursiveJobController {

    private final RecursiveJobService jobService;

    @Operation(summary = "Start a new recursive job",
               description = "Validates the request against the local grant store, resolves the BOM, "
                       + "and determines allowed child partners.")
    @ApiResponse(responseCode = "201", description = "Job created")
    @ApiResponse(responseCode = "400",
                 description = "Unknown use case, unsupported lifecycle, or invalid request",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Grant validation failed",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Request contains an unsupported aspect",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "406", description = "Requested response media type is not supported",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "415", description = "Request media type is not supported",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected recursive IRS error",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Recursive persistence unavailable",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @PostMapping("/jobs")
    public ResponseEntity<RecursiveJobStartResponse> startJob(
            @Valid @RequestBody final RecursiveJobRequest request) {
        log.info("POST /irs/recursive/jobs - openingId={}, useCase={}",
                RecursiveLogValue.of(request.getOpeningId()), RecursiveLogValue.of(request.getUseCase().name()));
        final UUID jobId = jobService.startJob(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RecursiveJobStartResponse.builder().jobId(jobId).build());
    }

    @Operation(summary = "Get recursive job status",
               description = "Returns the job envelope and, once terminal, the aggregated result. "
                       + "Note: job.state reflects the technical run (COMPLETED = all processing "
                       + "finished), result.resultStatus the business outcome (COMPLETE/PARTIAL/"
                       + "FAILED) - a COMPLETED job can carry a FAILED result.")
    @ApiResponse(responseCode = "200", description = "Job found")
    @ApiResponse(responseCode = "400", description = "Invalid job id",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Job not found",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "406", description = "Requested response media type is not supported",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected recursive IRS error",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Recursive persistence unavailable",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<RecursiveJobStatusResponse> getJobStatus(
            @PathVariable final UUID jobId) {
        log.info("GET /irs/recursive/jobs/{}", RecursiveLogValue.of(jobId.toString()));
        return ResponseEntity.ok(jobService.getJobStatus(jobId));
    }

    @Operation(summary = "List all recursive jobs")
    @ApiResponse(responseCode = "200", description = "Jobs returned")
    @ApiResponse(responseCode = "406", description = "Requested response media type is not supported",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected recursive IRS error",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Recursive persistence unavailable",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @GetMapping("/jobs")
    public ResponseEntity<List<RecursiveJobStatusResponse>> listJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }
}
