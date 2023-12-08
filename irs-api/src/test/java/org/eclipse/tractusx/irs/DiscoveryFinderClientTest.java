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

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.tractusx.irs.configuration.RestTemplateConfig;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryEndpoint;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClient;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClientImpl;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderRequest;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
                properties = "irs-edc-client.discoveryFinderClient.cacheTTL=PT0.1S")
@ActiveProfiles(profiles = "test")
@Import({ TestConfig.class })
class DiscoveryFinderClientTest {

    private static final DiscoveryResponse MOCKED_DISCOVERY_RESPONSE = new DiscoveryResponse(
            List.of(new DiscoveryEndpoint("test-endpoint", "desc", "test-endpoint-addr", "docs", "resId")));

    @Autowired
    private DiscoveryFinderClient testee;

    @MockBean(name = RestTemplateConfig.DTR_REST_TEMPLATE)
    private RestTemplate restTemplateMock;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void findDiscoveryEndpoints_WhenCalled_ResultsShouldBeCached() {

        // GIVEN
        final var request = new DiscoveryFinderRequest(List.of("bpn"));
        final var originalResponse = MOCKED_DISCOVERY_RESPONSE;
        simulateFindDiscoveryEndpointsRestRequest(request, originalResponse);

        // WHEN
        final var actualResult = testee.findDiscoveryEndpoints(request);

        // THEN
        // real endpoint must be called
        verify(restTemplateMock).postForObject("", request, DiscoveryResponse.class);

        // and the response must be cached
        {
            final var cachedResponse = requireDiscoveryEndpointsCacheValue(request);
            final var cachedAddresses = extractEndpointAddresses(cachedResponse);
            final var returnedAddresses = extractEndpointAddresses(actualResult);
            final var originalAddresses = extractEndpointAddresses(originalResponse);

            // which means that now the value in the cache must equal the original and the actual value
            assertThat(cachedAddresses).isEqualTo(originalAddresses).isEqualTo(returnedAddresses);

            // and subsequent calls must be answered from cache instead of calling http service again
            final DiscoveryResponse subsequentResult = testee.findDiscoveryEndpoints(request);
            assertThat(extractEndpointAddresses(subsequentResult)).isEqualTo(cachedAddresses);
            verifyNoMoreInteractions(restTemplateMock);
        }
    }

    @Test
    void evictDiscoveryEndpointsCacheValues_WhenScheduled_ShouldEvictCache() {

        // GIVEN
        final var request = new DiscoveryFinderRequest(List.of("bpn"));
        simulateFindDiscoveryEndpointsRestRequest(request, MOCKED_DISCOVERY_RESPONSE);
        testee.findDiscoveryEndpoints(request);
        final var cache = getDiscoveryEndpointsCache();
        assertThat(cache.get(request)).isNotNull();

        // WHEN
        Awaitility.await().atLeast(Duration.ofMillis(100))
                  // THEN
                  .untilAsserted(() -> assertThat(cache.get(request)).isNull());

    }

    private void simulateFindDiscoveryEndpointsRestRequest(final DiscoveryFinderRequest discoveryFinderRequest,
            final DiscoveryResponse discoveryResponse) {
        when(restTemplateMock.postForObject("", discoveryFinderRequest, DiscoveryResponse.class)).thenReturn(
                discoveryResponse);
    }

    private DiscoveryResponse requireDiscoveryEndpointsCacheValue(final DiscoveryFinderRequest request) {
        final var cache = getDiscoveryEndpointsCache();
        final var cacheValue = cache.get(request);
        assertThat(cacheValue).isNotNull();
        final DiscoveryResponse discoveryResponse = (DiscoveryResponse) cacheValue.get();
        assertThat(discoveryResponse).isNotNull();
        return discoveryResponse;
    }

    private Cache getDiscoveryEndpointsCache() {
        return cacheManager.getCache(DiscoveryFinderClientImpl.DISCOVERY_ENDPOINTS_CACHE);
    }

    private List<String> extractEndpointAddresses(final DiscoveryResponse discoveryResponse) {
        return discoveryResponse.endpoints().stream() //
                                .map(DiscoveryEndpoint::endpointAddress) //
                                .sorted() //
                                .collect(Collectors.toList());
    }
}
