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
import net.catenax.irs.aspectmodels.AspectModelTypes;

/**
 * Submodel Rest Client
 */
public interface SubmodelClient {

    /**
     * @return Returns the Submodel
     */
    AspectModel getSubmodel(final String endpointPath, final AspectModelTypes aspectModelTypes);
}
