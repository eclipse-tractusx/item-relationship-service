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
package org.eclipse.tractusx.irs.controllers;

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
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.IrsApplication;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.component.BatchOrderCreated;
import org.eclipse.tractusx.irs.component.BatchOrderResponse;
import org.eclipse.tractusx.irs.component.BatchResponse;
import org.eclipse.tractusx.irs.component.RegisterBatchOrder;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationBatchOrder;
import org.eclipse.tractusx.irs.dtos.ErrorResponse;
import org.eclipse.tractusx.irs.common.auth.AuthorizationService;
import org.eclipse.tractusx.irs.services.CreationBatchService;
import org.eclipse.tractusx.irs.services.QueryBatchService;
import org.eclipse.tractusx.irs.services.timeouts.CancelBatchProcessingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Batch processing.
 */
@Slf4j
@RestController
@RequestMapping(IrsApplication.API_PREFIX)
@RequiredArgsConstructor
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals",
                    "PMD.ExcessiveImports",
})
public class BatchController {

    private final CreationBatchService creationBatchService;
    private final QueryBatchService queryBatchService;
    private final CancelBatchProcessingService cancelBatchProcessingService;
    private final AuthorizationService authorizationService;

    @Operation(operationId = "registerOrder",
               summary = "Registers an IRS order with an array of {globalAssetIds}. "
                       + "Each globalAssetId will be processed in an IRS Job, grouped in batches.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Item Relationship Service" },
               description = "Registers an IRS order with an array of {globalAssetIds}. "
                       + "Each globalAssetId will be processed in an IRS Job, grouped in batches.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Returns orderId of registered Batch order.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = BatchOrderCreated.class),
                                                              examples = { @ExampleObject(name = "complete",
                                                                                          ref = "#/components/examples/job-handle")
                                                              })
                                         }),
                            @ApiResponse(responseCode = "400", description = "Batch Order registration failed.",
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
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@authorizationService.verifyBpn() && hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    public BatchOrderCreated registerBatchOrder(final @Valid @RequestBody RegisterBatchOrder request) {
        final UUID batchOrderId = creationBatchService.create(request);
        return BatchOrderCreated.builder().id(batchOrderId).build();
    }

    @Operation(operationId = "registerESSInvestigationOrder",
               summary = "Registers an order  for an ess investigation with an array of {globalAssetIds}. Each globalAssetId will be processed in an separate job, grouped in batches.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Environmental- and Social Standards" },
               description = "Registers an order  for an ess investigation with an array of {globalAssetIds}. Each globalAssetId will be processed in an separate job, grouped in batches.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Returns orderId of registered Batch order.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = BatchOrderCreated.class),
                                                              examples = { @ExampleObject(name = "complete",
                                                                                          ref = "#/components/examples/job-handle")
                                                              })
                                         }),
                            @ApiResponse(responseCode = "400", description = "Batch Order registration failed.",
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
    @PostMapping("/ess/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@authorizationService.verifyBpn() && hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    public BatchOrderCreated registerESSInvestigationOrder(final @Valid @RequestBody RegisterBpnInvestigationBatchOrder request) {
        final UUID batchOrderId = creationBatchService.create(request);
        return BatchOrderCreated.builder().id(batchOrderId).build();
    }

    @Operation(description = "Get a batch order for a given orderId.",
               operationId = "getBatchOrder",
               summary = "Get a batch order for a given orderId.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
                                         description = "Get a batch order for a given orderId.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = BatchOrderResponse.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/complete-order-result"))
                                         }),
                            @ApiResponse(responseCode = "400", description = "Return Batch Order failed.",
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
                            @ApiResponse(responseCode = "404", description = "Batch Order with the requested orderId not found.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-404"))
                                         }),
    })
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("@authorizationService.verifyBpn() && hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    public BatchOrderResponse getBatchOrder(
            @Parameter(description = "Id of the order.", schema = @Schema(implementation = UUID.class), name = "orderId",
                       example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @Size(min = IrsAppConstants.JOB_ID_SIZE,
                                                                               max = IrsAppConstants.JOB_ID_SIZE) @Valid @PathVariable final UUID orderId) {
        return queryBatchService.findOrderById(orderId);
    }

    @Operation(description = "Get a batch with a given batchId for a given orderId.",
               operationId = "getBatch",
               summary = "Get a batch with a given batchId for a given orderId.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
                                         description = "Get a batch with a given batchId for a given orderId.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = BatchResponse.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/complete-batch-result"))
                                         }),
                            @ApiResponse(responseCode = "400", description = "Return batch failed.",
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
                            @ApiResponse(responseCode = "404", description = "Batch with the requested orderId and batchId not found.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-404"))
                                         }),
    })
    @GetMapping("/orders/{orderId}/batches/{batchId}")
    @PreAuthorize("@authorizationService.verifyBpn() && hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    public BatchResponse getBatch(
            @Parameter(description = "Id of the order.", schema = @Schema(implementation = UUID.class), name = "orderId",
                       example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @Size(min = IrsAppConstants.JOB_ID_SIZE,
                                                                               max = IrsAppConstants.JOB_ID_SIZE) @Valid @PathVariable final UUID orderId,
            @Parameter(description = "Id of the batch.", schema = @Schema(implementation = UUID.class), name = "batchId",
                       example = "4bce40b8-64c7-41bf-9ca3-e9432c7fef98") @Size(min = IrsAppConstants.JOB_ID_SIZE,
                                                                               max = IrsAppConstants.JOB_ID_SIZE) @Valid @PathVariable final UUID batchId) {
        return queryBatchService.findBatchById(orderId, batchId);
    }

    @Operation(description = "Cancel a batch order for a given orderId.",
               operationId = "cancelBatchOrder",
               summary = "Cancel a batch order for a given orderId.",
               security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"),
               tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
                                         description = "Cancel a batch order for a given orderId.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = BatchOrderResponse.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/complete-order-result"))
                                         }),
                            @ApiResponse(responseCode = "400", description = "Return Batch Order failed.",
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
                            @ApiResponse(responseCode = "404", description = "Batch Order with the requested orderId not found.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-404"))
                                         }),
    })
    @PutMapping("/orders/{orderId}")
    @PreAuthorize("@authorizationService.verifyBpn() && hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
    public BatchOrderResponse cancelBatchOrder(
            @Parameter(description = "Id of the order.", schema = @Schema(implementation = UUID.class), name = "orderId",
                       example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @Size(min = IrsAppConstants.JOB_ID_SIZE,
                                                                               max = IrsAppConstants.JOB_ID_SIZE) @Valid @PathVariable final UUID orderId) {
        cancelBatchProcessingService.cancelNotFinishedJobsInBatchOrder(orderId);
        return queryBatchService.findOrderById(orderId);
    }

}
