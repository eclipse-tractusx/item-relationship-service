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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.dto.NodeType;
import net.catenax.irs.dto.ProcessingError;
import net.catenax.irs.dto.SubmodelType;
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
     * @param aasIdentifier The Asset Administration Shell's unique id
     * @return list of submodel addresses
     */
    public List<AbstractAasShell> getAASSubmodelEndpoint(final String aasIdentifier) {
        List<AbstractAasShell> result;
        try {
            final List<SubmodelDescriptor> submodelDescriptors = getSubmodelDescriptors(aasIdentifier);
            result = submodelDescriptors.stream()
                                        .filter(this::isAssemblyPartRelationship)
                                        .map(submodelDescriptor -> new AasShellSubmodelDescriptor(
                                                submodelDescriptor.getIdShort(), submodelDescriptor.getIdentification(),
                                                NodeType.NODE, SubmodelType.ASSEMBLY_PART_RELATIONSHIP,
                                                submodelDescriptor.getEndpoints()
                                                                  .get(0)
                                                                  .getProtocolInformation()
                                                                  .getEndpointAddress()))
                                        .collect(Collectors.toList());
            if (result.isEmpty()) {
                result = List.of(
                        getResponseTombStoneForResponse(aasIdentifier, "No AssemblyPartRelationship Descriptor found",
                                Optional.empty(), 1));
            }
        } catch (FeignException e) {
            result = List.of(getResponseTombStoneForResponse(aasIdentifier, e.getMessage(), Optional.of(e), 1));
        }
        return result;
    }

    private AbstractAasShell getResponseTombStoneForResponse(final String catenaXId, final String errorDetail,
            final Optional<Exception> exception, final int retryCounter) {
        final ProcessingError processingError = new ProcessingError(exception, errorDetail, retryCounter,
                Instant.now());
        final String idShort = "";
        return new AasShellTombstone(idShort, catenaXId, processingError);
    }

    private List<SubmodelDescriptor> getSubmodelDescriptors(final String aasIdentifier) {
        return digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(aasIdentifier).getSubmodelDescriptors();
    }

    /**
     * TODO: Adjust when we will know how to distinguish assembly part relationships
     *
     * @param submodelDescriptor the submodel descriptor
     * @return True, if AssemblyPartRelationship
     */
    private boolean isAssemblyPartRelationship(final SubmodelDescriptor submodelDescriptor) {
        final String assemblyPartRelationshipIdentifier = "urn:bamm:com.catenax.assembly_part_relationship:1.0.0";
        return assemblyPartRelationshipIdentifier.equals(
                submodelDescriptor.getSemanticId().getValue().stream().findFirst().orElse(null));
    }
}
