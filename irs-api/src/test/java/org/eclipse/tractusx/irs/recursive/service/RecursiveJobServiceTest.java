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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyPermissionException;
import org.eclipse.tractusx.irs.recursive.config.RecursiveProperties;
import org.eclipse.tractusx.irs.recursive.model.ItemUnitEnumeration;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspectItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveBomChild;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrantKey;
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
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneScope;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneType;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.eclipse.tractusx.irs.recursive.store.InMemoryRecursiveChainOpeningGrantStore;
import org.eclipse.tractusx.irs.recursive.store.RecursiveChainOpeningGrantStore;
import org.eclipse.tractusx.irs.recursive.store.RecursiveJobStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecursiveJobServiceTest {

    private static final String RAW_ITEM_STOCK_ASPECT = "urn:samm:io.catenax.item_stock:2.0.0#ItemStock";
    private static final String ITEM_STOCK_ANONYMIZED = RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId();

    private RecursiveJobService jobService;
    private RecursiveChainOpeningGrantStore grantStore;
    private Map<UUID, RecursiveJobState> jobs;
    private Map<String, UUID> messageIds;
    private final Instant clockStart = Instant.now();
    private RecursiveProperties recursiveProperties;
    private RecursiveSubmodelCollector submodelCollector;
    private RecursiveNotificationSender notificationSender;
    private Executor recursiveJobExecutor;
    private final AdjustableClock clock = new AdjustableClock(Clock.systemUTC());
    private final Executor forwardingExecutor = command -> recursiveJobExecutor.execute(command);
    private final RecursiveNotificationSender forwardingNotificationSender = new RecursiveNotificationSender() {
        @Override
        public void sendRequest(final String receiverBpnl, final RecursiveNotificationMessage message) {
            notificationSender.sendRequest(receiverBpnl, message);
        }

        @Override
        public void sendResponse(final String receiverBpnl, final RecursiveNotificationMessage message) {
            notificationSender.sendResponse(receiverBpnl, message);
        }
    };

    @BeforeEach
    void setUp() {
        jobs = new ConcurrentHashMap<>();
        messageIds = new ConcurrentHashMap<>();
        grantStore = new InMemoryRecursiveChainOpeningGrantStore();
        recursiveProperties = new RecursiveProperties();
        recursiveProperties.setLocalBpnl("BPNL0000ATLS0001");
        submodelCollector = emptySubmodelCollector();
        notificationSender = noOpNotificationSender();
        recursiveJobExecutor = Runnable::run;
        clock.set(Clock.systemUTC());
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of());
    }

    @Test
    void shouldCreateJobWhenGrantExists() {
        final RecursiveJobRequest request = validRequest();
        final RecursiveChainOpeningGrant grant = validGrant();
        grantStore.store(grant);

        final UUID jobId = jobService.startJob(request);

        assertThat(jobId).isNotNull();
        assertThat(grantStore.find(RecursiveChainOpeningGrantKey.of(grant))).contains(grant);
    }

    @Test
    void shouldReturnJobIdAfterTechnicalAcceptanceBeforeTraversalRuns() {
        final List<Runnable> queuedTasks = new ArrayList<>();
        final AtomicInteger resolverCalls = new AtomicInteger();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> {
            resolverCalls.incrementAndGet();
            return List.of();
        });
        recursiveJobExecutor = queuedTasks::add;
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        assertThat(jobId).isNotNull();
        assertThat(queuedTasks).hasSize(1);
        assertThat(resolverCalls).hasValue(0);
        assertThat(jobService.getJobStatus(jobId).getJob().getState()).isEqualTo(JobState.RUNNING);
    }

    @Test
    void shouldFailAcceptedJobWhenExecutorRejectsProcessing() {
        recursiveJobExecutor = command -> {
            throw new RejectedExecutionException("executor unavailable");
        };
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(status.getJob().getState()).isEqualTo(JobState.ERROR);
        assertThat(status.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.FAILED);
        assertThat(allTombstones(status))
                .anySatisfy(tombstone ->
                        assertThat(tombstone.getReason()).isEqualTo(RecursiveTombstoneReason.CHILD_BRANCH_FAILED));
    }

    @Test
    void shouldRejectJobWhenNoGrantExists() {
        assertThatThrownBy(() -> jobService.startJob(validRequest()))
                .isInstanceOf(RecursiveChainOpeningGrantInactiveException.class);
    }

    @Test
    void shouldRejectRawItemStockAspectForPurisRecursiveUseCase() {
        grantStore.store(validGrant());

        assertThatThrownBy(() -> jobService.startJob(validRequest().toBuilder()
                .aspects(List.of(RAW_ITEM_STOCK_ASPECT))
                .build()))
                .isInstanceOf(RecursiveUnsupportedAspectException.class)
                .hasMessageContaining(RAW_ITEM_STOCK_ASPECT);
    }

    @Test
    void shouldNameUnknownAndSupportedAspectsWhenRejectingRootRequest() {
        grantStore.store(validGrant());
        final String typo = "urn:samm:io.catenax.item_stock_anonymizd:1.0.0#ItemStockAnonymizd";

        assertThatThrownBy(() -> jobService.startJob(validRequest().toBuilder()
                .aspects(List.of(ITEM_STOCK_ANONYMIZED, typo))
                .build()))
                .isInstanceOfSatisfying(RecursiveUnsupportedAspectException.class, exception -> {
                    assertThat(exception.getUnknownAspects()).containsExactly(typo);
                    assertThat(exception.getSupportedAspects()).contains(ITEM_STOCK_ANONYMIZED);
                });
    }

    @Test
    void shouldReturnJobStatus() {
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);

        assertThat(status.getJob().getId()).isEqualTo(jobId);
        assertThat(status.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(status.getJob().getParameter().getOpeningId()).isEqualTo("opening-42");
        assertThat(status.getJob().getParameter().getUseCase())
                .isEqualTo(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE);
        assertThat(status.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.COMPLETE);
    }

    @Test
    void shouldNeverCollectRootAspectsForPuris() {
        final AtomicInteger collectorCalls = new AtomicInteger();
        submodelCollector = new RecursiveSubmodelCollector(null, null,
                new ObjectMapper().findAndRegisterModules()) {
            @Override
            RecursiveChildItem collect(final String globalAssetId, final String localBpnl,
                    final List<String> requestedAspects,
                    final AssetAdministrationShellDescriptor resolvedShell) {
                collectorCalls.incrementAndGet();
                return collectedNode(globalAssetId);
            }
        };
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of());
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(collectorCalls).hasValue(0);
        assertThat(status.getResult().getChildItems()).isEmpty();
        assertThat(collectedAssetIds(status, ITEM_STOCK_ANONYMIZED)).isEmpty();
    }

    @Test
    void shouldUseConfiguredDefaultTtlWhenRootRequestHasNoTtl() {
        recursiveProperties.getTimeout().setDefaultJobTtl(Duration.ofMinutes(5));
        recursiveProperties.getTimeout().setMaxJobTtl(Duration.ofHours(1));
        clock.set(Clock.fixed(clockStart, ZoneOffset.UTC));
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest().toBuilder()
                .ttl(null)
                .build());

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(status.getJob().getParameter().getDeadline())
                .isEqualTo(ZonedDateTime.ofInstant(clockStart.plus(Duration.ofMinutes(5)), ZoneOffset.UTC));
    }

    @Test
    void shouldThrowWhenJobNotFound() {
        assertThatThrownBy(() -> jobService.getJobStatus(namedJobId("nonexistent")))
                .isInstanceOf(RecursiveJobNotFoundException.class);
    }

    @Test
    void shouldCreateDistinctJobsForRepeatedRootRequests() {
        final RecursiveJobRequest request = validRequest();
        grantStore.store(validGrant());

        final UUID jobId1 = jobService.startJob(request);
        final UUID jobId2 = jobService.startJob(request);

        // Root jobs are no longer deduplicated by a caller-supplied messageId; each start gets a fresh id.
        assertThat(jobId1).isNotEqualTo(jobId2);
    }

    @Test
    void shouldStoreTraversalFailureAsTombstoneAfterGrantValidation() {
        final AtomicInteger attempts = new AtomicInteger();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> {
            if (attempts.getAndIncrement() == 0) {
                throw new IllegalStateException("transient traversal failure");
            }
            return List.of();
        });
        final RecursiveJobRequest request = validRequest();
        grantStore.store(validGrant());

        final UUID failedJobId = jobService.startJob(request);

        final RecursiveJobStatusResponse failedStatus = jobService.getJobStatus(failedJobId);
        assertThat(failedStatus.getJob().getState()).isEqualTo(JobState.ERROR);
        // External sanitized view: terminal reason surfaces via job.exception for FAILED jobs.
        assertThat(failedStatus.getJob().getException().getException()).isEqualTo("CHILD_BRANCH_FAILED");
        assertThat(allTombstones(failedStatus))
                .anySatisfy(tombstone -> {
                    assertThat(tombstone.getReason()).isEqualTo(RecursiveTombstoneReason.CHILD_BRANCH_FAILED);
                    assertThat(tombstone.getDetail()).isEqualTo("transient traversal failure");
                });

        final UUID retriedJobId = jobService.startJob(request);

        // A repeated root start creates a fresh job now - there is no caller messageId to deduplicate by.
        assertThat(jobService.getAllJobs()).hasSize(2);
        assertThat(retriedJobId).isNotEqualTo(failedJobId);
    }

    @Test
    void shouldStoreExternalTraversalFailureAsAnonymizedTombstone() {
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> {
            throw new RecursiveExternalCallException("DIGITAL_TWIN_REQUEST_FAILED",
                    "Digital twin registry request failed while resolving BOM relationships.",
                    new IllegalStateException("GET http://dtr.internal/shells?assetIds=urn:uuid:"
                            + "68904173-ad59-4a77-8412-3e73fcafbd8b failed for bpn=BPNL0000ATLS0001 "
                            + "assetId=atlas-bom-asset"));
        });
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(status.getJob().getState()).isEqualTo(JobState.ERROR);
        // External sanitized view: terminal reason surfaces via job.exception for FAILED jobs.
        assertThat(status.getJob().getException().getException()).isEqualTo("LOCAL_ASPECT_REQUEST_FAILED");
        assertThat(allTombstones(status))
                .anySatisfy(tombstone -> {
                    assertThat(tombstone.getReason()).isEqualTo(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED);
                    assertThat(tombstone.getDetail())
                            .contains("<url>", "<bpn>", "<assetId>")
                            .doesNotContain("http://")
                            .doesNotContain("BPNL0000ATLS0001")
                            .doesNotContain("urn:uuid")
                            .doesNotContain("atlas-bom-asset");
                });
    }

    @Test
    void shouldStoreUsagePolicyFailureWithDedicatedTombstoneReason() {
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> {
            throw new RecursiveExternalCallException("SUBMODEL_REQUEST_FAILED", "EDC policy validation failed.",
                    new UsagePolicyPermissionException(List.of(), Policy.Builder.newInstance().build(),
                            "BPNL0000ATLS0001"));
        });
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(status.getJob().getState()).isEqualTo(JobState.ERROR);
        // External sanitized view: terminal reason surfaces via job.exception for FAILED jobs.
        assertThat(status.getJob().getException().getException()).isEqualTo("LOCAL_ASPECT_REQUEST_FAILED");
        assertThat(allTombstones(status))
                .anySatisfy(tombstone -> assertThat(tombstone.getReason())
                        .isEqualTo(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED));
    }

    @Test
    void shouldListAllJobs() {
        grantStore.store(validGrant());

        jobService.startJob(RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                .requesterBpn("BPNL0000ATLS0001")
                .ttl("PT15M")
                .build());
        jobService.startJob(RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                .requesterBpn("BPNL0000ATLS0001")
                .ttl("PT15M")
                .build());

        assertThat(jobService.getAllJobs()).hasSize(2);
    }

    @Test
    void shouldAwaitChildResponsesForAllowedBomPartners() {
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(status.getJob().getState()).isEqualTo(JobState.RUNNING);
        assertChildProgress(status, 1, 0, 0);
    }

    @Test
    void shouldSendUtcTimestampsWithChildRequest() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        clock.set(Clock.fixed(clockStart, ZoneId.of("Europe/Berlin")));
        grantStore.store(validGrant());
        notificationSender = recordingSender(sentRequests);

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(sentRequests).hasSize(1);
        assertThat(sentRequests.get(0).getHeader().getSentDateTime()).isEqualTo(clockStart.toString());
        assertThat(sentRequests.get(0).getHeader().getExpectedResponseBy())
                .isEqualTo(status.getJob().getParameter().getChildResponseDeadline().toInstant().toString());
    }

    @Test
    void shouldFilterBomPartnersWhenGrantValidationIsEnabled() {
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000XXXX0001")));
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(status.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(status.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.COMPLETE);
        assertThat(allTombstones(status)).isEmpty();
    }

    @Test
    void shouldKeepGrantFilteredPartnerInvisibleInChildProgress() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:22222222-2222-2222-2222-222222222222", "BPNL0000BELF0001"),
                bomChild(
                        "urn:uuid:33333333-3333-3333-3333-333333333333", "BPNL0000CERS0001")));
        notificationSender = recordingSender(sentRequests);
        grantStore.store(validGrant().toBuilder().allowedBpnlSet(Set.of("BPNL0000BELF0001")).build());

        final UUID jobId = jobService.startJob(validRequest());
        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);

        assertThat(sentRequests).extracting(message -> message.getHeader().getReceiverBpnl())
                                .containsExactly("BPNL0000BELF0001");
        assertChildProgress(status, 1, 0, 0);
    }

    @Test
    void shouldNotModifyStoredGrantDuringJobExecution() {
        final RecursiveChainOpeningGrant grant = validGrant().toBuilder()
                .validFrom(ZonedDateTime.now().minusHours(2))
                .validTo(ZonedDateTime.now().plusHours(6))
                .createdAt(ZonedDateTime.now().minusHours(1))
                .updatedAt(ZonedDateTime.now().minusMinutes(30))
                .build();
        grantStore.store(grant);

        jobService.startJob(validRequest());

        assertThat(grantStore.find(RecursiveChainOpeningGrantKey.of(grant)))
                .contains(grant);
    }

    @Test
    void shouldAwaitChildResponsesAndCompleteWhenResponseArrives() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        notificationSender = recordingSender(sentRequests);

        final RecursiveJobRequest request = validRequest();
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(request);

        final RecursiveJobStatusResponse runningStatus = jobService.getJobStatus(jobId);
        assertThat(runningStatus.getJob().getState()).isEqualTo(JobState.RUNNING);
        assertChildProgress(runningStatus, 1, 0, 0);
        assertThat(sentRequests).hasSize(1);

        final RecursiveNotificationMessage childRequest = sentRequests.get(0);
        final RecursiveNotificationMessage response = childResponse(childRequest,
                RecursiveResponseStatus.COMPLETED,
                childResult(childRequest.getContent().getGlobalAssetId()));

        jobService.handleNotification(response);

        final RecursiveJobStatusResponse completedStatus = jobService.getJobStatus(jobId);
        assertThat(completedStatus.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertChildProgress(completedStatus, 0, 1, 0);
    }

    @Test
    void shouldCompleteCorrelatedBranchWhenChildResponseIsInvalid() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild("urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        notificationSender = recordingSender(sentRequests);
        grantStore.store(validGrant());
        final UUID jobId = jobService.startJob(validRequest());

        final String relatedMessageId = sentRequests.get(0).getHeader().getMessageId();
        assertThat(jobService.rejectInvalidCorrelatedResponse("BPNL0000BELF0001", relatedMessageId)).isTrue();

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(status.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertChildProgress(status, 0, 0, 1);
        assertThat(status.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.FAILED);
        assertThat(allTombstones(status))
                .extracting(RecursiveTombstone::getReason)
                .containsExactly(RecursiveTombstoneReason.CHILD_RESPONSE_INVALID);
    }

    @Test
    void shouldRejectCorrelatedResponseWhenRequestedAspectsDoNotMatch() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild("urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        notificationSender = recordingSender(sentRequests);
        grantStore.store(validGrant());
        final UUID jobId = jobService.startJob(validRequest());
        final RecursiveNotificationMessage request = sentRequests.get(0);

        final RecursiveNotificationMessage response = RecursiveNotificationMessage.builder()
                .header(notificationHeader(UUID.randomUUID().toString())
                        .relatedMessageId(request.getHeader().getMessageId())
                        .senderBpnl(request.getHeader().getReceiverBpnl())
                        .receiverBpnl(request.getHeader().getSenderBpnl())
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.RESPONSE)
                        .openingId(request.getContent().getOpeningId())
                        .useCase(request.getContent().getUseCase())
                        .bomLifecycle(request.getContent().getBomLifecycle())
                        .aspects(List.of(RecursiveAspect.DELIVERY_INFORMATION_ANONYMIZED.getSemanticId()))
                        .status(RecursiveResponseStatus.COMPLETED)
                        .result(childResult(request.getContent().getGlobalAssetId()))
                        .build())
                .build();

        assertThat(jobService.handleNotification(response)).isFalse();

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(status.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertChildProgress(status, 0, 0, 1);
        assertThat(allTombstones(status))
                .extracting(RecursiveTombstone::getReason)
                .containsExactly(RecursiveTombstoneReason.CHILD_RESPONSE_INVALID);
    }

    @Test
    void shouldIgnoreResponseFromUnexpectedSenderForChildRequest() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        notificationSender = recordingSender(sentRequests);
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());
        assertThat(sentRequests).hasSize(1);

        // Correct relatedMessageId, but a different (authenticated) partner answers for BELFAST's
        // child request - the response must be ignored, not applied to the job.
        final RecursiveNotificationMessage spoofed = RecursiveNotificationMessage.builder()
                .header(notificationHeader(UUID.randomUUID().toString())
                        .relatedMessageId(sentRequests.get(0).getHeader().getMessageId())
                        .senderBpnl("BPNL0000CERS0001")
                        .receiverBpnl("BPNL0000ATLS0001")
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.RESPONSE)
                        .openingId("opening-42")
                        .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                        .globalAssetId(validRequest().getGlobalAssetId())
                        .status(RecursiveResponseStatus.COMPLETED)
                        .build())
                .build();

        assertThatThrownBy(() -> jobService.handleNotification(spoofed))
                .isInstanceOf(RecursiveNotificationValidationException.class);

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(status.getJob().getState()).isEqualTo(JobState.RUNNING);
        assertChildProgress(status, 1, 0, 0);
    }

    @Test
    void shouldWaitForEveryChildAssetResponseWhenChildrenSharePartner() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001"),
                bomChild(
                        "urn:uuid:22222222-2222-2222-2222-222222222222", "BPNL0000BELF0001")));
        notificationSender = recordingSender(sentRequests);
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveJobStatusResponse runningStatus = jobService.getJobStatus(jobId);
        assertThat(sentRequests).hasSize(2);
        assertThat(sentRequests)
                .extracting(request -> request.getHeader().getReceiverBpnl())
                .containsExactly("BPNL0000BELF0001", "BPNL0000BELF0001");
        assertThat(sentRequests)
                .extracting(request -> request.getHeader().getMessageId())
                .doesNotHaveDuplicates();
        assertThat(runningStatus.getJob().getState()).isEqualTo(JobState.RUNNING);
        assertChildProgress(runningStatus, 2, 0, 0);

        jobService.handleNotification(childResponse(sentRequests.get(0), RecursiveResponseStatus.COMPLETED,
                childResult(sentRequests.get(0).getContent().getGlobalAssetId())));

        final RecursiveJobStatusResponse afterFirstResponse = jobService.getJobStatus(jobId);
        assertThat(afterFirstResponse.getJob().getState()).isEqualTo(JobState.RUNNING);
        assertChildProgress(afterFirstResponse, 1, 1, 0);

        jobService.handleNotification(childResponse(sentRequests.get(1), RecursiveResponseStatus.COMPLETED,
                childResult(sentRequests.get(1).getContent().getGlobalAssetId())));

        final RecursiveJobStatusResponse completedStatus = jobService.getJobStatus(jobId);
        assertThat(completedStatus.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertChildProgress(completedStatus, 0, 2, 0);
    }

    @Test
    void shouldKeepValidSiblingBranchWhenBomChildGlobalAssetIdIsInvalid() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        final String validChildId = "urn:uuid:22222222-2222-2222-2222-222222222222";
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild("provider-specific-child-id", "BPNL0000BELF0001"),
                bomChild(validChildId, "BPNL0000CERS0001")));
        notificationSender = recordingSender(sentRequests);
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveJobStatusResponse runningStatus = jobService.getJobStatus(jobId);
        assertThat(sentRequests).singleElement().satisfies(request -> {
            assertThat(request.getHeader().getReceiverBpnl()).isEqualTo("BPNL0000CERS0001");
            assertThat(request.getContent().getGlobalAssetId()).isEqualTo(validChildId);
        });
        assertChildProgress(runningStatus, 1, 0, 1);

        jobService.handleNotification(childResponse(sentRequests.get(0), RecursiveResponseStatus.COMPLETED,
                childResult(validChildId)));

        final RecursiveJobStatusResponse completedStatus = jobService.getJobStatus(jobId);
        assertThat(completedStatus.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertChildProgress(completedStatus, 0, 1, 1);
        assertThat(completedStatus.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.PARTIAL);
        assertThat(collectedAssetIds(completedStatus, ITEM_STOCK_ANONYMIZED))
                .containsExactly(anonymizedId(validChildId));
        assertThat(allTombstones(completedStatus))
                .extracting(RecursiveTombstone::getReason)
                .contains(RecursiveTombstoneReason.BOM_CHILD_GLOBAL_ASSET_ID_INVALID);
    }

    @Test
    void shouldNotLoseChildResponsesWhenTheyArriveConcurrently() throws InterruptedException {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001"),
                bomChild(
                        "urn:uuid:22222222-2222-2222-2222-222222222222", "BPNL0000CERS0001")));
        notificationSender = recordingSender(sentRequests);
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());
        assertThat(sentRequests).hasSize(2);

        // Both child branches answer at the same time. Without per-job serialization one of the
        // two read-modify-write cycles overwrites the other and the job never completes.
        final java.util.concurrent.CountDownLatch startSignal = new java.util.concurrent.CountDownLatch(1);
        final List<Thread> responders = sentRequests.stream()
                .map(request -> new Thread(() -> {
                    try {
                        startSignal.await();
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    jobService.handleNotification(childResponse(request, RecursiveResponseStatus.COMPLETED,
                            childResult(request.getContent().getGlobalAssetId())));
                }))
                .toList();
        responders.forEach(Thread::start);
        startSignal.countDown();
        for (final Thread responder : responders) {
            responder.join(5_000);
        }

        final RecursiveJobStatusResponse completedStatus = jobService.getJobStatus(jobId);
        assertThat(completedStatus.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertChildProgress(completedStatus, 0, 2, 0);
    }

    @Test
    void shouldCompleteSingleInstanceSelfLoopWithChildTreeOnly() {
        final String rootAssetId = "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b";
        final String childAssetId = "urn:uuid:11111111-1111-1111-1111-111111111111";
        recursiveProperties.setLocalBpnl("BPNL0000ATLS0001");
        submodelCollector = new RecursiveSubmodelCollector(null, null,
                new ObjectMapper().findAndRegisterModules()) {
            @Override
            public RecursiveChildItem collect(final String globalAssetId, final String localBpnl,
                    final List<String> requestedAspects) {
                return collectedNode(globalAssetId);
            }

            @Override
            RecursiveChildItem collect(final String globalAssetId, final String localBpnl,
                    final List<String> requestedAspects,
                    final AssetAdministrationShellDescriptor resolvedShell) {
                return collect(globalAssetId, localBpnl, requestedAspects);
            }
        };
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> rootAssetId.equals(globalAssetId)
                ? List.of(bomChild(childAssetId, "BPNL0000ATLS0001"))
                : List.of());

        final AtomicReference<RecursiveJobService> serviceRef = new AtomicReference<>(jobService);
        notificationSender = new RecursiveNotificationSender() {
            @Override
            public void sendRequest(final String receiverBpnl, final RecursiveNotificationMessage message) {
                serviceRef.get().handleNotification(message);
            }

            @Override
            public void sendResponse(final String receiverBpnl, final RecursiveNotificationMessage message) {
                serviceRef.get().handleNotification(message);
            }
        };
        grantStore.store(validGrant().toBuilder()
                .allowedBpnlSet(Set.of("BPNL0000ATLS0001"))
                .build());
        // The self-loop child request needs its own grant for the child material.
        grantStore.store(childGrant());

        final UUID jobId = jobService.startJob(validRequest().toBuilder()
                .globalAssetId(rootAssetId)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                .build());

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(status.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertChildProgress(status, 0, 1, 0);
        assertThat(allTombstones(status)).isEmpty();
        assertThat(collectedAssetIds(status, ITEM_STOCK_ANONYMIZED))
                .containsExactly(anonymizedId(childAssetId));
        assertThat(status.getResult().getChildItems()).singleElement().satisfies(child ->
                assertThat(child.getMaterialNumber()).isEqualTo("MNR-" + anonymizedId(childAssetId)));
        assertThat(jobService.getAllJobs()).hasSize(2);
    }

    @Test
    void shouldCompleteWaitingJobWithTombstoneWhenChildResponseDeadlineExpires() {
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        clock.set(Clock.fixed(clockStart, ZoneOffset.UTC));
        grantStore.store(validGrant());
        final RecursiveJobRequest request = validRequest().toBuilder()
                .ttl("PT2M")
                .build();
        final UUID jobId = jobService.startJob(request);

        clock.set(Clock.fixed(clockStart.plusSeconds(61), ZoneOffset.UTC));
        final int processedJobs = jobService.processExpiredJobs();

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(processedJobs).isEqualTo(1);
        assertThat(status.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(jobs.get(jobId).getTimedOutOn()).isNotNull();
        assertChildProgress(status, 0, 0, 1);
        assertThat(allTombstones(status))
                .anySatisfy(tombstone -> {
                    assertThat(tombstone.getReason()).isEqualTo(RecursiveTombstoneReason.CHILD_RESPONSE_TIMEOUT);
                    assertThat(tombstone.getScope()).isEqualTo(RecursiveTombstoneScope.CHILD_BRANCH);
                });
    }

    @Test
    void shouldSanitizeChildTombstonesWhenAggregatingParentPayload() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        notificationSender = recordingSender(sentRequests);
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveTombstone unsafeTombstone = RecursiveTombstone.builder()
                .type(RecursiveTombstoneType.RECURSIVE_TOMBSTONE)
                .scope(RecursiveTombstoneScope.LOCAL_NODE)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                .reason(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED)
                .retryable(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED.isRetryable())
                .detail("GET http://ceres.example/shells failed for bpn=BPNL0000CERS0001 "
                        + "assetId=urn:uuid:33333333-3333-3333-3333-333333333333")
                .occurrences(1)
                .errorRefs(List.of("4e3bea84-a48f-4319-8380-a50e4d615560"))
                .build();
        final RecursiveJobResult childResult = childResult(collectedNode("child").toBuilder()
                .items(List.of())
                .tombstones(List.of(unsafeTombstone))
                .build());

        jobService.handleNotification(childResponse(sentRequests.get(0), RecursiveResponseStatus.COMPLETED,
                childResult));

        final RecursiveJobStatusResponse completedStatus = jobService.getJobStatus(jobId);
        assertThat(completedStatus.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(allTombstones(completedStatus))
                .singleElement()
                .satisfies(tombstone -> {
                    assertThat(tombstone.getReason()).isEqualTo(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED);
                    assertThat(tombstone.getDetail())
                            .contains("<url>", "<bpn>", "<assetId>")
                            .doesNotContain("http://")
                            .doesNotContain("BPNL0000CERS0001")
                            .doesNotContain("urn:uuid");
                });
    }

    @Test
    void shouldCreateGenericTombstoneForFailedChildResponseWithoutPayloadTombstones() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        notificationSender = recordingSender(sentRequests);
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveTombstone rejection = RecursiveTombstone.builder()
                .type(RecursiveTombstoneType.RECURSIVE_TOMBSTONE)
                .scope(RecursiveTombstoneScope.RECURSIVE_CHAIN)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                .reason(RecursiveTombstoneReason.CHAIN_OPENING_REJECTED)
                .retryable(false)
                .detail("Rejected by https://belfast.example/api for bpn=BPNL0000BELF0001")
                .occurrences(1)
                .errorRefs(List.of("f410c848-2b87-48ae-9da4-294079226a1f"))
                .build();
        jobService.handleNotification(childResponse(sentRequests.get(0), RecursiveResponseStatus.FAILED,
                childResult(emptyNode().toBuilder().tombstones(List.of(rejection)).build())));

        final RecursiveJobStatusResponse failedStatus = jobService.getJobStatus(jobId);
        assertThat(failedStatus.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(failedStatus.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.FAILED);
        assertThat(allTombstones(failedStatus))
                .singleElement()
                .satisfies(tombstone -> {
                    assertThat(tombstone.getReason()).isEqualTo(RecursiveTombstoneReason.CHAIN_OPENING_REJECTED);
                    assertThat(tombstone.getDetail())
                            .contains("<url>", "<bpn>")
                            .doesNotContain("BPNL0000BELF0001")
                            .doesNotContain("https://");
                });
    }

    @Test
    void shouldIgnoreLateChildResponseAfterTimeoutResultWasStored() {
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        clock.set(Clock.fixed(clockStart, ZoneOffset.UTC));
        grantStore.store(validGrant());
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        notificationSender = recordingSender(sentRequests);
        final RecursiveJobRequest request = validRequest().toBuilder()
                .ttl("PT2M")
                .build();
        final UUID jobId = jobService.startJob(request);
        clock.set(Clock.fixed(clockStart.plusSeconds(61), ZoneOffset.UTC));
        jobService.processExpiredJobs();

        jobService.handleNotification(RecursiveNotificationMessage.builder()
                .header(notificationHeader(UUID.randomUUID().toString())
                        .relatedMessageId(sentRequests.get(0).getHeader().getMessageId())
                        .senderBpnl("BPNL0000BELF0001")
                        .receiverBpnl("BPNL0000ATLS0001")
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.RESPONSE)
                        .openingId("opening-42")
                        .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                        .globalAssetId(request.getGlobalAssetId())
                        .status(RecursiveResponseStatus.COMPLETED)
                        .result(childResult("late-child"))
                        .build())
                .build());

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(status.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(allTombstones(status)).hasSize(1);
        assertThat(collectedAssetIds(status, ITEM_STOCK_ANONYMIZED))
                .doesNotContain(anonymizedId("late-child"));
    }

    @Test
    void shouldFailWaitingJobWhenJobDeadlineExpires() {
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        clock.set(Clock.fixed(clockStart, ZoneOffset.UTC));
        grantStore.store(validGrant());
        final RecursiveJobRequest request = validRequest().toBuilder()
                .ttl("PT2M")
                .build();
        final UUID jobId = jobService.startJob(request);

        clock.set(Clock.fixed(clockStart.plusSeconds(121), ZoneOffset.UTC));
        final int processedJobs = jobService.processExpiredJobs();

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(processedJobs).isEqualTo(1);
        assertThat(status.getJob().getState()).isEqualTo(JobState.ERROR);
        assertThat(jobs.get(jobId).getTimedOutOn()).isNotNull();
        assertThat(status.getJob().getException().getException()).isEqualTo("RECURSIVE_DEADLINE_EXCEEDED");
        assertThat(status.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.FAILED);
        assertChildProgress(status, 0, 0, 1);
        assertThat(allTombstones(status))
                .extracting(RecursiveTombstone::getReason)
                .containsExactlyInAnyOrder(RecursiveTombstoneReason.RECURSIVE_DEADLINE_EXCEEDED,
                        RecursiveTombstoneReason.CHILD_RESPONSE_TIMEOUT);
    }

    @Test
    void shouldFailQueuedAcceptedJobWhenJobDeadlineExpires() {
        final List<Runnable> queuedTasks = new ArrayList<>();
        recursiveJobExecutor = queuedTasks::add;
        clock.set(Clock.fixed(clockStart, ZoneOffset.UTC));
        grantStore.store(validGrant());
        final RecursiveJobRequest request = validRequest().toBuilder()
                .ttl("PT2M")
                .build();
        final UUID jobId = jobService.startJob(request);

        clock.set(Clock.fixed(clockStart.plusSeconds(121), ZoneOffset.UTC));
        final int processedJobs = jobService.processExpiredJobs();

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(queuedTasks).hasSize(1);
        assertThat(processedJobs).isEqualTo(1);
        assertThat(status.getJob().getState()).isEqualTo(JobState.ERROR);
        assertThat(status.getJob().getException().getException()).isEqualTo("RECURSIVE_DEADLINE_EXCEEDED");
    }

    @Test
    void shouldStoreNotificationDeliveryFailureAsTombstone() {
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        notificationSender = new RecursiveNotificationSender() {
            @Override
            public void sendRequest(final String receiverBpnl, final RecursiveNotificationMessage message) {
                throw new IllegalStateException("edc notification failed");
            }

            @Override
            public void sendResponse(final String receiverBpnl, final RecursiveNotificationMessage message) {
                // not needed for root job
            }
        };
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(status.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(status.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.FAILED);
        assertThat(allTombstones(status))
                .anySatisfy(tombstone -> {
                    assertThat(tombstone.getReason()).isEqualTo(RecursiveTombstoneReason.CHILD_BRANCH_FAILED);
                    assertThat(tombstone.getDetail()).isEqualTo("A recursive child branch failed.");
                });
    }

    @Test
    void shouldKeepSendFailureTombstoneWhenAnotherChildTimesOut() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001"),
                bomChild(
                        "urn:uuid:22222222-2222-2222-2222-222222222222", "BPNL0000CERS0001")));
        clock.set(Clock.fixed(clockStart, ZoneOffset.UTC));
        notificationSender = new RecursiveNotificationSender() {
            @Override
            public void sendRequest(final String receiverBpnl, final RecursiveNotificationMessage message) {
                if ("BPNL0000BELF0001".equals(receiverBpnl)) {
                    throw new IllegalStateException("temporary connector failure");
                }
                sentRequests.add(message);
            }

            @Override
            public void sendResponse(final String receiverBpnl, final RecursiveNotificationMessage message) {
                // not needed for root job
            }
        };
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest().toBuilder()
                .ttl("PT2M")
                .build());

        final RecursiveJobStatusResponse runningStatus = jobService.getJobStatus(jobId);
        assertThat(sentRequests).hasSize(1);
        assertThat(runningStatus.getJob().getState()).isEqualTo(JobState.RUNNING);
        assertChildProgress(runningStatus, 1, 0, 1);

        clock.set(Clock.fixed(clockStart.plusSeconds(61), ZoneOffset.UTC));
        jobService.processExpiredJobs();

        final RecursiveJobStatusResponse completedStatus = jobService.getJobStatus(jobId);
        assertThat(completedStatus.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(completedStatus.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.FAILED);
        assertThat(allTombstones(completedStatus))
                .extracting(tombstone -> tombstone.getReason())
                .containsExactlyInAnyOrder(RecursiveTombstoneReason.CHILD_BRANCH_FAILED,
                        RecursiveTombstoneReason.CHILD_RESPONSE_TIMEOUT);
    }

    @Test
    void shouldHandleIncomingNotificationRequest() {
        recursiveProperties.setLocalBpnl("BPNL0000BELF0001");
        final RecursiveNotificationMessage notification = RecursiveNotificationMessage.builder()
                .header(notificationHeader(UUID.randomUUID().toString())
                        .senderBpnl("BPNL0000ATLS0001")
                        .receiverBpnl("BPNL0000BELF0001")
                        .expectedResponseBy(Instant.now().plus(Duration.ofMinutes(10)).toString())
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.REQUEST)
                        .openingId("opening-42")
                        .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                        .globalAssetId("urn:uuid:11111111-1111-1111-1111-111111111111")
                        .bomLifecycle(BomLifecycle.AS_PLANNED)
                        .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                        .build())
                .build();

        grantStore.store(childGrant());

        jobService.handleNotification(notification);

        assertThat(jobService.getAllJobs()).hasSize(1);
    }

    @Test
    void shouldRejectUnknownPartnerAspectWithControlledResponse() {
        recursiveProperties.setLocalBpnl("BPNL0000BELF0001");
        final String unsupportedByThisNode = "urn:samm:io.catenax.future_aspect_anonymized:9.9.9#FutureAspectAnonymized";
        final List<RecursiveNotificationMessage> sentResponses = new ArrayList<>();
        notificationSender = new RecursiveNotificationSender() {
            @Override
            public void sendRequest(final String receiverBpnl, final RecursiveNotificationMessage message) {
                // Invalid partner requests never create child requests.
            }

            @Override
            public void sendResponse(final String receiverBpnl, final RecursiveNotificationMessage message) {
                sentResponses.add(message);
            }
        };
        final RecursiveNotificationMessage notification = RecursiveNotificationMessage.builder()
                .header(notificationHeader(UUID.randomUUID().toString())
                        .senderBpnl("BPNL0000ATLS0001")
                        .receiverBpnl("BPNL0000BELF0001")
                        .expectedResponseBy(Instant.now().plus(Duration.ofMinutes(10)).toString())
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.REQUEST)
                        .openingId("opening-42")
                        .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                        .globalAssetId("urn:uuid:11111111-1111-1111-1111-111111111111")
                        .bomLifecycle(BomLifecycle.AS_PLANNED)
                        .aspects(List.of(ITEM_STOCK_ANONYMIZED, unsupportedByThisNode))
                        .build())
                .build();

        jobService.handleNotification(notification);

        assertThat(jobService.getAllJobs()).isEmpty();
        assertThat(sentResponses).singleElement().satisfies(response -> {
            assertThat(response.getContent().getStatus()).isEqualTo(RecursiveResponseStatus.FAILED);
            assertThat(response.getContent().getResult().getResultStatus())
                    .isEqualTo(RecursiveResultStatus.FAILED);
        });
    }

    @Test
    void shouldRejectIncomingNotificationRequestWithoutExpectedResponseBy() {
        recursiveProperties.setLocalBpnl("BPNL0000BELF0001");
        final RecursiveNotificationMessage notification = RecursiveNotificationMessage.builder()
                .header(notificationHeader(UUID.randomUUID().toString())
                        .senderBpnl("BPNL0000ATLS0001")
                        .receiverBpnl("BPNL0000BELF0001")
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.REQUEST)
                        .openingId("opening-42")
                        .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                        .globalAssetId("urn:uuid:11111111-1111-1111-1111-111111111111")
                        .bomLifecycle(BomLifecycle.AS_PLANNED)
                        .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                        .build())
                .build();

        assertThatThrownBy(() -> jobService.handleNotification(notification))
                .isInstanceOf(RecursiveNotificationValidationException.class)
                .hasMessageContaining("expectedResponseBy");
    }

    @Test
    void shouldIgnoreDuplicateNotification() {
        recursiveProperties.setLocalBpnl("BPNL0000BELF0001");
        final String messageId = UUID.randomUUID().toString();

        final RecursiveNotificationMessage notification = RecursiveNotificationMessage.builder()
                .header(notificationHeader(messageId)
                        .senderBpnl("BPNL0000ATLS0001")
                        .receiverBpnl("BPNL0000BELF0001")
                        .expectedResponseBy(Instant.now().plus(Duration.ofMinutes(10)).toString())
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.REQUEST)
                        .openingId("opening-42")
                        .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                        .globalAssetId("urn:uuid:11111111-1111-1111-1111-111111111111")
                        .bomLifecycle(BomLifecycle.AS_PLANNED)
                        .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                        .build())
                .build();

        grantStore.store(childGrant());

        jobService.handleNotification(notification);
        jobService.handleNotification(notification); // duplicate

        assertThat(jobService.getAllJobs()).hasSize(1); // only one job
    }

    @Test
    void shouldPersistDeliveryClassificationAndReuseErrorRefInTombstone() {
        final String errorRef = "008420a8-6cec-487e-b255-29ff370fdf34";
        jobService = newJobService((globalAssetId, bpnl, bomLifecycle) -> List.of(
                bomChild(
                        "urn:uuid:11111111-1111-1111-1111-111111111111", "BPNL0000BELF0001")));
        notificationSender = new RecursiveNotificationSender() {
            @Override
            public void sendRequest(final String receiverBpnl, final RecursiveNotificationMessage message) {
                throw new RecursiveNotificationDeliveryException(
                        RecursiveNotificationDeliveryFailureReason.NOTIFICATION_ASSET_NOT_FOUND, errorRef,
                        "notification asset missing", null);
            }

            @Override
            public void sendResponse(final String receiverBpnl, final RecursiveNotificationMessage message) {
                // root job sends no parent response
            }
        };
        grantStore.store(validGrant());

        final UUID jobId = jobService.startJob(validRequest());

        // Local view: the detailed classification is persisted on the job.
        assertThat(jobs.get(jobId).getChildBranches())
                .singleElement()
                .satisfies(branch -> {
                    assertThat(branch.getDeliveryFailure().getReason())
                            .isEqualTo(RecursiveNotificationDeliveryFailureReason.NOTIFICATION_ASSET_NOT_FOUND);
                    assertThat(branch.getDeliveryFailure().getErrorRef()).isEqualTo(errorRef);
                });
        // External view: only the coarse reason travels, correlated via the same errorRef.
        final RecursiveJobStatusResponse status = jobService.getJobStatus(jobId);
        assertThat(allTombstones(status))
                .singleElement()
                .satisfies(tombstone -> {
                    assertThat(tombstone.getReason()).isEqualTo(RecursiveTombstoneReason.CHILD_BRANCH_FAILED);
                    assertThat(tombstone.getDetail()).doesNotContain("NOTIFICATION_ASSET_NOT_FOUND");
                    assertThat(tombstone.getErrorRefs()).containsExactly(errorRef);
                });
    }

    @Test
    void shouldResumeGrantCheckedJobAfterRestart() {
        grantStore.store(validGrant());
        storeJob(seededState("stuck-job", RecursiveJobPhase.GRANT_CHECKED).build());

        final int resumed = jobService.recoverOpenJobs();

        assertThat(resumed).isEqualTo(1);
        assertThat(jobs.get(namedJobId("stuck-job")).getState()).isEqualTo(RecursiveJobPhase.COMPLETED);
    }

    @Test
    void shouldFailResumedJobWhenGrantDisappeared() {
        storeJob(seededState("orphaned-job", RecursiveJobPhase.GRANT_CHECKED).build());

        final int resumed = jobService.recoverOpenJobs();

        assertThat(resumed).isEqualTo(1);
        assertThat(jobs.get(namedJobId("orphaned-job")).getState()).isEqualTo(RecursiveJobPhase.FAILED);
        assertThat(jobService.getJobStatus(namedJobId("orphaned-job")).getResult().getTombstones())
                .extracting(tombstone -> tombstone.getReason())
                .containsExactly(RecursiveTombstoneReason.CHAIN_OPENING_REJECTED);
    }

    @Test
    void shouldFailRecoveredJobWithInvalidUseCasePolicy() {
        storeJob(seededState("invalid-job", RecursiveJobPhase.GRANT_CHECKED)
                .useCase(null)
                .aspects(List.of())
                .build());

        final int resumed = jobService.recoverOpenJobs();

        assertThat(resumed).isEqualTo(1);
        assertThat(jobs.get(namedJobId("invalid-job")).getState()).isEqualTo(RecursiveJobPhase.FAILED);
        assertThat(jobService.getJobStatus(namedJobId("invalid-job")).getResult().getResultStatus())
                .isEqualTo(RecursiveResultStatus.FAILED);
    }

    @Test
    void shouldResendOnlyUnansweredChildRequestsOnRecovery() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        notificationSender = recordingSender(sentRequests);
        grantStore.store(validGrant());
        storeJob(seededState("waiting-job", RecursiveJobPhase.AWAITING_CHILDREN)
                .childBranches(List.of(
                        RecursiveChildBranch.builder()
                                .messageId("answered-msg").partnerBpnl("BPNL0000BELF0001")
                                .childGlobalAssetId("urn:uuid:11111111-1111-1111-1111-111111111111")
                                .status(RecursiveResponseStatus.COMPLETED)
                                .build(),
                        RecursiveChildBranch.builder()
                                .messageId("unanswered-msg").partnerBpnl("BPNL0000CERS0001")
                                .childGlobalAssetId("urn:uuid:22222222-2222-2222-2222-222222222222").build()))
                .build());

        final int resumed = jobService.recoverOpenJobs();

        assertThat(resumed).isEqualTo(1);
        assertThat(sentRequests).singleElement().satisfies(request -> {
            assertThat(request.getHeader().getReceiverBpnl()).isEqualTo("BPNL0000CERS0001");
            assertThat(request.getHeader().getMessageId()).isEqualTo("unanswered-msg");
        });
    }

    @Test
    void shouldNotResumeTerminalOrExpiredJobs() {
        final List<RecursiveNotificationMessage> sentRequests = new ArrayList<>();
        notificationSender = recordingSender(sentRequests);
        grantStore.store(validGrant());
        storeJob(seededState("done-job", RecursiveJobPhase.COMPLETED).build());
        storeJob(seededState("expired-job", RecursiveJobPhase.GRANT_CHECKED)
                .deadline(ZonedDateTime.now().minusMinutes(1))
                .build());
        storeJob(seededState("expired-children-job", RecursiveJobPhase.AWAITING_CHILDREN)
                .childResponseDeadline(ZonedDateTime.now().minusMinutes(1))
                .childBranches(List.of(RecursiveChildBranch.builder()
                        .messageId("open-msg").partnerBpnl("BPNL0000CERS0001")
                        .childGlobalAssetId("urn:uuid:22222222-2222-2222-2222-222222222222").build()))
                .build());

        final int resumed = jobService.recoverOpenJobs();

        assertThat(resumed).isZero();
        assertThat(sentRequests).isEmpty();
        assertThat(jobs.get(namedJobId("done-job")).getState()).isEqualTo(RecursiveJobPhase.COMPLETED);
        assertThat(jobs.get(namedJobId("expired-job")).getState()).isEqualTo(RecursiveJobPhase.GRANT_CHECKED);
    }

    @Test
    void shouldResendParentResponseForDuplicateRequestOnTerminalChildJob() {
        recursiveProperties.setLocalBpnl("BPNL0000BELF0001");
        final List<RecursiveNotificationMessage> sentResponses = new ArrayList<>();
        notificationSender = new RecursiveNotificationSender() {
            @Override
            public void sendRequest(final String receiverBpnl, final RecursiveNotificationMessage message) {
                // leaf job sends no child requests
            }

            @Override
            public void sendResponse(final String receiverBpnl, final RecursiveNotificationMessage message) {
                sentResponses.add(message);
            }
        };
        grantStore.store(childGrant());
        final String messageId = UUID.randomUUID().toString();

        jobService.handleNotification(incomingRequest(messageId));
        assertThat(sentResponses).hasSize(1);

        // The parent retries after a restart: the child answers the duplicate with its result
        // again instead of leaving the parent waiting for the timeout.
        jobService.handleNotification(incomingRequest(messageId));

        assertThat(jobService.getAllJobs()).hasSize(1);
        assertThat(sentResponses).hasSize(2);
        assertThat(sentResponses.get(1).getHeader().getRelatedMessageId()).isEqualTo(messageId);
    }

    private RecursiveNotificationMessage incomingRequest(final String messageId) {
        return RecursiveNotificationMessage.builder()
                .header(notificationHeader(messageId)
                        .senderBpnl("BPNL0000ATLS0001")
                        .receiverBpnl("BPNL0000BELF0001")
                        .expectedResponseBy(Instant.now().plus(Duration.ofMinutes(10)).toString())
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.REQUEST)
                        .openingId("opening-42")
                        .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                        .globalAssetId("urn:uuid:11111111-1111-1111-1111-111111111111")
                        .bomLifecycle(BomLifecycle.AS_PLANNED)
                        .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                        .build())
                .build();
    }

    /** Minimal persisted job state as a restart would find it; phase and details vary per test. */
    private RecursiveJobState.RecursiveJobStateBuilder seededState(final String jobId,
            final RecursiveJobPhase phase) {
        return RecursiveJobState.builder()
                .jobId(namedJobId(jobId))
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                .bomLifecycle(org.eclipse.tractusx.irs.component.enums.BomLifecycle.AS_PLANNED)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                .requesterBpnl("BPNL0000ATLS0001")
                .receiverBpnl("BPNL0000BELF0001")
                .messageId(jobId + "-message")
                .createdOn(ZonedDateTime.now().minusMinutes(5))
                .lastModifiedOn(ZonedDateTime.now().minusMinutes(5))
                .deadline(ZonedDateTime.now().plusMinutes(10))
                .childResponseDeadline(ZonedDateTime.now().plusMinutes(9))
                .state(phase)
                .rootJob(true)
                .bomChildren(List.of())
                .childBranches(List.of());
    }

    private void storeJob(final RecursiveJobState state) {
        jobs.put(state.getJobId(), state);
    }

    private static UUID namedJobId(final String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private RecursiveNotificationSender recordingSender(final List<RecursiveNotificationMessage> sentRequests) {
        return new RecursiveNotificationSender() {
            @Override
            public void sendRequest(final String receiverBpnl, final RecursiveNotificationMessage message) {
                sentRequests.add(message);
            }

            @Override
            public void sendResponse(final String receiverBpnl, final RecursiveNotificationMessage message) {
                // not needed for root job
            }
        };
    }

    private RecursiveNotificationMessage childResponse(final RecursiveNotificationMessage request,
            final RecursiveResponseStatus status, final RecursiveJobResult result) {
        final RecursiveJobResult responseResult = result == null ? null : RecursiveJobResult.builder()
                .resultStatus(result.getResultStatus())
                .useCase(request.getContent().getUseCase())
                .bomLifecycle(request.getContent().getBomLifecycle())
                .requestedAspects(request.getContent().getAspects())
                .childItems(result.getChildItems())
                .tombstones(result.getTombstones())
                .build();
        return RecursiveNotificationMessage.builder()
                .header(notificationHeader(UUID.randomUUID().toString())
                        .relatedMessageId(request.getHeader().getMessageId())
                        .senderBpnl(request.getHeader().getReceiverBpnl())
                        .receiverBpnl(request.getHeader().getSenderBpnl())
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.RESPONSE)
                        .openingId(request.getContent().getOpeningId())
                        .useCase(request.getContent().getUseCase())
                        .bomLifecycle(request.getContent().getBomLifecycle())
                        .aspects(request.getContent().getAspects())
                        .status(status)
                        .result(responseResult)
                        .build())
                .build();
    }

    private RecursiveNotificationMessage.Header.HeaderBuilder notificationHeader(final String messageId) {
        return RecursiveNotificationMessage.Header.builder()
                .messageId(messageId)
                .context(RecursiveNotificationMessage.HEADER_CONTEXT)
                .sentDateTime(Instant.now().toString())
                .version(RecursiveNotificationMessage.HEADER_VERSION);
    }

    private List<Object> collectedAssetIds(final RecursiveJobStatusResponse status, final String aspect) {
        final List<Object> assetIds = new ArrayList<>();
        collectAssetIds(status.getResult().getChildItems(), aspect, assetIds);
        return assetIds;
    }

    private void collectAssetIds(final List<RecursiveChildItem> childItems, final String aspect,
            final List<Object> assetIds) {
        for (final RecursiveChildItem child : childItems == null ? List.<RecursiveChildItem>of() : childItems) {
            if (child.getItems() != null) {
                child.getItems().stream()
                     .filter(item -> aspect.equals(item.getAspect()))
                     .map(item -> item.getItems().get("materialGlobalAssetIdAnonymized"))
                     .filter(java.util.Objects::nonNull)
                     .forEach(assetIds::add);
            }
            collectAssetIds(child.getChildItems(), aspect, assetIds);
        }
    }

    private List<RecursiveTombstone> allTombstones(final RecursiveJobStatusResponse status) {
        final List<RecursiveTombstone> tombstones = new ArrayList<>(status.getResult().getTombstones());
        collectTombstones(status.getResult().getChildItems(), tombstones);
        return tombstones;
    }

    private void collectTombstones(final List<RecursiveChildItem> childItems,
            final List<RecursiveTombstone> tombstones) {
        for (final RecursiveChildItem child : childItems == null ? List.<RecursiveChildItem>of() : childItems) {
            if (child.getTombstones() != null) {
                tombstones.addAll(child.getTombstones());
            }
            collectTombstones(child.getChildItems(), tombstones);
        }
    }

    private static RecursiveChildItem collectedNode(final String globalAssetId) {
        return RecursiveChildItem.builder()
                                 .materialNumber("MNR-" + anonymizedId(globalAssetId))
                                 .materialName("Material " + anonymizedId(globalAssetId))
                                 .items(List.of(RecursiveAspectItem.builder()
                                         .aspect(ITEM_STOCK_ANONYMIZED)
                                         .items(validItemStockPayload(globalAssetId))
                                         .build()))
                                 .tombstones(List.of())
                                 .childItems(List.of())
                                 .build();
    }

    private RecursiveChildItem emptyNode() {
        return RecursiveChildItem.builder()
                                 .items(List.of())
                                 .tombstones(List.of())
                                 .childItems(List.of())
                                 .build();
    }

    private RecursiveJobResult childResult(final String globalAssetId) {
        return childResult(collectedNode(globalAssetId));
    }

    private RecursiveJobResult childResult(final RecursiveChildItem child) {
        return RecursiveJobResult.builder()
                                 .resultStatus(RecursiveResultStatus.COMPLETE)
                                 .childItems(List.of(child))
                                 .tombstones(List.of())
                                 .build();
    }

    private RecursiveBomChild bomChild(final String globalAssetId, final String partnerBpnl) {
        return new RecursiveBomChild(globalAssetId, partnerBpnl,
                org.eclipse.tractusx.irs.recursive.model.RecursiveQuantity.builder()
                        .value(1.0)
                        .unit(ItemUnitEnumeration.UNIT_PIECE)
                        .build());
    }

    private static Map<String, Object> validItemStockPayload(final String globalAssetId) {
        return Map.of(
                "materialGlobalAssetIdAnonymized", anonymizedId(globalAssetId),
                "allocatedStocks", List.of(Map.of(
                        "stockLocationBpnsAnonymized", "hashed-stock-location",
                        "isBlocked", false,
                        "lastUpdatedOnDateTime", "2026-07-14T12:00:00Z",
                        "quantityOnAllocatedStock", Map.of("value", 20.0, "unit", "unit:piece"))),
                "direction", "INBOUND");
    }

    private static String anonymizedId(final String globalAssetId) {
        return "hashed-material-" + Integer.toUnsignedString(globalAssetId.hashCode());
    }

    private RecursiveJobRequest validRequest() {
        return RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                .requesterBpn("BPNL0000ATLS0001")
                .ttl("PT15M")
                .build();
    }

    private RecursiveJobService newJobService(final TestBomResolver bomResolver) {
        final RecursiveChainOpeningGrantService grantService = new RecursiveChainOpeningGrantService(grantStore);
        final RecursiveTraversalService traversalService = new StubRecursiveTraversalService(bomResolver);
        return new RecursiveJobService(grantService, traversalService, newJobStateStore(),
                forwardingNotificationSender, submodelCollector, recursiveProperties, forwardingExecutor, clock);
    }

    private static RecursiveSubmodelCollector emptySubmodelCollector() {
        return new RecursiveSubmodelCollector(null, null,
                new ObjectMapper().findAndRegisterModules()) {
            @Override
            RecursiveChildItem collect(final String globalAssetId, final String localBpnl,
                    final List<String> requestedAspects,
                    final AssetAdministrationShellDescriptor resolvedShell) {
                return null;
            }
        };
    }

    private static RecursiveNotificationSender noOpNotificationSender() {
        return new RecursiveNotificationSender() {
            @Override
            public void sendRequest(final String receiverBpnl, final RecursiveNotificationMessage message) {
            }

            @Override
            public void sendResponse(final String receiverBpnl, final RecursiveNotificationMessage message) {
            }
        };
    }

    @FunctionalInterface
    private interface TestBomResolver {
        List<RecursiveBomChild> resolve(String globalAssetId, String bpnl, BomLifecycle bomLifecycle);
    }

    private static final class StubRecursiveTraversalService extends RecursiveTraversalService {

        private final TestBomResolver bomResolver;

        private StubRecursiveTraversalService(final TestBomResolver bomResolver) {
            super(null, null, null);
            this.bomResolver = bomResolver;
        }

        @Override
        TraversalResult resolve(final String globalAssetId, final String bpnl, final BomLifecycle bomLifecycle) {
            return new TraversalResult(List.copyOf(bomResolver.resolve(globalAssetId, bpnl, bomLifecycle)), null);
        }
    }

    private static final class AdjustableClock extends Clock {

        private Clock delegate;

        private AdjustableClock(final Clock delegate) {
            this.delegate = delegate;
        }

        private void set(final Clock delegate) {
            this.delegate = delegate;
        }

        @Override
        public ZoneId getZone() {
            return delegate.getZone();
        }

        @Override
        public Clock withZone(final ZoneId zone) {
            return delegate.withZone(zone);
        }

        @Override
        public Instant instant() {
            return delegate.instant();
        }
    }

    private RecursiveChainOpeningGrant validGrant() {
        return RecursiveChainOpeningGrant.builder()
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .requesterBpn("BPNL0000ATLS0001")
                .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                .allowedBpnlSet(Set.of("BPNL0000BELF0001", "BPNL0000CERS0001", "BPNL0000DLTA0001"))
                .validFrom(ZonedDateTime.now().minusHours(1))
                .validTo(ZonedDateTime.now().plusHours(12))
                .build();
    }

    /** Grant for the child material requested via incoming REQUEST notifications. */
    private RecursiveChainOpeningGrant childGrant() {
        return validGrant().toBuilder()
                .globalAssetId("urn:uuid:11111111-1111-1111-1111-111111111111")
                .build();
    }

    private RecursiveJobStateStore newJobStateStore() {
        return new RecursiveJobStateStore() {
            @Override
            public void save(final RecursiveJobState state) {
                jobs.put(state.getJobId(), state);
            }

            @Override
            public Optional<RecursiveJobState> findById(final UUID jobId) {
                return Optional.ofNullable(jobs.get(jobId));
            }

            @Override
            public Optional<UUID> findJobIdByIncomingRequestMessageId(final String messageId) {
                return Optional.ofNullable(messageIds.get("in:" + messageId));
            }

            @Override
            public void registerIncomingRequestMessageId(final String messageId, final UUID jobId) {
                if (messageId != null) {
                    messageIds.put("in:" + messageId, jobId);
                }
            }

            @Override
            public Optional<UUID> findJobIdByChildRequestMessageId(final String messageId) {
                return Optional.ofNullable(messageIds.get("out:" + messageId));
            }

            @Override
            public void registerChildRequestMessageId(final String messageId, final UUID jobId) {
                if (messageId != null) {
                    messageIds.put("out:" + messageId, jobId);
                }
            }

            @Override
            public List<RecursiveJobState> findAll() {
                return List.copyOf(jobs.values());
            }
        };
    }

    private void assertChildProgress(final RecursiveJobStatusResponse status, final int running,
            final int completed, final int failed) {
        assertThat(status.getJob().getAsyncFetchedItems().getRunning()).isEqualTo(running);
        assertThat(status.getJob().getAsyncFetchedItems().getCompleted()).isEqualTo(completed);
        assertThat(status.getJob().getAsyncFetchedItems().getFailed()).isEqualTo(failed);
    }
}
