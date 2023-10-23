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
package org.eclipse.tractusx.irs.policystore.controllers;

import static org.eclipse.tractusx.irs.common.ApiConstants.FORBIDDEN_DESC;
import static org.eclipse.tractusx.irs.common.ApiConstants.UNAUTHORIZED_DESC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
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
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.dtos.ErrorResponse;
import org.eclipse.tractusx.irs.policystore.models.CreatePolicyRequest;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.policystore.models.UpdatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.services.PolicyStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Policy Store REST controller.
 */
@Slf4j
@RestController
@RequestMapping("irs")
@RequiredArgsConstructor
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals",
                    "PMD.ExcessiveImports"
})
public class PolicyStoreController {

    private final PolicyStoreService service;

    @Operation(operationId = "registerAllowedPolicy",
               summary = "Register a policy that should be accepted in EDC negotiation.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Item Relationship Service" },
               description = "Register a policy that should be accepted in EDC negotiation.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201"),
                            @ApiResponse(responseCode = "400", description = "Policy registration failed.",
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
    @PostMapping("/policies")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@authorizationService.verifyBpn() && hasAuthority('" + IrsRoles.ADMIN_IRS + "')")
    public void registerAllowedPolicy(final @Valid @RequestBody CreatePolicyRequest request) {
        service.registerPolicy(request);
    }

    @Operation(operationId = "getAllowedPolicies",
               summary = "Lists the registered policies that should be accepted in EDC negotiation.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Item Relationship Service" },
               description = "Lists the registered policies that should be accepted in EDC negotiation.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Returns the policies.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(
                                                 schema = @Schema(implementation = Policy.class)))
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
    @GetMapping("/policies")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@authorizationService.verifyBpn() && hasAuthority('" + IrsRoles.ADMIN_IRS + "')")
    public List<Policy> getPolicies() {
        return service.getStoredPolicies();
    }

    @Operation(operationId = "deleteAllowedPolicy",
               summary = "Removes a policy that should no longer be accepted in EDC negotiation.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Item Relationship Service" },
               description = "Removes a policy that should no longer be accepted in EDC negotiation.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200"),
                            @ApiResponse(responseCode = "400", description = "Policy deletion failed.",
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
    @DeleteMapping("/policies/{policyId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@authorizationService.verifyBpn() && hasAuthority('" + IrsRoles.ADMIN_IRS + "')")
    public void deleteAllowedPolicy(@PathVariable("policyId") final String policyId) {
        service.deletePolicy(policyId);
    }

    @Operation(operationId = "updateAllowedPolicy",
               summary = "Updates an existing policy with new validUntil value.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Item Relationship Service" },
               description = "Updates an existing policy with new validUntil value.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200"),
                            @ApiResponse(responseCode = "400", description = "Policy update failed.",
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
    @PutMapping("/policies/{policyId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@authorizationService.verifyBpn() && hasAuthority('" + IrsRoles.ADMIN_IRS + "')")
    public void updateAllowedPolicy(@PathVariable("policyId") final String policyId,
            final @Valid @RequestBody UpdatePolicyRequest request) {
        service.updatePolicy(policyId, request);
    }
}
