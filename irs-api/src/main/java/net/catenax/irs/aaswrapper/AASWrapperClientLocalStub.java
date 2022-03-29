package net.catenax.irs.aaswrapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.registry.domain.AssetAdministrationShellDescriptor;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryClient;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelClient;
import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.AspectModelTypes;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Asset-Administration-Shell Rest Client Stub used in local environment
 */
@Profile("local")
@Service
@Slf4j
@AllArgsConstructor
public class AASWrapperClientLocalStub implements AASWrapperClient {

    private final DigitalTwinRegistryClient digitalTwinRegistryClient;
    private final SubmodelClient submodelClient;

    @Override
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {
        log.info("Requesting AAS Shell Descriptor for id: " + aasIdentifier);
        return digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(aasIdentifier);
    }

    @Override
    public AspectModel getSubmodel(final String endpointPath, final AspectModelTypes aspectModel) {
        log.info("Requesting Submodel for path: " + endpointPath + " and aspectModel: " + aspectModel);
        return submodelClient.getSubmodel(endpointPath, aspectModel);
    }
}
