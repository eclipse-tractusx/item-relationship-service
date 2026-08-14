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
import java.util.UUID;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobPhase;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.store.RecursiveJobStateStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Recursive job deadline integration")
class RecursiveJobDeadlineIntegrationTest extends RecursiveIntegrationTestBase {

    @Test
    @DisplayName("Scheduler fails a non-terminal job after the global job deadline")
    void shouldFailNonTerminalJobWhenSchedulerDetectsExpiredDeadline() {
        final RecursiveIrsInstance atlas = startInstance(ATLAS, ATLAS_BPN);
        final String jobId = "68904173-ad59-4a77-8412-3e73fcafbd86";
        final ZonedDateTime now = ZonedDateTime.now();
        atlas.context().getBean(RecursiveJobStateStore.class).save(RecursiveJobState.builder()
                .jobId(UUID.fromString(jobId))
                .openingId(OPENING_ID)
                .useCase(PURIS_USE_CASE)
                .globalAssetId(ATLAS_ASSET)
                .bomLifecycle(BomLifecycle.AS_PLANNED)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                .requesterBpnl(ATLAS_BPN)
                .receiverBpnl(ATLAS_BPN)
                .messageId("deadline-exceeded-message")
                .createdOn(now.minusMinutes(5))
                .lastModifiedOn(now.minusMinutes(5))
                .deadline(now.minusSeconds(1))
                .childResponseDeadline(now.minusSeconds(1))
                .state(RecursiveJobPhase.GRANT_CHECKED)
                .rootJob(true)
                .bomChildren(List.of())
                .childBranches(List.of())
                .build());

        await().pollDelay(Duration.ZERO)
                .pollInterval(Duration.ofMillis(100))
                .timeout(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    final RecursiveJobStatusResponse status = client.jobStatus(atlas, jobId);
                    assertThat(status.getJob().getState()).isEqualTo(JobState.ERROR);
                    assertThat(status.getJob().getException().getExceptionDate()).isNotNull();
                    assertThat(status.getJob().getException().getException())
                            .isEqualTo(RecursiveTombstoneReason.RECURSIVE_DEADLINE_EXCEEDED.name());
                });
    }
}
