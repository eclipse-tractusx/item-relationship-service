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

import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.recursive.model.ItemUnitEnumeration;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobRequest;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Item Stock recursive supply-chain integration")
class ItemStockRecursiveSupplyChainIntegrationTest extends RecursiveIntegrationTestBase {

    @Test
    @DisplayName("Atlas->Belfast->Ceres->Delta->Echo returns stock 2,3,5,7")
    void shouldCollectItemStockAcrossFiveInstanceLinearSupplyChain() {
        externalSystems.stubDiscovery(List.of(ATLAS_BPN, BELFAST_BPN, CERES_BPN, DELTA_BPN, ECHO_BPN));
        final Map<String, String> atlasContracts = externalSystems.stubShell(purisManufacturerShell(ATLAS_ASSET, ATLAS_BPN,
                "AtlasPart", bomAsPlanned(ATLAS, ATLAS_ASSET, List.of(child(BELFAST_ASSET, BELFAST_BPN))),
                ATLAS, 13));
        final Map<String, String> belfastContracts = externalSystems.stubShell(purisManufacturerShell(BELFAST_ASSET, BELFAST_BPN,
                "BelfastPart", bomAsPlanned(BELFAST, BELFAST_ASSET, List.of(child(CERES_ASSET, CERES_BPN))),
                BELFAST, 7));
        final Map<String, String> ceresContracts = externalSystems.stubShell(purisManufacturerShell(CERES_ASSET, CERES_BPN,
                "CeresPart", bomAsPlanned(CERES, CERES_ASSET, List.of(child(DELTA_ASSET, DELTA_BPN))),
                CERES, 5));
        final Map<String, String> deltaContracts = externalSystems.stubShell(purisManufacturerShell(DELTA_ASSET, DELTA_BPN,
                "DeltaPart", bomAsPlanned(DELTA, DELTA_ASSET, List.of(child(ECHO_ASSET, ECHO_BPN))),
                DELTA, 3));
        final Map<String, String> echoContracts = externalSystems.stubShell(purisManufacturerShell(ECHO_ASSET, ECHO_BPN,
                "EchoPart", bomAsPlanned(ECHO, ECHO_ASSET, List.of()), ECHO, 2));

        final RecursiveIrsInstance atlas = startInstance(ATLAS, ATLAS_BPN);
        final RecursiveIrsInstance belfast = startInstance(BELFAST, BELFAST_BPN);
        final RecursiveIrsInstance ceres = startInstance(CERES, CERES_BPN);
        final RecursiveIrsInstance delta = startInstance(DELTA, DELTA_BPN);
        final RecursiveIrsInstance echo = startInstance(ECHO, ECHO_BPN);
        seedEndpointDataReferences(atlas, atlasContracts.values());
        seedEndpointDataReferences(belfast, belfastContracts.values());
        seedEndpointDataReferences(ceres, ceresContracts.values());
        seedEndpointDataReferences(delta, deltaContracts.values());
        seedEndpointDataReferences(echo, echoContracts.values());
        seedBidirectionalNotificationRoute(atlas, belfast);
        seedBidirectionalNotificationRoute(belfast, ceres);
        seedBidirectionalNotificationRoute(ceres, delta);
        seedBidirectionalNotificationRoute(delta, echo);
        client.registerGrant(atlas, grant(PURIS_USE_CASE, ATLAS_BPN, ATLAS_ASSET, Set.of(BELFAST_BPN)));
        client.registerGrant(belfast, grant(PURIS_USE_CASE, ATLAS_BPN, BELFAST_ASSET, Set.of(CERES_BPN)));
        client.registerGrant(ceres, grant(PURIS_USE_CASE, BELFAST_BPN, CERES_ASSET, Set.of(DELTA_BPN)));
        client.registerGrant(delta, grant(PURIS_USE_CASE, CERES_BPN, DELTA_ASSET, Set.of(ECHO_BPN)));
        client.registerGrant(echo, grant(PURIS_USE_CASE, DELTA_BPN, ECHO_ASSET, Set.of()));

        final String jobId = client.startRootJob(atlas, RecursiveJobRequest.builder()
                .openingId(OPENING_ID)
                .useCase(PURIS_USE_CASE)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                .globalAssetId(ATLAS_ASSET)
                .build());

        final RecursiveJobStatusResponse atlasJob = client.waitForTerminalJob(atlas, jobId);
        assertThat(atlasJob.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(atlasJob.getJob().getAsyncFetchedItems().getCompleted()).isEqualTo(1);
        assertThat(atlasJob.getJob().getAsyncFetchedItems().getRunning()).isZero();
        assertThat(quantities(atlasJob.getResult())).containsExactly(2, 3, 5, 7);
        final RecursiveChildItem belfastNode = atlasJob.getResult().getChildItems().get(0);
        final RecursiveChildItem ceresNode = belfastNode.getChildItems().get(0);
        final RecursiveChildItem deltaNode = ceresNode.getChildItems().get(0);
        final RecursiveChildItem echoNode = deltaNode.getChildItems().get(0);
        assertThat(belfastNode.getMaterialNumber()).isEqualTo("MNR-BELFAST");
        assertThat(belfastNode.getMaterialName()).isEqualTo("Material belfast");
        assertThat(belfastNode.getQuantity().getValue()).isEqualTo(1.0);
        assertThat(belfastNode.getQuantity().getUnit()).isEqualTo(ItemUnitEnumeration.UNIT_PIECE);
        assertThat(ceresNode.getMaterialNumber()).isEqualTo("MNR-CERES");
        assertThat(deltaNode.getMaterialNumber()).isEqualTo("MNR-DELTA");
        assertThat(echoNode.getMaterialNumber()).isEqualTo("MNR-ECHO");
        assertThat(echoNode.getChildItems()).isEmpty();
        assertThat(tombstones(atlasJob)).isEmpty();
        assertThat(client.jobs(belfast)).hasSize(1);
        assertThat(client.jobs(ceres)).hasSize(1);
        assertThat(client.jobs(delta)).hasSize(1);
        assertThat(client.jobs(echo)).hasSize(1);
    }

    @Test
    @DisplayName("Atlas->Belfast tries connector endpoints in deterministic order until one succeeds")
    void shouldUseFirstWorkingConnectorEndpointInDeterministicOrder() {
        final String missingBelfastConnectorEndpointOne = externalSystems.baseUrl() + "/aaa-missing-belfast";
        final String missingBelfastConnectorEndpointTwo = externalSystems.baseUrl() + "/bbb-missing-belfast";
        final String belfastConnectorEndpoint = externalSystems.edcConnectorEndpoint(BELFAST_BPN);

        externalSystems.stubDiscovery(Map.of(
                ATLAS_BPN, List.of(externalSystems.edcConnectorEndpoint(ATLAS_BPN)),
                BELFAST_BPN, List.of(
                        missingBelfastConnectorEndpointOne,
                        missingBelfastConnectorEndpointTwo,
                        belfastConnectorEndpoint)));
        externalSystems.rejectCatalogRequestsForConnectorEndpoint(missingBelfastConnectorEndpointOne);
        externalSystems.rejectCatalogRequestsForConnectorEndpoint(missingBelfastConnectorEndpointTwo);
        final Map<String, String> atlasContracts = externalSystems.stubShell(purisManufacturerShell(ATLAS_ASSET, ATLAS_BPN,
                "AtlasPart", bomAsPlanned(ATLAS, ATLAS_ASSET, List.of(child(BELFAST_ASSET, BELFAST_BPN))),
                ATLAS, 13));
        final Map<String, String> belfastContracts = externalSystems.stubShell(purisManufacturerShell(BELFAST_ASSET,
                BELFAST_BPN, "BelfastPart", bomAsPlanned(BELFAST, BELFAST_ASSET, List.of()), BELFAST, 7));

        final RecursiveIrsInstance atlas = startInstance(ATLAS, ATLAS_BPN);
        final RecursiveIrsInstance belfast = startInstance(BELFAST, BELFAST_BPN);
        seedEndpointDataReferences(atlas, atlasContracts.values());
        seedEndpointDataReferences(belfast, belfastContracts.values());
        seedBidirectionalNotificationRoute(atlas, belfast);
        client.registerGrant(atlas, grant(PURIS_USE_CASE, ATLAS_BPN, ATLAS_ASSET, Set.of(BELFAST_BPN)));
        client.registerGrant(belfast, grant(PURIS_USE_CASE, ATLAS_BPN, BELFAST_ASSET, Set.of()));

        final String jobId = client.startRootJob(atlas, RecursiveJobRequest.builder()
                .openingId(OPENING_ID)
                .useCase(PURIS_USE_CASE)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                .globalAssetId(ATLAS_ASSET)
                .build());

        final RecursiveJobStatusResponse atlasJob = client.waitForTerminalJob(atlas, jobId);
        assertThat(atlasJob.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(atlasJob.getJob().getAsyncFetchedItems().getCompleted()).isEqualTo(1);
        assertThat(atlasJob.getJob().getAsyncFetchedItems().getRunning()).isZero();
        assertThat(quantities(atlasJob.getResult())).containsExactly(7);
        assertThat(tombstones(atlasJob)).isEmpty();
        assertThat(client.jobs(belfast)).hasSize(1);
        externalSystems.verifyCatalogRequestedForConnectorEndpoint(missingBelfastConnectorEndpointOne);
        externalSystems.verifyCatalogRequestedForConnectorEndpoint(missingBelfastConnectorEndpointTwo);
        externalSystems.verifyCatalogRequestedForConnectorEndpoint(belfastConnectorEndpoint);
    }

    @Test
    @DisplayName("Atlas branches to Belfast/Ceres/Delta according to its grant allow-list")
    void shouldCollectItemStockAcrossBranchingSupplyChainWithExplicitGrants() {
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
                "CeresPart", bomAsPlanned(CERES, CERES_ASSET, List.of()), CERES, 2, 5));
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
        seedBidirectionalNotificationRoute(atlas, ceres);
        seedBidirectionalNotificationRoute(atlas, delta);
        client.registerGrant(atlas, grant(PURIS_USE_CASE, ATLAS_BPN, ATLAS_ASSET,
                Set.of(BELFAST_BPN, CERES_BPN, DELTA_BPN)));
        client.registerGrant(belfast, grant(PURIS_USE_CASE, ATLAS_BPN, BELFAST_ASSET, Set.of()));
        client.registerGrant(ceres, grant(PURIS_USE_CASE, ATLAS_BPN, CERES_ASSET, Set.of()));
        client.registerGrant(delta, grant(PURIS_USE_CASE, ATLAS_BPN, DELTA_ASSET, Set.of()));

        final String jobId = client.startRootJob(atlas, RecursiveJobRequest.builder()
                .openingId(OPENING_ID)
                .useCase(PURIS_USE_CASE)
                .globalAssetId(ATLAS_ASSET)
                .build());

        final RecursiveJobStatusResponse atlasJob = client.waitForTerminalJob(atlas, jobId);
        assertThat(atlasJob.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(atlasJob.getJob().getAsyncFetchedItems().getCompleted()).isEqualTo(3);
        assertThat(atlasJob.getJob().getAsyncFetchedItems().getRunning()).isZero();
        assertThat(quantities(atlasJob.getResult())).containsExactly(2, 3, 5, 7);
        assertThat(aspectIds(atlasJob.getResult()))
                .containsExactlyInAnyOrder(
                        ITEM_STOCK_ANONYMIZED,
                        DELIVERY_INFORMATION_ANONYMIZED,
                        PLANNED_PRODUCTION_OUTPUT_ANONYMIZED);
        assertThat(tombstones(atlasJob)).isEmpty();
        assertThat(client.jobs(belfast)).hasSize(1);
        assertThat(client.jobs(ceres)).hasSize(1);
        assertThat(client.jobs(delta)).hasSize(1);
    }
}
