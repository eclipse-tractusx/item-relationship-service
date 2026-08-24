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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.component.Shell;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.ProtocolInformation;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SemanticId;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecursiveSubmodelCollectorTest {

    private static final String ITEM_STOCK_ANONYMIZED =
            RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId();
    private static final String GLOBAL_ASSET_ID = "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b";
    private static final String LOCAL_BPNL = "BPNL0000LOCAL001";
    private static final String FIRST_DSP = "http://edc-one";
    private static final String SECOND_DSP = "http://edc-two";

    @Mock
    private DigitalTwinRegistryService digitalTwinRegistryService;
    @Mock
    private EdcSubmodelFacade submodelFacade;

    @Test
    void shouldFallBackToNextEndpointWhenFirstEndpointFails() throws Exception {
        final RecursiveSubmodelCollector collector = newCollector();
        when(digitalTwinRegistryService.fetchShell(any()))
                .thenReturn(Optional.of(shellWithEndpoints(FIRST_DSP, SECOND_DSP)));
        when(submodelFacade.getSubmodelPayload(eq(FIRST_DSP), anyString(), anyString(), eq(LOCAL_BPNL)))
                .thenThrow(new EdcClientException("first endpoint unreachable"));
        when(submodelFacade.getSubmodelPayload(eq(SECOND_DSP), anyString(), eq("part-type-asset"), eq(LOCAL_BPNL)))
                .thenReturn(new org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor(
                        "cid", """
                                {
                                  "partTypeInformation": {
                                    "manufacturerPartId": "MNR-1",
                                    "nameAtManufacturer": "Semiconductor"
                                  }
                                }
                                """));
        when(submodelFacade.getSubmodelPayload(eq(SECOND_DSP), anyString(), eq("item-stock-asset"), eq(LOCAL_BPNL)))
                .thenReturn(new org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor(
                        "cid", """
                                {
                                  "materialGlobalAssetIdAnonymized": "%s"
                                }
                                """.formatted(GLOBAL_ASSET_ID)));

        final RecursiveChildItem result = collector.collect(GLOBAL_ASSET_ID, LOCAL_BPNL,
                List.of(ITEM_STOCK_ANONYMIZED));

        assertThat(result.getMaterialNumber()).isEqualTo("MNR-1");
        assertThat(result.getMaterialName()).isEqualTo("Semiconductor");
        assertThat(result.getTombstones()).isEmpty();
        assertThat(result.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getAspect()).isEqualTo(ITEM_STOCK_ANONYMIZED);
            assertThat(item.getItems()).containsEntry("materialGlobalAssetIdAnonymized", GLOBAL_ASSET_ID);
        });
    }

    @Test
    void shouldReportMetadataAndAspectFailuresWhenAllEndpointsFail() throws Exception {
        final RecursiveSubmodelCollector collector = newCollector();
        when(digitalTwinRegistryService.fetchShell(any()))
                .thenReturn(Optional.of(shellWithEndpoints(FIRST_DSP, SECOND_DSP)));
        when(submodelFacade.getSubmodelPayload(anyString(), anyString(), anyString(), eq(LOCAL_BPNL)))
                .thenThrow(new EdcClientException("endpoint unreachable"));

        final RecursiveChildItem result = collector.collect(GLOBAL_ASSET_ID, LOCAL_BPNL,
                List.of(ITEM_STOCK_ANONYMIZED));

        verify(submodelFacade, times(2))
                .getSubmodelPayload(eq(FIRST_DSP), anyString(), anyString(), eq(LOCAL_BPNL));
        verify(submodelFacade, times(2))
                .getSubmodelPayload(eq(SECOND_DSP), anyString(), anyString(), eq(LOCAL_BPNL));
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTombstones()).extracting("reason")
                .containsExactlyInAnyOrder(
                        RecursiveTombstoneReason.PART_TYPE_INFORMATION_REQUEST_FAILED,
                        RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED);
    }

    @Test
    void shouldRejectNonStringMaterialMetadata() throws Exception {
        final RecursiveSubmodelCollector collector = newCollector();
        when(digitalTwinRegistryService.fetchShell(any()))
                .thenReturn(Optional.of(shellWithEndpoints(FIRST_DSP)));
        when(submodelFacade.getSubmodelPayload(eq(FIRST_DSP), anyString(), eq("part-type-asset"), eq(LOCAL_BPNL)))
                .thenReturn(new org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor(
                        "cid", """
                                {
                                  "manufacturerPartId": {
                                    "value": "MNR-1"
                                  },
                                  "nameAtManufacturer": [
                                    "Semiconductor"
                                  ]
                                }
                                """));
        when(submodelFacade.getSubmodelPayload(eq(FIRST_DSP), anyString(), eq("item-stock-asset"), eq(LOCAL_BPNL)))
                .thenReturn(new org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor(
                        "cid", "{\"materialGlobalAssetIdAnonymized\":\"" + GLOBAL_ASSET_ID + "\"}"));

        final RecursiveChildItem result = collector.collect(GLOBAL_ASSET_ID, LOCAL_BPNL,
                List.of(ITEM_STOCK_ANONYMIZED));

        assertThat(result.getMaterialNumber()).isNull();
        assertThat(result.getMaterialName()).isNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTombstones()).singleElement().satisfies(tombstone ->
                assertThat(tombstone.getReason())
                        .isEqualTo(RecursiveTombstoneReason.PART_TYPE_INFORMATION_NOT_AVAILABLE));
    }

    @Test
    void shouldRejectUnsupportedAspectIdentifierBeforeDtrLookup() {
        final RecursiveSubmodelCollector collector = newCollector();

        final RecursiveChildItem result = collector.collect(GLOBAL_ASSET_ID, LOCAL_BPNL,
                List.of(RecursiveAspect.ITEM_STOCK_ANONYMIZED.getIdShort()));

        verifyNoInteractions(digitalTwinRegistryService, submodelFacade);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTombstones()).singleElement()
                .satisfies(tombstone -> assertThat(tombstone.getReason())
                        .isEqualTo(RecursiveTombstoneReason.UNSUPPORTED_ANONYMIZED_ASPECT));
    }

    @Test
    void shouldUseIdShortWhenSemanticIdIsMissing() throws Exception {
        final RecursiveSubmodelCollector collector = newCollector();
        final Shell shell = shellWithEndpoints(FIRST_DSP);
        shell.payload().getSubmodelDescriptors().forEach(descriptor -> descriptor.setSemanticId(null));
        when(digitalTwinRegistryService.fetchShell(any())).thenReturn(Optional.of(shell));
        when(submodelFacade.getSubmodelPayload(eq(FIRST_DSP), anyString(), eq("part-type-asset"), eq(LOCAL_BPNL)))
                .thenReturn(new org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor(
                        "cid", """
                                {
                                  "manufacturerPartId": "MNR-1",
                                  "nameAtManufacturer": "Semiconductor"
                                }
                                """));
        when(submodelFacade.getSubmodelPayload(eq(FIRST_DSP), anyString(), eq("item-stock-asset"), eq(LOCAL_BPNL)))
                .thenReturn(new org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor(
                        "cid", "{\"materialGlobalAssetIdAnonymized\":\"" + GLOBAL_ASSET_ID + "\"}"));

        final RecursiveChildItem result = collector.collect(GLOBAL_ASSET_ID, LOCAL_BPNL,
                List.of(ITEM_STOCK_ANONYMIZED));

        assertThat(result.getMaterialNumber()).isEqualTo("MNR-1");
        assertThat(result.getMaterialName()).isEqualTo("Semiconductor");
        assertThat(result.getItems()).singleElement()
                .satisfies(item -> assertThat(item.getAspect()).isEqualTo(ITEM_STOCK_ANONYMIZED));
        assertThat(result.getTombstones()).isEmpty();
    }

    @Test
    void shouldReportUnreadableDescriptorInsteadOfTreatingItAsMissing() throws Exception {
        final RecursiveSubmodelCollector collector = newCollector();
        final SubmodelDescriptor unreadableDescriptor = mock(SubmodelDescriptor.class);
        when(unreadableDescriptor.getSemanticId()).thenThrow(new IllegalStateException("descriptor cannot be read"));
        when(digitalTwinRegistryService.fetchShell(any())).thenReturn(Optional.of(new Shell("contract-agreement",
                AssetAdministrationShellDescriptor.builder()
                        .submodelDescriptors(List.of(unreadableDescriptor))
                        .build())));

        final RecursiveChildItem result = collector.collect(GLOBAL_ASSET_ID, LOCAL_BPNL,
                List.of(ITEM_STOCK_ANONYMIZED));

        verifyNoInteractions(submodelFacade);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTombstones()).extracting("reason")
                .containsExactlyInAnyOrder(RecursiveTombstoneReason.PART_TYPE_INFORMATION_REQUEST_FAILED,
                        RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED);
    }

    @Test
    void shouldKeepMaterialMetadataWhenAspectDescriptorFails() throws Exception {
        final RecursiveSubmodelCollector collector = newCollector();
        final SubmodelDescriptor partType = descriptor("PartTypeInformation",
                RecursiveSubmodelCollector.PART_TYPE_INFORMATION_SEMANTIC_ID,
                "part-type-asset", FIRST_DSP);
        final SubmodelDescriptor unreadableAspect = mock(SubmodelDescriptor.class);
        when(unreadableAspect.getSemanticId()).thenThrow(new IllegalStateException("descriptor cannot be read"));
        when(digitalTwinRegistryService.fetchShell(any())).thenReturn(Optional.of(new Shell("contract-agreement",
                AssetAdministrationShellDescriptor.builder()
                        .submodelDescriptors(List.of(partType, unreadableAspect))
                        .build())));
        when(submodelFacade.getSubmodelPayload(eq(FIRST_DSP), anyString(), eq("part-type-asset"), eq(LOCAL_BPNL)))
                .thenReturn(new org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor(
                        "cid", """
                                {
                                  "partTypeInformation": {
                                    "manufacturerPartId": "MNR-1",
                                    "nameAtManufacturer": "Semiconductor"
                                  }
                                }
                                """));

        final RecursiveChildItem result = collector.collect(GLOBAL_ASSET_ID, LOCAL_BPNL,
                List.of(ITEM_STOCK_ANONYMIZED));

        assertThat(result.getMaterialNumber()).isEqualTo("MNR-1");
        assertThat(result.getMaterialName()).isEqualTo("Semiconductor");
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTombstones()).singleElement().satisfies(tombstone ->
                assertThat(tombstone.getReason()).isEqualTo(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED));
    }

    @Test
    void shouldNotDiscoverConnectorEndpointWhenSubmodelEndpointHasNoDspEndpoint() throws Exception {
        final RecursiveSubmodelCollector collector = newCollector();
        final SubmodelDescriptor itemStock = descriptorWithoutDspEndpoint("ItemStockAnonymized", ITEM_STOCK_ANONYMIZED,
                "item-stock-asset");
        final SubmodelDescriptor partType = descriptorWithoutDspEndpoint("PartTypeInformation",
                RecursiveSubmodelCollector.PART_TYPE_INFORMATION_SEMANTIC_ID, "part-type-asset");
        when(digitalTwinRegistryService.fetchShell(any())).thenReturn(Optional.of(new Shell("contract-agreement",
                AssetAdministrationShellDescriptor.builder()
                        .submodelDescriptors(List.of(partType, itemStock))
                        .build())));

        final RecursiveChildItem result = collector.collect(GLOBAL_ASSET_ID, LOCAL_BPNL,
                List.of(ITEM_STOCK_ANONYMIZED));

        verifyNoInteractions(submodelFacade);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTombstones()).extracting("reason")
                .containsExactlyInAnyOrder(RecursiveTombstoneReason.PART_TYPE_INFORMATION_REQUEST_FAILED,
                        RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED);
    }

    private RecursiveSubmodelCollector newCollector() {
        return new RecursiveSubmodelCollector(digitalTwinRegistryService, submodelFacade,
                new ObjectMapper().findAndRegisterModules());
    }

    private Shell shellWithEndpoints(final String... dspEndpoints) {
        final SubmodelDescriptor itemStock = descriptor("ItemStockAnonymized", ITEM_STOCK_ANONYMIZED,
                "item-stock-asset", dspEndpoints);
        final SubmodelDescriptor partType = descriptor("PartTypeInformation",
                RecursiveSubmodelCollector.PART_TYPE_INFORMATION_SEMANTIC_ID,
                "part-type-asset", dspEndpoints);
        return new Shell("contract-agreement", AssetAdministrationShellDescriptor.builder()
                .submodelDescriptors(List.of(partType, itemStock))
                .build());
    }

    private SubmodelDescriptor descriptor(final String idShort, final String semanticId,
            final String assetId, final String... dspEndpoints) {
        final List<Endpoint> endpoints = List.of(dspEndpoints).stream()
                .map(dsp -> Endpoint.builder()
                        .protocolInformation(ProtocolInformation.builder()
                                .href("http://dataplane/submodel")
                                .subprotocolBody("dspEndpoint=" + dsp + ";id=" + assetId)
                                .build())
                        .build())
                .toList();
        return SubmodelDescriptor.builder()
                .idShort(idShort)
                .semanticId(Reference.builder()
                        .keys(List.of(SemanticId.builder().value(semanticId).build()))
                        .build())
                .endpoints(endpoints)
                .build();
    }

    private SubmodelDescriptor descriptorWithoutDspEndpoint(final String idShort, final String semanticId,
            final String assetId) {
        return SubmodelDescriptor.builder()
                .idShort(idShort)
                .semanticId(Reference.builder()
                        .keys(List.of(SemanticId.builder().value(semanticId).build()))
                        .build())
                .endpoints(List.of(Endpoint.builder()
                        .protocolInformation(ProtocolInformation.builder()
                                .href("http://dataplane/submodel")
                                .subprotocolBody("id=" + assetId)
                                .build())
                        .build()))
                .build();
    }
}
