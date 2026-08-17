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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tractusx.irs.recursive.model.RecursiveChildBranch;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveQuantity;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;

/** Builds the material tree returned by a recursive PURIS job. */
@SuppressWarnings("PMD.TooManyMethods")
class RecursiveResultAggregator {

    /* package */ RecursiveJobResult aggregate(final RecursiveJobState state,
            final List<RecursiveTombstone> additionalTombstones,
            final RecursiveResultStatus forcedResultStatus) {
        final Optional<Set<String>> selectedAspects = selectedAspects(state);
        if (selectedAspects.isEmpty()) {
            return invalidPolicyResult();
        }

        final List<String> requestedAspects = List.copyOf(selectedAspects.get());
        final List<RecursiveChildItem> directChildren = childItems(state, requestedAspects);
        final List<RecursiveTombstone> rootTombstones;
        final List<RecursiveChildItem> resultChildren;

        if (state.isRootJob()) {
            resultChildren = directChildren;
            rootTombstones = RecursiveResultTreeSanitizer.aggregateTombstones(additionalTombstones);
        } else {
            final RecursiveChildItem localNode = localNode(state, directChildren, additionalTombstones,
                    requestedAspects);
            resultChildren = List.of(localNode);
            rootTombstones = List.of();
        }

        final RecursiveResultStatus resultStatus = forcedResultStatus == null
                ? derivedStatus(resultChildren, rootTombstones)
                : forcedResultStatus;
        return RecursiveJobResult.builder()
                                 .resultStatus(resultStatus)
                                 .useCase(state.getUseCase())
                                 .bomLifecycle(state.getBomLifecycle())
                                 .requestedAspects(requestedAspects)
                                 .childItems(List.copyOf(resultChildren))
                                 .tombstones(rootTombstones)
                                 .build();
    }

    private List<RecursiveChildItem> childItems(final RecursiveJobState state, final List<String> requestedAspects) {
        final List<RecursiveChildItem> children = new ArrayList<>();

        for (final RecursiveChildBranch childBranch : state.getChildBranches()) {
            children.add(childItem(childBranch, requestedAspects));
        }
        return List.copyOf(children);
    }

    private RecursiveChildItem childItem(final RecursiveChildBranch childBranch, final List<String> requestedAspects) {
        final RecursiveJobResult childResult = childBranch.getResponsePayload();
        final RecursiveResponseStatus status = childBranch.getStatus();
        final List<RecursiveTombstone> branchTombstones = new ArrayList<>(
                RecursiveResultTreeSanitizer.sanitizeTombstones(childBranch.getTombstones(), requestedAspects));
        final RecursiveChildItem childNode;

        if (childResult != null && childResult.getChildItems() != null
                && childResult.getChildItems().size() == 1
                && childResult.getChildItems().get(0) != null) {
            childNode = RecursiveResultTreeSanitizer.sanitizeAndAggregateNode(
                    childResult.getChildItems().get(0), requestedAspects);
        } else {
            childNode = RecursiveResultTreeSanitizer.emptyNode();
            if (childResult != null && !isFailureResultWithoutNode(childResult)) {
                branchTombstones.add(childBranchTombstone(requestedAspects,
                        RecursiveTombstoneReason.CHILD_RESPONSE_INVALID,
                        "A child recursive response contained an invalid material tree."));
            }
        }

        if (childResult != null) {
            branchTombstones.addAll(RecursiveResultTreeSanitizer.sanitizeTombstones(
                    childResult.getTombstones(), requestedAspects));
        }

        addStatusTombstone(childBranch, childResult, status, requestedAspects,
                !childNode.getTombstones().isEmpty(), branchTombstones);

        final RecursiveQuantity quantity = RecursiveResultTreeSanitizer.sanitizeQuantity(
                childBranch.getQuantity());
        if (quantity == null) {
            branchTombstones.add(localTombstone(RecursiveTombstoneReason.BOM_QUANTITY_NOT_AVAILABLE,
                    "The BOM relationship does not provide a complete quantity."));
        }

        final List<RecursiveTombstone> nodeTombstones = new ArrayList<>(childNode.getTombstones());
        nodeTombstones.addAll(branchTombstones);
        return childNode.toBuilder()
                        .quantity(quantity)
                        .tombstones(RecursiveResultTreeSanitizer.aggregateTombstones(nodeTombstones))
                        .build();
    }

    private boolean isFailureResultWithoutNode(final RecursiveJobResult childResult) {
        return childResult.getResultStatus() == RecursiveResultStatus.FAILED
                && (childResult.getChildItems() == null || childResult.getChildItems().isEmpty())
                && childResult.getTombstones() != null
                && !childResult.getTombstones().isEmpty();
    }

