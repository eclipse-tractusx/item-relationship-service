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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

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
 * in {@link RecursiveResultAggregator}, child requests in {@link RecursiveChildRequestDispatcher},
 * child responses in {@link RecursiveChildResponseProcessor}, responses to the parent in
 * {@link RecursiveParentResponder}, deadline/timeout termination in
 * {@link RecursiveJobExpiry}, restart recovery in {@link RecursiveJobRecovery} and all state
 * mutations are serialized per job through {@link RecursiveJobRepository}.
 * Notifications to partners are always sent outside the job lock.</p>
 */
@Slf4j
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.TooManyMethods" })
public class RecursiveJobService {

    private final RecursiveChainOpeningGrantService grantService;
    private final RecursiveTraversalService traversalService;
    private final RecursiveJobRepository repository;
    private final RecursiveRequestFactory requestFactory;
    private final RecursiveParentResponder parentResponder;
    private final RecursiveJobExpiry jobExpiry;
    private final RecursiveJobRecovery jobRecovery;
    private final RecursiveChildResponseProcessor childResponseProcessor;
    private final RecursiveChildRequestDispatcher childRequestDispatcher;

    private final Executor recursiveJobExecutor;
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
        Objects.requireNonNull(notificationSender, "notificationSender must not be null");
        this.submodelCollector = Objects.requireNonNull(submodelCollector, "submodelCollector must not be null");
        this.recursiveProperties = Objects.requireNonNull(recursiveProperties,
                "recursiveProperties must not be null");
        this.recursiveJobExecutor = Objects.requireNonNull(recursiveJobExecutor,
                "recursiveJobExecutor must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.requestFactory = new RecursiveRequestFactory(recursiveProperties, clock);
        this.parentResponder = new RecursiveParentResponder(notificationSender, this::now, this::localBpnl);
        this.jobExpiry = new RecursiveJobExpiry(repository, resultAggregator, this::now, parentResponder);
        this.childResponseProcessor = new RecursiveChildResponseProcessor(repository, resultAggregator, this::now,
                this::localBpnl, parentResponder);
        this.childRequestDispatcher = new RecursiveChildRequestDispatcher(repository, notificationSender, this::now,
                childResponseProcessor::applyChildBranches, parentResponder);
        this.jobRecovery = new RecursiveJobRecovery(repository, grantService, recursiveJobExecutor, this::now,
                this::processAcceptedJob, childRequestDispatcher, this::markAcceptedJobFailed);
    }

    /**
     * Starts a ROOT job (called via POST /irs/recursive/jobs).
     *
     * @param request the root recursive job request
     * @return the created or reused job ID
     */
    public UUID startJob(final RecursiveJobRequest request) {
        return createJob(request, true, null, null);
    }

