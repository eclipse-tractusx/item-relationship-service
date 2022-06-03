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

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.job.AspectTypeFilter;
import net.catenax.irs.component.assemblypartrelationship.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assemblypartrelationship.SubmodelDescriptor;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.dto.JobParameter;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for digital twin registry domain
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalTwinRegistryFacade {

    private final AspectTypeFilter aspectTypeFilter = new AspectTypeFilter();
    private final DigitalTwinRegistryClient digitalTwinRegistryClient;

    /**
     * Combines required data from Digital Twin Registry Service
     *
     * @param aasIdentifier The Asset Administration Shell's unique id
     * @param jobData       the job data parameters
     * @return list of submodel addresses
     */
    public AssetAdministrationShellDescriptor getAASShellDescriptor(final String aasIdentifier,
            final JobParameter jobData) {
        final AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(
                aasIdentifier);
        final List<SubmodelDescriptor> submodelDescriptors = filterByAspectType(
                assetAdministrationShellDescriptor.getSubmodelDescriptors(), jobData.getAspectTypes());
        return assetAdministrationShellDescriptor.toBuilder().submodelDescriptors(submodelDescriptors).build();
    }

    /**
     * @param submodelDescriptor the submodel descriptor
     * @param aspectTypes        the aspect types which should be filtered by
     * @return True, if the aspect type of the submodelDescriptor is part of
     * the given consumer aspectTypes
     */
    private List<SubmodelDescriptor> filterByAspectType(final List<SubmodelDescriptor> submodelDescriptor,
            final List<String> aspectTypes) {

        final List<String> filterAspectTypes = new ArrayList<>(aspectTypes);

        if (containsAssemblyPartRelationship(filterAspectTypes)) {
            filterAspectTypes.add(AspectType.ASSEMBLY_PART_RELATIONSHIP.toString());
        }
        log.info("Adjusted Aspect Type Filter '{}'", filterAspectTypes);

        return aspectTypeFilter.filterDescriptorsByAspectTypes(submodelDescriptor, filterAspectTypes);
    }

    private boolean containsAssemblyPartRelationship(final List<String> filterAspectTypes) {
        return !filterAspectTypes.contains(AspectType.ASSEMBLY_PART_RELATIONSHIP.toString());
    }

}
