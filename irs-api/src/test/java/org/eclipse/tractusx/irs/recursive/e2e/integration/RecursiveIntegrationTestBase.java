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

import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0;
import static org.eclipse.tractusx.irs.recursive.e2e.testcontainers.RecursivePartnerContainers.containersFor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tractusx.irs.edc.client.storage.EndpointDataReferenceStorage;
import org.eclipse.tractusx.irs.recursive.e2e.integration.RecursiveExternalSystems.Shell;
import org.eclipse.tractusx.irs.recursive.e2e.integration.RecursiveExternalSystems.Submodel;
import org.eclipse.tractusx.irs.recursive.e2e.testcontainers.RecursivePartnerContainers;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base fixture for recursive end-to-end tests.
 * It starts isolated IRS instances, seeds EDC routes and builds the DTR/submodel payloads used by the test chain.
 */
abstract class RecursiveIntegrationTestBase {

    public static final String RECURSIVE_NOTIFICATION_ASSET_ID = "irs-recursive-notification-asset";
    private static final String RECURSIVE_NOTIFICATION_API_TYPE =
            "https://w3id.org/catenax/taxonomy#RecursiveIrsNotificationApi";
    private static final String RECURSIVE_NOTIFICATION_API_VERSION = "1.0";

    protected static final String OPENING_ID = "recursive-irs-demo-opening";
    protected static final RecursiveUseCase PURIS_USE_CASE =
            RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE;
    protected static final String UNSUPPORTED_ASPECT =
            "urn:samm:io.catenax.unsupported:1.0.0#UnsupportedAspect";
    protected static final String ITEM_STOCK = "urn:samm:io.catenax.item_stock:2.0.0#ItemStock";
    protected static final String ITEM_STOCK_ANONYMIZED =
            RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId();
    protected static final String DELIVERY_INFORMATION_ANONYMIZED =
            RecursiveAspect.DELIVERY_INFORMATION_ANONYMIZED.getSemanticId();
    protected static final String PLANNED_PRODUCTION_OUTPUT_ANONYMIZED =
            RecursiveAspect.PLANNED_PRODUCTION_OUTPUT_ANONYMIZED.getSemanticId();
    protected static final String PART_TYPE_INFORMATION =
            "urn:samm:io.catenax.part_type_information:1.0.0#PartTypeInformation";

    protected static final String ATLAS = "atlas";
    protected static final String BELFAST = "belfast";
    protected static final String CERES = "ceres";
    protected static final String DELTA = "delta";
    protected static final String ECHO = "echo";

    protected static final String ATLAS_BPN = "BPNL0000ATLS0001";
    protected static final String BELFAST_BPN = "BPNL0000BELF0001";
    protected static final String CERES_BPN = "BPNL0000CERS0001";
    protected static final String DELTA_BPN = "BPNL0000DLTA0001";
    protected static final String ECHO_BPN = "BPNL0000ECHO0001";

    protected static final String ATLAS_ASSET = "urn:uuid:aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1";
    protected static final String BELFAST_ASSET = "urn:uuid:bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb2";
    protected static final String CERES_ASSET = "urn:uuid:cccccccc-cccc-4ccc-8ccc-ccccccccccc3";
    protected static final String DELTA_ASSET = "urn:uuid:dddddddd-dddd-4ddd-8ddd-ddddddddddd4";
    protected static final String ECHO_ASSET = "urn:uuid:eeeeeeee-eeee-4eee-8eee-eeeeeeeeeee5";

    protected final RecursiveIntegrationTestClient client = new RecursiveIntegrationTestClient();
    protected final Map<String, RecursiveIrsInstance> instances = new LinkedHashMap<>();

    protected RecursiveExternalSystems externalSystems;

    @BeforeEach
    void setUpExternalSystems() {
        externalSystems = new RecursiveExternalSystems();
        externalSystems.start();
    }

    @AfterEach
    void tearDownIntegrationStack() {
        instances.values()
                .stream()
                .sorted(Comparator.comparing(RecursiveIrsInstance::id).reversed())
                .forEach(RecursiveIrsInstance::close);
        if (externalSystems != null) {
            externalSystems.close();
        }
        instances.clear();
    }

