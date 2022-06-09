//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
//
package net.catenax.irs.aaswrapper.registry.domain;

import java.util.ArrayList;
import java.util.List;

import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.Endpoint;
import net.catenax.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import net.catenax.irs.component.assetadministrationshell.LangString;
import net.catenax.irs.component.assetadministrationshell.ProtocolInformation;
import net.catenax.irs.component.assetadministrationshell.Reference;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;

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
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder().build()))
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
