//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.registry.domain;

import static java.util.Objects.isNull;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Digital Twin Registry Rest Client configuration.
 */
class DigitalTwinRegistryClientConfiguration {

    public static final String CLIENT_REGISTRATION_ID = "keycloak";

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    /* package */ DigitalTwinRegistryClientConfiguration(final OAuth2AuthorizedClientService oAuth2AuthorizedClientService, final ClientRegistrationRepository clientRegistrationRepository) {
        this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    /**
     * Register authorization request interceptor - every request contains authorization header.
     *
     * @return authorizationRequestInterceptor
     */
    @Bean
    public RequestInterceptor authorizationRequestInterceptor(final OAuth2AuthorizedClientManager authorizedClientManager) {
        final ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(CLIENT_REGISTRATION_ID);

        final OAuthClientCredentialsFeignManager clientCredentialsFeignManager = new OAuthClientCredentialsFeignManager(
                authorizedClientManager, clientRegistration);

        return requestTemplate -> requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer " + clientCredentialsFeignManager.getAccessToken());
    }

    @Bean
    /* package */ OAuth2AuthorizedClientManager authorizedClientManager() {
        final OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                                                                                                       .clientCredentials()
                                                                                                       .build();

        final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    /**
     * asf
     */
    @Slf4j
    /* package */ static class OAuthClientCredentialsFeignManager {
        private final OAuth2AuthorizedClientManager manager;
        private final ClientRegistration clientRegistration;

        /* package */ OAuthClientCredentialsFeignManager(final OAuth2AuthorizedClientManager manager, final ClientRegistration clientRegistration) {
            this.manager = manager;
            this.clientRegistration = clientRegistration;
        }

        public String getAccessToken() {
            final OAuth2AuthorizeRequest oAuth2AuthorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(clientRegistration.getRegistrationId())
                    .build();
            final OAuth2AuthorizedClient client = manager.authorize(oAuth2AuthorizeRequest);
            if (isNull(client)) {
                throw new IllegalStateException("client credentials flow on " + clientRegistration.getRegistrationId() + " failed, client is null");
            }
            return client.getAccessToken().getTokenValue();
        }
    }
}
