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
import java.util.Set;

import org.eclipse.tractusx.irs.recursive.model.RecursiveChildBranch;
import org.eclipse.tractusx.irs.component.AsyncFetchedItems;
import org.eclipse.tractusx.irs.component.JobErrorDetails;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobParameter;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobPhase;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobView;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;

/** Maps internal recursive job state to the privacy-safe external result contract. */
final class RecursiveResponseMapper {

    private RecursiveResponseMapper() {
    }

    /* package */ static RecursiveJobStatusResponse toStatusResponse(final RecursiveJobState state) {
        final List<String> selectedAspectIds = selectedAspectIds(state);
        return RecursiveJobStatusResponse.builder()
                                         .job(toJobView(state, selectedAspectIds))
                                         .result(toExternalResult(state.getResult(), state.getUseCase(),
                                                 state.getBomLifecycle(), state.getAspects()))
                                         .build();
    }

    @SuppressWarnings("PMD.NullAssignment")
    private static RecursiveJobView toJobView(final RecursiveJobState state, final List<String> selectedAspectIds) {
        final boolean terminal = state.getState().isTerminal();
        return RecursiveJobView.builder()
                               .id(state.getJobId())
                               .globalAssetId(state.getGlobalAssetId())
                               .state(state.getState().toJobState())
                               .createdOn(state.getCreatedOn())
                               .completedOn(terminal ? state.getLastModifiedOn() : null)
                               .lastModifiedOn(state.getLastModifiedOn())
                               .asyncFetchedItems(toAsyncFetchedItems(state))
                               .exception(toJobException(state))
                               .parameter(RecursiveJobParameter.builder()
                                                               .openingId(state.getOpeningId())
                                                               .useCase(state.getUseCase())
                                                               .bomLifecycle(state.getBomLifecycle())
                                                               .aspects(selectedAspectIds)
                                                               .deadline(state.getDeadline())
                                                               .childResponseDeadline(state.getChildResponseDeadline())
                                                               .build())
                               .build();
    }

    /* package */ static List<String> selectedAspectIds(final RecursiveJobState state) {
        if (state.getUseCase() == null || state.getBomLifecycle() == null || state.getAspects() == null) {
            return List.of();
        }
        return state.getUseCase()
                    .selectAspectIds(state.getBomLifecycle(), state.getAspects())
                    .map(List::copyOf)
                    .orElseGet(List::of);
    }

    @SuppressWarnings("PMD.NullAssignment")
    private static JobErrorDetails toJobException(final RecursiveJobState state) {
        if (state.getState() != RecursiveJobPhase.FAILED) {
            return null;
        }
        final String reason = Optional.ofNullable(state.getFailureReason())
                                      .map(Enum::name)
                                      .orElse(RecursiveTombstoneReason.CHILD_BRANCH_FAILED.name());
        final ZonedDateTime occurredOn = state.getTimedOutOn() != null
                ? state.getTimedOutOn()
                : state.getLastModifiedOn();
        return JobErrorDetails.builder()
                              .exception(reason)
                              .exceptionDate(occurredOn)
                              .build();
    }

    private static AsyncFetchedItems toAsyncFetchedItems(final RecursiveJobState state) {
        final List<RecursiveResponseStatus> statuses = state.getChildBranches().stream()
                .map(RecursiveChildBranch::getStatus)
                .toList();
        final int completed = (int) statuses.stream()
                .filter(RecursiveResponseStatus.COMPLETED::equals)
                .count();
        final int failed = (int) statuses.stream()
                .filter(status -> status == RecursiveResponseStatus.FAILED
                        || status == RecursiveResponseStatus.TIMED_OUT)
                .count();
        final int running = (int) statuses.stream().filter(status -> status == null).count();
        return AsyncFetchedItems.builder()
                                .running(running)
                                .completed(completed)
                                .failed(failed)
                                .build();
    }

    @SuppressWarnings("PMD.NullAssignment")
    /* package */ static RecursiveJobResult toExternalResult(final RecursiveJobResult rawResult,
            final RecursiveUseCase useCase, final BomLifecycle bomLifecycle, final List<String> fallbackAspects) {
        if (rawResult == null) {
            return null;
        }

        final Optional<Set<String>> selectedAspects = useCase == null || bomLifecycle == null || fallbackAspects == null
                ? Optional.empty()
                : useCase.selectAspectIds(bomLifecycle, fallbackAspects);
        final List<String> selectedAspectIds = selectedAspects.map(List::copyOf).orElseGet(List::of);
        final List<RecursiveChildItem> childItems = selectedAspects.isEmpty()
                ? List.of()
                : RecursiveResultTreeSanitizer.sanitizeNodes(rawResult.getChildItems(), selectedAspectIds);
        final List<RecursiveTombstone> tombstones = RecursiveResultTreeSanitizer.sanitizeTombstones(
                rawResult.getTombstones(), selectedAspectIds);

        final RecursiveResultStatus resultStatus;
        if (selectedAspects.isEmpty()
                || rawResult.getResultStatus() == RecursiveResultStatus.FAILED) {
            resultStatus = RecursiveResultStatus.FAILED;
        } else {
            resultStatus = RecursiveResultAggregator.derivedStatus(childItems, tombstones);
        }

        return RecursiveJobResult.builder()
                                 .resultStatus(resultStatus)
                                 .useCase(selectedAspects.isPresent() ? useCase : null)
                                 .bomLifecycle(selectedAspects.isPresent() ? bomLifecycle : null)
                                 .requestedAspects(selectedAspectIds)
                                 .childItems(childItems)
                                 .tombstones(tombstones)
                                 .build();
    }

}
