/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
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
package org.eclipse.tractusx.irs.configuration;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.policy.model.PolicyRegistrationTypes;
import org.eclipse.tractusx.irs.common.OutboundMeterRegistryService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * Rest template config with OAuth2 interceptor
 */
@Configuration
@RequiredArgsConstructor
@SuppressWarnings("PMD.ExcessiveImports")
public class RestTemplateConfig {

    public static final String DTR_REST_TEMPLATE = "dtrRestTemplate";
    public static final String BPDM_REST_TEMPLATE = "bpdmRestTemplate";
    public static final String SEMHUB_REST_TEMPLATE = "semhubRestTemplate";
    public static final String NO_ERROR_REST_TEMPLATE = "noErrorRestTemplate";
    public static final String DISCOVERY_REST_TEMPLATE = "discoveryRestTemplate";
    public static final String EDC_REST_TEMPLATE = "edcClientRestTemplate";

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    private RestTemplateBuilder oAuthRestTemplate(final RestTemplateBuilder restTemplateBuilder,
            final Duration readTimeout, final Duration connectTimeout, final String clientRegistrationId) {
        final var clientRegistration = clientRegistrationRepository.findByRegistrationId(clientRegistrationId);

        return restTemplateBuilder.additionalInterceptors(
                                          new OAuthClientCredentialsRestTemplateInterceptor(authorizedClientManager(), clientRegistration))
                                  .setReadTimeout(readTimeout)
                                  .setConnectTimeout(connectTimeout);
    }

    @Bean(DTR_REST_TEMPLATE)
        /* package */ RestTemplate digitalTwinRegistryRestTemplate(final RestTemplateBuilder restTemplateBuilder,
            @Value("${digitalTwinRegistry.timeout.read}") final Duration readTimeout,
            @Value("${digitalTwinRegistry.timeout.connect}") final Duration connectTimeout,
            @Value("${digitalTwinRegistry.oAuthClientId}") final String clientRegistrationId,
            final OutboundMeterRegistryService meterRegistryService) {

        return oAuthRestTemplate(restTemplateBuilder, readTimeout, connectTimeout,
                clientRegistrationId).additionalInterceptors(getRegistryInterceptor(meterRegistryService)).build();
    }

    @NotNull
    private static ClientHttpRequestInterceptor getRegistryInterceptor(
            final OutboundMeterRegistryService meterRegistryService) {
        return (request, body, execution) -> {
            try {
                return execution.execute(request, body);
            } catch (SocketTimeoutException e) {
                meterRegistryService.incrementRegistryTimeoutCounter();
                throw e;
            }
        };
    }

    @Bean(SEMHUB_REST_TEMPLATE)
        /* package */ RestTemplate semanticHubRestTemplate(final RestTemplateBuilder restTemplateBuilder,
            @Value("${semanticshub.timeout.read}") final Duration readTimeout,
            @Value("${semanticshub.timeout.connect}") final Duration connectTimeout,
            @Value("${semanticshub.oAuthClientId}") final String clientRegistrationId) {
        return oAuthRestTemplate(restTemplateBuilder, readTimeout, connectTimeout, clientRegistrationId).build();
    }

    @Bean(BPDM_REST_TEMPLATE)
        /* package */ RestTemplate bpdmRestTemplate(final RestTemplateBuilder restTemplateBuilder,
            @Value("${bpdm.timeout.read}") final Duration readTimeout,
            @Value("${bpdm.timeout.connect}") final Duration connectTimeout,
            @Value("${bpdm.oAuthClientId}") final String clientRegistrationId) {
        return oAuthRestTemplate(restTemplateBuilder, readTimeout, connectTimeout, clientRegistrationId).build();
    }

    @Bean(DISCOVERY_REST_TEMPLATE)
        /* package */ RestTemplate discoveryRestTemplate(final RestTemplateBuilder restTemplateBuilder,
            @Value("${ess.discovery.timeout.read}") final Duration readTimeout,
            @Value("${ess.discovery.timeout.connect}") final Duration connectTimeout,
            @Value("${ess.discovery.oAuthClientId}") final String clientRegistrationId) {
        return oAuthRestTemplate(restTemplateBuilder, readTimeout, connectTimeout, clientRegistrationId).build();
    }

