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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.AspectModelTypes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Submodel Rest Client
 */
//@Profile("!local")
//@FeignClient(contextId = "submodelClientContextId", value = "submodelClient",
//        url = "${feign.client.config.submodel.url}")
public interface SubmodelClient {

    /**
     * @return Returns the Submodel
     */
//    @GetMapping(value = "/submodel", consumes = APPLICATION_JSON_VALUE)
    AspectModel getSubmodel(final String endpointPath, final AspectModelTypes aspectModelTypes);

}
