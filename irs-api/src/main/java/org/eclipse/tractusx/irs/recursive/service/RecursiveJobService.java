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

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.recursive.config.RecursiveProperties;
import org.eclipse.tractusx.irs.recursive.model.RecursiveBomChild;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildBranch;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobPhase;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobRequest;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationDeliveryFailureReason;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationType;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.repository.RecursiveJobRepository;
import org.eclipse.tractusx.irs.recursive.service.RecursiveRequestFactory.PreparedRecursiveJob;
import org.eclipse.tractusx.irs.recursive.store.RecursiveJobStateStore;
import org.eclipse.tractusx.irs.recursive.util.RecursiveGlobalAssetId;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;

/**
 * Choreography of the recursive IRS job lifecycle.
 *
 * <h3>Flow</h3>
 * <ol>
 *   <li>Request -> validate grant -> accept job -> resolve BOM</li>
 *   <li>Granted child partners exist -> send REQUEST each, phase = AWAITING_CHILDREN</li>
 *   <li>Leaf node (no partners) -> collect own anonymized aspects -> send RESPONSE to parent immediately</li>
 *   <li>All child responses arrive -> merge child payloads with own anonymized aspects -> send RESPONSE to parent</li>
 *   <li>The root job exposes its direct BOM children and never collects its own PURIS aspects</li>
 * </ol>
 *
 * <p>Request normalization and deadlines live in {@link RecursiveRequestFactory}, result building
 * in {@link RecursiveResultAggregator}, deadline/timeout termination in {@link RecursiveJobExpiry}
 * and all state mutations are serialized per job through {@link RecursiveJobRepository}.
 * Notifications to partners are always sent outside the job lock.</p>
 */
@Slf4j
public class RecursiveJobService {

    private final RecursiveChainOpeningGrantService grantService;
    private final RecursiveTraversalService traversalService;
    private final RecursiveJobRepository repository;
    private final RecursiveRequestFactory requestFactory;
    private final RecursiveJobExpiry jobExpiry;

    private final Executor recursiveJobExecutor;
    private final RecursiveNotificationSender notificationSender;
    private final RecursiveSubmodelCollector submodelCollector;

    private final RecursiveResultAggregator resultAggregator = new RecursiveResultAggregator();

    private final RecursiveProperties recursiveProperties;
    private final Clock clock;

