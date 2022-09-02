/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.aaswrapper.registry.domain;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.assetadministrationshell.LangString;
import org.eclipse.tractusx.irs.component.assetadministrationshell.ProtocolInformation;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;

/**
 * Class to create AssetAdministrationShell Testdata
 * As AASWrapper is not deployed, we are using this class to Stub responses
 */
class AssetAdministrationShellTestdataCreator {

    public AssetAdministrationShellDescriptor createDummyAssetAdministrationShellDescriptorForId(
            final String catenaXId) {
        final List<SubmodelDescriptor> submodelDescriptors = new ArrayList<>();

        submodelDescriptors.add(createAssemblyPartRelationshipSubmodelDescriptor(catenaXId));
        submodelDescriptors.add(createSerialPartTypizationSubmodelDescriptor(catenaXId));

        final Reference globalAssetId = Reference.builder().value(List.of(catenaXId)).build();
        return AssetAdministrationShellDescriptor.builder()
                                                 .description(List.of(LangString.builder().build()))
                                                 .globalAssetId(globalAssetId)
                                                 .idShort("idShort")
                                                 .identification(catenaXId)
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder().key("ManufacturerId").value("BPNL00000003AYRE").build()))
                                                 .submodelDescriptors(submodelDescriptors)
                                                 .build();
    }

    private SubmodelDescriptor createAssemblyPartRelationshipSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:com.catenax.assembly_part_relationship:1.0.0",
                "assemblyPartRelationship");
    }

    private SubmodelDescriptor createSerialPartTypizationSubmodelDescriptor(final String catenaXId) {
        return createSubmodelDescriptor(catenaXId, "urn:bamm:com.catenax.serial_part_typization:1.0.0",
                "serialPartTypization");
    }

    private SubmodelDescriptor createSubmodelDescriptor(final String catenaXId, final String submodelUrn,
            final String submodelName) {
        final ProtocolInformation protocolInformation = ProtocolInformation.builder()
                                                                           .endpointAddress(catenaXId)
                                                                           .endpointProtocol("AAS/SUBMODEL")
                                                                           .endpointProtocolVersion("1.0RC02")
                                                                           .build();

        final Endpoint endpoint = Endpoint.builder()
                                          .interfaceInformation("https://TEST.connector")
                                          .protocolInformation(protocolInformation)
                                          .build();

        final Reference reference = Reference.builder().value(List.of(submodelUrn)).build();

        return SubmodelDescriptor.builder()
                                 .identification(catenaXId)
                                 .idShort(submodelName)
                                 .endpoints(List.of(endpoint))
                                 .semanticId(reference)
                                 .build();
    }
}
