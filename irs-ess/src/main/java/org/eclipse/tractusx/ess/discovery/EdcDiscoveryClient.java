/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
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
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * EDC Discovery Service Rest Client
 */
interface EdcDiscoveryClient {

    /**
     * Lookup EDC Address for BPN number
     *
     * @param bpn number
     * @return EDC addresses
     */
    EdcAddressResponse[] getEdcBaseUrl(String bpn);

}

/**
 * EDC Discovery Service Rest Client Stub used in local environment
 */
@Service
@Profile({ "local",
           "stubtest"
})
class EdcDiscoveryClientLocalStub implements EdcDiscoveryClient {

    private final EdcDiscoveryMockConfig edcDiscoveryMockConfig;

    /* package */ EdcDiscoveryClientLocalStub(final EdcDiscoveryMockConfig edcDiscoveryMockConfig) {
        this.edcDiscoveryMockConfig = edcDiscoveryMockConfig;
    }

    @Override
    public EdcAddressResponse[] getEdcBaseUrl(final String bpn) {
        final Optional<List<String>> connectorEndpoint = Optional.ofNullable(
                this.edcDiscoveryMockConfig.getMockEdcAddress().get(bpn));

        return new EdcAddressResponse[] { EdcAddressResponse.builder()
                                                            .bpn(bpn)
                                                            .connectorEndpoint(connectorEndpoint.orElseGet(
                                                                    Collections::emptyList)).build()
        };
    }
}

/**
 * EDC Discovery Service Rest Client Implementation
 */
@Service
@Profile({ "!local && !stubtest" })
class EdcDiscoveryClientImpl implements EdcDiscoveryClient {

    private final RestTemplate restTemplate;
    private final String discoveryAddressUrl;

    /* package */ EdcDiscoveryClientImpl(@Qualifier("discoveryRestTemplate") final RestTemplate restTemplate,
            @Value("${ess.discovery.endpoint:}") final String discoveryAddressUrl) {
        this.restTemplate = restTemplate;
        this.discoveryAddressUrl = discoveryAddressUrl;
    }

    @Override
    public EdcAddressResponse[] getEdcBaseUrl(final String bpn) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(discoveryAddressUrl);

        return restTemplate.postForObject(uriBuilder.build().toUri(), Collections.singletonList(bpn),
                EdcAddressResponse[].class);
    }
}
