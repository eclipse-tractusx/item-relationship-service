package net.catenax.irs.aaswrapper;

import net.catenax.irs.aaswrapper.registry.domain.model.AssetAdministrationShellDescriptor;
import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.AspectModelTypes;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * AAS-Wrapper Rest Client
 */
@Profile("!local")
@Service
public interface AASWrapperClient {

    AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier);

    AspectModel getSubmodel(final String endpointPath, final AspectModelTypes aspectModel);
}