    /**
     * Returns the current status of a job.
     *
     * @param jobId the job identifier
     * @return the current job status snapshot
     */
    public RecursiveJobStatusResponse getJobStatus(final UUID jobId) {
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
                : childResponseProcessor.handleResponse(message);
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
        return childResponseProcessor.rejectInvalidCorrelatedResponse(senderBpnl, relatedMessageId);
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
     * Resumes jobs that a pod restart left in a non-terminal phase.
     *
     * @return number of jobs whose processing was resumed
     */
    public int recoverOpenJobs() {
        return jobRecovery.recoverOpenJobs();
    }

    private UUID createJob(final RecursiveJobRequest request, final boolean isRootJob,
            final ZonedDateTime inheritedDeadline, final String inheritedMessageId) {
        final PreparedRecursiveJob prepared = requestFactory.prepare(request, isRootJob, inheritedDeadline,
                inheritedMessageId);
        log.info("Creating {} job: openingId={}, useCase={}, globalAssetId={}, bomLifecycle={}, aspects={}",
                isRootJob ? "ROOT" : "CHILD", RecursiveLogValue.of(prepared.openingId()),
                RecursiveLogValue.of(prepared.useCase().name()), RecursiveLogValue.of(prepared.globalAssetId()),
                prepared.bomLifecycle(), RecursiveLogValue.of(prepared.aspects().toString()));

        final Optional<UUID> existing = isRootJob
                ? Optional.empty()
                : repository.findJobIdByIncomingRequestMessageId(prepared.messageId());
        if (existing.isPresent()) {
            log.warn("Duplicate messageId={} -> returning existing jobId={}",
                    RecursiveLogValue.of(prepared.messageId()), RecursiveLogValue.of(existing.get().toString()));
            // Duplicate REQUEST on a finished job -> resend the terminal response, the parent may have restarted.
            repository.findById(existing.get())
                      .filter(state -> !isRootJob && RecursiveJobRepository.isTerminal(state))
                      .ifPresent(state -> recursiveJobExecutor.execute(
                              () -> parentResponder.sendParentResponseQuietly(state)));
            return existing.get();
        }

        final RecursiveChainOpeningGrant grant = grantService.getActiveGrant(
                prepared.openingId(), prepared.useCase(), prepared.requesterBpnl(), prepared.globalAssetId());

        final UUID jobId = UUID.randomUUID();
        final RecursiveJobState state = prepared.toState(jobId);
        repository.saveNew(state);
        if (!isRootJob) {
            repository.registerIncomingRequestMessageId(prepared.messageId(), jobId);
        }

        log.info("Job accepted: jobId={}, phase={}", RecursiveLogValue.of(jobId.toString()), state.getState());
        try {
            recursiveJobExecutor.execute(() -> processAcceptedJob(grant, state));
        } catch (final RejectedExecutionException e) {
            markAcceptedJobFailed(state, e);
        }
        return jobId;
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
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
                        RecursiveLogValue.of(acceptedState.getJobId().toString()));
                return;
            }

            log.info("Job processing started: jobId={}, phase={}, grantedChildPartners={}",
                    RecursiveLogValue.of(targetState.getJobId().toString()), phase,
                    RecursiveLogValue.of(grantedChildPartners.toString()));

            if (!sendableChildBranches.isEmpty()) {
                childRequestDispatcher.sendChildRequests(targetState, sendableChildBranches);
            }
            if (phase == RecursiveJobPhase.COMPLETED) {
                parentResponder.sendParentResponseQuietly(targetState);
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
        final RecursiveNotificationMessage.Content cnt = msg.getContent();
        if (cnt == null) {
            throw new RecursiveNotificationValidationException("Notification content is required for requests");
        }
        final RecursiveNotificationMessage.Header hdr = msg.getHeader();
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
            parentResponder.sendRejectionResponseQuietly(msg, RecursiveTombstones.chain(cnt.getAspects(),
                    RecursiveTombstoneReason.CHAIN_OPENING_REJECTED,
                    "The recursive chain opening grant was rejected."));
            return false;
        } catch (final IllegalArgumentException e) {
            log.warn("Incoming recursive request rejected");
            parentResponder.sendRejectionResponseQuietly(msg, RecursiveTombstones.chain(cnt.getAspects(),
                    RecursiveTombstoneReason.CHILD_BRANCH_FAILED,
                    "The recursive partner request was invalid."));
            return false;
        }
        return true;
    }

    private void markAcceptedJobFailed(final RecursiveJobState acceptedState, final Exception exception) {
        final RecursiveTombstoneReason reason = RecursiveFailureReasonMapper.failureReason(exception);
        log.warn("Recursive job {} failed after acceptance: reason={} causeType={}",
                RecursiveLogValue.of(acceptedState.getJobId().toString()), RecursiveLogValue.of(reason.name()),
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

        failed.ifPresent(parentResponder::sendParentResponseQuietly);
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

    private ZonedDateTime now() {
        return ZonedDateTime.now(clock);
    }

    private String localBpnl() {
        return recursiveProperties.getLocalBpnl();
    }
}