    protected RecursiveIrsInstance startInstance(final String id, final String bpnl) {
        return launchInstance(id, bpnl);
    }

    protected RecursiveIrsInstance startInstanceWithGrantValidation(final String id, final String bpnl) {
        return launchInstance(id, bpnl);
    }

    private RecursiveIrsInstance launchInstance(final String id, final String bpnl) {
        final RecursivePartnerContainers partnerContainers = containersFor("ri-" + id);
        partnerContainers.start();
        final RecursiveIrsInstance instance =
                RecursiveIrsInstanceLauncher.start(id, bpnl, partnerContainers, externalSystems);
        instances.put(id, instance);
        return instance;
    }

    protected void seedEndpointDataReferences(final RecursiveIrsInstance instance,
            final Iterable<String> contractAgreementIds) {
        for (final String contractAgreementId : contractAgreementIds) {
            seedEndpointDataReference(instance, contractAgreementId, externalSystems.baseUrl() + "/unused-edr");
        }
    }

    protected void seedNotificationRoute(final RecursiveIrsInstance sender, final String receiverBpnl,
            final String receiverBaseUrl) {
        externalSystems.stubEdcAssetByTypeAndVersion(RECURSIVE_NOTIFICATION_ASSET_ID,
                RECURSIVE_NOTIFICATION_API_TYPE, RECURSIVE_NOTIFICATION_API_VERSION);
        final String storageId = RECURSIVE_NOTIFICATION_ASSET_ID + externalSystems.edcDspEndpoint(receiverBpnl);
        seedEndpointDataReference(sender, storageId, receiverBaseUrl + "/irs/recursive/notifications");
    }

    protected void seedBidirectionalNotificationRoute(final RecursiveIrsInstance sender,
            final RecursiveIrsInstance receiver) {
        seedNotificationRoute(sender, receiver.bpnl(), receiver.baseUrl());
        seedNotificationRoute(receiver, sender.bpnl(), sender.baseUrl());
    }

    protected void seedEndpointDataReference(final RecursiveIrsInstance instance, final String storageId,
            final String endpoint) {
        instance.context()
                .getBean(EndpointDataReferenceStorage.class)
                .put(storageId, externalSystems.endpointDataReference(storageId, endpoint));
    }

    protected RecursiveChainOpeningGrant grant(final RecursiveUseCase useCase, final String requesterBpn,
            final String globalAssetId, final Set<String> allowedBpnls) {
        return RecursiveChainOpeningGrant.builder()
                .openingId(OPENING_ID)
                .useCase(useCase)
                .requesterBpn(requesterBpn)
                .globalAssetId(globalAssetId)
                .allowedBpnlSet(allowedBpnls)
                .validFrom(ZonedDateTime.now().minusMinutes(5))
                .validTo(ZonedDateTime.now().plusHours(1))
                .build();
    }

    protected Shell shell(final String globalAssetId, final String bpnl, final String idShort,
            final Submodel... submodels) {
        return new Shell(globalAssetId, bpnl, idShort, List.of(submodels));
    }

    /**
     * Creates the manufacturer-side PURIS shell used by a supplier tier in the recursive test chain.
     */
    protected Shell purisManufacturerShell(final String globalAssetId, final String bpnl, final String idShort,
            final Submodel bom, final String partnerId, final int... itemStockQuantities) {
        return shell(globalAssetId, bpnl, idShort, bom,
                partTypeInformation(partnerId),
                itemStockAnonymized(partnerId, globalAssetId, itemStockQuantities),
                deliveryInformationAnonymized(partnerId, globalAssetId, itemStockQuantities),
                plannedProductionOutputAnonymized(partnerId, globalAssetId, itemStockQuantities));
    }

    protected Submodel bomAsPlanned(final String partnerId, final String parentAsset, final List<Child> children) {
        return new Submodel("SingleLevelBomAsPlanned", SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0,
                "asset-" + partnerId + "-bom-as-planned", partnerId + "-bom-as-planned",
                singleLevelBomPayload(parentAsset, children));
    }

