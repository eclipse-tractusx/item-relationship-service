/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.registryclient.central;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.assetadministrationshell.LangString;
import org.eclipse.tractusx.irs.component.assetadministrationshell.ProtocolInformation;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SemanticId;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.data.CxTestDataContainer;
import org.springframework.web.client.RestClientException;

/**
 * Class to create AssetAdministrationShell Testdata
 * As AASWrapper is not deployed, we are using this class to Stub responses
 */
@SuppressWarnings({ "PMD.TooManyMethods" })
class AssetAdministrationShellTestdataCreator {

    private final CxTestDataContainer cxTestDataContainer;

    /* package */ AssetAdministrationShellTestdataCreator(final CxTestDataContainer cxTestDataContainer) {
        this.cxTestDataContainer = cxTestDataContainer;
    }

    public AssetAdministrationShellDescriptor createDummyAssetAdministrationShellDescriptorForId(
            final String catenaXId) {
        final Optional<CxTestDataContainer.CxTestData> cxTestData = this.cxTestDataContainer.getByCatenaXId(catenaXId);
        if (cxTestData.isEmpty()) {
            throw new RestClientException("Dummy Exception");
        }

        final List<SubmodelDescriptor> submodelDescriptors = new ArrayList<>();

        cxTestData.get()
                  .getSingleLevelBomAsBuilt()
                  .ifPresent(submodel -> submodelDescriptors.add(
                          createSingleLevelBomAsBuiltSubmodelDescriptor(catenaXId)));
        cxTestData.get()
                  .getSerialPart()
                  .ifPresent(submodel -> submodelDescriptors.add(createSerialPartSubmodelDescriptor(catenaXId)));
        cxTestData.get()
                  .getSingleLevelUsageAsBuilt()
                  .ifPresent(submodel -> submodelDescriptors.add(
                          createSingleLevelUsageAsBuiltSubmodelDescriptor(catenaXId)));
        cxTestData.get()
                  .getSingleLevelBomAsSpecified()
                  .ifPresent(submodel -> submodelDescriptors.add(
                          createSingleLevelBomAsSpecifiedSubmodelDescriptor(catenaXId)));
        cxTestData.get()
                  .getSingleLevelBomAsPlanned()
                  .ifPresent(submodel -> submodelDescriptors.add(
                          createSingleLevelBomAsPlannedSubmodelDescriptor(catenaXId)));
        cxTestData.get()
                  .getPartAsPlanned()
                  .ifPresent(submodel -> submodelDescriptors.add(createPartAsPlannedSubmodelDescriptor(catenaXId)));
        cxTestData.get()
                  .getBatch()
                  .ifPresent(submodel -> submodelDescriptors.add(createBatchSubmodelDescriptor(catenaXId)));
        cxTestData.get()
                  .getMaterialForRecycling()
                  .ifPresent(
                          submodel -> submodelDescriptors.add(createMaterialForRecyclingSubmodelDescriptor(catenaXId)));
        cxTestData.get()
                  .getProductDescription()
                  .ifPresent(
                          submodel -> submodelDescriptors.add(createProductDescriptionSubmodelDescriptor(catenaXId)));
        cxTestData.get()
                  .getPhysicalDimension()
                  .ifPresent(submodel -> submodelDescriptors.add(createPhysicalDimensionSubmodelDescriptor(catenaXId)));
        cxTestData.get()
                  .getPartAsSpecified()
                  .ifPresent(submodel -> submodelDescriptors.add(createPartAsSpecifiedSubmodelDescriptor(catenaXId)));

        return AssetAdministrationShellDescriptor.builder()
                                                 .description(List.of(LangString.builder().build()))
                                                 .globalAssetId(catenaXId)
                                                 .idShort("idShort")
                                                 .id(catenaXId)
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                                                 .name("ManufacturerId")
                                                                                                 .value("BPNL00000003AYRE")
                                                                                                 .build()))
                                                 .submodelDescriptors(submodelDescriptors)
                                                 .build();
    }

    private SubmodelDescriptor createSingleLevelBomAsBuiltSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:io.catenax.single_level_bom_as_built:1.0.0",
                "singleLevelBomAsBuilt");
    }

    private SubmodelDescriptor createSingleLevelUsageAsBuiltSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:io.catenax.single_level_usage_as_built:1.0.0",
                "singleLevelUsageAsBuilt");
    }

    private SubmodelDescriptor createSingleLevelBomAsSpecifiedSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:io.catenax.single_level_bom_as_specified:1.0.0",
                "singleLevelBomAsSpecified");
    }

    private SubmodelDescriptor createSerialPartSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:io.catenax.serial_part:1.0.0", "serialPart");
    }

    private SubmodelDescriptor createSingleLevelBomAsPlannedSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:io.catenax.single_level_bom_as_planned:1.0.0",
                "singleLevelBomAsPlanned");
    }

    private SubmodelDescriptor createPartAsPlannedSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:io.catenax.part_as_planned:1.0.0", "partAsPlanned");
    }

    private SubmodelDescriptor createBatchSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:io.catenax.batch:1.0.0", "batch");
    }

    private SubmodelDescriptor createMaterialForRecyclingSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:io.catenax.material_for_recycling:1.0.0",
                "materialForRecycling");
    }

    private SubmodelDescriptor createProductDescriptionSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:io.catenax.product_description:1.0.0",
                "productDescription");
    }

    private SubmodelDescriptor createPhysicalDimensionSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:io.catenax.physical_dimension:1.0.0", "physicalDimension");
    }

    private SubmodelDescriptor createPartAsSpecifiedSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:io.catenax.part_as_specified:1.0.0",
                "partAsSpecified");
    }

    private SubmodelDescriptor createSubmodelDescriptor(final String catenaXId, final String submodelUrn,
            final String submodelName) {
        final ProtocolInformation protocolInformation = ProtocolInformation.builder()
                                                                           .href(submodelName)
                                                                           .endpointProtocol("AAS/SUBMODEL")
                                                                           .endpointProtocolVersion(List.of("1.0RC02"))
                                                                           .subprotocolBody("id=" + catenaXId
                                                                                   + ";dspEndpoint=http://edc.control.plane/")
                                                                           .build();

        final Endpoint endpoint = Endpoint.builder()
                                          .interfaceInformation("https://TEST.connector")
                                          .protocolInformation(protocolInformation)
                                          .build();

        final Reference reference = Reference.builder()
                                             .keys(List.of(SemanticId.builder().value(submodelUrn).build()))
                                             .build();

        return SubmodelDescriptor.builder()
                                 .id(catenaXId)
                                 .idShort(submodelName)
                                 .endpoints(List.of(endpoint))
                                 .semanticId(reference)
                                 .build();
    }
}
