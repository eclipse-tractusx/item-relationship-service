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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobRequest;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Item Stock recursive aspect-selection integration")
class ItemStockRecursiveAspectSelectionIntegrationTest extends RecursiveIntegrationTestBase {

    @Test
    @DisplayName("Atlas->Belfast->Ceres->Delta returns explicitly selected anonymized stock")
    void shouldCollectExplicitAnonymizedItemStockAspect() {
        externalSystems.stubDiscovery(List.of(ATLAS_BPN, BELFAST_BPN, CERES_BPN, DELTA_BPN));
        final Map<String, String> atlasContracts = externalSystems.stubShell(purisManufacturerShell(ATLAS_ASSET, ATLAS_BPN,
                "AtlasPart", bomAsPlanned(ATLAS, ATLAS_ASSET, List.of(child(BELFAST_ASSET, BELFAST_BPN))),
                ATLAS, 11));
        final Map<String, String> belfastContracts = externalSystems.stubShell(purisManufacturerShell(BELFAST_ASSET, BELFAST_BPN,
                "BelfastPart", bomAsPlanned(BELFAST, BELFAST_ASSET, List.of(child(CERES_ASSET, CERES_BPN))),
                BELFAST, 7));
        final Map<String, String> ceresContracts = externalSystems.stubShell(purisManufacturerShell(CERES_ASSET, CERES_BPN,
                "CeresPart", bomAsPlanned(CERES, CERES_ASSET, List.of(child(DELTA_ASSET, DELTA_BPN))),
                CERES, 5));
        final Map<String, String> deltaContracts = externalSystems.stubShell(purisManufacturerShell(DELTA_ASSET, DELTA_BPN,
                "DeltaPart", bomAsPlanned(DELTA, DELTA_ASSET, List.of()), DELTA, 3));

        final RecursiveIrsInstance atlas = startInstance(ATLAS, ATLAS_BPN);
        final RecursiveIrsInstance belfast = startInstance(BELFAST, BELFAST_BPN);
        final RecursiveIrsInstance ceres = startInstance(CERES, CERES_BPN);
        final RecursiveIrsInstance delta = startInstance(DELTA, DELTA_BPN);
        seedEndpointDataReferences(atlas, atlasContracts.values());
        seedEndpointDataReferences(belfast, belfastContracts.values());
        seedEndpointDataReferences(ceres, ceresContracts.values());
        seedEndpointDataReferences(delta, deltaContracts.values());
        seedBidirectionalNotificationRoute(atlas, belfast);
        seedBidirectionalNotificationRoute(belfast, ceres);
        seedBidirectionalNotificationRoute(ceres, delta);
        client.registerGrant(atlas, grant(PURIS_USE_CASE, ATLAS_BPN, ATLAS_ASSET, Set.of(BELFAST_BPN)));
        client.registerGrant(belfast, grant(PURIS_USE_CASE, ATLAS_BPN, BELFAST_ASSET, Set.of(CERES_BPN)));
        client.registerGrant(ceres, grant(PURIS_USE_CASE, BELFAST_BPN, CERES_ASSET, Set.of(DELTA_BPN)));
        client.registerGrant(delta, grant(PURIS_USE_CASE, CERES_BPN, DELTA_ASSET, Set.of()));

        final String jobId = client.startRootJob(atlas, RecursiveJobRequest.builder()
                .openingId(OPENING_ID)
                .useCase(PURIS_USE_CASE)
                .bomLifecycle(BomLifecycle.AS_PLANNED)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                .globalAssetId(ATLAS_ASSET)
                .build());

        final RecursiveJobStatusResponse atlasJob = client.waitForTerminalJob(atlas, jobId);
        assertThat(atlasJob.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(atlasJob.getResult().getBomLifecycle()).isEqualTo(BomLifecycle.AS_PLANNED);
        assertThat(aspectIds(atlasJob.getResult())).containsOnly(ITEM_STOCK_ANONYMIZED);
        assertThat(quantities(atlasJob.getResult())).containsExactly(3, 5, 7);
        assertThat(tombstones(atlasJob)).isEmpty();
    }
}
