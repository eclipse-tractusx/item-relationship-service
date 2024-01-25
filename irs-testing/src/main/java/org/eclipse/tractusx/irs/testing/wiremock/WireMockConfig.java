/********************************************************************************
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.testing.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import java.net.InetSocketAddress;
import java.net.Proxy;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Common configurations for Wiremock tests.
 */
public final class WireMockConfig {
    private WireMockConfig() {
    }

    /**
     * Configured RestTemplate which proxies all requests to the provided host / port.
     *
     * @param proxyServerHost the host where all requests will be proxied to
     * @param httpPort        the port of the host where all requests will be proxied to
     * @return the configured {@link RestTemplate}
     */
    public static RestTemplate restTemplateProxy(final String proxyServerHost, final int httpPort) {
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyServerHost, httpPort));
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(proxy);
        return new RestTemplate(requestFactory);
    }

    public static ResponseDefinitionBuilder responseWithStatus(final int statusCode) {
        return aResponse().withStatus(statusCode).withHeader("Content-Type", "application/json;charset=UTF-8");
    }
}