    public RecursiveJobService(final RecursiveChainOpeningGrantService grantService,
            final RecursiveTraversalService traversalService, final RecursiveJobStateStore jobStateStore,
            final RecursiveNotificationSender notificationSender,
            final RecursiveSubmodelCollector submodelCollector,
            final RecursiveProperties recursiveProperties,
            final Executor recursiveJobExecutor,
            final Clock clock) {
        this.grantService = Objects.requireNonNull(grantService, "grantService must not be null");
        this.traversalService = Objects.requireNonNull(traversalService, "traversalService must not be null");
        this.repository = new RecursiveJobRepository(
                Objects.requireNonNull(jobStateStore, "jobStateStore must not be null"));
        this.notificationSender = Objects.requireNonNull(notificationSender, "notificationSender must not be null");
        this.submodelCollector = Objects.requireNonNull(submodelCollector, "submodelCollector must not be null");
        this.recursiveProperties = Objects.requireNonNull(recursiveProperties,
                "recursiveProperties must not be null");
        this.recursiveJobExecutor = Objects.requireNonNull(recursiveJobExecutor,
                "recursiveJobExecutor must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.requestFactory = new RecursiveRequestFactory(recursiveProperties, clock);
        this.jobExpiry = new RecursiveJobExpiry(repository, () -> resultAggregator, this::now,
                this::sendParentResponseQuietly);
    }

    /**
     * Starts a ROOT job (called via POST /irs/recursive/jobs).
     *
     * @param request the root recursive job request
     * @return the created or reused job ID
     */
    public String startJob(final RecursiveJobRequest request) {
        return createJob(request, true, null, null);
    }

    /**
     * Returns the current status of a job.
     *
     * @param jobId the job identifier
     * @return the current job status snapshot
     */
    public RecursiveJobStatusResponse getJobStatus(final String jobId) {
        return repository.findById(jobId)
                         .map(RecursiveResponseMapper::toStatusResponse)
                         .orElseThrow(() -> new RecursiveJobNotFoundException("Job not found: " + jobId));
    }

    /**
     * Returns all known jobs.
     *
     * @return all persisted recursive jobs visible to this IRS instance
     */
    public List<RecursiveJobStatusResponse> getAllJobs() {
        return repository.findAll().stream().map(RecursiveResponseMapper::toStatusResponse).toList();
    }

    /**
     * Handles incoming REQUEST or RESPONSE notifications from partners.
     *
     * @param message the partner notification to process
     * @return true when the notification was accepted, false when a correlatable branch was rejected
     */
    public boolean handleNotification(final RecursiveNotificationMessage message) {
        log.info("Notification: type={}, messageId={}, from={}", message.getContent().getType(),
                RecursiveLogValue.of(message.getHeader().getMessageId()),
                RecursiveLogValue.of(message.getHeader().getSenderBpnl()));

        return message.getContent().getType() == RecursiveNotificationType.REQUEST
                ? handleRequest(message)
                : handleResponse(message);
    }

    /**
     * Records an authenticated response whose complete notification contract is invalid.
     * The related message id and sender must still identify one expected child request.
     *
     * @param senderBpnl       authenticated sender of the invalid response
     * @param relatedMessageId request message referenced by the invalid response
     * @return true when the response belongs to a known child branch
     */
    public boolean rejectInvalidCorrelatedResponse(final String senderBpnl, final String relatedMessageId) {
        final Optional<CorrelatedChildResponse> correlated = correlateToChildResponse(relatedMessageId, senderBpnl);
        if (correlated.isEmpty()) {
            return false;
        }
        recordInvalidChildResponse(correlated.get());
        return true;
    }

    /**
     * Completes non-terminal jobs whose job or child response deadline expired.
     *
     * @return number of jobs completed because a deadline expired
     */
    public int processExpiredJobs() {
        return jobExpiry.processExpiredJobs();
    }

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
     *   <li>Jobs past their deadline are left to the timeout sweeper.</li>
     * </ul>
     *
     * @return number of jobs whose processing was resumed
     */
    public int recoverOpenJobs() {
        int resumed = 0;
        for (final RecursiveJobState state : repository.findAll()) {
            try {
                if (resumeJob(state)) {
                    resumed++;
                }
            } catch (final RuntimeException e) {
                log.warn("Could not resume recursive job {} after restart: causeType={}",
                        RecursiveLogValue.of(state.getJobId()), e.getClass().getName());
            }
        }
        return resumed;
    }

    private boolean resumeJob(final RecursiveJobState state) {
        if (RecursiveJobRepository.isTerminal(state)
                || (state.getDeadline() != null && now().isAfter(state.getDeadline()))) {
            return false;
        }
        if (state.getUseCase() == null
                || state.getBomLifecycle() == null
                || state.getAspects() == null
                || state.getUseCase().selectAspectIds(state.getBomLifecycle(), state.getAspects()).isEmpty()) {
            markAcceptedJobFailed(state, new IllegalArgumentException("Invalid recursive use-case selection"));
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
                    RecursiveLogValue.of(state.getJobId()));
            markAcceptedJobFailed(state, e);
            return true;
        }
        log.info("Resuming recursive job {} in phase {} after restart", RecursiveLogValue.of(state.getJobId()),
                state.getState());
        recursiveJobExecutor.execute(() -> processAcceptedJob(grant, state));
        return true;
    }

    private boolean resendUnansweredChildRequests(final RecursiveJobState state) {
        // Past the child response deadline the job belongs to the timeout sweeper - a re-send would distort its result.
        if (state.getChildResponseDeadline() != null && now().isAfter(state.getChildResponseDeadline())) {
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
                RecursiveLogValue.of(state.getJobId()), unanswered.size());
        recursiveJobExecutor.execute(() -> sendChildRequests(state, unanswered));
        return true;
    }

