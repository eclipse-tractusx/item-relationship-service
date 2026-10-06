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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.recursive.model.RecursiveChildBranch;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobPhase;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.repository.RecursiveJobRepository;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;

/**
 * Applies child RESPONSE notifications to the parent job that sent the child request.
 *
 * <p>A response is correlated through the related child request message id and the sending
 * partner. A response that cannot be correlated is rejected. A correlated response whose routing
 * or request fields do not match the job fails its child branch. Once every expected branch is
 * answered, the job completes with the aggregated result and the response to its own parent is
 * sent outside the job lock.</p>
 */
@Slf4j
@RequiredArgsConstructor
class RecursiveChildResponseProcessor {

    private final RecursiveJobRepository repository;
    private final RecursiveResultAggregator resultAggregator;
    private final Supplier<ZonedDateTime> now;
    private final Supplier<String> localBpnl;
    private final Consumer<RecursiveJobState> parentResponder;

    /* package */ boolean handleResponse(final RecursiveNotificationMessage msg) {
        final RecursiveNotificationMessage.Header hdr = msg.getHeader();
        final RecursiveNotificationMessage.Content cnt = msg.getContent();
        log.info("RESPONSE from={} relatedMessageId={} status={}", RecursiveLogValue.of(hdr.getSenderBpnl()),
                RecursiveLogValue.of(hdr.getRelatedMessageId()), cnt.getStatus());

        final Optional<CorrelatedChildResponse> correlated = correlateToChildResponse(hdr.getRelatedMessageId(),
                hdr.getSenderBpnl());
        if (correlated.isEmpty()) {
            log.warn("Cannot correlate response msgId={} relMsgId={}", RecursiveLogValue.of(hdr.getMessageId()),
                    RecursiveLogValue.of(hdr.getRelatedMessageId()));
            throw new RecursiveNotificationValidationException("Recursive response cannot be correlated.");
        }

        if (!matchesExpectedJob(msg, correlated.get().state())) {
            recordInvalidChildResponse(correlated.get());
            return false;
        }

        final RecursiveJobState correlatedState = correlated.get().state();
        final String childRequestMessageId = correlated.get().childBranch().getMessageId();
        final Optional<RecursiveJobState> updated = repository.updateIfNotTerminal(correlatedState.getJobId(),
                correlatedState, current -> {
                    return applyChildResponse(current, childRequestMessageId, cnt.getStatus(), cnt.getResult());
                });
        if (updated.isEmpty()) {
            log.warn("Response from {} for jobId={} not applied (terminal or unexpected)",
                    RecursiveLogValue.of(hdr.getSenderBpnl()),
                    RecursiveLogValue.of(correlatedState.getJobId().toString()));
            return false;
        }

        final RecursiveJobState state = updated.get();
        log.info("Job {} -> {} ({}/{} responses)", RecursiveLogValue.of(state.getJobId().toString()),
                state.getState(),
                answeredChildBranches(state.getChildBranches()), state.expectedChildResponseCount());

        if (RecursiveJobRepository.isTerminal(state)) {
            parentResponder.accept(state);
        }
        return true;
    }

    /**
     * Records an authenticated response whose complete notification contract is invalid.
     * The related message id and sender must still identify one expected child request.
     *
     * @param senderBpnl       authenticated sender of the invalid response
     * @param relatedMessageId request message referenced by the invalid response
     * @return true when the response belongs to a known child branch
     */
    /* package */ boolean rejectInvalidCorrelatedResponse(final String senderBpnl, final String relatedMessageId) {
        final Optional<CorrelatedChildResponse> correlated = correlateToChildResponse(relatedMessageId, senderBpnl);
        if (correlated.isEmpty()) {
            return false;
        }
        recordInvalidChildResponse(correlated.get());
        return true;
    }

    /**
     * Replaces the child branches and, when the job becomes terminal, attaches the aggregated result.
     */
    /* package */ RecursiveJobState applyChildBranches(final RecursiveJobState current,
            final List<RecursiveChildBranch> branches) {
        final RecursiveJobPhase nextPhase = answeredChildBranches(branches) < current.expectedChildResponseCount()
                ? RecursiveJobPhase.AWAITING_CHILDREN
                : RecursiveJobPhase.COMPLETED;
        RecursiveJobState updated = current.toBuilder()
                .lastModifiedOn(now.get())
                .state(nextPhase)
                .childBranches(List.copyOf(branches))
                .build();

        if (RecursiveJobRepository.isTerminal(updated)) {
            final RecursiveJobResult aggregated = resultAggregator.aggregate(updated, List.of(), null);
            updated = updated.toBuilder().result(aggregated).build();
        }
        return updated;
    }

