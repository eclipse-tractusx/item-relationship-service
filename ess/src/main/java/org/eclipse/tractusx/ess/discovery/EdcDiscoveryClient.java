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
package org.eclipse.tractusx.ess.discovery;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * EDC Discovery Service Rest Client
 */
interface EdcDiscoveryClient {

    /**
     * Lookup EDC Address for BPN number
     * @param bpn number
     * @return EDC addresses
     */
    EdcAddressResponse getEdcBaseUrl(String bpn);

}

/**
 * EDC Discovery Service Rest Client Stub used in local environment
 */
@Service
class EdcDiscoveryClientLocalStub implements EdcDiscoveryClient {

    private final EdcDiscoveryMockConfig edcDiscoveryMockConfig;

    /* package */ EdcDiscoveryClientLocalStub(final EdcDiscoveryMockConfig edcDiscoveryMockConfig) {
        this.edcDiscoveryMockConfig = edcDiscoveryMockConfig;
    }

    @Override
    public EdcAddressResponse getEdcBaseUrl(final String bpn) {
        final Optional<String> connectorEndpoint = Optional.ofNullable(
                this.edcDiscoveryMockConfig.getMockEdcAddress().get(bpn));

        return EdcAddressResponse.builder()
                                 .bpn(bpn)
                                 .connectorEndpoint(connectorEndpoint
                                         .map(Collections::singletonList)
                                         .orElseGet(Collections::emptyList))
                                 .build();
    }

    /**
     * Config for BPN - EDC url mapping
     */
    @Configuration
    @ConfigurationProperties("ess.discovery")
    @Data
    /* package */ static class EdcDiscoveryMockConfig {
        private Map<String, String> mockEdcAddress;
    }
}

/**
 * EDC Discovery Service Rest Client Implementation
 */
class EdcDiscoveryClientImpl implements EdcDiscoveryClient {

    private final RestTemplate restTemplate;
    private final String discoveryAddressUrl;

    /* package */ EdcDiscoveryClientImpl(@Qualifier("discoveryRestTemplate") final RestTemplate restTemplate,
            @Value("${ess.discovery.endpoint:}") final String discoveryAddressUrl) {
        this.restTemplate = restTemplate;
        this.discoveryAddressUrl = discoveryAddressUrl;
    }

    @Override
    public EdcAddressResponse getEdcBaseUrl(final String bpn) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(discoveryAddressUrl);

        return restTemplate.postForObject(uriBuilder.build().toUri(), Collections.singletonList(bpn), EdcAddressResponse.class);
    }
}
