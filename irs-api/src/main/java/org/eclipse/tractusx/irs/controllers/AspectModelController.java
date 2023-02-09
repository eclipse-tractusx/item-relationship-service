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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.irs.IrsApplication;
import org.eclipse.tractusx.irs.dtos.ErrorResponse;
import org.eclipse.tractusx.irs.semanticshub.AspectModels;
import org.eclipse.tractusx.irs.services.SemanticHubService;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(IrsApplication.API_PREFIX)
@RequiredArgsConstructor
public class AspectModelController {

    private final SemanticHubService semanticHubService;

    @Operation(operationId = "getAllAspectModels",
               summary = "Get all available aspect models from semantic hub or local models.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"), tags = { "Aspect Models" },
               description = "Get all available aspect models from semantic hub or local models.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Returns all available aspect models.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = AspectModels.class),
                                                              examples = { @ExampleObject(name = "complete")
                                                              })
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
    @GetMapping("/aspectmodels")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('view_irs')")
    public AspectModels getAllAvailableAspectModels() throws SchemaNotFoundException {
        return semanticHubService.getAllAspectModels();
    }
}
