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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationType;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.junit.jupiter.api.Test;

class RecursiveNotificationReceiverTest {

    private static final String PARENT_BPNL = "BPNL0000PARENT01";
    private static final String MESSAGE_ID = "68904173-ad59-4a77-8412-3e73fcafbd8b";
    private static final String RELATED_MESSAGE_ID = "11111111-1111-1111-1111-111111111111";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RecursiveJobService jobService = mock(RecursiveJobService.class);
    private final RecursiveNotificationMessageValidator messageValidator =
            mock(RecursiveNotificationMessageValidator.class);
    private final RecursiveNotificationReceiver receiver =
            new RecursiveNotificationReceiver(jobService, messageValidator);

    @Test
    void acceptsNotificationWhenSenderMatchesTransportIdentity() {
        final RecursiveNotificationMessage message = request(PARENT_BPNL);
        final JsonNode payload = objectMapper.valueToTree(message);
        when(messageValidator.validate(payload)).thenReturn(Optional.of(message));
        when(jobService.handleNotification(message)).thenReturn(true);

        final RecursiveNotificationResponse response = receiver.receive(PARENT_BPNL, payload);

        assertThat(response.getStatus()).isEqualTo("accepted");
        assertThat(response.getMessageId()).isEqualTo(MESSAGE_ID);
        verify(jobService).handleNotification(message);
    }

    @Test
    void returnsRejectedWhenJobServiceRejectsNotification() {
        final RecursiveNotificationMessage message = request(PARENT_BPNL);
        final JsonNode payload = objectMapper.valueToTree(message);
        when(messageValidator.validate(payload)).thenReturn(Optional.of(message));
        when(jobService.handleNotification(message)).thenReturn(false);

        final RecursiveNotificationResponse response = receiver.receive(PARENT_BPNL, payload);

        assertThat(response.getStatus()).isEqualTo("rejected");
        assertThat(response.getMessageId()).isEqualTo(MESSAGE_ID);
    }

    @Test
    void rejectsNotificationWhenEdcBpnHeaderMissing() {
        final RecursiveNotificationMessage message = request(PARENT_BPNL);
        final JsonNode payload = objectMapper.valueToTree(message);

        assertThatThrownBy(() -> receiver.receive(null, payload))
                .isInstanceOf(RecursiveNotificationAuthenticationException.class);
        verifyNoInteractions(jobService, messageValidator);
    }

    @Test
    void rejectsNotificationWhenSenderBpnDoesNotMatchTransportIdentity() {
        final RecursiveNotificationMessage message = request("BPNL0000SPOOFED9");
        final JsonNode payload = objectMapper.valueToTree(message);
        when(messageValidator.validate(payload)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> receiver.receive(PARENT_BPNL, payload))
                .isInstanceOf(RecursiveNotificationAuthenticationException.class);
        verifyNoInteractions(jobService);
    }

    @Test
    void rejectsCorrelatedNotificationWhenPayloadIsInvalid() {
        final JsonNode payload = invalidResponse(MESSAGE_ID, RELATED_MESSAGE_ID);
        when(messageValidator.validate(payload)).thenReturn(Optional.empty());
        when(jobService.rejectInvalidCorrelatedResponse(PARENT_BPNL, RELATED_MESSAGE_ID)).thenReturn(true);

        final RecursiveNotificationResponse response = receiver.receive(PARENT_BPNL, payload);

        assertThat(response.getStatus()).isEqualTo("rejected");
        assertThat(response.getMessageId()).isEqualTo(MESSAGE_ID);
        verify(jobService).rejectInvalidCorrelatedResponse(PARENT_BPNL, RELATED_MESSAGE_ID);
    }

    @Test
    void doesNotRejectChildBranchForInvalidRequest() {
        final JsonNode payload = objectMapper.valueToTree(request(PARENT_BPNL));
        when(messageValidator.validate(payload)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiver.receive(PARENT_BPNL, payload))
                .isInstanceOf(RecursiveNotificationValidationException.class);
        verifyNoInteractions(jobService);
    }

    @Test
    void doesNotUseInvalidRelatedMessageIdForCorrelation() {
        final JsonNode payload = invalidResponse(MESSAGE_ID, "invalid");
        when(messageValidator.validate(payload)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiver.receive(PARENT_BPNL, payload))
                .isInstanceOf(RecursiveNotificationValidationException.class);
        verifyNoInteractions(jobService);
    }

    @Test
    void doesNotReflectInvalidMessageIdInRejectedResponse() {
        final JsonNode payload = invalidResponse("invalid\nmessage", RELATED_MESSAGE_ID);
        when(messageValidator.validate(payload)).thenReturn(Optional.empty());
        when(jobService.rejectInvalidCorrelatedResponse(PARENT_BPNL, RELATED_MESSAGE_ID)).thenReturn(true);

        final RecursiveNotificationResponse response = receiver.receive(PARENT_BPNL, payload);

        assertThat(response.getStatus()).isEqualTo("rejected");
        assertThat(response.getMessageId()).isNull();
        verify(jobService).rejectInvalidCorrelatedResponse(PARENT_BPNL, RELATED_MESSAGE_ID);
    }

    @Test
    void rejectsPayloadWithoutHeaderBeforeMessageValidation() {
        final JsonNode payload = objectMapper.createObjectNode();

        assertThatThrownBy(() -> receiver.receive(PARENT_BPNL, payload))
                .isInstanceOf(RecursiveNotificationValidationException.class);
        verifyNoInteractions(jobService, messageValidator);
    }

    private JsonNode invalidResponse(final String messageId, final String relatedMessageId) {
        final ObjectNode payload = objectMapper.valueToTree(request(PARENT_BPNL));
        ((ObjectNode) payload.get("header")).put("messageId", messageId).put("relatedMessageId", relatedMessageId);
        ((ObjectNode) payload.get("content")).put("type", RecursiveNotificationType.RESPONSE.name());
        return payload;
    }

    private RecursiveNotificationMessage request(final String senderBpnl) {
        return RecursiveNotificationMessage.builder()
                .header(RecursiveNotificationMessage.Header.builder()
                        .messageId(MESSAGE_ID)
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
