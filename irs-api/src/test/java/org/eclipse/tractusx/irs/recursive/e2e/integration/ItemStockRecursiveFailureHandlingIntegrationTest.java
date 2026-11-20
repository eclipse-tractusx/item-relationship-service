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
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.recursive.e2e.integration.RecursiveExternalSystems.Submodel;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobRequest;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Item Stock recursive failure-handling integration")
class ItemStockRecursiveFailureHandlingIntegrationTest extends RecursiveIntegrationTestBase {

    @Test
    @DisplayName("Malformed BOM entries preserve allowed children and anonymous incomplete results")
    void shouldReturnPartialResultForIncompleteBomsAcrossTiers() {
        externalSystems.stubDiscovery(List.of(ATLAS_BPN, BELFAST_BPN));
        final String atlasBom = """
                {
                  "catenaXId": "%s",
                  "childItems": [
                    {
                      "catenaXId": { "globalAssetId": "%s" },
                      "businessPartner": "%s"
                    },
                    {
                      "catenaXId": "%s"
                    },
                    {
                      "catenaXId": "%s",
                      "businessPartner": "%s",
                      "quantity": {
                        "value": 1,
                        "unit": "unit:piece"
                      }
                    }
                  ]
                }
                """.formatted(ATLAS_ASSET, CERES_ASSET, CERES_BPN, DELTA_ASSET, BELFAST_ASSET, BELFAST_BPN);
        final String belfastBom = """
                {
                  "catenaXId": "%s",
                  "childItems": [
                    null,
                    null
                  ]
                }
                """.formatted(BELFAST_ASSET);
        final Map<String, String> atlasContracts = externalSystems.stubShell(purisManufacturerShell(
                ATLAS_ASSET, ATLAS_BPN, "AtlasPart", new Submodel("SingleLevelBomAsPlanned",
                        SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0, "asset-atlas-bom-as-planned", "atlas-bom-as-planned",
                        atlasBom), ATLAS, 13));
        final Map<String, String> belfastContracts = externalSystems.stubShell(purisManufacturerShell(
                BELFAST_ASSET, BELFAST_BPN, "BelfastPart", new Submodel("SingleLevelBomAsPlanned",
                        SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0, "asset-belfast-bom-as-planned", "belfast-bom-as-planned",
                        belfastBom), BELFAST, 7));
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
        assertThat(atlasJob.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.PARTIAL);
        assertThat(quantities(atlasJob.getResult())).containsExactly(7);
        assertThat(atlasJob.getResult().getTombstones()).hasSize(1);
        assertThat(atlasJob.getResult().getRelatedItems()).singleElement().satisfies(child -> {
            assertThat(child.getRelatedItems()).isEmpty();
            assertThat(child.getTombstones()).isEmpty();
        });
        assertThat(tombstones(atlasJob)).hasSize(1).allSatisfy(tombstone -> {
            assertThat(tombstone.getReason()).isEqualTo(RecursiveTombstoneReason.BOM_INCOMPLETE);
            assertThat(tombstone.getDetail()).isEqualTo("The BOM is incomplete.");
            assertThat(tombstone.getOccurrences()).isEqualTo(1);
            assertThat(tombstone.getErrorRefs()).hasSize(1);
        });
        assertThat(new ObjectMapper().findAndRegisterModules().valueToTree(atlasJob.getResult()).toString())
                .doesNotContain(CERES_ASSET, CERES_BPN, DELTA_ASSET, DELTA_BPN, BELFAST_ASSET, BELFAST_BPN);
    }

    @Test
    @DisplayName("Atlas DTR failure after job acceptance becomes sanitized tombstone")
    void shouldStoreDigitalTwinRequestFailureAsSanitizedTombstoneAfterJobAcceptance() {
        externalSystems.stubDiscovery(List.of(ATLAS_BPN));
        externalSystems.stubShellLookupFailure(ATLAS_ASSET);

        final RecursiveIrsInstance atlas = startInstance(ATLAS, ATLAS_BPN);
        client.registerGrant(atlas, grant(PURIS_USE_CASE, ATLAS_BPN, ATLAS_ASSET, Set.of()));

        final String jobId = client.startRootJob(atlas, RecursiveJobRequest.builder()
                .openingId(OPENING_ID)
                .useCase(PURIS_USE_CASE)
                .globalAssetId(ATLAS_ASSET)
                .build());

        final RecursiveJobStatusResponse atlasJob = client.waitForTerminalJob(atlas, jobId);
        assertThat(atlasJob.getJob().getState()).isEqualTo(JobState.ERROR);
        assertThat(atlasJob.getJob().getException().getException()).isEqualTo("LOCAL_ASPECT_REQUEST_FAILED");
        assertThat(tombstones(atlasJob))
                .singleElement()
                .satisfies(tombstone -> {
                    assertThat(tombstone.getReason())
                            .isEqualTo(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED);
                    assertThat(tombstone.getAspects()).containsExactlyInAnyOrder(
                            ITEM_STOCK_ANONYMIZED,
                            DELIVERY_INFORMATION_ANONYMIZED,
                            PLANNED_PRODUCTION_OUTPUT_ANONYMIZED);
                    assertThat(tombstone.getDetail())
                            .doesNotContain("http://")
                            .doesNotContain("BPNL")
                            .doesNotContain("urn:uuid")
                            .doesNotContain("atlas-bom-asset");
                });
    }

