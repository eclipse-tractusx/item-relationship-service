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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.recursive.model.RecursiveChildBranch;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobPhase;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.repository.RecursiveJobRepository;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;

/**
 * Terminates recursive jobs whose deadlines expired (the "timeout sweeper").
 *
 * <p>Two cases per sweep, checked in this order: the hard job deadline (job fails with a
 * {@code RECURSIVE_DEADLINE_EXCEEDED} chain tombstone) and the child response deadline (open
 * branches become TIMED_OUT, the job completes with a PARTIAL result). A timed-out branch must
 * not erase earlier send failures, so failure tombstones for FAILED statuses are kept and the
 * timeout tombstones are appended. One broken job must not keep the other expired jobs waiting,
 * so each job is processed in its own error scope.</p>
 */
@Slf4j
@RequiredArgsConstructor
class RecursiveJobExpiry {

    private final RecursiveJobRepository repository;
    private final Supplier<RecursiveResultAggregator> resultAggregator;
    private final Supplier<ZonedDateTime> now;
    private final Consumer<RecursiveJobState> parentResponder;

    /**
     * Completes non-terminal jobs whose job or child response deadline expired.
     *
     * @return number of jobs completed because a deadline expired
     */
    /* package */ int processExpiredJobs() {
        final ZonedDateTime checkTime = now.get();
        int processed = 0;
        for (final RecursiveJobState state : repository.findAll()) {
            try {
                if (completeDeadlineExceeded(state, checkTime) || completeTimedOut(state, checkTime)) {
                    processed++;
                }
            } catch (final RuntimeException e) {
                log.warn("Could not process expired recursive job {}: causeType={}",
                        RecursiveLogValue.of(state.getJobId()), e.getClass().getName());
            }
        }
        return processed;
    }

    private boolean completeDeadlineExceeded(final RecursiveJobState candidate, final ZonedDateTime checkTime) {
        final Optional<RecursiveJobState> failed = repository.updateIfNotTerminal(candidate.getJobId(), candidate,
                current -> {
                    if (current.getDeadline() == null || !checkTime.isAfter(current.getDeadline())) {
                        return null;
                    }
                    final String detail = "The recursive chain deadline was exceeded.";
                    final RecursiveTombstone deadlineTombstone = RecursiveTombstones.chain(current.getAspects(),
                            RecursiveTombstoneReason.RECURSIVE_DEADLINE_EXCEEDED, detail);
                    final RecursiveJobState deadlineExceededState = current.toBuilder()
                            .childBranches(branchesWithTimeouts(current))
                            .build();
                    final RecursiveJobResult result = resultAggregator.get().aggregate(
                            deadlineExceededState, List.of(deadlineTombstone),
                            RecursiveResultStatus.FAILED);
                    return deadlineExceededState.toBuilder()
                                  .lastModifiedOn(checkTime)
                                  .timedOutOn(checkTime)
                                  .state(RecursiveJobPhase.FAILED)
                                  .failureReason(RecursiveTombstoneReason.RECURSIVE_DEADLINE_EXCEEDED)
                                  .result(result)
                                  .build();
                });

        failed.ifPresent(state -> {
            log.warn("Recursive job {} failed because its deadline expired: {}",
                    RecursiveLogValue.of(state.getJobId()), state.getDeadline());
            parentResponder.accept(state);
        });
        return failed.isPresent();
    }

    private boolean completeTimedOut(final RecursiveJobState candidate, final ZonedDateTime checkTime) {
        final Optional<RecursiveJobState> completed = repository.updateIfNotTerminal(candidate.getJobId(), candidate,
                current -> {
                    if (!isExpiredAwaitingChildren(current, checkTime)) {
                        return null;
                    }
                    if (openChildBranches(current) == 0) {
                        return null;
                    }

                    final RecursiveJobState timedOutState = current.toBuilder()
                                  .lastModifiedOn(checkTime)
                                  .timedOutOn(checkTime)
                                  .state(RecursiveJobPhase.COMPLETED)
                                  .childBranches(branchesWithTimeouts(current))
                                  .build();
                    return timedOutState.toBuilder()
                            .result(resultAggregator.get().aggregate(timedOutState, List.of(), null))
                            .build();
                });

        completed.ifPresent(state -> {
            log.warn("Recursive job {} completed with timed out child responses: {}",
                    RecursiveLogValue.of(state.getJobId()), statusCount(state, RecursiveResponseStatus.TIMED_OUT));
            parentResponder.accept(state);
        });
        return completed.isPresent();
    }

    private boolean isExpiredAwaitingChildren(final RecursiveJobState state, final ZonedDateTime checkTime) {
        return state.getState() == RecursiveJobPhase.AWAITING_CHILDREN
                && state.getChildResponseDeadline() != null
                && checkTime.isAfter(state.getChildResponseDeadline());
    }

    private int openChildBranches(final RecursiveJobState state) {
        return (int) state.getChildBranches().stream()
                .filter(childBranch -> childBranch.getStatus() == null)
                .count();
    }

    private List<RecursiveChildBranch> branchesWithTimeouts(final RecursiveJobState state) {
        return state.getChildBranches().stream()
                .map(childBranch -> childBranch.getStatus() == null
                        ? childBranch.toBuilder().status(RecursiveResponseStatus.TIMED_OUT).build()
                        : childBranch)
                .toList();
    }

    private long statusCount(final RecursiveJobState state, final RecursiveResponseStatus status) {
        return state.getChildBranches().stream()
                .map(RecursiveChildBranch::getStatus)
                .filter(status::equals)
                .count();
    }
}
