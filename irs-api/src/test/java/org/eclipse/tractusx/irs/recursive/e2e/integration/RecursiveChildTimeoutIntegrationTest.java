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
package org.eclipse.tractusx.irs.recursive.e2e.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.recursive.model.ItemUnitEnumeration;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspectItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildBranch;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationType;
import org.eclipse.tractusx.irs.recursive.model.RecursiveQuantity;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobPhase;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.store.RecursiveJobStateStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Deterministic child-timeout scenario: the parent job is seeded directly with an already
 * expired child response deadline, so the sweeper (1s interval in the launcher) completes it on
 * its next run. No real waiting for a partner that never answers, no timing race between
 * PARTIAL and FAILED - the answered branch is part of the seeded state.
 */
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Recursive child timeout integration")
class RecursiveChildTimeoutIntegrationTest extends RecursiveIntegrationTestBase {

    private static final String ANSWERED_MESSAGE_ID = "68904173-ad59-4a77-8412-3e73fcafbd81";
    private static final String UNANSWERED_MESSAGE_ID = "68904173-ad59-4a77-8412-3e73fcafbd82";
    private static final String PARENT_MESSAGE_ID = "68904173-ad59-4a77-8412-3e73fcafbd83";
    private static final String JOB_ID = "68904173-ad59-4a77-8412-3e73fcafbd85";

    @Test
    @DisplayName("Sweeper completes an expired job as PARTIAL and ignores the late child response")
    void shouldCompletePartialOnChildTimeoutAndIgnoreLateResponse() {
        final RecursiveIrsInstance atlas = startInstance(ATLAS, ATLAS_BPN);
        seedExpiredAwaitingChildrenJob(atlas);

        await().pollDelay(Duration.ZERO)
                .pollInterval(Duration.ofMillis(100))
                .timeout(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    final RecursiveJobStatusResponse status = client.jobStatus(atlas, JOB_ID);
                    assertThat(status.getJob().getState()).isEqualTo(JobState.COMPLETED);
                });

        final RecursiveJobStatusResponse completed = client.jobStatus(atlas, JOB_ID);
        assertThat(completed.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.PARTIAL);
        assertThat(tombstoneReasons(completed)).contains(RecursiveTombstoneReason.CHILD_RESPONSE_TIMEOUT);
        assertThat(quantities(completed.getResult())).containsExactly(7);

        // A response arriving after the terminal state must not change the result.
        client.sendNotification(atlas, CERES_BPN, lateResponse());
        final RecursiveJobStatusResponse afterLateResponse = client.jobStatus(atlas, JOB_ID);
        assertThat(afterLateResponse.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.PARTIAL);
        assertThat(tombstoneReasons(afterLateResponse)).contains(RecursiveTombstoneReason.CHILD_RESPONSE_TIMEOUT);
        assertThat(quantities(afterLateResponse.getResult())).containsExactly(7);
    }

    /**
     * Root job waiting for two children: Belfast answered with one ItemStock payload, Ceres
     * never answered and the child response deadline is already in the past.
     */
    private void seedExpiredAwaitingChildrenJob(final RecursiveIrsInstance atlas) {
        final ZonedDateTime now = ZonedDateTime.now();
        final RecursiveJobStateStore store = atlas.context().getBean(RecursiveJobStateStore.class);
        store.save(RecursiveJobState.builder()
                .jobId(UUID.fromString(JOB_ID))
                .openingId(OPENING_ID)
                .useCase(PURIS_USE_CASE)
                .globalAssetId(ATLAS_ASSET)
                .bomLifecycle(BomLifecycle.AS_PLANNED)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                .requesterBpnl(ATLAS_BPN)
                .receiverBpnl(ATLAS_BPN)
                .messageId(PARENT_MESSAGE_ID)
                .createdOn(now.minusMinutes(5))
                .lastModifiedOn(now.minusMinutes(5))
                .deadline(now.plusMinutes(10))
                .childResponseDeadline(now.minusSeconds(1))
                .state(RecursiveJobPhase.AWAITING_CHILDREN)
                .rootJob(true)
                .bomChildren(List.of())
                .childBranches(List.of(
                        RecursiveChildBranch.builder()
                                .messageId(ANSWERED_MESSAGE_ID)
                                .partnerBpnl(BELFAST_BPN)
                                .childGlobalAssetId(BELFAST_ASSET)
                                .quantity(quantity())
                                .status(RecursiveResponseStatus.COMPLETED)
                                .responsePayload(childResult("belfast", 7))
                                .build(),
                        RecursiveChildBranch.builder()
                                .messageId(UNANSWERED_MESSAGE_ID)
                                .partnerBpnl(CERES_BPN)
                                .childGlobalAssetId(CERES_ASSET)
                                .quantity(quantity())
                                .build()))
                .build());
        // The late response correlates via this mapping, exactly like a real child request.
        store.registerChildRequestMessageId(UNANSWERED_MESSAGE_ID, UUID.fromString(JOB_ID));
    }

    private RecursiveNotificationMessage lateResponse() {
        return RecursiveNotificationMessage.builder()
                .header(RecursiveNotificationMessage.Header.builder()
                        .messageId("68904173-ad59-4a77-8412-3e73fcafbd84")
                        .relatedMessageId(UNANSWERED_MESSAGE_ID)
                        .context(RecursiveNotificationMessage.HEADER_CONTEXT)
                        .sentDateTime(ZonedDateTime.now().toInstant().toString())
                        .senderBpnl(CERES_BPN)
                        .receiverBpnl(ATLAS_BPN)
                        .version(RecursiveNotificationMessage.HEADER_VERSION)
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.RESPONSE)
                        .openingId(OPENING_ID)
                        .useCase(PURIS_USE_CASE)
                        .bomLifecycle(BomLifecycle.AS_PLANNED)
                        .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                        .status(RecursiveResponseStatus.COMPLETED)
                        .result(childResult("ceres", 999))
                        .build())
                .build();
    }

    private RecursiveJobResult childResult(final String material, final int stock) {
        final Map<String, Object> payload = Map.of(
                "materialGlobalAssetIdAnonymized", "hashed-material-" + material,
                "direction", "OUTBOUND",
                "allocatedStocks", List.of(Map.of(
                        "quantityOnAllocatedStock", Map.of("unit", "unit:piece", "value", stock),
                        "isBlocked", false,
                        "stockLocationBpnsAnonymized", "test-stock-location-" + stock,
                        "lastUpdatedOnDateTime", "2026-05-05T00:00:00Z")));
        return RecursiveJobResult.builder()
                                 .resultStatus(RecursiveResultStatus.COMPLETE)
                                 .useCase(PURIS_USE_CASE)
                                 .bomLifecycle(BomLifecycle.AS_PLANNED)
                                 .requestedAspects(List.of(ITEM_STOCK_ANONYMIZED))
                                 .childItems(List.of(RecursiveChildItem.builder()
                                         .materialNumber("MNR-" + material)
                                         .materialName(material)
                                         .items(List.of(RecursiveAspectItem.builder()
                                                 .aspect(ITEM_STOCK_ANONYMIZED)
                                                 .items(payload)
                                                 .build()))
                                         .tombstones(List.of())
                                         .childItems(List.of())
                                         .build()))
                                 .tombstones(List.of())
                                 .build();
    }

    private RecursiveQuantity quantity() {
        return RecursiveQuantity.builder().value(1.0).unit(ItemUnitEnumeration.UNIT_PIECE).build();
    }
}
