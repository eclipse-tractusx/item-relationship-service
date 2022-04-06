//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.dto.SubmodelEndpoint;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.aspectmodels.AspectModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Asset-Administration-Shell Rest Client Stub used in local environment
 */
@Profile("local")
@Service
@Slf4j
@RequiredArgsConstructor
public class AASWrapperClientLocalStub implements AASWrapperClient {

    private final DigitalTwinRegistryFacade digitalTwinRegistryFacade;
    private final SubmodelFacade submodelFacade;

    public List<SubmodelEndpoint> getAASSubmodelEndpointAddresses(final String aasIdentifier) {
        return digitalTwinRegistryFacade.getAASSubmodelEndpointAddresses(aasIdentifier);
    }

//    @Override
//    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {
//        return digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(aasIdentifier);
//    }
//
//    @Override
//    public AspectModel getSubmodel(final String endpointPath, final Class<? extends AspectModel> aspectModelClass) {
//        return submodelClient.getSubmodel(endpointPath, aspectModelClass);
//    }
}
