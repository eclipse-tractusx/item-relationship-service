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

import net.catenax.irs.dto.SubmodelType;

/**
 * Class to create AssetAdministrationShell Testdata
 * As AASWrapper is not deployed, we are using this class to Stub responses
 */
class AssetAdministrationShellTestdataCreator {

    public AssetAdministrationShellDescriptor createDummyAssetAdministrationShellDescriptorForId(
            final String catenaXId) {
        final List<SubmodelDescriptor> submodelDescriptors = new ArrayList<>();

        submodelDescriptors.add(createAssemblyPartRelationshipSubmodelDescriptor(catenaXId));

        return AssetAdministrationShellDescriptor.builder()
                                                 .description(List.of(new LangString()))
                                                 .globalAssetId(new Reference())
                                                 .idShort("idShort")
                                                 .identification(catenaXId)
                                                 .specificAssetIds(List.of(new IdentifierKeyValuePair()))
                                                 .submodelDescriptors(submodelDescriptors)
                                                 .build();
    }

    private SubmodelDescriptor createAssemblyPartRelationshipSubmodelDescriptor(final String catenaXId) {
        final ProtocolInformation protocolInformation = new ProtocolInformation();
        protocolInformation.setEndpointAddress(catenaXId);
        protocolInformation.setEndpointProtocol("AAS/SUBMODEL");
        protocolInformation.setEndpointProtocolVersion("1.0RC02");

        final Endpoint endpoint = new Endpoint();
        endpoint.setInterfaceInformation("https://TEST.connector");
        endpoint.setProtocolInformation(protocolInformation);

        final Reference reference = new Reference();
        reference.setValue(List.of(SubmodelType.ASSEMBLY_PART_RELATIONSHIP.getValue()));

        final SubmodelDescriptor submodelDescriptor = new SubmodelDescriptor();
        submodelDescriptor.setIdentification(catenaXId);
        submodelDescriptor.setIdShort("assemblyPartRelationship");
        submodelDescriptor.setEndpoints(List.of(endpoint));
        submodelDescriptor.setSemanticId(reference);

        return submodelDescriptor;
    }
}
