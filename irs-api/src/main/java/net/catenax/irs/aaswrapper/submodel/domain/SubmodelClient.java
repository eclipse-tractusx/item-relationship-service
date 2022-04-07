//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import net.catenax.irs.aaswrapper.dto.AspectModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Submodel Rest Client
 */
interface SubmodelClient {

    /**
     * @return Returns the Submodel
     */
    AspectModel getSubmodel(String endpointUrl);

}

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
@Profile("local")
@Service
class SubmodelClientLocalStub implements SubmodelClient {

    @Override
    public AspectModel getSubmodel(final String endpointUrl) {
        // String level, String content, String extent
        final SubmodelTestdataCreator submodelTestdataCreator = new SubmodelTestdataCreator();

        return submodelTestdataCreator.createDummyAssemblyPartRelationshipForId(endpointUrl);
    }
}
