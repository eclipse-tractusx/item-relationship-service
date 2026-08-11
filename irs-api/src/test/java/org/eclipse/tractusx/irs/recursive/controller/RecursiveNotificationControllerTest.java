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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorCode;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationType;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.eclipse.tractusx.irs.recursive.service.RecursiveJobService;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationAuthenticationException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationContractValidator;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RecursiveNotificationControllerTest {

    private static final String PARENT_BPN = "BPNL0000PARENT01";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RecursiveJobService jobService = mock(RecursiveJobService.class);
    private final RecursiveNotificationContractValidator contractValidator =
            mock(RecursiveNotificationContractValidator.class);
    private final RecursiveNotificationController controller =
            new RecursiveNotificationController(jobService, contractValidator);
    private final RecursiveExceptionHandler exceptionHandler = new RecursiveExceptionHandler();

    @BeforeEach
    void setUp() {
        when(jobService.handleNotification(any())).thenReturn(true);
    }

    @Test
    void acceptsNotificationWhenSenderMatchesTransportIdentity() {
        final RecursiveNotificationMessage message = message(PARENT_BPN);
        final JsonNode payload = validPayload(message);

        final ResponseEntity<RecursiveNotificationResponse> response =
                controller.receiveNotification(PARENT_BPN, payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo("accepted");
        assertThat(response.getBody().getMessageId()).isEqualTo("68904173-ad59-4a77-8412-3e73fcafbd8b");
        verify(jobService).handleNotification(message);
    }

    @Test
    void rejectsNotificationWhenEdcBpnHeaderMissing() {
        final RecursiveNotificationMessage message = message(PARENT_BPN);
        final JsonNode payload = validPayload(message);

        assertThatThrownBy(() -> controller.receiveNotification(null, payload))
                .isInstanceOf(RecursiveNotificationAuthenticationException.class);
        verifyNoInteractions(jobService);
    }

    @Test
    void rejectsNotificationWhenSenderBpnDoesNotMatchTransportIdentity() {
        final RecursiveNotificationMessage message = message("BPNL0000SPOOFED9");
        final JsonNode payload = validPayload(message);

        assertThatThrownBy(() -> controller.receiveNotification(PARENT_BPN, payload))
                .isInstanceOf(RecursiveNotificationAuthenticationException.class);
        verifyNoInteractions(jobService);
    }

    @Test
    void authenticationFailureMapsToForbidden() {
        final ResponseEntity<RecursiveErrorResponse> response = exceptionHandler.handleNotificationAuthentication(
                new RecursiveNotificationAuthenticationException("mismatch"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getCode())
                .isEqualTo(RecursiveErrorCode.NOTIFICATION_AUTHENTICATION_FAILED);
    }

    @Test
    void rejectsCorrelatedNotificationWhenPayloadCannotBeDecoded() {
        final RecursiveNotificationMessage message = message(PARENT_BPN);
        final JsonNode payload = OBJECT_MAPPER.valueToTree(message);
        final String relatedMessageId = "11111111-1111-1111-1111-111111111111";
        when(contractValidator.readRoutingFields(payload)).thenReturn(Optional.of(
                new RecursiveNotificationContractValidator.RoutingFields(PARENT_BPN,
                        message.getHeader().getMessageId(), relatedMessageId, RecursiveNotificationType.RESPONSE)));
        when(contractValidator.decode(payload)).thenReturn(Optional.empty());
        when(jobService.rejectInvalidCorrelatedResponse(PARENT_BPN, relatedMessageId)).thenReturn(true);

        final ResponseEntity<RecursiveNotificationResponse> response =
                controller.receiveNotification(PARENT_BPN, payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo("rejected");
        verify(jobService).rejectInvalidCorrelatedResponse(PARENT_BPN, relatedMessageId);
    }

    @Test
    void doesNotRejectChildBranchForInvalidRequest() {
        final RecursiveNotificationMessage message = message(PARENT_BPN);
        final JsonNode payload = OBJECT_MAPPER.valueToTree(message);
        final String relatedMessageId = "11111111-1111-1111-1111-111111111111";
        when(contractValidator.readRoutingFields(payload)).thenReturn(Optional.of(
                new RecursiveNotificationContractValidator.RoutingFields(PARENT_BPN,
                        message.getHeader().getMessageId(), relatedMessageId, RecursiveNotificationType.REQUEST)));
        when(contractValidator.decode(payload)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.receiveNotification(PARENT_BPN, payload))
                .isInstanceOf(RecursiveNotificationValidationException.class);
        verifyNoInteractions(jobService);
    }

    @Test
    void doesNotUseInvalidRelatedMessageIdForCorrelation() {
        final RecursiveNotificationMessage message = message(PARENT_BPN);
        final JsonNode payload = OBJECT_MAPPER.valueToTree(message);
        when(contractValidator.readRoutingFields(payload)).thenReturn(Optional.of(
                new RecursiveNotificationContractValidator.RoutingFields(PARENT_BPN,
                        message.getHeader().getMessageId(), "invalid", RecursiveNotificationType.RESPONSE)));
        when(contractValidator.decode(payload)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.receiveNotification(PARENT_BPN, payload))
                .isInstanceOf(RecursiveNotificationValidationException.class);
        verifyNoInteractions(jobService);
    }

    @Test
    void doesNotReflectInvalidMessageIdInRejectedResponse() {
        final RecursiveNotificationMessage message = message(PARENT_BPN);
        final JsonNode payload = OBJECT_MAPPER.valueToTree(message);
        final String relatedMessageId = "11111111-1111-1111-1111-111111111111";
        when(contractValidator.readRoutingFields(payload)).thenReturn(Optional.of(
                new RecursiveNotificationContractValidator.RoutingFields(PARENT_BPN,
                        "invalid\nmessage", relatedMessageId, RecursiveNotificationType.RESPONSE)));
        when(contractValidator.decode(payload)).thenReturn(Optional.empty());
        when(jobService.rejectInvalidCorrelatedResponse(PARENT_BPN, relatedMessageId)).thenReturn(true);

        final ResponseEntity<RecursiveNotificationResponse> response =
                controller.receiveNotification(PARENT_BPN, payload);

        assertThat(response.getBody().getStatus()).isEqualTo("rejected");
        assertThat(response.getBody().getMessageId()).isNull();
        verify(jobService).rejectInvalidCorrelatedResponse(PARENT_BPN, relatedMessageId);
    }

    private JsonNode validPayload(final RecursiveNotificationMessage message) {
        final JsonNode payload = OBJECT_MAPPER.valueToTree(message);
        when(contractValidator.readRoutingFields(payload)).thenReturn(Optional.of(
                new RecursiveNotificationContractValidator.RoutingFields(message.getHeader().getSenderBpnl(),
                        message.getHeader().getMessageId(), message.getHeader().getRelatedMessageId(),
                        message.getContent().getType())));
        when(contractValidator.decode(payload)).thenReturn(Optional.of(message));
        when(contractValidator.isValid(message)).thenReturn(true);
        return payload;
    }

    private RecursiveNotificationMessage message(final String senderBpnl) {
        return RecursiveNotificationMessage.builder()
                .header(RecursiveNotificationMessage.Header.builder()
                        .messageId("68904173-ad59-4a77-8412-3e73fcafbd8b")
                        .context(RecursiveNotificationMessage.HEADER_CONTEXT)
                        .sentDateTime("2026-08-06T08:00:00Z")
                        .senderBpnl(senderBpnl)
                        .receiverBpnl("BPNL0000ATLS0001")
                        .expectedResponseBy("2026-08-06T08:10:00Z")
                        .version(RecursiveNotificationMessage.HEADER_VERSION)
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.REQUEST)
                        .openingId("opening-42")
                        .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                        .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                        .bomLifecycle(BomLifecycle.AS_PLANNED)
                        .aspects(List.of(RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId()))
                        .build())
                .build();
    }
}
