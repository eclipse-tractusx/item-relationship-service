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

import static net.catenax.irs.configuration.RestTemplateConfig.BASIC_AUTH_REST_TEMPLATE;

import java.net.URI;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
    <T> T getSubmodel(String submodelEndpointAddress, Class<T> submodelClass);

}

/**
 * Submodel client Rest Client Stub used in local environment
 */
@Service
@Profile({"local", "test"})
class SubmodelClientLocalStub implements SubmodelClient {

    @Override
    public <T> T getSubmodel(final String submodelEndpointAddress, final Class<T> submodelClass) {
        if ("urn:uuid:c35ee875-5443-4a2d-bc14-fdacd64b9446".equals(submodelEndpointAddress)) {
            throw new RestClientException("Dummy Exception");
        }
        final SubmodelTestdataCreator submodelTestdataCreator = new SubmodelTestdataCreator();
        return (T) submodelTestdataCreator.createDummyAssemblyPartRelationshipForId(submodelEndpointAddress);
    }

}

/**
 * Submodel Rest Client Implementation
 */
@Slf4j
@Service
@Profile({ "!local && !test" })
class SubmodelClientImpl implements SubmodelClient {

    private final RestTemplate restTemplate;
    private final AASWrapperUriAddressRewritePolicy aasWrapperUriAddressRewritePolicy;
    private final JsonUtil jsonUtil = new JsonUtil();

    /* package */ SubmodelClientImpl(@Qualifier(BASIC_AUTH_REST_TEMPLATE) final RestTemplate restTemplate,
            @Value("${aasWrapper.host}") final String aasWrapperHost) {
        this.restTemplate = restTemplate;
        this.aasWrapperUriAddressRewritePolicy = new AASWrapperUriAddressRewritePolicy(aasWrapperHost);
    }

    @Override
    public <T> T getSubmodel(final String submodelEndpointAddress, final Class<T> submodelClass) {
        final ResponseEntity<String> entity = restTemplate.getForEntity(buildUri(submodelEndpointAddress),
                String.class);
        return jsonUtil.fromString(entity.getBody(), submodelClass);
    }

    private URI buildUri(final String submodelEndpointAddress) {
        return this.aasWrapperUriAddressRewritePolicy.rewriteToAASWrapperUri(submodelEndpointAddress);
    }

}
