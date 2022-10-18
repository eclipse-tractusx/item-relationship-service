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
package org.eclipse.tractusx.irs.configuration;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.time.Duration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
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
public class RestTemplateConfig {

    public static final String OAUTH_REST_TEMPLATE = "oAuthRestTemplate";
    public static final String BASIC_AUTH_REST_TEMPLATE = "basicAuthRestTemplate";
    public static final String NO_ERROR_REST_TEMPLATE = "noErrorRestTemplate";

    private static final String CLIENT_REGISTRATION_ID = "keycloak";
    private static final int TIMEOUT_SECONDS = 90;

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean(OAUTH_REST_TEMPLATE)
    /* package */ RestTemplate oAuthRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        final var clientRegistration = clientRegistrationRepository.findByRegistrationId(CLIENT_REGISTRATION_ID);

        return restTemplateBuilder.additionalInterceptors(
                                          new OAuthClientCredentialsRestTemplateInterceptor(authorizedClientManager(), clientRegistration))
                                  .setReadTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                                  .setConnectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                                  .build();
    }

    @Bean(BASIC_AUTH_REST_TEMPLATE)
    /* package */ RestTemplate basicAuthRestTemplate(final RestTemplateBuilder restTemplateBuilder,
            @Value("${aasWrapper.username}") final String aasWrapperUsername,
            @Value("${aasWrapper.password}") final String aasWrapperPassword) {
        return restTemplateBuilder.additionalInterceptors(
                                          new BasicAuthenticationInterceptor(aasWrapperUsername, aasWrapperPassword))
                                  .setReadTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                                  .setConnectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                                  .build();
    }

    @Bean(NO_ERROR_REST_TEMPLATE)
    /* package */ RestTemplate noErrorRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        final RestTemplate restTemplate = restTemplateBuilder.setReadTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                                                      .setConnectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                                                      .build();

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            /**
             * Dont care what is the response, just log status code on console
             * @param statusCode omitted
             * @return false
             */
            @Override
            public boolean hasError(final HttpStatus statusCode) {
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
            final OAuth2AuthorizeRequest oAuth2AuthorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(clientRegistration.getRegistrationId())
                    .principal(clientRegistration.getClientName())
                    .build();

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
