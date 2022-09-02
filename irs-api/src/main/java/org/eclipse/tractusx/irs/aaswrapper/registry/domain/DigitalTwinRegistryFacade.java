//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.aaswrapper.registry.domain;

import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.dto.JobParameter;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for digital twin registry domain
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalTwinRegistryFacade {

    private final DigitalTwinRegistryClient digitalTwinRegistryClient;

    /**
     * Combines required data from Digital Twin Registry Service
     *
     * @param globalAssetId The Asset Administration Shell's global id
     * @param jobData       the job data parameters
     * @return list of submodel addresses
     */
    public AssetAdministrationShellDescriptor getAAShellDescriptor(final String globalAssetId,
            final JobParameter jobData) {
        final String aaShellIdentification = getAAShellIdentificationOrGlobalAssetId(globalAssetId);
        log.info("Retrieved AAS Identification {} for globalAssetId {}", aaShellIdentification, globalAssetId);

        final AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(aaShellIdentification);
        return assetAdministrationShellDescriptor.withFilteredSubmodelDescriptors(jobData.getAspectTypes());
    }

    private String getAAShellIdentificationOrGlobalAssetId(final String globalAssetId) {
        final IdentifierKeyValuePair identifierKeyValuePair = IdentifierKeyValuePair.builder()
                                                                           .key("globalAssetId")
                                                                           .value(globalAssetId)
                                                                           .build();

        final List<String> allAssetAdministrationShellIdsByAssetLink = digitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                Collections.singletonList(identifierKeyValuePair));

        return allAssetAdministrationShellIdsByAssetLink.stream().findFirst().orElse(globalAssetId);
    }

}
