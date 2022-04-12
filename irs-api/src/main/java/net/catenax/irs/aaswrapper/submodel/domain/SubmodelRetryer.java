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

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for retrying the retirement of submodel json's
 * from the remote aas wrapper service
 */
@Slf4j
@Service
public class SubmodelRetryer {
    private final SubmodelClient client;

    public SubmodelRetryer(final SubmodelClient client) {
        this.client = client;
    }

    /**
     * Retry the call getting a submodel from the remote
     * aas wrapper service
     *
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @param submodelClass           The Aspect Model for the given submodel
     *                                like AssemblyPartRelationship
     * @param <T>                     the generic Aspect Model
     * @return the requested submodel
     */
    @Retry(name = "submodelRetryer")
    public <T> T retrySubmodel(final String submodelEndpointAddress, final Class<T> submodelClass) {
        return this.client.getSubmodel(submodelEndpointAddress, submodelClass);
    }

}
