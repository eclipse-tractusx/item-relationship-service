/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.aaswrapper.submodel.domain;

import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.BASIC_AUTH_REST_TEMPLATE;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.function.Supplier;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.tractusx.irs.services.OutboundMeterRegistryService;
import org.eclipse.tractusx.irs.util.JsonUtil;
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
    private final RetryRegistry retryRegistry;
    private final UrlValidator urlValidator;

    /* package */ SubmodelClientImpl(@Qualifier(BASIC_AUTH_REST_TEMPLATE) final RestTemplate restTemplate,
            @Value("${aasWrapper.host}") final String aasWrapperHost, final JsonUtil jsonUtil,
            final OutboundMeterRegistryService meterRegistryService, final RetryRegistry retryRegistry,
            final UrlValidator urlValidator) {
        this.restTemplate = restTemplate;
        this.urlValidator = urlValidator;
        this.aasWrapperUriAddressRewritePolicy = new AASWrapperUriAddressRewritePolicy(aasWrapperHost);
        this.jsonUtil = jsonUtil;
        this.meterRegistryService = meterRegistryService;
        this.retryRegistry = retryRegistry;
    }

    @Override
    public <T> T getSubmodel(final String submodelEndpointAddress, final Class<T> submodelClass) {
        return execute(submodelEndpointAddress, () -> {
            final ResponseEntity<String> entity = restTemplate.getForEntity(buildUri(submodelEndpointAddress),
                    String.class);
            return jsonUtil.fromString(entity.getBody(), submodelClass);
        });
    }

    @Override
    public String getSubmodel(final String submodelEndpointAddress) {
        return execute(submodelEndpointAddress, () -> {
            final ResponseEntity<String> entity = restTemplate.getForEntity(buildUri(submodelEndpointAddress),
                    String.class);
            return entity.getBody();
        });
    }

    private URI buildUri(final String submodelEndpointAddress) {
        return this.aasWrapperUriAddressRewritePolicy.rewriteToAASWrapperUri(submodelEndpointAddress);
    }

    private <T> T execute(final String endpointAddress, final Supplier<T> supplier) {
        if (!urlValidator.isValid(endpointAddress)) {
            throw new IllegalArgumentException(String.format("Malformed endpoint address '%s'", endpointAddress));
        }
        final String host = URI.create(endpointAddress).getHost();
        final Retry retry = retryRegistry.retry(host, "default");
        return Retry.decorateSupplier(retry, () -> {
            try {
                return supplier.get();
            } catch (ResourceAccessException e) {
                if (e.getCause() instanceof SocketTimeoutException) {
                    meterRegistryService.incrementSubmodelTimeoutCounter(endpointAddress);
                }
                throw e;
            }
        }).get();
    }

}