    private void recordInvalidChildResponse(final CorrelatedChildResponse correlated) {
        final RecursiveJobState state = correlated.state();
        final String childRequestMessageId = correlated.childBranch().getMessageId();
        final Optional<RecursiveJobState> updated = repository.updateIfNotTerminal(state.getJobId(), state,
                current -> applyChildResponse(current, childRequestMessageId, RecursiveResponseStatus.FAILED,
                        invalidChildResponseResult(current)));

        log.warn("Rejected invalid recursive response for jobId={}, childRequest={}",
                RecursiveLogValue.of(state.getJobId().toString()), RecursiveLogValue.of(childRequestMessageId));
        updated.filter(RecursiveJobRepository::isTerminal).ifPresent(parentResponder);
    }

    private RecursiveJobResult invalidChildResponseResult(final RecursiveJobState state) {
        final RecursiveTombstone tombstone = RecursiveTombstones.childBranch(state.getAspects(),
                RecursiveTombstoneReason.CHILD_RESPONSE_INVALID,
                "A child recursive response did not match the notification contract.");
        return RecursiveJobResult.builder()
                                 .resultStatus(RecursiveResultStatus.FAILED)
                                 .useCase(state.getUseCase())
                                 .bomLifecycle(state.getBomLifecycle())
                                 .requestedAspects(state.getAspects())
                                 .childItems(List.of())
                                 .tombstones(List.of(tombstone))
                                 .build();
    }

    /**
     * Applies a child response to the job and, when the job becomes terminal, attaches the
     * aggregated result.
     */
    private RecursiveJobState applyChildResponse(final RecursiveJobState current, final String childRequestMessageId,
            final RecursiveResponseStatus status, final RecursiveJobResult payload) {
        final List<RecursiveChildBranch> branches = new ArrayList<>();
        boolean changed = false;
        for (final RecursiveChildBranch childBranch : current.getChildBranches()) {
            if (Objects.equals(childBranch.getMessageId(), childRequestMessageId)) {
                if (childBranch.getStatus() != null) {
                    return null;
                }
                branches.add(childBranch.toBuilder().status(status).responsePayload(payload).build());
                changed = true;
                continue;
            }
            branches.add(childBranch);
        }
        return changed ? applyChildBranches(current, branches) : null;
    }

    private int answeredChildBranches(final List<RecursiveChildBranch> branches) {
        return (int) branches.stream()
                .filter(childBranch -> childBranch.getStatus() != null)
                .count();
    }

    private Optional<CorrelatedChildResponse> correlateToChildResponse(final String relatedMessageId,
            final String senderBpnl) {
        if (relatedMessageId == null || senderBpnl == null) {
            return Optional.empty();
        }
        return repository.findJobIdByChildRequestMessageId(relatedMessageId)
                         .flatMap(repository::findById)
                         .flatMap(state -> state.getChildBranches().stream()
                                 .filter(childBranch -> Objects.equals(childBranch.getMessageId(), relatedMessageId))
                                 .filter(childBranch -> Objects.equals(childBranch.getPartnerBpnl(), senderBpnl))
                                 .findFirst()
                                 .map(childBranch -> new CorrelatedChildResponse(state, childBranch)));
    }

    private boolean matchesExpectedJob(final RecursiveNotificationMessage message,
            final RecursiveJobState state) {
        final RecursiveNotificationMessage.Header header = message.getHeader();
        final RecursiveNotificationMessage.Content content = message.getContent();
        return Objects.equals(header.getReceiverBpnl(), localBpnl.get())
                && Objects.equals(content.getOpeningId(), state.getOpeningId())
                && Objects.equals(content.getUseCase(), state.getUseCase())
                && content.getBomLifecycle() == state.getBomLifecycle()
                && content.getAspects() != null && state.getAspects() != null
                && new HashSet<>(content.getAspects()).equals(new HashSet<>(state.getAspects()));
    }

    private record CorrelatedChildResponse(RecursiveJobState state,
                                           RecursiveChildBranch childBranch) {
    }
}
