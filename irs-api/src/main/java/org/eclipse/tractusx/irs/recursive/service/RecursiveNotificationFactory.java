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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.irs.recursive.model.RecursiveChildBranch;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationType;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;

/**
 * Creates recursive partner notifications with CX-0151 (Saturn) compliant header fields.
 */
final class RecursiveNotificationFactory {

    private RecursiveNotificationFactory() {
    }

    /* package */ static RecursiveNotificationMessage childRequest(final RecursiveJobState state,
            final RecursiveChildBranch childBranch, final ZonedDateTime sentAt) {
        return RecursiveNotificationMessage.builder()
                .header(RecursiveNotificationMessage.Header.builder()
                        .messageId(childBranch.getMessageId())
                        .relatedMessageId(state.getMessageId())
                        .context(RecursiveNotificationMessage.HEADER_CONTEXT)
                        .sentDateTime(formatTimestamp(sentAt))
                        .senderBpnl(state.getReceiverBpnl())
                        .receiverBpnl(childBranch.getPartnerBpnl())
                        .expectedResponseBy(formatTimestamp(state.getChildResponseDeadline()))
                        .version(RecursiveNotificationMessage.HEADER_VERSION)
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.REQUEST)
                        .openingId(state.getOpeningId())
                        .useCase(state.getUseCase())
                        .globalAssetId(childBranch.getChildGlobalAssetId())
                        .bomLifecycle(state.getBomLifecycle())
                        .aspects(state.getAspects())
                        .build())
                .build();
    }

    /* package */ static RecursiveNotificationMessage parentResponse(final RecursiveJobState state,
            final String localBpnl, final RecursiveResponseStatus status, final RecursiveJobResult result,
            final List<String> responseAspects, final ZonedDateTime sentAt) {
        return RecursiveNotificationMessage.builder()
                .header(responseHeader(newMessageId(), state.getMessageId(), localBpnl, state.getRequesterBpnl(),
                        sentAt))
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.RESPONSE)
                        .openingId(state.getOpeningId())
                        .useCase(state.getUseCase())
                        .bomLifecycle(responseAspects.isEmpty() ? null : state.getBomLifecycle())
                        .aspects(responseAspects)
                        .status(status)
                        .result(result)
                        .build())
                .build();
    }

    /* package */ static RecursiveNotificationMessage rejectionResponse(final RecursiveNotificationMessage request,
            final RecursiveTombstone rejection, final String localBpnl, final ZonedDateTime sentAt) {
        final RecursiveNotificationMessage.Header requestHeader = request.getHeader();
        final RecursiveNotificationMessage.Content requestContent = request.getContent();
        final RecursiveJobResult result = RecursiveJobResult.builder()
                .resultStatus(RecursiveResultStatus.FAILED)
                .useCase(requestContent.getUseCase())
                .bomLifecycle(requestContent.getBomLifecycle())
                .requestedAspects(requestContent.getAspects())
                .childItems(List.of())
                .tombstones(List.of(rejection))
                .build();
        return RecursiveNotificationMessage.builder()
                .header(responseHeader(newMessageId(), requestHeader.getMessageId(), localBpnl,
                        requestHeader.getSenderBpnl(), sentAt))
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.RESPONSE)
                        .openingId(requestContent.getOpeningId())
                        .useCase(requestContent.getUseCase())
                        .bomLifecycle(requestContent.getBomLifecycle())
                        .aspects(requestContent.getAspects())
                        .status(RecursiveResponseStatus.FAILED)
                        .result(result)
                        .build())
                .build();
    }

    private static RecursiveNotificationMessage.Header responseHeader(final String messageId,
            final String relatedMessageId, final String senderBpnl, final String receiverBpnl,
            final ZonedDateTime sentAt) {
        return RecursiveNotificationMessage.Header.builder()
                .messageId(messageId)
                .relatedMessageId(relatedMessageId)
                .context(RecursiveNotificationMessage.HEADER_CONTEXT)
                .sentDateTime(formatTimestamp(sentAt))
                .senderBpnl(senderBpnl)
                .receiverBpnl(receiverBpnl)
                .version(RecursiveNotificationMessage.HEADER_VERSION)
                .build();
    }

    private static String newMessageId() {
        return UUID.randomUUID().toString();
    }

    private static String formatTimestamp(final ZonedDateTime timestamp) {
        return timestamp.toInstant().toString();
    }
}
