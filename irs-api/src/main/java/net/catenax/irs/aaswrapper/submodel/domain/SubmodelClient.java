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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Submodel client
 */
interface SubmodelClient {

    /**
     * @return Returns the Submodel
     */
    <T> T getSubmodel(String submodelEndpointAddress, Class<T> submodelClass);

}

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
@Service
class SubmodelClientLocalStub implements SubmodelClient {

    @Override
    public <T> T getSubmodel(final String submodelEndpointAddress, final Class<T> submodelClass) {
        final SubmodelTestdataCreator submodelTestdataCreator = new SubmodelTestdataCreator();

        return (T) submodelTestdataCreator.createDummyAssemblyPartRelationshipForId(submodelEndpointAddress);
    }

}

/**
 * Submodel Rest Client Implementation
 */
class SubmodelClientImpl implements SubmodelClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public <T> T getSubmodel(final String submodelEndpointAddress, final Class<T> submodelClass) {
        return restTemplate.getForEntity(submodelEndpointAddress, submodelClass).getBody();
    }

}

/**
 * Submodel Rest Client Stub used in local environment
 */
class SubmodelClientImplStub implements SubmodelClient {

    @Override
    public <T> T getSubmodel(final String submodelEndpointAddress, final Class<T> submodelClass) {
        throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "AASWrapper remote exception");
    }

}
