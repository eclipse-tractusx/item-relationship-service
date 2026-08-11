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

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Pattern;

import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationType;
import org.eclipse.tractusx.irs.recursive.service.RecursiveJobService;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationAuthenticationException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationContractValidator;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationValidationException;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;
import org.eclipse.tractusx.irs.recursive.util.RecursivePatternStore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
@Slf4j
@RestController
@Validated
@RequestMapping("/irs/recursive")
@RequiredArgsConstructor
@Tag(name = "Recursive IRS Notifications", description = "Partner-to-partner recursive notification endpoint")
public class RecursiveNotificationController {

    private final RecursiveJobService jobService;
    private final RecursiveNotificationContractValidator contractValidator;

    @Operation(summary = "Receive a recursive notification",
               description = "Unified endpoint for child requests and parent responses. "
                       + "The content.type field (REQUEST or RESPONSE) determines the processing path. "
                       + "REQUEST starts one downstream hop and requires header.expectedResponseBy, content.globalAssetId, "
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
        final RecursiveNotificationContractValidator.RoutingFields routing = contractValidator
                .readRoutingFields(payload)
                .orElseThrow(() -> new RecursiveNotificationValidationException(
                        "Invalid recursive notification header."));
        validateTransportIdentity(edcBpnl);

        final Optional<RecursiveNotificationMessage> decodedMessage = contractValidator.decode(payload);
        final boolean senderMatchesTransport = edcBpnl.equals(routing.senderBpnl());
        if (isInvalidNotification(decodedMessage, senderMatchesTransport)) {
            return handleInvalidNotification(routing, edcBpnl, senderMatchesTransport);
        }
        final RecursiveNotificationMessage message = decodedMessage.orElseThrow();
        log.info("POST /irs/recursive/notifications - type={}, messageId={}",
                message.getContent().getType(), RecursiveLogValue.of(message.getHeader().getMessageId()));
        final boolean accepted = jobService.handleNotification(message);
        return notificationResponse(accepted ? "accepted" : "rejected", message.getHeader().getMessageId());
    }

    /** Validates the transport identity supplied by the consuming EDC data plane. */
    private void validateTransportIdentity(final String edcBpnl) {
        if (edcBpnl == null || !RecursivePatternStore.BPNL_PATTERN.matcher(edcBpnl).matches()) {
            throw new RecursiveNotificationAuthenticationException(
                    "Missing or invalid EDC transport identity (edc-bpn header) on recursive notification.");
        }
    }

    private boolean isInvalidNotification(final Optional<RecursiveNotificationMessage> decodedMessage,
            final boolean senderMatchesTransport) {
        return decodedMessage.isEmpty()
                || !contractValidator.isValid(decodedMessage.orElseThrow())
                || !senderMatchesTransport;
    }

    private ResponseEntity<RecursiveNotificationResponse> handleInvalidNotification(
            final RecursiveNotificationContractValidator.RoutingFields routing, final String edcBpnl,
            final boolean senderMatchesTransport) {
        if (isCorrelatableResponse(routing)
                && jobService.rejectInvalidCorrelatedResponse(edcBpnl, routing.relatedMessageId())) {
            return rejectedNotificationResponse(routing.messageId());
        }
        if (!senderMatchesTransport) {
            throw new RecursiveNotificationAuthenticationException(
                    "Recursive notification sender BPN does not match the EDC transport identity.");
        }
        throw new RecursiveNotificationValidationException("Invalid recursive notification payload.");
    }

    private ResponseEntity<RecursiveNotificationResponse> rejectedNotificationResponse(final String messageId) {
        if (validMessageId(messageId)) {
            return notificationResponse("rejected", messageId);
        }
        return ResponseEntity.ok(RecursiveNotificationResponse.builder().status("rejected").build());
    }

    private boolean isCorrelatableResponse(final RecursiveNotificationContractValidator.RoutingFields routing) {
        return routing.type() == RecursiveNotificationType.RESPONSE
                && validMessageId(routing.relatedMessageId());
    }

    private boolean validMessageId(final String messageId) {
        return messageId != null && RecursivePatternStore.MESSAGE_ID_PATTERN.matcher(messageId).matches();
    }

    private ResponseEntity<RecursiveNotificationResponse> notificationResponse(final String status,
            final String messageId) {
        return ResponseEntity.ok(RecursiveNotificationResponse.builder()
                                                              .status(status)
                                                              .messageId(messageId)
                                                              .build());
    }
}
