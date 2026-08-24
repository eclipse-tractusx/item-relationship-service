/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.e2e.integration;

import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.DISCOVERY_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.DTR_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.EDC_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.NO_ERROR_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.SEMHUB_REST_TEMPLATE;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.policy.model.PolicyRegistrationTypes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Test-only RestTemplate wiring for recursive IRS integration test contexts.
 *
 * <p>The business beans stay untouched. This configuration only replaces the RestTemplate setup that is disabled by the
 * integrationtest profile, so the multi-context test can call dynamic WireMock endpoints directly.</p>
 */
@TestConfiguration
public class RecursiveIntegrationRestTemplateConfiguration {

    private static final String RECURSIVE_NOTIFICATION_PATH = "/irs/recursive/notifications";
    private static final String EDC_BPN_HEADER = "edc-bpn";

    @Primary
    @Profile("integrationtest")
    @Bean(DTR_REST_TEMPLATE)
    RestTemplate dtrRestTemplate() {
        return restTemplate();
    }

    @Primary
    @Profile("integrationtest")
    @Bean(EDC_REST_TEMPLATE)
    RestTemplate edcRestTemplate() {
        final RestTemplate restTemplate = restTemplate();
        final List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (final HttpMessageConverter<?> converter : messageConverters) {
            if (converter instanceof final MappingJackson2HttpMessageConverter jacksonConverter) {
                final ObjectMapper mapper = jacksonConverter.getObjectMapper();
                PolicyRegistrationTypes.TYPES.forEach(mapper::registerSubtypes);
            }
        }
        restTemplate.getInterceptors().add(edcBpnHeaderInterceptor());
        return restTemplate;
    }

    @Primary
    @Profile("integrationtest")
    @Bean(NO_ERROR_REST_TEMPLATE)
    RestTemplate noErrorRestTemplate() {
        return restTemplate();
    }

    @Primary
    @Profile("integrationtest")
    @Bean(DISCOVERY_REST_TEMPLATE)
    RestTemplate discoveryRestTemplate() {
        return restTemplate();
    }

    @Primary
    @Profile("integrationtest")
    @Bean(SEMHUB_REST_TEMPLATE)
    @Qualifier(SEMHUB_REST_TEMPLATE)
    RestTemplate semanticHubRestTemplate() {
        return restTemplate();
    }

    private RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private ClientHttpRequestInterceptor edcBpnHeaderInterceptor() {
        final ObjectMapper objectMapper = new ObjectMapper();
        return (request, body, execution) -> {
            if (request.getURI().getPath().endsWith(RECURSIVE_NOTIFICATION_PATH)) {
                final String senderBpnl = senderBpnl(objectMapper, body);
                if (senderBpnl != null) {
                    request.getHeaders().set(EDC_BPN_HEADER, senderBpnl);
                }
            }
            return execution.execute(request, body);
        };
    }

    private String senderBpnl(final ObjectMapper objectMapper, final byte[] body) {
        try {
            final JsonNode senderBpnl = objectMapper.readTree(body).path("header").path("senderBpn");
            return senderBpnl.isTextual() && !senderBpnl.asText().isBlank() ? senderBpnl.asText() : null;
        } catch (final IOException e) {
            return null;
        }
    }
}
