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
package org.eclipse.tractusx.ess.controller;

import static org.eclipse.tractusx.irs.common.ApiConstants.FORBIDDEN_DESC;
import static org.eclipse.tractusx.irs.common.ApiConstants.UNAUTHORIZED_DESC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.ess.service.EssService;
import org.eclipse.tractusx.irs.common.auth.AuthorizationService;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.eclipse.tractusx.irs.dtos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Application REST controller.
 */
@Slf4j
@RestController
@RequestMapping("/ess")
@RequiredArgsConstructor
@Validated
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals",
                    "PMD.ShortVariable",
                    "PMD.ExcessiveImports"
})
class EssController {

    private final EssService essService;
    private final AuthorizationService authorizationService;

    @Operation(operationId = "registerBPNInvestigation",
               summary = "Registers an IRS job to start an investigation if a given bpn is contained in a part chain of a given globalAssetId.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Environmental- and Social Standards" },
               description = "Registers an IRS job to start an investigation if a given bpn is contained in a part chain of a given globalAssetId.")
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
    @PostMapping("/bpn/investigations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@authorizationService.verifyBpn() && hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    public JobHandle registerBPNInvestigation(final @Valid @RequestBody RegisterBpnInvestigationJob request) {
        return essService.startIrsJob(request);
    }

    @Operation(description = "Return job with additional supplyChainImpacted information.",
               operationId = "getBPNInvestigation",
               summary = "Return job with additional supplyChainImpacted information.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Environmental- and Social Standards" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
                                         description = "Return job with item graph for the requested id.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = Jobs.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/complete-ess-job-result"))
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
    @GetMapping("/bpn/investigations/{id}")
    @PreAuthorize("@authorizationService.verifyBpn() && hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    public Jobs getBPNInvestigation(
            @Parameter(description = "Id of the job.", schema = @Schema(implementation = UUID.class), name = "id",
                       example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @Valid @PathVariable final UUID id) {
        return essService.getIrsJob(id.toString());
    }

}
