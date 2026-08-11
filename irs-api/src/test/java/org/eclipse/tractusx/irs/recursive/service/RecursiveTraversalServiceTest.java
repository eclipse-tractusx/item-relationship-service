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
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.MeasurementUnit;
import org.eclipse.tractusx.irs.component.Quantity;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Shell;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.ProtocolInformation;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SemanticId;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.RelationshipSubmodel;
import org.eclipse.tractusx.irs.recursive.model.RecursiveBomChild;
import org.eclipse.tractusx.irs.recursive.model.RecursiveQuantity;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecursiveTraversalServiceTest {

    private static final String PARENT_ASSET_ID = "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b";
    private static final String CHILD_ASSET_ID = "urn:uuid:e8c48a8e-d2d7-43d9-a867-65c70c85f5b8";
    private static final String LOCAL_BPNL = "BPNL0000LOCAL001";
    private static final String CHILD_BPNL = "BPNL00000000015G";

    @Mock
    private DigitalTwinRegistryService digitalTwinRegistryService;
    @Mock
    private EdcSubmodelFacade submodelFacade;
    @Mock
    private JsonUtil jsonUtil;
    @Mock
    private AssetAdministrationShellDescriptor shellDescriptor;

    @Test
    void shouldResolveBomChildWithQuantity() throws Exception {
        final Endpoint endpoint = Endpoint.builder()
                .protocolInformation(ProtocolInformation.builder()
                        .href("http://dataplane/submodel")
                        .subprotocolBody("id=bom-asset;dspEndpoint=http://partner-edc")
                        .build())
                .build();
        final Relationship relationship = Relationship.builder()
                .bpn(CHILD_BPNL)
                .linkedItem(LinkedItem.builder()
                        .childCatenaXId(GlobalAssetIdentification.of(CHILD_ASSET_ID))
                        .quantity(Quantity.builder()
                                .quantityNumber(2.5)
                                .measurementUnit(MeasurementUnit.builder()
                                        .lexicalValue("unit:piece")
                                        .build())
                                .build())
                        .build())
                .build();
        final RelationshipSubmodel relationshipSubmodel = () -> List.of(relationship);
        when(digitalTwinRegistryService.fetchShell(any()))
                .thenReturn(Optional.of(new Shell("agreement-id", shellDescriptor)));
        when(shellDescriptor.getSubmodelDescriptors())
                .thenReturn(List.of(descriptor(SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0, endpoint)));
        when(submodelFacade.getSubmodelPayload(eq("http://partner-edc"), eq("http://dataplane/submodel"),
                eq("bom-asset"), eq(LOCAL_BPNL)))
                .thenReturn(new org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor("agreement-id", "{}"));
        doReturn(relationshipSubmodel).when(jsonUtil).fromString(eq("{}"), any());
        final RecursiveTraversalService service = new RecursiveTraversalService(digitalTwinRegistryService,
                submodelFacade, jsonUtil);

        final List<RecursiveBomChild> children = service.resolve(
                PARENT_ASSET_ID, LOCAL_BPNL, BomLifecycle.AS_PLANNED).bomChildren();

        assertThat(children).singleElement().satisfies(child -> {
            assertThat(child.childGlobalAssetId()).isEqualTo(CHILD_ASSET_ID);
            assertThat(child.partnerBpnl()).isEqualTo(CHILD_BPNL);
            assertThat(child.quantity()).isEqualTo(RecursiveQuantity.builder()
                    .value(2.5)
                    .unit("unit:piece")
                    .build());
        });
    }

    @Test
    void shouldReturnUniqueNonBlankPartnerBpnls() {
        final RecursiveTraversalService service = new RecursiveTraversalService(digitalTwinRegistryService,
                submodelFacade, jsonUtil);
        final List<RecursiveBomChild> children = List.of(
                new RecursiveBomChild("asset-1", CHILD_BPNL, null),
                new RecursiveBomChild("asset-2", CHILD_BPNL, null),
                new RecursiveBomChild("asset-3", "", null),
                new RecursiveBomChild("asset-4", null, null));

        assertThat(service.extractPartnerBpnls(children)).containsExactly(CHILD_BPNL);
    }

    @Test
    void shouldTreatTwinWithoutBomDescriptorAsLeaf() throws Exception {
        final Endpoint endpoint = endpoint("id=part-type-asset;dspEndpoint=http://partner-edc");
        when(digitalTwinRegistryService.fetchShell(any()))
                .thenReturn(Optional.of(new Shell("agreement-id", shellDescriptor)));
        when(shellDescriptor.getSubmodelDescriptors()).thenReturn(List.of(
                descriptor("urn:samm:io.catenax.part_type_information:1.0.0#PartTypeInformation", endpoint)));
        final RecursiveTraversalService service = new RecursiveTraversalService(digitalTwinRegistryService,
                submodelFacade, jsonUtil);

        final RecursiveTraversalService.TraversalResult result = service.resolve(
                PARENT_ASSET_ID, LOCAL_BPNL, BomLifecycle.AS_PLANNED);

        assertThat(result.bomChildren()).isEmpty();
        assertThat(result.shellDescriptor()).isSameAs(shellDescriptor);
        verifyNoInteractions(submodelFacade);
    }

    @Test
    void shouldResolveSupportedBomSemanticIdFromAnyReferenceKey() throws Exception {
        final Endpoint endpoint = endpoint("id=bom-asset;dspEndpoint=http://partner-edc");
        final RelationshipSubmodel relationshipSubmodel = List::of;
        when(digitalTwinRegistryService.fetchShell(any()))
                .thenReturn(Optional.of(new Shell("agreement-id", shellDescriptor)));
        when(shellDescriptor.getSubmodelDescriptors()).thenReturn(List.of(descriptor(List.of(
                "urn:samm:io.catenax.part_type_information:1.0.0#PartTypeInformation",
                SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0), endpoint)));
        when(submodelFacade.getSubmodelPayload(eq("http://partner-edc"), eq("http://dataplane/submodel"),
                eq("bom-asset"), eq(LOCAL_BPNL)))
                .thenReturn(new org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor("agreement-id", "{}"));
        doReturn(relationshipSubmodel).when(jsonUtil).fromString(eq("{}"), any());
        final RecursiveTraversalService service = new RecursiveTraversalService(digitalTwinRegistryService,
                submodelFacade, jsonUtil);

        final List<RecursiveBomChild> children = service.resolve(
                PARENT_ASSET_ID, LOCAL_BPNL, BomLifecycle.AS_PLANNED).bomChildren();

        assertThat(children).isEmpty();
    }

    @Test
    void shouldRejectUnsupportedBomSemanticVersion() throws Exception {
        final Endpoint endpoint = endpoint("id=bom-asset;dspEndpoint=http://partner-edc");
        when(digitalTwinRegistryService.fetchShell(any()))
                .thenReturn(Optional.of(new Shell("agreement-id", shellDescriptor)));
        when(shellDescriptor.getSubmodelDescriptors()).thenReturn(List.of(
                descriptor("urn:samm:io.catenax.single_level_bom_as_planned:3.1.0#SingleLevelBomAsPlanned",
                        endpoint)));
        final RecursiveTraversalService service = new RecursiveTraversalService(digitalTwinRegistryService,
                submodelFacade, jsonUtil);

        assertThatThrownBy(() -> service.resolve(PARENT_ASSET_ID, LOCAL_BPNL, BomLifecycle.AS_PLANNED))
                .isInstanceOfSatisfying(RecursiveExternalCallException.class, exception ->
                        assertThat(exception.getReason()).isEqualTo("BOM_SUBMODEL_NOT_SUPPORTED"));
        verifyNoInteractions(submodelFacade);
    }

    @Test
    void shouldRejectMultipleSupportedBomDescriptors() throws Exception {
        final Endpoint endpoint = endpoint("id=bom-asset;dspEndpoint=http://partner-edc");
        when(digitalTwinRegistryService.fetchShell(any()))
                .thenReturn(Optional.of(new Shell("agreement-id", shellDescriptor)));
        when(shellDescriptor.getSubmodelDescriptors()).thenReturn(List.of(
                descriptor(SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0, endpoint),
                descriptor(SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0, endpoint)));
        final RecursiveTraversalService service = new RecursiveTraversalService(digitalTwinRegistryService,
                submodelFacade, jsonUtil);

        assertThatThrownBy(() -> service.resolve(PARENT_ASSET_ID, LOCAL_BPNL, BomLifecycle.AS_PLANNED))
                .isInstanceOfSatisfying(RecursiveExternalCallException.class, exception ->
                        assertThat(exception.getReason()).isEqualTo("BOM_SUBMODEL_NOT_SUPPORTED"));
        verifyNoInteractions(submodelFacade);
    }

    @Test
    void shouldRejectBomEndpointWithoutDspEndpoint() throws Exception {
        final Endpoint endpoint = endpoint("id=bom-asset");
        when(digitalTwinRegistryService.fetchShell(any()))
                .thenReturn(Optional.of(new Shell("agreement-id", shellDescriptor)));
        when(shellDescriptor.getSubmodelDescriptors())
                .thenReturn(List.of(descriptor(SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0, endpoint)));
        final RecursiveTraversalService service = new RecursiveTraversalService(digitalTwinRegistryService,
                submodelFacade, jsonUtil);

        assertThatThrownBy(() -> service.resolve(PARENT_ASSET_ID, LOCAL_BPNL, BomLifecycle.AS_PLANNED))
                .isInstanceOfSatisfying(RecursiveExternalCallException.class, exception ->
                        assertThat(exception.getReason()).isEqualTo("BOM_SUBMODEL_ENDPOINT_MISSING"));
        verifyNoInteractions(submodelFacade);
    }

    private Endpoint endpoint(final String subprotocolBody) {
        return Endpoint.builder()
                .protocolInformation(ProtocolInformation.builder()
                        .href("http://dataplane/submodel")
                        .subprotocolBody(subprotocolBody)
                        .build())
                .build();
    }

    private SubmodelDescriptor descriptor(final String semanticId, final Endpoint endpoint) {
        return descriptor(List.of(semanticId), endpoint);
    }

    private SubmodelDescriptor descriptor(final List<String> semanticIds, final Endpoint endpoint) {
        return SubmodelDescriptor.builder()
                .semanticId(Reference.builder()
                        .keys(semanticIds.stream()
                                .map(value -> SemanticId.builder().value(value).build())
                                .toList())
                        .build())
                .endpoints(List.of(endpoint))
                .build();
    }
}
