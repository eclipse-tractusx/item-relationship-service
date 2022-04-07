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

import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.configuration.SubmodelClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Submodel Rest Client
 */
@Profile("!local")
@FeignClient(contextId = "submodelClientContextId", value = "submodelClient",
        url = "${feign.client.config.submodel.url}", configuration = SubmodelClientConfig.class)
public interface SubmodelClient {

    /**
     * @param level
     * @param content
     * @param extent
     * @return the submodel
     */
    @GetMapping(value = "/submodel", consumes = APPLICATION_JSON_VALUE)
    AssemblyPartRelationship getSubmodel(@RequestParam("level") String level, @RequestParam("content") String content,
            @RequestParam("extent") String extent);

}

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
@Profile("local")
@Service
@ExcludeFromCodeCoverageGeneratedReport
class SubmodelClientLocalStub implements SubmodelClient {

    /**
     * @param level
     * @param content
     * @param extent
     * @return the submodel from the remote api
     */
    @Override
    public AssemblyPartRelationship getSubmodel(final String level, final String content, final String extent) {
        return new AssemblyPartRelationship();
    }
}
