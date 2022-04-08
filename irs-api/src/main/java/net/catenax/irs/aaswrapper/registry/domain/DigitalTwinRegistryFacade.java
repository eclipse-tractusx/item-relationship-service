//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.registry.domain;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.catenax.irs.dto.SubmodelEndpoint;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for digital twin registry domain
 */
@RequiredArgsConstructor
@Service
public class DigitalTwinRegistryFacade {

    private final DigitalTwinRegistryClient digitalTwinRegistryClient;

    /**
     * Combines required data from Digital Twin Registry Service
     *
     * @param aasIdentifier The Asset Administration Shell’s unique id
     * @return list of submodel addresses
     */
    public List<SubmodelEndpoint> getAASSubmodelEndpoints(final String aasIdentifier) {
        final List<SubmodelDescriptor> submodelDescriptors = digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(
                aasIdentifier).getSubmodelDescriptors();

        return submodelDescriptors.stream()
                                  .filter(this::isAssemblyPartRelationship)
                                  .map(submodelDescriptor -> new SubmodelEndpoint(
                                          submodelDescriptor.getEndpoint()
                                                            .getProtocolInformation()
                                                            .getEndpointAddress()))
                                  .collect(Collectors.toList());
    }

    /**
     * TODO Adjust when we will know how to distinguish assembly part relationships
     * @param submodelDescriptor
     * @return
     */
    private boolean isAssemblyPartRelationship(final SubmodelDescriptor submodelDescriptor) {
        return submodelDescriptor.getIdShort().equals("assemblyPartRelationship");
    }
}