    private String createJob(final RecursiveJobRequest request, final boolean isRootJob,
            final ZonedDateTime inheritedDeadline, final String inheritedMessageId) {
        final PreparedRecursiveJob prepared = requestFactory.prepare(request, isRootJob, inheritedDeadline,
                inheritedMessageId);
        log.info("Creating {} job: openingId={}, useCase={}, globalAssetId={}, bomLifecycle={}, aspects={}",
                isRootJob ? "ROOT" : "CHILD", RecursiveLogValue.of(prepared.openingId()),
                RecursiveLogValue.of(prepared.useCase().name()), RecursiveLogValue.of(prepared.globalAssetId()),
                prepared.bomLifecycle(), RecursiveLogValue.of(prepared.aspects().toString()));

        final Optional<String> existing = isRootJob
                ? Optional.empty()
                : repository.findJobIdByIncomingRequestMessageId(prepared.messageId());
        if (existing.isPresent()) {
            log.warn("Duplicate messageId={} -> returning existing jobId={}",
                    RecursiveLogValue.of(prepared.messageId()), RecursiveLogValue.of(existing.get()));
            // Duplicate REQUEST on a finished job -> resend the terminal response, the parent may have restarted.
            repository.findById(existing.get())
                      .filter(state -> !isRootJob && RecursiveJobRepository.isTerminal(state))
                      .ifPresent(state -> recursiveJobExecutor.execute(() -> sendParentResponseQuietly(state)));
            return existing.get();
        }

        final RecursiveChainOpeningGrant grant = grantService.getActiveGrant(
                prepared.openingId(), prepared.useCase(), prepared.requesterBpnl(), prepared.globalAssetId());

        final String jobId = UUID.randomUUID().toString();
        final RecursiveJobState state = prepared.toState(jobId);
        repository.saveNew(state);
        if (!isRootJob) {
            repository.registerIncomingRequestMessageId(prepared.messageId(), jobId);
        }

        log.info("Job accepted: jobId={}, phase={}", RecursiveLogValue.of(jobId), state.getState());
        try {
            recursiveJobExecutor.execute(() -> processAcceptedJob(grant, state));
        } catch (final RuntimeException e) {
            markAcceptedJobFailed(state, e);
        }
        return jobId;
    }

    private void processAcceptedJob(final RecursiveChainOpeningGrant grant, final RecursiveJobState acceptedState) {
        try {
            final RecursiveTraversalService.TraversalResult traversal = traversalService.resolve(
                    acceptedState.getGlobalAssetId(), acceptedState.getReceiverBpnl(),
                    acceptedState.getBomLifecycle());
            final List<RecursiveBomChild> bomChildren = traversal.bomChildren();
            final RecursiveChildItem localNode = collectLocalNode(acceptedState, traversal);

            final Set<String> grantedChildPartners = selectGrantedChildPartners(bomChildren, grant);
            final List<RecursiveChildBranch> childBranches =
                    buildChildBranches(bomChildren, grantedChildPartners, acceptedState.getAspects());
            final List<RecursiveChildBranch> sendableChildBranches = sendableChildBranches(childBranches);

            final RecursiveJobPhase phase = sendableChildBranches.isEmpty()
                    ? RecursiveJobPhase.COMPLETED
                    : RecursiveJobPhase.AWAITING_CHILDREN;

            RecursiveJobState processedState = acceptedState.toBuilder()
                    .lastModifiedOn(now())
                    .state(phase)
                    .bomChildren(bomChildren)
                    .childBranches(childBranches)
                    .localNode(localNode)
                    .build();

            if (phase == RecursiveJobPhase.COMPLETED) {
                final RecursiveJobResult localResult = resultAggregator.aggregate(processedState, List.of(), null);
                processedState = processedState.toBuilder().result(localResult).build();
            }
            final RecursiveJobState targetState = processedState;

            // The deadline sweeper may have failed the job while we resolved the BOM.
            final Optional<RecursiveJobState> saved = repository.updateIfNotTerminal(
                    acceptedState.getJobId(), acceptedState, current -> targetState);
            if (saved.isEmpty()) {
                log.warn("Recursive job {} reached a terminal state during traversal - keeping it",
                        RecursiveLogValue.of(acceptedState.getJobId()));
                return;
            }

            log.info("Job processing started: jobId={}, phase={}, grantedChildPartners={}",
                    RecursiveLogValue.of(targetState.getJobId()), phase,
                    RecursiveLogValue.of(grantedChildPartners.toString()));

            if (!sendableChildBranches.isEmpty()) {
                sendChildRequests(targetState, sendableChildBranches);
            }
            if (phase == RecursiveJobPhase.COMPLETED) {
                sendParentResponseQuietly(targetState);
            }
        } catch (final Exception e) {
            markAcceptedJobFailed(acceptedState, e);
        }
    }

