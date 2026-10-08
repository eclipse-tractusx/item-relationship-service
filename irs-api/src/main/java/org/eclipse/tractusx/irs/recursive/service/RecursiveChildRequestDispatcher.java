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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.recursive.model.RecursiveChildBranch;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationDeliveryFailureReason;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.repository.RecursiveJobRepository;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;

/**
 * Sends REQUEST notifications for the child branches of a job.
 *
 * <p>Each child request message id is registered before sending so that the child response can
 * be correlated. A branch whose request could not be delivered to any connector endpoint of the
 * partner is marked FAILED with the delivery failure reason; if that answers the last open branch,
 * the job completes and responds to its parent.</p>
 */
@Slf4j
@RequiredArgsConstructor
class RecursiveChildRequestDispatcher {

    private final RecursiveJobRepository repository;
    private final RecursiveNotificationSender notificationSender;
    private final Supplier<ZonedDateTime> now;
    private final BiFunction<RecursiveJobState, List<RecursiveChildBranch>, RecursiveJobState> childBranchUpdater;
    private final RecursiveParentResponder parentResponder;

    @SuppressWarnings({ "PMD.AvoidCatchingGenericException", "PMD.UseConcurrentHashMap" })
    /* package */ void sendChildRequests(final RecursiveJobState state,
            final List<RecursiveChildBranch> childBranches) {
        final Map<String, RuntimeException> deliveryFailuresByChildRequestMessageId = new LinkedHashMap<>();
        for (final RecursiveChildBranch childBranch : childBranches) {
            if (!childBranch.isSendNotification()) {
                continue;
            }
            final RecursiveNotificationMessage notification =
                    RecursiveNotificationFactory.childRequest(state, childBranch, now.get());

            repository.registerChildRequestMessageId(childBranch.getMessageId(), state.getJobId());

            log.info("-> CHILD_REQUEST to {} childAsset={}",
                    RecursiveLogValue.of(childBranch.getPartnerBpnl()),
                    RecursiveLogValue.of(childBranch.getChildGlobalAssetId()));
            try {
                notificationSender.sendRequest(childBranch.getPartnerBpnl(), notification);
            } catch (final RuntimeException e) {
                deliveryFailuresByChildRequestMessageId.put(childBranch.getMessageId(), e);
                log.warn("Could not send recursive child request for jobId={}, childRequest={}: causeType={}",
                        RecursiveLogValue.of(state.getJobId().toString()),
                        RecursiveLogValue.of(childBranch.getMessageId()),
                        e.getClass().getName());
            }
        }
        if (!deliveryFailuresByChildRequestMessageId.isEmpty()) {
            recordChildDeliveryFailures(state, deliveryFailuresByChildRequestMessageId);
        }
    }

    /**
     * Marks child requests as failed after delivery to all candidate connector endpoints failed.
     */
    private void recordChildDeliveryFailures(final RecursiveJobState state,
            final Map<String, RuntimeException> deliveryFailuresByChildRequestMessageId) {
        final Optional<RecursiveJobState> updated = repository.updateIfNotTerminal(state.getJobId(), state,
                current -> {
                    final List<RecursiveChildBranch> branches = new ArrayList<>();
                    boolean changed = false;
                    for (final RecursiveChildBranch childBranch : current.getChildBranches()) {
                        final RuntimeException deliveryFailure =
                                deliveryFailuresByChildRequestMessageId.get(childBranch.getMessageId());
                        if (deliveryFailure == null || childBranch.getStatus() != null) {
                            branches.add(childBranch);
                        } else {
                            branches.add(childBranch.toBuilder()
                                    .status(RecursiveResponseStatus.FAILED)
                                    .deliveryFailure(classifyDeliveryFailure(deliveryFailure))
                                    .build());
                            changed = true;
                        }
                    }
                    return changed ? childBranchUpdater.apply(current, branches) : null;
                });

        updated.filter(RecursiveJobRepository::isTerminal).ifPresent(parentResponder::sendParentResponseQuietly);
    }

    private RecursiveChildBranch.DeliveryFailure classifyDeliveryFailure(final RuntimeException failure) {
        if (failure instanceof RecursiveNotificationDeliveryException delivery) {
            return RecursiveChildBranch.DeliveryFailure.builder()
                                                         .reason(delivery.getReason())
                                                         .errorRef(delivery.getErrorRef())
                                                         .build();
        }
        return RecursiveChildBranch.DeliveryFailure.builder()
                                                     .reason(RecursiveNotificationDeliveryFailureReason
                                                             .EDC_NOTIFICATION_FAILED)
                                                     .errorRef(UUID.randomUUID().toString())
                                                     .build();
    }
}
