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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobRequest;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Item Stock recursive grant integration")
class ItemStockRecursiveGrantIntegrationTest extends RecursiveIntegrationTestBase {

    @Test
    @DisplayName("Atlas grant allows Belfast/Ceres, Ceres grant allows Delta")
    void shouldFilterBranchingSupplyChainWithEnabledChainOpeningGrants() {
        externalSystems.stubDiscovery(List.of(ATLAS_BPN, BELFAST_BPN, CERES_BPN, DELTA_BPN));
        final Map<String, String> atlasContracts = externalSystems.stubShell(purisManufacturerShell(ATLAS_ASSET, ATLAS_BPN,
                "AtlasPart", bomAsPlanned(ATLAS, ATLAS_ASSET, List.of(
                        child(BELFAST_ASSET, BELFAST_BPN),
                        child(CERES_ASSET, CERES_BPN),
                        child(DELTA_ASSET, DELTA_BPN))),
                ATLAS, 13));
        final Map<String, String> belfastContracts = externalSystems.stubShell(purisManufacturerShell(BELFAST_ASSET, BELFAST_BPN,
                "BelfastPart", bomAsPlanned(BELFAST, BELFAST_ASSET, List.of()), BELFAST, 7));
        final Map<String, String> ceresContracts = externalSystems.stubShell(purisManufacturerShell(CERES_ASSET, CERES_BPN,
                "CeresPart", bomAsPlanned(CERES, CERES_ASSET, List.of(child(DELTA_ASSET, DELTA_BPN))),
                CERES, 5));
        final Map<String, String> deltaContracts = externalSystems.stubShell(purisManufacturerShell(DELTA_ASSET, DELTA_BPN,
                "DeltaPart", bomAsPlanned(DELTA, DELTA_ASSET, List.of()), DELTA, 3));

        final RecursiveIrsInstance atlas = startInstanceWithGrantValidation(ATLAS, ATLAS_BPN);
        final RecursiveIrsInstance belfast = startInstanceWithGrantValidation(BELFAST, BELFAST_BPN);
        final RecursiveIrsInstance ceres = startInstanceWithGrantValidation(CERES, CERES_BPN);
        final RecursiveIrsInstance delta = startInstanceWithGrantValidation(DELTA, DELTA_BPN);
        seedEndpointDataReferences(atlas, atlasContracts.values());
        seedEndpointDataReferences(belfast, belfastContracts.values());
        seedEndpointDataReferences(ceres, ceresContracts.values());
        seedEndpointDataReferences(delta, deltaContracts.values());
        seedBidirectionalNotificationRoute(atlas, belfast);
        seedBidirectionalNotificationRoute(atlas, ceres);
        seedBidirectionalNotificationRoute(ceres, delta);
        client.registerGrant(atlas, grant(PURIS_USE_CASE, ATLAS_BPN, ATLAS_ASSET, Set.of(BELFAST_BPN, CERES_BPN)));
        client.registerGrant(belfast, grant(PURIS_USE_CASE, ATLAS_BPN, BELFAST_ASSET, Set.of()));
        client.registerGrant(ceres, grant(PURIS_USE_CASE, ATLAS_BPN, CERES_ASSET, Set.of(DELTA_BPN)));
        client.registerGrant(delta, grant(PURIS_USE_CASE, CERES_BPN, DELTA_ASSET, Set.of()));

        final String jobId = client.startRootJob(atlas, RecursiveJobRequest.builder()
                .openingId(OPENING_ID)
                .useCase(PURIS_USE_CASE)
                .globalAssetId(ATLAS_ASSET)
                .build());

        final RecursiveJobStatusResponse atlasJob = client.waitForTerminalJob(atlas, jobId);
        assertThat(atlasJob.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(atlasJob.getJob().getAsyncFetchedItems().getCompleted()).isEqualTo(2);
        assertThat(atlasJob.getJob().getAsyncFetchedItems().getRunning()).isZero();
        assertThat(quantities(atlasJob.getResult())).containsExactly(3, 5, 7);
        assertThat(tombstones(atlasJob)).isEmpty();
        assertThat(client.jobs(belfast)).hasSize(1);
        assertThat(client.jobs(ceres)).hasSize(1);
        assertThat(client.jobs(delta)).hasSize(1);
    }

    @Test
    @DisplayName("Atlas rejects job before creation when opening grant is missing")
    void shouldRejectRootJobBeforeCreationWhenGrantValidationIsEnabledAndGrantIsMissing() {
        final RecursiveIrsInstance atlas = startInstanceWithGrantValidation(ATLAS, ATLAS_BPN);

        assertThatThrownBy(() -> client.startRootJob(atlas, RecursiveJobRequest.builder()
                .openingId("missing-opening-id")
                .useCase(PURIS_USE_CASE)
                .globalAssetId(ATLAS_ASSET)
                .build()))
                .isInstanceOf(HttpClientErrorException.Forbidden.class);
        assertThat(client.jobs(atlas)).isEmpty();
    }
}
