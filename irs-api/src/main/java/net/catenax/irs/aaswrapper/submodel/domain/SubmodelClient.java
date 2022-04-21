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

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Submodel client
 */
interface SubmodelClient {

    /**
     * @return Returns the Submodel
     */
    Aspect getSubmodel(String submodelEndpointAddress, String catenaXId, Class<? extends Aspect> submodelClass)
            throws SubmodelClientException;

}

/**
 * Submodel Client Stub used in local environment
 */
@Profile("local")
@Service
class SubmodelClientLocalStub implements SubmodelClient {
    @Override
    public Aspect getSubmodel(final String submodelEndpointAddress, final String catenaXId,
            final Class<? extends Aspect> submodelClass) throws SubmodelClientException {
        if ("c35ee875-5443-4a2d-bc14-fdacd64b9446".equals(catenaXId)) {
            throw new SubmodelClientException("Dummy Exception");
        }
        final SubmodelTestdataCreator submodelTestdataCreator = new SubmodelTestdataCreator();
        return submodelTestdataCreator.createDummyAssemblyPartRelationshipForId(catenaXId);
    }

}

/**
 * Submodel Rest Client Implementation
 */
@Profile("!local")
@Slf4j
class SubmodelClientImpl implements SubmodelClient {

    private final RestTemplate restTemplate;

    /* package */ SubmodelClientImpl(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Aspect getSubmodel(final String submodelEndpointAddress, final String catenaXId,
            final Class<? extends Aspect> submodelClass) throws SubmodelClientException {
        try {
            final ResponseEntity<? extends Aspect> responseEntity = restTemplate.getForEntity(submodelEndpointAddress,
                    submodelClass);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return responseEntity.getBody();
            } else {
                throw new SubmodelClientException(responseEntity.getStatusCode().toString());
            }
        } catch (RestClientException e) {
            log.error("RestClientException: ", e);
            throw new SubmodelClientException("Request not successful: " + e.getMessage(), e);
        }
    }

}
