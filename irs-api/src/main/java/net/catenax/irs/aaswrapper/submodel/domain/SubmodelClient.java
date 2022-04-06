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

import net.catenax.irs.aspectmodels.AspectModel;

/**
 * Submodel Rest Client
 */
public interface SubmodelClient {

    /**
     * @param endpointPath the url pointing to the endpoint
     * @param aspectModelClass the class of aspect model which is expected
     * @return Returns the expected aspect model
     */
    AspectModel getSubmodel(String endpointPath, Class<? extends AspectModel> aspectModelClass);
}
