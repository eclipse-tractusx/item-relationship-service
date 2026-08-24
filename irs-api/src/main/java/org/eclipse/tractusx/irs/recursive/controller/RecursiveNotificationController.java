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

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Pattern;

import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationResponse;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationReceiver;
import org.eclipse.tractusx.irs.recursive.util.RecursivePatternStore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unified notification endpoint for partner-to-partner recursive communication.
 *
 * <p>A single endpoint handles both REQUEST and RESPONSE notifications,
 * aligned with the Industry-Core target of one notification asset.
 * The {@code content.type} field determines the processing path.</p>
 *
 * <p>In the EDC context, this endpoint is published as a single notification asset
 * with type {@code cx-taxo:RecursiveIrsNotificationApi} and version {@code 1.0}.</p>
 */
@RestController
@Validated
@RequestMapping("/irs/recursive")
@RequiredArgsConstructor
@Tag(name = "Recursive IRS Notifications", description = "Partner-to-partner recursive notification endpoint")
@PreAuthorize("hasAnyAuthority('" + IrsRoles.ADMIN_IRS + "', '" + IrsRoles.VIEW_IRS + "')")
public class RecursiveNotificationController {

    private final RecursiveNotificationReceiver notificationReceiver;

    @Operation(summary = "Receive a recursive notification",
               description = "Unified endpoint for child requests and parent responses. "
                       + "The content.type field (REQUEST or RESPONSE) determines the processing path. "
                       + "REQUEST starts the next recursive child hop and requires header.expectedResponseBy, "
                       + "content.globalAssetId, "
                       + "content.bomLifecycle=asPlanned and a non-empty content.aspects list. "
                       + "RESPONSE returns the correlated child result and requires header.relatedMessageId, "
                       + "content.bomLifecycle=asPlanned, a non-empty content.aspects list, content.status and "
                       + "content.result; header.expectedResponseBy must be omitted. "
                       + "This is the endpoint that the EDC notification asset points to.")
    @ApiResponse(responseCode = "200", description = "Notification accepted or correlated response rejected")
    @ApiResponse(responseCode = "400", description = "Notification payload is invalid",
                 content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecursiveErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Transport identity check failed",
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
    @PostMapping("/notifications")
    public ResponseEntity<RecursiveNotificationResponse> receiveNotification(
            @Parameter(name = "edc-bpn", in = ParameterIn.HEADER, required = true,
                       description = "EDC transport identity of the sending partner.",
                       schema = @Schema(pattern = RecursivePatternStore.BPNL_STRING,
                                        example = "BPNL000000000000"))
            @RequestHeader(value = "edc-bpn", required = false)
            @Pattern(regexp = RecursivePatternStore.BPNL_STRING, message = "edc-bpn must be a valid BPNL")
            final String edcBpnl,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    description = "Recursive notification. See RecursiveNotificationMessage, Header and Content "
                            + "for REQUEST and RESPONSE field rules.",
                    content = @Content(schema = @Schema(implementation = RecursiveNotificationMessage.class)))
            @RequestBody final JsonNode payload) {
        return ResponseEntity.ok(notificationReceiver.receive(edcBpnl, payload));
    }
}