    protected Submodel itemStockAnonymized(final String partnerId, final String materialGlobalAssetId,
            final int... quantities) {
        return new Submodel("ItemStockAnonymized", ITEM_STOCK_ANONYMIZED,
                "asset-" + partnerId + "-item-stock-anonymized", partnerId + "-item-stock-anonymized",
                itemStockAnonymizedPayload(materialGlobalAssetId, quantities));
    }

    protected Submodel partTypeInformation(final String partnerId) {
        return new Submodel("PartTypeInformation", PART_TYPE_INFORMATION,
                "asset-" + partnerId + "-part-type-information", partnerId + "-part-type-information",
                """
                        {
                          "manufacturerPartId": "MNR-%s",
                          "nameAtManufacturer": "Material %s"
                        }
                        """.formatted(partnerId.toUpperCase(), partnerId));
    }

    protected Submodel deliveryInformationAnonymized(final String partnerId, final String materialGlobalAssetId,
            final int... quantities) {
        return new Submodel("DeliveryInformationAnonymized", DELIVERY_INFORMATION_ANONYMIZED,
                "asset-" + partnerId + "-delivery-information-anonymized",
                partnerId + "-delivery-information-anonymized",
                deliveryInformationAnonymizedPayload(materialGlobalAssetId, quantities));
    }

    protected Submodel plannedProductionOutputAnonymized(final String partnerId, final String materialGlobalAssetId,
            final int... quantities) {
        return new Submodel("PlannedProductionOutputAnonymized", PLANNED_PRODUCTION_OUTPUT_ANONYMIZED,
                "asset-" + partnerId + "-planned-production-output-anonymized",
                partnerId + "-planned-production-output-anonymized",
                plannedProductionOutputAnonymizedPayload(materialGlobalAssetId, quantities));
    }

    protected Child child(final String globalAssetId, final String bpnl) {
        return new Child(globalAssetId, bpnl);
    }

    protected List<Integer> quantities(final RecursiveJobResult result) {
        final Map<String, List<Object>> aspectPayloads = aspectPayloadsByType(result);
        final Object anonymizedItemStockValues = aspectPayloads.get(ITEM_STOCK_ANONYMIZED);
        if (anonymizedItemStockValues instanceof final List<?> anonymizedItemStockPayloads) {
            return anonymizedItemStockQuantities(anonymizedItemStockPayloads);
        }
        return List.of();
    }

    protected Set<String> aspectIds(final RecursiveJobResult result) {
        return aspectPayloadsByType(result).keySet();
    }

    private static Map<String, List<Object>> aspectPayloadsByType(final RecursiveJobResult result) {
        final Map<String, List<Object>> grouped = new LinkedHashMap<>();
        collectAspectPayloads(result.getChildItems(), grouped);
        return grouped;
    }

    private static void collectAspectPayloads(final List<RecursiveChildItem> childItems,
            final Map<String, List<Object>> grouped) {
        for (final RecursiveChildItem child : childItems == null ? List.<RecursiveChildItem>of() : childItems) {
            if (child.getItems() != null) {
                child.getItems().forEach(item -> grouped.computeIfAbsent(item.getAspect(), key -> new ArrayList<>())
                        .add(item.getItems()));
            }
            collectAspectPayloads(child.getChildItems(), grouped);
        }
    }

    private List<Integer> anonymizedItemStockQuantities(final List<?> itemStockPayloads) {
        if (itemStockPayloads.isEmpty()) {
            return List.of();
        }

        final List<Integer> quantities = new ArrayList<>();
        for (final Object itemStock : itemStockPayloads) {
            final Map<?, ?> itemStockMap = (Map<?, ?>) itemStock;
            for (final Object stock : (List<?>) itemStockMap.get("allocatedStocks")) {
                final Map<?, ?> stockMap = (Map<?, ?>) stock;
                final Map<?, ?> quantity = (Map<?, ?>) stockMap.get("quantityOnAllocatedStock");
                quantities.add(((Number) quantity.get("value")).intValue());
            }
        }
        return quantities.stream().sorted().toList();
    }

    protected List<RecursiveTombstoneReason> tombstoneReasons(final RecursiveJobStatusResponse response) {
        return tombstones(response).stream()
                .map(RecursiveTombstone::getReason)
                .toList();
    }

