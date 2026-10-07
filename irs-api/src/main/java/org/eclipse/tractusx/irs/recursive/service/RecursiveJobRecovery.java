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
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildBranch;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.repository.RecursiveJobRepository;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;

/**
 * Resumes jobs that a pod restart left in a non-terminal phase, instead of letting them
 * idle until their deadline expires.
 *
 * <ul>
 *   <li>{@code GRANT_CHECKED}: the async BOM resolution was lost - the grant
 *       is re-validated and processing restarts. A grant that disappeared in the meantime
 *       (e.g. wiped grant store) fails the job with CHAIN_OPENING_REJECTED.</li>
 *   <li>{@code AWAITING_CHILDREN}: child requests without a recorded response are sent
 *       again. Re-sending is idempotent - children dedupe by messageId, and children whose
 *       job already finished answer a duplicate REQUEST by re-sending their response.</li>
 *   <li>A job whose stored aspect selection is invalid for its use case fails.</li>
 *   <li>Jobs past their deadline are left to the timeout sweeper.</li>
 * </ul>
 *
 * <p>One broken job must not keep the other open jobs from resuming, so each job is
 * processed in its own error scope.</p>
 */
@Slf4j
@RequiredArgsConstructor
class RecursiveJobRecovery {

    private final RecursiveJobRepository repository;
    private final RecursiveChainOpeningGrantService grantService;
    private final Executor recursiveJobExecutor;
    private final Supplier<ZonedDateTime> now;
    private final BiConsumer<RecursiveChainOpeningGrant, RecursiveJobState> jobProcessor;
    private final RecursiveChildRequestDispatcher childRequestDispatcher;
    private final BiConsumer<RecursiveJobState, Exception> jobFailureHandler;

    /**
     * Resumes all open jobs of this IRS instance.
     *
     * @return number of jobs whose processing was resumed
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    /* package */ int recoverOpenJobs() {
        int resumed = 0;
        for (final RecursiveJobState state : repository.findAll()) {
            try {
                if (resumeJob(state)) {
                    resumed++;
                }
            } catch (final RuntimeException e) {
                log.warn("Could not resume recursive job {} after restart: causeType={}",
                        RecursiveLogValue.of(state.getJobId().toString()), e.getClass().getName());
            }
        }
        return resumed;
    }

    private boolean resumeJob(final RecursiveJobState state) {
        if (RecursiveJobRepository.isTerminal(state)
                || state.getDeadline() != null && now.get().isAfter(state.getDeadline())) {
            return false;
        }
        if (RecursiveResponseMapper.selectedAspectIds(state).isEmpty()) {
            jobFailureHandler.accept(state, new IllegalArgumentException("Invalid recursive use-case selection"));
            return true;
        }
        return switch (state.getState()) {
            case GRANT_CHECKED -> resumeAcceptedJob(state);
            case AWAITING_CHILDREN -> resendUnansweredChildRequests(state);
            default -> false;
        };
    }

    private boolean resumeAcceptedJob(final RecursiveJobState state) {
        final RecursiveChainOpeningGrant grant;
        try {
            grant = grantService.getActiveGrant(state.getOpeningId(), state.getUseCase(),
                    state.getRequesterBpnl(), state.getGlobalAssetId());
        } catch (final RecursiveChainOpeningGrantInactiveException e) {
            log.warn("Grant no longer valid while resuming recursive job {}",
                    RecursiveLogValue.of(state.getJobId().toString()));
            jobFailureHandler.accept(state, e);
            return true;
        }
        log.info("Resuming recursive job {} in phase {} after restart",
                RecursiveLogValue.of(state.getJobId().toString()),
                state.getState());
        recursiveJobExecutor.execute(() -> jobProcessor.accept(grant, state));
        return true;
    }

    private boolean resendUnansweredChildRequests(final RecursiveJobState state) {
        // Past the child response deadline the job belongs to the timeout sweeper - a re-send would distort its result.
        if (state.getChildResponseDeadline() != null && now.get().isAfter(state.getChildResponseDeadline())) {
            return false;
        }
        final List<RecursiveChildBranch> unanswered = state.getChildBranches().stream()
                .filter(RecursiveChildBranch::isSendNotification)
                .filter(childBranch -> childBranch.getStatus() == null)
                .toList();
        if (unanswered.isEmpty()) {
            return false;
        }
        log.info("Resuming recursive job {} after restart: re-sending {} unanswered child request(s)",
                RecursiveLogValue.of(state.getJobId().toString()), unanswered.size());
        recursiveJobExecutor.execute(() -> childRequestDispatcher.sendChildRequests(state, unanswered));
        return true;
    }
}
