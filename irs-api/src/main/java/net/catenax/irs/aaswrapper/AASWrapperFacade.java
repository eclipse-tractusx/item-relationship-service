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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.dto.SubmodelEndpoint;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import org.springframework.stereotype.Service;

/**
 * Asset-Administration-Shell Rest Client Stub used in local environment
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AASWrapperFacade {

    private final DigitalTwinRegistryFacade digitalTwinRegistryFacade;
//    private final SubmodelFacade submodelFacade;

    public List<SubmodelEndpoint> getAASSubmodelEndpointAddresses(final String aasIdentifier) {
        return digitalTwinRegistryFacade.getAASSubmodelEndpointAddresses(aasIdentifier);
    }

//    @Override
//    public AspectModel getSubmodel(final String endpointPath, final Class<? extends AspectModel> aspectModelClass) {
//        return submodelClient.getSubmodel(endpointPath, aspectModelClass);
//    }
}
