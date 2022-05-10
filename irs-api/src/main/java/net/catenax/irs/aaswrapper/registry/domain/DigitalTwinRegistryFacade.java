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
import java.util.Locale;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.catenax.irs.dto.JobParameter;
import net.catenax.irs.dto.SubmodelEndpoint;
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
     * @param jobData       the job data parameters
     * @return list of submodel addresses
     */
    public List<SubmodelEndpoint> getAASSubmodelEndpoints(final String aasIdentifier, final JobParameter jobData) {
        final List<SubmodelDescriptor> submodelDescriptors = digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(
                aasIdentifier).getSubmodelDescriptors();

        return submodelDescriptors.stream()
                                  .filter(submodelDescriptor -> filterByAspectType(submodelDescriptor, jobData))
                                  .map(submodelDescriptor -> new SubmodelEndpoint(submodelDescriptor.getEndpoints()
                                                                                                    .get(0)
                                                                                                    .getProtocolInformation()
                                                                                                    .getEndpointAddress(),
                                          convert(submodelDescriptor.getIdShort())))
                                  .collect(Collectors.toList());
    }

    /**
     * TODO: Adjust when we will know how to distinguish assembly part relationships
     *
     * @param submodelDescriptor the submodel descriptor
     * @param jobData            the job data parameters
     * @return True, if no filter has been selected otherwise filter the submodelDescriptor
     * according to the given consumer aspectType
     */
    private boolean filterByAspectType(final SubmodelDescriptor submodelDescriptor, final JobParameter jobData) {
        final List<String> aspectTypes = jobData.getAspectTypes();

        if (shouldFilterByAspectType(aspectTypes)) {
            final String type = submodelDescriptor.getIdShort();
            return aspectTypes.contains(type.toLowerCase(Locale.ROOT));
        }
        return true;
    }

    private boolean shouldFilterByAspectType(final List<String> aspectTypes) {
        return aspectTypes != null && !aspectTypes.isEmpty();
    }

    /**
     * Convert from AspectType value into an SubmodelType enum
     *
     * @param aspectType the given consumer AspectType value
     * @return the converted SubmodelType enum
     */
    private SubmodelType convert(final String aspectType) {
        SubmodelType submodelType;

        switch (aspectType) {
            case "assemblypartrelationship":
                submodelType = SubmodelType.ASSEMBLY_PART_RELATIONSHIP;
                break;
            case "serialparttypization":
                submodelType = SubmodelType.SERIAL_PART_TYPIZATION;
                break;
            default:
                // TODO (Saber Dridi) Extend the submodel types and improve the default case
                submodelType = SubmodelType.ASSEMBLY_PART_RELATIONSHIP;
                break;
        }
        return submodelType;
    }

}
