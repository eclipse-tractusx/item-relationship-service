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
package org.eclipse.tractusx.irs.recursive.service;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationType;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;
import org.eclipse.tractusx.irs.recursive.util.RecursivePatternStore;

/** Processes incoming recursive notifications before they enter the job service. */
@Slf4j
@RequiredArgsConstructor
public class RecursiveNotificationReceiver {

    private static final String ACCEPTED = "accepted";
    private static final String REJECTED = "rejected";

    private final RecursiveJobService jobService;
    private final RecursiveNotificationMessageValidator messageValidator;

    public RecursiveNotificationResponse receive(final String edcBpnl, final JsonNode payload) {
        final RoutingFields routing = readRoutingFields(payload)
                .orElseThrow(() -> new RecursiveNotificationValidationException(
                        "Invalid recursive notification header."));
        validateTransportIdentity(edcBpnl);

        final Optional<RecursiveNotificationMessage> validatedMessage = messageValidator.validate(payload);
        final boolean senderMatchesTransport = edcBpnl.equals(routing.senderBpnl());
        if (validatedMessage.isEmpty() || !senderMatchesTransport) {
            return handleInvalidNotification(routing, edcBpnl, senderMatchesTransport);
        }

        final RecursiveNotificationMessage message = validatedMessage.orElseThrow();
        log.info("POST /irs/recursive/notifications - type={}, messageId={}",
                message.getContent().getType(), RecursiveLogValue.of(message.getHeader().getMessageId()));
        final boolean accepted = jobService.handleNotification(message);
        return notificationResponse(accepted ? ACCEPTED : REJECTED, message.getHeader().getMessageId());
    }

    private Optional<RoutingFields> readRoutingFields(final JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            return Optional.empty();
        }
        final JsonNode header = payload.get("header");
        if (header == null || !header.isObject()) {
            return Optional.empty();
        }
        final JsonNode content = payload.get("content");
        return Optional.of(new RoutingFields(textValue(header, "senderBpn"), textValue(header, "messageId"),
                textValue(header, "relatedMessageId"), notificationType(content)));
    }

    private void validateTransportIdentity(final String edcBpnl) {
        if (edcBpnl == null || !RecursivePatternStore.BPNL_PATTERN.matcher(edcBpnl).matches()) {
            throw new RecursiveNotificationAuthenticationException(
                    "Missing or invalid EDC transport identity (edc-bpn header) on recursive notification.");
        }
    }

    private RecursiveNotificationResponse handleInvalidNotification(final RoutingFields routing,
            final String edcBpnl, final boolean senderMatchesTransport) {
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

    private RecursiveNotificationResponse rejectedNotificationResponse(final String messageId) {
        if (validMessageId(messageId)) {
            return notificationResponse(REJECTED, messageId);
        }
        return RecursiveNotificationResponse.builder().status(REJECTED).build();
    }

    private boolean isCorrelatableResponse(final RoutingFields routing) {
        return routing.type() == RecursiveNotificationType.RESPONSE
                && validMessageId(routing.relatedMessageId());
    }

    private boolean validMessageId(final String messageId) {
        return messageId != null && RecursivePatternStore.MESSAGE_ID_PATTERN.matcher(messageId).matches();
    }

    private RecursiveNotificationResponse notificationResponse(final String status, final String messageId) {
        return RecursiveNotificationResponse.builder().status(status).messageId(messageId).build();
    }

    private RecursiveNotificationType notificationType(final JsonNode content) {
        final String type = textValue(content, "type");
        if (type == null) {
            return null;
        }
        try {
            return RecursiveNotificationType.valueOf(type);
        } catch (final IllegalArgumentException exception) {
            return null;
        }
    }

    private String textValue(final JsonNode parent, final String fieldName) {
        if (parent == null || !parent.isObject()) {
            return null;
        }
        final JsonNode value = parent.get(fieldName);
        return value == null || !value.isTextual() ? null : value.textValue();
    }

    private record RoutingFields(String senderBpnl, String messageId, String relatedMessageId,
                                 RecursiveNotificationType type) {
    }
}
