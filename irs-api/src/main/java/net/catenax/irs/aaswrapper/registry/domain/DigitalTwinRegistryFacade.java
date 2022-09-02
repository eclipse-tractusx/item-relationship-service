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

import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.IdentifierKeyValuePair;
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
     * Retrieves {@link AssetAdministrationShellDescriptor} from Digital Twin Registry Service.
     * As a first step id of shell is being retrieved by globalAssetId.
     *
     * @param globalAssetId The Asset Administration Shell's global id
     * @return AAShell
     */
    public AssetAdministrationShellDescriptor getAAShellDescriptor(final String globalAssetId) {
        final String aaShellIdentification = getAAShellIdentificationOrGlobalAssetId(globalAssetId);
        log.info("Retrieved AAS Identification {} for globalAssetId {}", aaShellIdentification, globalAssetId);

        return digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(aaShellIdentification);
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