    private void addStatusTombstone(final RecursiveChildBranch childBranch, final RecursiveJobResult childResult,
            final RecursiveResponseStatus status, final List<String> requestedAspects,
            final boolean hasNodeTombstones, final List<RecursiveTombstone> tombstones) {
        if (status == RecursiveResponseStatus.TIMED_OUT) {
            tombstones.add(childBranchTombstone(requestedAspects, RecursiveTombstoneReason.CHILD_RESPONSE_TIMEOUT,
                    "A child branch did not provide a response before the recursive deadline."));
        } else if (status == RecursiveResponseStatus.FAILED && tombstones.isEmpty() && !hasNodeTombstones) {
            tombstones.add(deliveryFailureTombstone(childBranch, requestedAspects));
        } else if (childResult == null && status == RecursiveResponseStatus.COMPLETED) {
            tombstones.add(childBranchTombstone(requestedAspects,
                    RecursiveTombstoneReason.CHILD_RESPONSE_INVALID,
                    "A child recursive response did not contain a result."));
        }
    }

    private RecursiveTombstone deliveryFailureTombstone(final RecursiveChildBranch childBranch,
            final List<String> requestedAspects) {
        final RecursiveChildBranch.DeliveryFailure deliveryFailure = childBranch.getDeliveryFailure();
        if (deliveryFailure == null || deliveryFailure.getErrorRef() == null
                || deliveryFailure.getErrorRef().isBlank()) {
            return childBranchTombstone(requestedAspects, RecursiveTombstoneReason.CHILD_BRANCH_FAILED,
                    "A recursive child branch failed.");
        }
        return RecursiveTombstones.childBranch(requestedAspects,
                RecursiveTombstoneReason.CHILD_BRANCH_FAILED,
                "A recursive child branch failed.", List.of(deliveryFailure.getErrorRef()));
    }

    private RecursiveChildItem localNode(final RecursiveJobState state,
            final List<RecursiveChildItem> directChildren,
            final List<RecursiveTombstone> additionalTombstones,
            final List<String> requestedAspects) {
        final RecursiveChildItem collected = state.getLocalNode() == null
                ? RecursiveResultTreeSanitizer.emptyNode()
                : RecursiveResultTreeSanitizer.sanitizeAndAggregateNode(
                        state.getLocalNode(), requestedAspects);
        final List<RecursiveTombstone> tombstones = new ArrayList<>(collected.getTombstones());
        tombstones.addAll(additionalTombstones);
        return collected.toBuilder()
                        .quantity(null)
                        .childItems(List.copyOf(directChildren))
                        .tombstones(RecursiveResultTreeSanitizer.aggregateTombstones(tombstones))
                        .build();
    }

    private RecursiveTombstone localTombstone(final RecursiveTombstoneReason reason, final String detail) {
        return RecursiveTombstones.local(null, reason, detail);
    }

    private RecursiveTombstone childBranchTombstone(final List<String> requestedAspects,
            final RecursiveTombstoneReason reason, final String detail) {
        return RecursiveTombstones.childBranch(requestedAspects, reason, detail);
    }

    /* package */ static RecursiveResultStatus derivedStatus(final List<RecursiveChildItem> childItems,
            final List<RecursiveTombstone> rootTombstones) {
        final boolean hasTombstones = !rootTombstones.isEmpty()
                || childItems.stream().anyMatch(RecursiveResultTreeSanitizer::hasTombstones);
        if (!hasTombstones) {
            return RecursiveResultStatus.COMPLETE;
        }
        return childItems.stream().anyMatch(RecursiveResultTreeSanitizer::hasUsableData)
                ? RecursiveResultStatus.PARTIAL
                : RecursiveResultStatus.FAILED;
    }

    private static Optional<Set<String>> selectedAspects(final RecursiveJobState state) {
        if (state.getUseCase() == null || state.getBomLifecycle() == null || state.getAspects() == null) {
            return Optional.empty();
        }
        return state.getUseCase()
                .selectAspectIds(state.getBomLifecycle(), state.getAspects())
                .map(LinkedHashSet::new)
                .map(Collections::unmodifiableSet);
    }

    private RecursiveJobResult invalidPolicyResult() {
        final RecursiveTombstone tombstone = RecursiveTombstones.chain(List.of(),
                RecursiveTombstoneReason.CHILD_BRANCH_FAILED, "The recursive job policy is invalid.");
        return RecursiveJobResult.builder()
                                 .resultStatus(RecursiveResultStatus.FAILED)
                                 .requestedAspects(List.of())
                                 .childItems(List.of())
                                 .tombstones(List.of(tombstone))
                                 .build();
    }
}