    @Test
    @DisplayName("Atlas->Belfast notification delivery failure becomes tombstone")
    void shouldStoreNotificationDeliveryFailureAsTombstone() {
        externalSystems.stubDiscovery(List.of(ATLAS_BPN, BELFAST_BPN));
        final Map<String, String> atlasContracts = externalSystems.stubShell(purisManufacturerShell(ATLAS_ASSET, ATLAS_BPN,
                "AtlasPart", bomAsPlanned(ATLAS, ATLAS_ASSET, List.of(child(BELFAST_ASSET, BELFAST_BPN))),
                ATLAS, 13));
        final RecursiveIrsInstance atlas = startInstance(ATLAS, ATLAS_BPN);
        seedEndpointDataReferences(atlas, atlasContracts.values());
        client.registerGrant(atlas, grant(PURIS_USE_CASE, ATLAS_BPN, ATLAS_ASSET, Set.of(BELFAST_BPN)));

        final String jobId = client.startRootJob(atlas, RecursiveJobRequest.builder()
                .openingId(OPENING_ID)
                .useCase(PURIS_USE_CASE)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                .globalAssetId(ATLAS_ASSET)
                .build());

        final RecursiveJobStatusResponse atlasJob = client.waitForTerminalJob(atlas, jobId);
        assertThat(atlasJob.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(atlasJob.getResult().getResultStatus()).isEqualTo(RecursiveResultStatus.FAILED);
        assertThat(tombstoneReasons(atlasJob))
                .containsExactly(RecursiveTombstoneReason.CHILD_BRANCH_FAILED);
    }

    @Test
    @DisplayName("Atlas->Belfast rejected ItemStock policy becomes usage-policy tombstone")
    void shouldStoreRejectedProviderPolicyAsUsagePolicyTombstone() {
        externalSystems.stubDiscovery(List.of(ATLAS_BPN, BELFAST_BPN));
        final Map<String, String> atlasContracts = externalSystems.stubShell(purisManufacturerShell(ATLAS_ASSET, ATLAS_BPN,
                "AtlasPart", bomAsPlanned(ATLAS, ATLAS_ASSET, List.of(child(BELFAST_ASSET, BELFAST_BPN))),
                ATLAS, 13));
        final Map<String, String> belfastContracts = externalSystems.stubShell(purisManufacturerShell(BELFAST_ASSET, BELFAST_BPN,
                "BelfastPart", bomAsPlanned(BELFAST, BELFAST_ASSET, List.of()), BELFAST, 7));
        externalSystems.rejectPolicyForAsset("asset-" + BELFAST + "-item-stock-anonymized");

        final RecursiveIrsInstance atlas = startInstance(ATLAS, ATLAS_BPN);
        final RecursiveIrsInstance belfast = startInstance(BELFAST, BELFAST_BPN);
        seedEndpointDataReferences(atlas, atlasContracts.values());
        seedEndpointDataReference(belfast, belfastContracts.get("asset-" + BELFAST + "-bom-as-planned"),
                externalSystems.baseUrl() + "/unused-edr");
        seedEndpointDataReference(belfast,
                belfastContracts.get("asset-" + BELFAST + "-part-type-information"),
                externalSystems.baseUrl() + "/unused-edr");
        seedEndpointDataReference(belfast,
                belfastContracts.get("asset-" + BELFAST + "-delivery-information-anonymized"),
                externalSystems.baseUrl() + "/unused-edr");
        seedEndpointDataReference(belfast,
                belfastContracts.get("asset-" + BELFAST + "-planned-production-output-anonymized"),
                externalSystems.baseUrl() + "/unused-edr");
        seedBidirectionalNotificationRoute(atlas, belfast);
        client.registerGrant(atlas, grant(PURIS_USE_CASE, ATLAS_BPN, ATLAS_ASSET, Set.of(BELFAST_BPN)));
        client.registerGrant(belfast, grant(PURIS_USE_CASE, ATLAS_BPN, BELFAST_ASSET, Set.of()));

        final String jobId = client.startRootJob(atlas, RecursiveJobRequest.builder()
                .openingId(OPENING_ID)
                .useCase(PURIS_USE_CASE)
                .globalAssetId(ATLAS_ASSET)
                .build());

        final RecursiveJobStatusResponse atlasJob = client.waitForTerminalJob(atlas, jobId);
        assertThat(atlasJob.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(tombstoneReasons(atlasJob))
                .containsExactly(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED);
        assertThat(quantities(atlasJob.getResult())).isEmpty();
    }
}