    private RecursiveChildItem collectLocalNode(final RecursiveJobState state,
            final RecursiveTraversalService.TraversalResult traversal) {
        if (state.isRootJob()) {
            return null;
        }
        return submodelCollector.collect(state.getGlobalAssetId(), state.getReceiverBpnl(), state.getAspects(),
                traversal.shellDescriptor());
    }

    private boolean handleRequest(final RecursiveNotificationMessage msg) {
        final RecursiveNotificationMessage.Header hdr = msg.getHeader();
        final RecursiveNotificationMessage.Content cnt = msg.getContent();
        if (cnt == null) {
            throw new RecursiveNotificationValidationException("Notification content is required for requests");
        }
        if (!Objects.equals(hdr.getReceiverBpnl(), localBpnl())) {
            throw new RecursiveNotificationValidationException(
                    "Recursive request receiver does not match this IRS instance.");
        }
        log.info("REQUEST from={} globalAssetId={}", RecursiveLogValue.of(hdr.getSenderBpnl()),
                RecursiveLogValue.of(cnt.getGlobalAssetId()));
        final ZonedDateTime expectedResponseBy =
                RecursiveRequestFactory.parseExpectedResponseBy(hdr.getExpectedResponseBy());

        try {
            createJob(RecursiveJobRequest.builder()
                                         .openingId(cnt.getOpeningId())
                                         .useCase(cnt.getUseCase())
                                         .globalAssetId(cnt.getGlobalAssetId())
                                         .bomLifecycle(cnt.getBomLifecycle())
                                         .aspects(cnt.getAspects())
                                         .requesterBpn(hdr.getSenderBpnl())
                                         .build(), false, expectedResponseBy,
                    hdr.getMessageId());
        } catch (final RecursiveChainOpeningGrantInactiveException e) {
            log.warn("Grant rejected for incoming recursive request");
            sendRejectionResponse(msg, RecursiveTombstones.chain(cnt.getAspects(),
                    RecursiveTombstoneReason.CHAIN_OPENING_REJECTED,
                    "The recursive chain opening grant was rejected."));
            return false;
        } catch (final IllegalArgumentException e) {
            log.warn("Incoming recursive request rejected");
            sendRejectionResponse(msg, RecursiveTombstones.chain(cnt.getAspects(),
                    RecursiveTombstoneReason.CHILD_BRANCH_FAILED,
                    "The recursive partner request was invalid."));
            return false;
        }
        return true;
    }

