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

package org.eclipse.tractusx.irs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryEndpoint;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClient;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClientImpl;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderRequest;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles(profiles = "test")
@Import({ TestConfig.class })
class DiscoveryFinderClientTest {

    @Autowired
    private DiscoveryFinderClient testee;

    @MockBean(name = "dtrRestTemplate")
    private RestTemplate restTemplateMock;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldGenerateCacheRecordWhenFindDiscoveryEndpointsCalled() {

        // preparations
        final var expectedEndpoint = "test-endpoint-addr";
        final var discoveryFinderRequest = new DiscoveryFinderRequest(List.of("bpn"));
        simulateFindDiscoveryEndpointsRestRequest(discoveryFinderRequest, expectedEndpoint);

        // -------------
        // first call
        // -------------
        final var result = testee.findDiscoveryEndpoints(discoveryFinderRequest);

        // should call real endpoint
        verifyDiscoveryFinderHttpRequestPerformed(discoveryFinderRequest);

        // and the result should be cached
        {
            final var cachedResponse = getCachedDiscoveryResponse(discoveryFinderRequest);
            assertThat(cachedResponse.endpoints()).isNotNull();

            final var cachedAddresses = extractCachedEndpointAddresses(cachedResponse);
            assertThat(cachedAddresses).as("assert that the endpoint address '%s' has been cached", expectedEndpoint)
                                       .containsExactly(expectedEndpoint);

            assertThat(cachedResponse).as("assert that the cached value is the response from the first request")
                                      .isEqualTo(result);
        }

        // -------------
        // second call
        // -------------
        testee.findDiscoveryEndpoints(discoveryFinderRequest);
        // should be answered from cache
        verifyNoMoreInteractions(restTemplateMock);

    }

    private static List<String> extractCachedEndpointAddresses(final DiscoveryResponse cachedResponse) {
        return cachedResponse.endpoints().stream().map(DiscoveryEndpoint::endpointAddress).collect(Collectors.toList());
    }

    private void verifyDiscoveryFinderHttpRequestPerformed(final DiscoveryFinderRequest discoveryFinderRequest) {
        verify(restTemplateMock).postForObject("", discoveryFinderRequest, DiscoveryResponse.class);
    }

    private DiscoveryResponse getCachedDiscoveryResponse(final DiscoveryFinderRequest discoveryFinderRequest) {

        final var cache = cacheManager.getCache(DiscoveryFinderClientImpl.DISCOVERY_ENDPOINTS_CACHE);
        final var cacheValue = Objects.requireNonNull(cache).get(discoveryFinderRequest);

        assertThat(cacheValue).isNotNull();

        return (DiscoveryResponse) cacheValue.get();
    }

    @SuppressWarnings("SameParameterValue")
    private void simulateFindDiscoveryEndpointsRestRequest(final DiscoveryFinderRequest discoveryFinderRequest,
            final String expectedEndpoint) {
        when(restTemplateMock.postForObject("", discoveryFinderRequest, DiscoveryResponse.class)).thenReturn(
                new DiscoveryResponse(
                        List.of(new DiscoveryEndpoint("test-endpoint", "desc", expectedEndpoint, "docs", "resId"))));
    }

}
