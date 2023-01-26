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
package org.eclipse.tractusx.ess.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.UUID;

import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.eclipse.tractusx.irs.dtos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/irs")
@RequiredArgsConstructor
@Validated
class EssController {

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
                            @ApiResponse(responseCode = "401", description = "Authorized failed.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-401"))
                                         }),
                            @ApiResponse(responseCode = "403", description = "Authorized failed.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-403"))
                                         }),
    })
    @PostMapping("/jobs/bpnInvestigation")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('view_irs')")
    public JobHandle registerBPNInvestigation(final @Valid @RequestBody RegisterBpnInvestigationJob request) {
        return JobHandle.builder().id(UUID.randomUUID()).build();
    }

}
