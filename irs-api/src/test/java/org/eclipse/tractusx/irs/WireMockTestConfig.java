/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs;

import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.DISCOVERY_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.DTR_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.EDC_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.NO_ERROR_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.SEMHUB_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.restTemplateProxy;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.policy.model.PolicyRegistrationTypes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class WireMockTestConfig {
    public static final int HTTP_PORT = 8085;
    private static final String PROXY_SERVER_HOST = "127.0.0.1";

    @Primary
    @Profile("integrationtest")
    @Bean(DTR_REST_TEMPLATE)
    RestTemplate dtrRestTemplate() {
        return restTemplateProxy(PROXY_SERVER_HOST, HTTP_PORT);
    }

    @Primary
    @Profile("integrationtest")
    @Bean(EDC_REST_TEMPLATE)
    RestTemplate edcRestTemplate() {
        final RestTemplate edcRestTemplate = restTemplateProxy(PROXY_SERVER_HOST, HTTP_PORT);
        final List<HttpMessageConverter<?>> messageConverters = edcRestTemplate.getMessageConverters();
        for (final HttpMessageConverter<?> converter : messageConverters) {
            if (converter instanceof final MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
                final ObjectMapper mappingJackson2HttpMessageConverterObjectMapper = mappingJackson2HttpMessageConverter.getObjectMapper();
                PolicyRegistrationTypes.TYPES.forEach(
                        mappingJackson2HttpMessageConverterObjectMapper::registerSubtypes);
            }
        }
        return edcRestTemplate;
    }

    @Primary
    @Profile("integrationtest")
    @Bean(NO_ERROR_REST_TEMPLATE)
    RestTemplate noErrorRestTemplate() {
        return restTemplateProxy(PROXY_SERVER_HOST, HTTP_PORT);
    }

    @Primary
    @Profile("integrationtest")
    @Bean(DISCOVERY_REST_TEMPLATE)
    RestTemplate discoveryRestTemplate() {
        return restTemplateProxy(PROXY_SERVER_HOST, HTTP_PORT);
    }

    @Primary
    @Profile("integrationtest")
    @Bean(SEMHUB_REST_TEMPLATE)
    @Qualifier(SEMHUB_REST_TEMPLATE)
    RestTemplate semanticHubRestTemplate() {
        return restTemplateProxy(PROXY_SERVER_HOST, HTTP_PORT);
    }

}
