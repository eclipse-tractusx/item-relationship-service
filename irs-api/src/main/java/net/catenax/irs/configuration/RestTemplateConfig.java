//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.configuration;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.time.Duration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
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
import org.springframework.web.client.RestTemplate;

/**
 * Rest template config with OAuth2 interceptor
 */
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    public static final String OAUTH_REST_TEMPLATE = "oAuthRestTemplate";
    public static final String BASIC_AUTH_REST_TEMPLATE = "basicAuthRestTemplate";

    private static final String CLIENT_REGISTRATION_ID = "keycloak";
    private static final int TIMEOUT_SECONDS = 90;

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean(OAUTH_REST_TEMPLATE)
    /* package */ RestTemplate oAuthRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        final var clientRegistration = clientRegistrationRepository.findByRegistrationId(CLIENT_REGISTRATION_ID);

        return restTemplateBuilder
                .additionalInterceptors(new OAuthClientCredentialsRestTemplateInterceptor(authorizedClientManager(), clientRegistration))
                .setReadTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .setConnectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    @Bean(BASIC_AUTH_REST_TEMPLATE)
        /* package */ RestTemplate basicAuthRestTemplate(final RestTemplateBuilder restTemplateBuilder,
            @Value("${aasProxy.submodel.username}") final String aasProxySubmodelUsername, @Value("${aasProxy.submodel.password}") final String aasProxySubmodelPassword) {
        return restTemplateBuilder
                .additionalInterceptors(new BasicAuthenticationInterceptor(aasProxySubmodelUsername, aasProxySubmodelPassword))
                .setReadTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .setConnectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    @Bean
    /* package */ OAuth2AuthorizedClientManager authorizedClientManager() {
        final var authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                                                                                  .clientCredentials()
                                                                                  .build();

        final var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    /**
     * Interceptor to add Authorization header to every call done via Rest template
     */
    @RequiredArgsConstructor
    @Slf4j
    @ExcludeFromCodeCoverageGeneratedReport
    /* package */ static class OAuthClientCredentialsRestTemplateInterceptor implements ClientHttpRequestInterceptor {

        private final OAuth2AuthorizedClientManager manager;
        private final ClientRegistration clientRegistration;

        @Override
        public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
            final OAuth2AuthorizeRequest oAuth2AuthorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(clientRegistration.getRegistrationId())
                    .principal(clientRegistration.getClientName())
                    .build();

            final OAuth2AuthorizedClient client = manager.authorize(oAuth2AuthorizeRequest);

            if (isNull(client)) {
                throw new IllegalStateException("Client credentials flow on " + clientRegistration.getRegistrationId() + " failed, client is null");
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