    @Bean(NO_ERROR_REST_TEMPLATE)
        /* package */ RestTemplate noErrorRestTemplate(final RestTemplateBuilder restTemplateBuilder,
            @Value("${irs.job.callback.timeout.read}") final Duration readTimeout,
            @Value("${irs.job.callback.timeout.connect}") final Duration connectTimeout) {
        final RestTemplate restTemplate = restTemplateBuilder.setReadTimeout(readTimeout)
                                                             .setConnectTimeout(connectTimeout)
                                                             .build();

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            /**
             * Dont care what is the response, just log status code on console
             * @param statusCode omitted
             * @return false
             */
            @Override
            public boolean hasError(final ClientHttpResponse statusCode) {
                return false;
            }
        });

        return restTemplate;
    }

    @Bean
        /* package */ OAuth2AuthorizedClientManager authorizedClientManager() {
        final var authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                                                                                  .clientCredentials()
                                                                                  .build();

        final var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, oAuth2AuthorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean(EDC_REST_TEMPLATE)
    @Qualifier(EDC_REST_TEMPLATE)
        /* package */ RestTemplate edcRestTemplate(final RestTemplateBuilder restTemplateBuilder,
            @Value("${irs-edc-client.submodel.timeout.read}") final Duration readTimeout,
            @Value("${irs-edc-client.submodel.timeout.connect}") final Duration connectTimeout,
            final OutboundMeterRegistryService meterRegistryService) {
        final RestTemplate restTemplate = restTemplateBuilder.setReadTimeout(readTimeout)
                                                             .setConnectTimeout(connectTimeout)
                                                             .additionalInterceptors(
                                                                     getEdcInterceptor(meterRegistryService))
                                                             .build();
        final List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (final HttpMessageConverter<?> converter : messageConverters) {
            if (converter instanceof final MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
                final ObjectMapper mappingJackson2HttpMessageConverterObjectMapper = mappingJackson2HttpMessageConverter.getObjectMapper();
                PolicyRegistrationTypes.TYPES.forEach(
                        mappingJackson2HttpMessageConverterObjectMapper::registerSubtypes);
            }
        }
        return restTemplate;
    }

    @NotNull
    private static ClientHttpRequestInterceptor getEdcInterceptor(
            final OutboundMeterRegistryService meterRegistryService) {
        return (request, body, execution) -> {
            try {
                return execution.execute(request, body);
            } catch (SocketTimeoutException e) {
                meterRegistryService.incrementSubmodelTimeoutCounter(request.getURI().getHost());
                throw e;
            }
        };
    }

    /**
     * Interceptor to add Authorization header to every call done via Rest template
     */
    @RequiredArgsConstructor
    @Slf4j
    /* package */ static class OAuthClientCredentialsRestTemplateInterceptor implements ClientHttpRequestInterceptor {

        private final OAuth2AuthorizedClientManager manager;
        private final ClientRegistration clientRegistration;

        @Override
        public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
                final ClientHttpRequestExecution execution) throws IOException {
            final OAuth2AuthorizeRequest oAuth2AuthorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(
                    clientRegistration.getRegistrationId()).principal(clientRegistration.getClientName()).build();

            final OAuth2AuthorizedClient client = manager.authorize(oAuth2AuthorizeRequest);

            if (isNull(client)) {
                throw new IllegalStateException("Client credentials flow on " + clientRegistration.getRegistrationId()
                        + " failed, client is null");
            }

            log.debug("Adding Authorization header to the request");

            request.getHeaders().add(HttpHeaders.AUTHORIZATION, buildAuthorizationHeaderValue(client.getAccessToken()));
            return execution.execute(request, body);
        }

        @NotNull
        private String buildAuthorizationHeaderValue(final OAuth2AccessToken accessToken) {
            return accessToken.getTokenType().getValue() + " " + accessToken.getTokenValue();
        }
    }

}
