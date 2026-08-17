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

import static org.eclipse.tractusx.irs.common.ApiConstants.FORBIDDEN_DESC;
import static org.eclipse.tractusx.irs.common.ApiConstants.UNAUTHORIZED_DESC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.dtos.ErrorResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantNotFoundException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantService;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;
import org.eclipse.tractusx.irs.recursive.util.RecursivePatternStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for managing recursive chain opening grants.
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/irs/recursive/chain-openings/grants")
@RequiredArgsConstructor
@Tag(name = "Recursive Chain Opening Grants",
     description = "Grant storage and lookup for recursive chain openings")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.ExcessiveImports" })
public class RecursiveChainOpeningGrantController {

    private final RecursiveChainOpeningGrantService chainOpeningGrantService;

    @Operation(operationId = "registerRecursiveChainOpeningGrant", summary = "Register a chain opening grant",
               security = @SecurityRequirement(name = "api_key"), tags = { "Recursive Chain Opening Grants" },
               description = "Stores a grant that authorizes recursive traversal for a specific chain opening. "
                       + "The grant key (openingId + globalAssetId + requesterBpn + useCase) is taken from the "
                       + "request body; several grants may exist per openingId, one per requested material and "
                       + "requesting partner. Only the configured PURIS recursive use case is accepted.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Grant registered"),
                            @ApiResponse(responseCode = "400",
                                         description = "Invalid grant payload",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "401", description = UNAUTHORIZED_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-401"))
                                         }),
                            @ApiResponse(responseCode = "403", description = FORBIDDEN_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "406",
                                         description = "Requested response media type is not supported",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "409",
                                         description = "A grant with the same openingId + globalAssetId "
                                                 + "+ requesterBpn + useCase already exists",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "415", description = "Request media type is not supported",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "500", description = "Unexpected recursive IRS error",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "503", description = "Recursive persistence unavailable",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
    })
    @PostMapping
    @PreAuthorize("hasAuthority('" + IrsRoles.ADMIN_IRS + "')")
    public ResponseEntity<RecursiveChainOpeningGrant> registerGrant(
            final @Valid @RequestBody RecursiveChainOpeningGrant grant) {
        log.info("POST /irs/recursive/chain-openings/grants - openingId={}, globalAssetId={}, requesterBpn={}, "
                        + "useCase={}", RecursiveLogValue.of(grant.getOpeningId()),
                RecursiveLogValue.of(grant.getGlobalAssetId()), RecursiveLogValue.of(grant.getRequesterBpn()),
                RecursiveLogValue.of(grant.getUseCase().name()));
        final RecursiveChainOpeningGrant storedGrant = chainOpeningGrantService.registerGrant(grant);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storedGrant);
    }

    @Operation(operationId = "listRecursiveChainOpeningGrants", summary = "List chain opening grants",
               security = @SecurityRequirement(name = "api_key"), tags = { "Recursive Chain Opening Grants" },
               description = "Admin query endpoint for chain opening grants. By default only currently valid grants "
                       + "are returned; set validOnly=false to include expired and not-yet-valid grants.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Grant list returned",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE) }),
                            @ApiResponse(responseCode = "400", description = "Invalid grant filter",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "401", description = UNAUTHORIZED_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-401"))
                                         }),
                            @ApiResponse(responseCode = "403", description = FORBIDDEN_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "406",
                                         description = "Requested response media type is not supported",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "500", description = "Unexpected recursive IRS error",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "503", description = "Recursive persistence unavailable",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
    })
    @PreAuthorize("hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    @GetMapping
    public ResponseEntity<List<RecursiveChainOpeningGrant>> listGrants(
            @Parameter(description = "Optional chain opening filter.", example = "opening-42")
            @Pattern(regexp = RecursivePatternStore.OPTIONAL_SAFE_SINGLE_LINE_STRING,
                     message = "openingId must not contain control or line separator characters")
            final @RequestParam(name = "openingId", required = false) String openingId,
            @Parameter(description = "Optional filter on the granted material.",
                       example = "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
            @Pattern(regexp = RecursivePatternStore.OPTIONAL_GLOBAL_ASSET_ID_STRING,
                     message = "globalAssetId must be blank or a valid UUID or URN UUID")
            final @RequestParam(name = "globalAssetId", required = false) String globalAssetId,
            @Parameter(description = "Optional filter on the requesting partner.", example = "BPNL0000ATLS0001")
            @Pattern(regexp = RecursivePatternStore.OPTIONAL_BPNL_STRING,
                     message = "requesterBpn must be blank or a valid BPNL")
            final @RequestParam(name = "requesterBpn", required = false) String requesterBpn,
            @Parameter(description = "Recursive use case.", example = "PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE")
            final @RequestParam(name = "useCase", required = false) RecursiveUseCase useCase,
            @Parameter(description = "Return only grants valid at query time.", example = "true")
            final @RequestParam(name = "validOnly", defaultValue = "true") boolean validOnly) {
        return ResponseEntity.ok(
                chainOpeningGrantService.findGrants(openingId, globalAssetId, requesterBpn, useCase, validOnly));
    }

    @Operation(operationId = "replaceRecursiveChainOpeningGrant", summary = "Replace a chain opening grant",
               security = @SecurityRequirement(name = "api_key"), tags = { "Recursive Chain Opening Grants" },
               description = "Replaces a grant identified by the grant key (openingId + globalAssetId "
                       + "+ requesterBpn + useCase) in the request body. The stored grant keeps its original "
                       + "createdAt timestamp and receives a fresh updatedAt. Only the configured PURIS recursive "
                       + "use case is accepted.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Grant replaced"),
                            @ApiResponse(responseCode = "400",
                                         description = "Invalid grant payload",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "401", description = UNAUTHORIZED_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-401"))
                                         }),
                            @ApiResponse(responseCode = "403", description = FORBIDDEN_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "406",
                                         description = "Requested response media type is not supported",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "415", description = "Request media type is not supported",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "500", description = "Unexpected recursive IRS error",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "503", description = "Recursive persistence unavailable",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
    })
    @PutMapping
    @PreAuthorize("hasAuthority('" + IrsRoles.ADMIN_IRS + "')")
    public ResponseEntity<RecursiveChainOpeningGrant> replaceGrant(
            final @Valid @RequestBody RecursiveChainOpeningGrant grant) {
        final RecursiveChainOpeningGrant storedGrant = chainOpeningGrantService.replaceGrant(grant);
        return ResponseEntity.ok(storedGrant);
    }

    @Operation(operationId = "deleteRecursiveChainOpeningGrant", summary = "Delete a chain opening grant",
               security = @SecurityRequirement(name = "api_key"), tags = { "Recursive Chain Opening Grants" })
    @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Grant deleted"),
                            @ApiResponse(responseCode = "400", description = "Invalid grant key",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "401", description = UNAUTHORIZED_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-401"))
                                         }),
                            @ApiResponse(responseCode = "403", description = FORBIDDEN_DESC,
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "404", description = "Grant not found",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "500", description = "Unexpected recursive IRS error",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
                            @ApiResponse(responseCode = "503", description = "Recursive persistence unavailable",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(
                                                                      implementation = RecursiveErrorResponse.class))
                                         }),
    })
    @DeleteMapping
    @PreAuthorize("hasAuthority('" + IrsRoles.ADMIN_IRS + "')")
    public ResponseEntity<Void> deleteGrant(
            @Parameter(description = "Id of the recursive chain opening.", example = "opening-42")
            @NotBlank
            @Pattern(regexp = RecursivePatternStore.SAFE_SINGLE_LINE_STRING,
                     message = "openingId must not contain control or line separator characters")
            final @RequestParam(name = "openingId") String openingId,
            @Parameter(description = "The granted material.",
                       example = "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
            @NotBlank
            @Pattern(regexp = RecursivePatternStore.GLOBAL_ASSET_ID_STRING,
                     message = "globalAssetId must be a valid UUID or URN UUID")
            final @RequestParam(name = "globalAssetId") String globalAssetId,
            @Parameter(description = "The requesting partner.", example = "BPNL0000ATLS0001")
            @NotBlank
            @Pattern(regexp = RecursivePatternStore.BPNL_STRING,
                     message = "requesterBpn must be a valid BPNL")
            final @RequestParam(name = "requesterBpn") String requesterBpn,
            @Parameter(description = "Recursive use case.", example = "PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE")
            @NotNull
            final @RequestParam(name = "useCase") RecursiveUseCase useCase) {
        if (chainOpeningGrantService.deleteGrant(openingId, globalAssetId, requesterBpn, useCase)) {
            return ResponseEntity.noContent().build();
        }
        throw new RecursiveChainOpeningGrantNotFoundException();
    }
}
