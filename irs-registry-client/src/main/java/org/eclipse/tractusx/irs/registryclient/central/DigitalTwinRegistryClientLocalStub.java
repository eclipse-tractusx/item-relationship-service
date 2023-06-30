package org.eclipse.tractusx.irs.registryclient.central;

import java.util.Collections;
import java.util.List;

import org.eclipse.tractusx.irs.common.CxTestDataContainer;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
@Service
@Profile({ "local",
           "stubtest"
})
class DigitalTwinRegistryClientLocalStub implements DigitalTwinRegistryClient {

    private final AssetAdministrationShellTestdataCreator testdataCreator;

    /* package */ DigitalTwinRegistryClientLocalStub(final CxTestDataContainer cxTestDataContainer) {
        this.testdataCreator = new AssetAdministrationShellTestdataCreator(cxTestDataContainer);
    }

    @Override
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {
        return testdataCreator.createDummyAssetAdministrationShellDescriptorForId(aasIdentifier);
    }

    @Override
    public List<String> getAllAssetAdministrationShellIdsByAssetLink(final List<IdentifierKeyValuePair> assetIds) {
        return Collections.emptyList();
    }
}
