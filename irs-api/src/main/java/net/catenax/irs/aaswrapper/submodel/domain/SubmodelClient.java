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

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.function.Supplier;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.services.OutboundMeterRegistryService;
import net.catenax.irs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
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

    /**
     * @return Returns the Submodel as String
     */
    String getSubmodel(String submodelEndpointAddress);

}

/**
 * Submodel client Rest Client Stub used in local environment
 */
@Service
@Profile({ "local",
           "stubtest"
})
class SubmodelClientLocalStub implements SubmodelClient {

    private final JsonUtil jsonUtil = new JsonUtil();

    @Override
    public <T> T getSubmodel(final String submodelEndpointAddress, final Class<T> submodelClass) {
        return jsonUtil.fromString(getStubData(submodelEndpointAddress), submodelClass);
    }

    @Override
    public String getSubmodel(final String submodelEndpointAddress) {
        return getStubData(submodelEndpointAddress);
    }

    private String getStubData(final String submodelEndpointAddress) {
        if ("urn:uuid:c35ee875-5443-4a2d-bc14-fdacd64b9446".equals(submodelEndpointAddress)) {
            throw new RestClientException("Dummy Exception");
        }
        final SubmodelTestdataCreator submodelTestdataCreator = new SubmodelTestdataCreator();

        if ("urn:uuid:ea724f73-cb93-4b7b-b92f-d97280ff888b".equals(submodelEndpointAddress)) {
            return submodelTestdataCreator.createDummySerialPartTypizationString();
        }

        final AssemblyPartRelationship relationship = submodelTestdataCreator.createDummyAssemblyPartRelationshipForId(
                submodelEndpointAddress);
        return jsonUtil.asString(relationship);
    }

}

/**
 * Submodel Rest Client Implementation
 */
@Slf4j
@Service
@Profile({ "!local && !stubtest" })
class SubmodelClientImpl implements SubmodelClient {

    private final RestTemplate restTemplate;
    private final AASWrapperUriAddressRewritePolicy aasWrapperUriAddressRewritePolicy;
    private final JsonUtil jsonUtil;
    private final OutboundMeterRegistryService meterRegistryService;

    /* package */ SubmodelClientImpl(@Qualifier(BASIC_AUTH_REST_TEMPLATE) final RestTemplate restTemplate,
            @Value("${aasWrapper.host}") final String aasWrapperHost, final JsonUtil jsonUtil,
            final OutboundMeterRegistryService meterRegistryService) {
        this.restTemplate = restTemplate;
        this.aasWrapperUriAddressRewritePolicy = new AASWrapperUriAddressRewritePolicy(aasWrapperHost);
        this.jsonUtil = jsonUtil;
        this.meterRegistryService = meterRegistryService;
    }

    @Override
    @Retry(name = "submodel")
    public <T> T getSubmodel(final String submodelEndpointAddress, final Class<T> submodelClass) {
        final ResponseEntity<String> entity = restTemplate.getForEntity(buildUri(submodelEndpointAddress),
                String.class);
        return jsonUtil.fromString(entity.getBody(), submodelClass);
    }

    @Override
    @Retry(name = "submodel")
    public String getSubmodel(final String submodelEndpointAddress) {
        return execute(() -> {
            final ResponseEntity<String> entity = restTemplate.getForEntity(buildUri(submodelEndpointAddress),
                    String.class);
            return entity.getBody();
        });
    }

    private URI buildUri(final String submodelEndpointAddress) {
        return execute(() -> this.aasWrapperUriAddressRewritePolicy.rewriteToAASWrapperUri(submodelEndpointAddress));
    }

    private <T> T execute(final Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                meterRegistryService.incrementSubmodelTimeoutCounter();
            }
            throw e;
        }
    }

}
