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
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.recursive.model.RecursiveJobPhase;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;

/**
 * Sends RESPONSE notifications to the requesting parent: the terminal job result or the
 * rejection of a request that did not become a job.
 *
 * <p>A delivery failure is logged and never changes the local job state; the parent covers a
 * missing response through its child response deadline.</p>
 */
@Slf4j
@RequiredArgsConstructor
class RecursiveParentResponder {

    private final RecursiveNotificationSender notificationSender;
    private final Supplier<ZonedDateTime> now;
    private final Supplier<String> localBpnl;

    /** Sends the terminal result to the parent; root jobs have no parent and send nothing. */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    /* package */ void sendParentResponseQuietly(final RecursiveJobState state) {
        if (state.isRootJob()) {
            return;
        }
        try {
            sendParentResponse(state);
        } catch (final RuntimeException e) {
            log.warn("Could not send recursive response to parent for job {}: causeType={}",
                    RecursiveLogValue.of(state.getJobId().toString()), e.getClass().getName());
        }
    }

    /* package */ void sendRejectionResponseQuietly(final RecursiveNotificationMessage request,
            final RecursiveTombstone rejection) {
        final RecursiveNotificationMessage.Header requestHeader = request.getHeader();
        final RecursiveNotificationMessage response = RecursiveNotificationFactory.rejectionResponse(
                request, rejection, localBpnl.get(), now.get());
        try {
            notificationSender.sendResponse(requestHeader.getSenderBpnl(), response);
        } catch (final RecursiveNotificationDeliveryException exception) {
            log.warn("Could not deliver recursive rejection response for messageId={}: reason={} errorRef={}",
                    RecursiveLogValue.of(requestHeader.getMessageId()), exception.getReason(),
                    RecursiveLogValue.of(exception.getErrorRef()));
        }
    }

    private void sendParentResponse(final RecursiveJobState state) {
        final List<String> responseAspects = RecursiveResponseMapper.selectedAspectIds(state);
        final RecursiveJobResult externalResult = RecursiveResponseMapper.toExternalResult(state.getResult(),
                state.getUseCase(), state.getBomLifecycle(), responseAspects);
        final RecursiveResponseStatus status = state.getState() == RecursiveJobPhase.COMPLETED
                ? RecursiveResponseStatus.COMPLETED : RecursiveResponseStatus.FAILED;

        final RecursiveNotificationMessage full = RecursiveNotificationFactory.parentResponse(
                state, localBpnl.get(), status, externalResult, responseAspects, now.get());

        log.info("= PARENT_RESPONSE ({}) to {} for jobId={}", status,
                RecursiveLogValue.of(state.getRequesterBpnl()), RecursiveLogValue.of(state.getJobId().toString()));
        notificationSender.sendResponse(state.getRequesterBpnl(), full);
    }
}
