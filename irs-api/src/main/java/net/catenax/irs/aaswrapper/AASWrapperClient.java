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

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * AAS-Wrapper Rest Client
 */
@Profile("!local")
@Service
public interface AASWrapperClient {

//    SubmodelEndpoint getAssetAdministrationShellSubmodelEndpointAddress(String aasIdentifier);
//
//    AspectModel getSubmodel(String endpointPath, Class<? extends AspectModel> aspectModelClass);
}
