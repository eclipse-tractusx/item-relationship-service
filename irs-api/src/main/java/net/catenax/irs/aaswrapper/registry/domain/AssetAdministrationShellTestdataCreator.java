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

import lombok.AllArgsConstructor;
import net.catenax.irs.aaswrapper.dto.AspectModel;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelTestdataCreator;

/**
 * Class to create AssetAdministrationShell Testdata
 */
@AllArgsConstructor
public class AssetAdministrationShellTestdataCreator {
    private final SubmodelTestdataCreator submodelTestdataCreator;

    public AssetAdministrationShellDescriptor createAASShellDescriptorForIdFromTestData(final String catenaXId) {
        final AspectModel aspectmodel = submodelTestdataCreator.createDummyAssemblyPartRelationshipForId(catenaXId);
        return createDummyAssetAdministrationShellDescriptorForId(catenaXId, List.of(aspectmodel));
    }

    public AssetAdministrationShellDescriptor createDummyAssetAdministrationShellDescriptorForId(final String catenaXId,
            final List<AspectModel> endpointAspectModels) {

        final List<SubmodelDescriptor> submodelDescriptors = new ArrayList<>();
        endpointAspectModels.forEach(
                aspectModel -> submodelDescriptors.add(createAssemblyPartRelationshipSubmodelDescriptor(catenaXId)));

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
        reference.setValue(List.of("urn:bamm:com.catenax.assembly_part_relationtship:1.0.0"));

        final SubmodelDescriptor submodelDescriptor = new SubmodelDescriptor();
        submodelDescriptor.setIdentification(catenaXId);
        submodelDescriptor.setIdShort("idShort");
        submodelDescriptor.setEndpoint(endpoint);
        submodelDescriptor.setSemanticId(reference);

        return submodelDescriptor;
    }
}
