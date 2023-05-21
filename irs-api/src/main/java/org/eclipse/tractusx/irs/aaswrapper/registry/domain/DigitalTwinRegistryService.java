package org.eclipse.tractusx.irs.aaswrapper.registry.domain;

import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;

public interface DigitalTwinRegistryService {

    /**
     * Retrieves {@link AssetAdministrationShellDescriptor} from Digital Twin Registry Service.
     * As a first step id of shell is being retrieved by DigitalTwinRegistryKey.
     *
     * @param key The Asset Administration Shell's DigitalTwinRegistryKey
     * @return AAShell
     */
    AssetAdministrationShellDescriptor getAAShellDescriptor(final DigitalTwinRegistryKey key);

}