    private boolean handleResponse(final RecursiveNotificationMessage msg) {
        final RecursiveNotificationMessage.Header hdr = msg.getHeader();
        final RecursiveNotificationMessage.Content cnt = msg.getContent();
        log.info("RESPONSE from={} relatedMessageId={} status={}", RecursiveLogValue.of(hdr.getSenderBpnl()),
                RecursiveLogValue.of(hdr.getRelatedMessageId()), cnt.getStatus());

        final Optional<CorrelatedChildResponse> correlated = correlateToChildResponse(msg);
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
                    RecursiveLogValue.of(correlatedState.getJobId()));
            return false;
        }

        final RecursiveJobState state = updated.get();
        log.info("Job {} -> {} ({}/{} responses)", RecursiveLogValue.of(state.getJobId()), state.getState(),
                answeredChildBranches(state), state.expectedChildResponseCount());

        if (RecursiveJobRepository.isTerminal(state)) {
            sendParentResponseQuietly(state);
        }
        return true;
    }

    private void recordInvalidChildResponse(final CorrelatedChildResponse correlated) {
        final RecursiveJobState state = correlated.state();
        final String childRequestMessageId = correlated.childBranch().getMessageId();
        final Optional<RecursiveJobState> updated = repository.updateIfNotTerminal(state.getJobId(), state,
                current -> applyChildResponse(current, childRequestMessageId, RecursiveResponseStatus.FAILED,
                        invalidChildResponseResult(current)));

        log.warn("Rejected invalid recursive response for jobId={}, childRequest={}",
                RecursiveLogValue.of(state.getJobId()), RecursiveLogValue.of(childRequestMessageId));
        updated.filter(RecursiveJobRepository::isTerminal).ifPresent(this::sendParentResponseQuietly);
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
                    return changed ? applyChildBranches(current, branches) : null;
                });

        updated.filter(RecursiveJobRepository::isTerminal).ifPresent(this::sendParentResponseQuietly);
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

    /**
     * Applies a child response to the job and, when the job becomes terminal, attaches the
     * aggregated result.
     */
    private RecursiveJobState applyChildResponse(final RecursiveJobState current, final String childRequestMessageId,
            final RecursiveResponseStatus status, final RecursiveJobResult payload) {
        final List<RecursiveChildBranch> branches = new ArrayList<>();
        boolean changed = false;
        for (final RecursiveChildBranch childBranch : current.getChildBranches()) {
            if (!Objects.equals(childBranch.getMessageId(), childRequestMessageId)) {
                branches.add(childBranch);
            } else if (childBranch.getStatus() != null) {
                return null;
            } else {
                branches.add(childBranch.toBuilder()
                        .status(status)
                        .responsePayload(payload)
                        .build());
                changed = true;
            }
        }
        return changed ? applyChildBranches(current, branches) : null;
    }

    private RecursiveJobState applyChildBranches(final RecursiveJobState current,
            final List<RecursiveChildBranch> branches) {
        final RecursiveJobPhase nextPhase = answeredChildBranches(branches) < current.expectedChildResponseCount()
                ? RecursiveJobPhase.AWAITING_CHILDREN
                : RecursiveJobPhase.COMPLETED;
        RecursiveJobState updated = current.toBuilder()
                .lastModifiedOn(now())
                .state(nextPhase)
                .childBranches(List.copyOf(branches))
                .build();

        if (RecursiveJobRepository.isTerminal(updated)) {
            final RecursiveJobResult aggregated = resultAggregator.aggregate(updated, List.of(), null);
            updated = updated.toBuilder().result(aggregated).build();
        }
        return updated;
    }

    private int answeredChildBranches(final RecursiveJobState state) {
        return answeredChildBranches(state.getChildBranches());
    }

    private int answeredChildBranches(final List<RecursiveChildBranch> branches) {
        return (int) branches.stream()
                .filter(childBranch -> childBranch.getStatus() != null)
                .count();
    }

    private void sendChildRequests(final RecursiveJobState state, final List<RecursiveChildBranch> childBranches) {
        final Map<String, RuntimeException> deliveryFailuresByChildRequestMessageId = new LinkedHashMap<>();
        for (final RecursiveChildBranch childBranch : childBranches) {
            if (!childBranch.isSendNotification()) {
                continue;
            }
            final RecursiveNotificationMessage notification =
                    RecursiveNotificationFactory.childRequest(state, childBranch, now());

            repository.registerChildRequestMessageId(childBranch.getMessageId(), state.getJobId());

            log.info("-> CHILD_REQUEST to {} childAsset={}",
                    RecursiveLogValue.of(childBranch.getPartnerBpnl()),
                    RecursiveLogValue.of(childBranch.getChildGlobalAssetId()));
            try {
                notificationSender.sendRequest(childBranch.getPartnerBpnl(), notification);
            } catch (final RuntimeException e) {
                deliveryFailuresByChildRequestMessageId.put(childBranch.getMessageId(), e);
                log.warn("Could not send recursive child request for jobId={}, childRequest={}: causeType={}",
                        RecursiveLogValue.of(state.getJobId()),
                        RecursiveLogValue.of(childBranch.getMessageId()),
                        e.getClass().getName());
            }
        }
        if (!deliveryFailuresByChildRequestMessageId.isEmpty()) {
            recordChildDeliveryFailures(state, deliveryFailuresByChildRequestMessageId);
        }
    }

    private void sendParentResponse(final RecursiveJobState state) {
        final List<String> responseAspects = RecursiveResponseMapper.selectedAspectIds(state);
        final RecursiveJobResult externalResult = RecursiveResponseMapper.toExternalResult(state.getResult(),
                state.getUseCase(), state.getBomLifecycle(), responseAspects);
        final RecursiveResponseStatus status = state.getState() == RecursiveJobPhase.COMPLETED
                ? RecursiveResponseStatus.COMPLETED : RecursiveResponseStatus.FAILED;

        final RecursiveNotificationMessage full = RecursiveNotificationFactory.parentResponse(
                state, localBpnl(), status, externalResult, responseAspects, now());

        log.info("= PARENT_RESPONSE ({}) to {} for jobId={}", status,
                RecursiveLogValue.of(state.getRequesterBpnl()), RecursiveLogValue.of(state.getJobId()));
        notificationSender.sendResponse(state.getRequesterBpnl(), full);
    }

    /** Sends the terminal result to the parent; root jobs have no parent and send nothing. */
    private void sendParentResponseQuietly(final RecursiveJobState state) {
        if (state.isRootJob()) {
            return;
        }
        try {
            sendParentResponse(state);
        } catch (final RuntimeException e) {
            log.warn("Could not send recursive response to parent for job {}: causeType={}",
                    RecursiveLogValue.of(state.getJobId()), e.getClass().getName());
        }
    }

    private void sendRejectionResponse(final RecursiveNotificationMessage request,
            final RecursiveTombstone rejection) {
        final RecursiveNotificationMessage.Header requestHeader = request.getHeader();
        final RecursiveNotificationMessage response = RecursiveNotificationFactory.rejectionResponse(
                request, rejection, localBpnl(), now());
        notificationSender.sendResponse(requestHeader.getSenderBpnl(), response);
    }

    private void markAcceptedJobFailed(final RecursiveJobState acceptedState, final Exception exception) {
        final RecursiveTombstoneReason reason = RecursiveFailureReasonMapper.failureReason(exception);
        log.warn("Recursive job {} failed after acceptance: reason={} causeType={}",
                RecursiveLogValue.of(acceptedState.getJobId()), RecursiveLogValue.of(reason.name()),
                exception.getClass().getName());
        final String detail = RecursiveFailureDetails.anonymizedDetail(exception);
        final RecursiveTombstone failureTombstone = RecursiveTombstones.chain(
                acceptedState.getAspects(), reason, detail);

        final Optional<RecursiveJobState> failed = repository.updateIfNotTerminal(acceptedState.getJobId(),
                acceptedState, current -> current.toBuilder()
                        .lastModifiedOn(now())
                        .state(RecursiveJobPhase.FAILED)
                        .failureReason(reason)
                        .result(resultAggregator.aggregate(current, List.of(failureTombstone),
                                RecursiveResultStatus.FAILED))
                        .build());

        failed.ifPresent(this::sendParentResponseQuietly);
    }

    private Optional<CorrelatedChildResponse> correlateToChildResponse(final RecursiveNotificationMessage message) {
        if (message == null || message.getHeader() == null) {
            return Optional.empty();
        }
        return correlateToChildResponse(message.getHeader().getRelatedMessageId(),
                message.getHeader().getSenderBpnl());
    }

    private Optional<CorrelatedChildResponse> correlateToChildResponse(final String relatedMessageId,
            final String senderBpnl) {
        if (relatedMessageId == null || senderBpnl == null) {
            return Optional.empty();
        }
        return repository.findJobIdByChildRequestMessageId(relatedMessageId)
                         .flatMap(repository::findById)
                         .flatMap(state -> findChildBranch(state, relatedMessageId, senderBpnl)
                                 .map(childBranch -> new CorrelatedChildResponse(state, childBranch)));
    }

    private boolean matchesExpectedJob(final RecursiveNotificationMessage message,
            final RecursiveJobState state) {
        final RecursiveNotificationMessage.Header header = message.getHeader();
        final RecursiveNotificationMessage.Content content = message.getContent();
        return Objects.equals(header.getReceiverBpnl(), localBpnl())
                && Objects.equals(content.getOpeningId(), state.getOpeningId())
                && Objects.equals(content.getUseCase(), state.getUseCase())
                && content.getBomLifecycle() == state.getBomLifecycle()
                && containsSameAspects(content.getAspects(), state.getAspects());
    }

    private boolean containsSameAspects(final List<String> actual, final List<String> expected) {
        return actual != null && expected != null && new HashSet<>(actual).equals(new HashSet<>(expected));
    }

    private Set<String> selectGrantedChildPartners(final List<RecursiveBomChild> bomChildren,
            final RecursiveChainOpeningGrant grant) {
        final Set<String> bomPartners = traversalService.extractPartnerBpnls(bomChildren);
        return grantService.filterAllowedPartners(bomPartners, grant);
    }

    private List<RecursiveChildBranch> buildChildBranches(final List<RecursiveBomChild> bomChildren,
            final Set<String> allowedPartners, final List<String> requestedAspects) {
        return bomChildren.stream()
                          .filter(child -> allowedPartners.contains(child.partnerBpnl()))
                          .map(child -> buildChildBranch(child, requestedAspects))
                          .toList();
    }

    private RecursiveChildBranch buildChildBranch(final RecursiveBomChild child,
            final List<String> requestedAspects) {
        final RecursiveChildBranch.RecursiveChildBranchBuilder builder = RecursiveChildBranch.builder()
                .messageId(UUID.randomUUID().toString())
                .partnerBpnl(child.partnerBpnl())
                .quantity(child.quantity());
        try {
            return builder.childGlobalAssetId(RecursiveGlobalAssetId.canonicalize(child.childGlobalAssetId()))
                          .build();
        } catch (final IllegalArgumentException exception) {
            return builder.sendNotification(false)
                          .status(RecursiveResponseStatus.FAILED)
                          .tombstones(List.of(RecursiveTombstones.childBranch(requestedAspects,
                                  RecursiveTombstoneReason.BOM_CHILD_GLOBAL_ASSET_ID_INVALID,
                                  "The BOM relationship contains an invalid child global asset id.")))
                          .build();
        }
    }

    private List<RecursiveChildBranch> sendableChildBranches(final List<RecursiveChildBranch> childBranches) {
        return childBranches.stream()
                            .filter(RecursiveChildBranch::isSendNotification)
                            .filter(childBranch -> childBranch.getStatus() == null)
                            .toList();
    }

    private Optional<RecursiveChildBranch> findChildBranch(final RecursiveJobState state,
            final String relatedMessageId, final String senderBpnl) {
        return state.getChildBranches().stream()
                    .filter(childBranch -> Objects.equals(childBranch.getMessageId(), relatedMessageId))
                    .filter(childBranch -> Objects.equals(childBranch.getPartnerBpnl(), senderBpnl))
                    .findFirst();
    }

    private record CorrelatedChildResponse(RecursiveJobState state,
                                           RecursiveChildBranch childBranch) {
    }

    private ZonedDateTime now() {
        return ZonedDateTime.now(clock);
    }

    private String localBpnl() {
        return recursiveProperties.getLocalBpnl();
    }
}