    protected List<RecursiveTombstone> tombstones(final RecursiveJobStatusResponse response) {
        final List<RecursiveTombstone> tombstones = new ArrayList<>(response.getResult().getTombstones());
        collectTombstones(response.getResult().getChildItems(), tombstones);
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

    private String singleLevelBomPayload(final String parentAsset, final List<Child> children) {
        return """
                {
                  "catenaXId": "%s",
                  "childItems": [
                    %s
                  ]
                }
                """.formatted(parentAsset, children.stream()
                .map(this::singleLevelBomChildPayload)
                .collect(Collectors.joining(",\n")));
    }

    private String singleLevelBomChildPayload(final Child child) {
        return """
                    {
                      "catenaXId": "%s",
                      "businessPartner": "%s",
                      "hasAlternatives": false,
                      "createdOn": "2026-05-05T00:00:00Z",
                      "lastModifiedOn": "2026-05-05T00:00:00Z",
                      "quantity": {
                        "value": 1.0,
                        "unit": "unit:piece"
                      }
                    }""".formatted(child.globalAssetId(), child.bpnl());
    }

    private String itemStockAnonymizedPayload(final String materialGlobalAssetId, final int... quantities) {
        final String allocatedStocks = Arrays.stream(quantities)
                .mapToObj(quantity -> """
                        {
                          "quantityOnAllocatedStock": {
                            "unit": "unit:piece",
                            "value": %d
                          },
                          "isBlocked": false,
                          "stockLocationBpnsAnonymized": "hashed-stock-location-%d",
                          "lastUpdatedOnDateTime": "2026-05-05T00:00:00Z"
                        }""".formatted(quantity, quantity))
                .collect(Collectors.joining(",\n"));
        return """
                {
                  "materialGlobalAssetIdAnonymized": "hashed-material-%s",
                  "direction": "OUTBOUND",
                  "allocatedStocks": [
                    %s
                  ]
                }
                """.formatted(Integer.toUnsignedString(materialGlobalAssetId.hashCode()), allocatedStocks);
    }

    private String deliveryInformationAnonymizedPayload(final String materialGlobalAssetId, final int... quantities) {
        final String deliveries = Arrays.stream(quantities)
                .mapToObj(quantity -> """
                        {
                          "deliveryQuantity": {
                            "unit": "unit:piece",
                            "value": %d
                          },
                          "lastUpdatedOnDateTime": "2026-05-05T00:00:00Z",
                          "originBpnsAnonymized": "hashed-origin-%d",
                          "destinationBpnsAnonymized": "hashed-destination-%d",
                          "transitEvents": [{
                            "dateTimeOfEvent": "2026-05-06T00:00:00Z",
                            "eventType": "estimated-departure"
                          }]
                        }""".formatted(quantity, quantity, quantity))
                .collect(Collectors.joining(",\n"));
        return """
                {
                  "materialGlobalAssetIdAnonymized": "hashed-material-%s",
                  "deliveries": [
                    %s
                  ]
                }
                """.formatted(Integer.toUnsignedString(materialGlobalAssetId.hashCode()), deliveries);
    }

    private String plannedProductionOutputAnonymizedPayload(final String materialGlobalAssetId,
            final int... quantities) {
        final String outputs = Arrays.stream(quantities)
                .mapToObj(quantity -> """
                        {
                          "plannedProductionQuantity": {
                            "unit": "unit:piece",
                            "value": %d
                          },
                          "productionSiteBpnsAnonymized": "hashed-production-site-%d",
                          "estimatedTimeOfCompletion": "2026-05-05T00:00:00Z",
                          "lastUpdatedOnDateTime": "2026-05-04T00:00:00Z"
                        }""".formatted(quantity, quantity))
                .collect(Collectors.joining(",\n"));
        return """
                {
                  "materialGlobalAssetIdAnonymized": "hashed-material-%s",
                  "allocatedPlannedProductionOutputs": [
                    %s
                  ]
                }
                """.formatted(Integer.toUnsignedString(materialGlobalAssetId.hashCode()), outputs);
    }

    protected record Child(String globalAssetId, String bpnl) {
    }
}
