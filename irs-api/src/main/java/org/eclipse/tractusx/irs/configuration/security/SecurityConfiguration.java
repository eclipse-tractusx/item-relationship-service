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
package org.eclipse.tractusx.irs.configuration.security;

import static java.util.Arrays.stream;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.irs.common.ApiConstants;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;
import org.springframework.security.web.header.writers.PermissionsPolicyHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Security config bean
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@SuppressWarnings({ "PMD.ExcessiveImports" })
public class SecurityConfiguration {

    private static final String[] WHITELIST = { "/actuator/health",
                                                "/actuator/health/readiness",
                                                "/actuator/health/liveness",
                                                "/actuator/prometheus",
                                                "/api/swagger-ui/**",
                                                "/api/api-docs",
                                                "/api/api-docs.yaml",
                                                "/api/api-docs/swagger-config",
                                                "/favicon.ico",
                                                "/" + ApiConstants.API_PREFIX_INTERNAL + "/endpoint-data-reference",
                                                "/ess/mock/notification/receive",
                                                "/ess/notification/receive",
                                                "/ess/notification/receive-recursive"
    };
    private static final long HSTS_MAX_AGE_DAYS = 365;
    private static final String ONLY_SELF_SCRIPT_SRC = "script-src 'self'";
    private static final String PERMISSION_POLICY = "microphone=(), geolocation=(), camera=()";

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Bean
    /* package */ SecurityFilterChain securityFilterChain(final HttpSecurity httpSecurity,
            final AuthenticationService authenticationService) throws Exception {
        httpSecurity.httpBasic(AbstractHttpConfigurer::disable);
        httpSecurity.formLogin(AbstractHttpConfigurer::disable);
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.logout(AbstractHttpConfigurer::disable);
        httpSecurity.cors(Customizer.withDefaults());

        httpSecurity.headers(headers -> headers.httpStrictTransportSecurity(
                httpStrictTransportSecurity -> httpStrictTransportSecurity.maxAgeInSeconds(
                        Duration.ofDays(HSTS_MAX_AGE_DAYS).toSeconds()).includeSubDomains(true).preload(true).requestMatcher(AnyRequestMatcher.INSTANCE)));

        httpSecurity.headers(headers -> headers.xssProtection(xXssConfig -> xXssConfig.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)));

        httpSecurity.headers(headers -> headers.addHeaderWriter(new ContentSecurityPolicyHeaderWriter(ONLY_SELF_SCRIPT_SRC)));

        httpSecurity.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        httpSecurity.headers(headers -> headers.addHeaderWriter(new ReferrerPolicyHeaderWriter(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)));
        httpSecurity.headers(headers -> headers.addHeaderWriter(new PermissionsPolicyHeaderWriter(PERMISSION_POLICY)));

        httpSecurity.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(STATELESS));

        httpSecurity.authorizeHttpRequests(auth -> auth.requestMatchers(WHITELIST).permitAll().requestMatchers("/**").authenticated());

        httpSecurity.addFilterBefore(new IgnoreWhitelistedPathFilter(new ApiKeyAuthenticationFilter(authenticationService, new JsonUtil())), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    /**
     * Dont execute delegate filter on whitelisted paths
     */
    @RequiredArgsConstructor
    private static final class IgnoreWhitelistedPathFilter extends OncePerRequestFilter {

        private final Filter delegate;
        private final AntPathMatcher pathMatcher = new AntPathMatcher();

        @Override
        protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws
                ServletException, IOException {
            if (isNotWhitelisted(request)) {
                delegate.doFilter(request, response, filterChain);
            } else {
                filterChain.doFilter(request, response);
            }
        }

        private boolean isNotWhitelisted(final HttpServletRequest request) {
            return stream(WHITELIST).noneMatch(path -> pathMatcher.match(path, request.getRequestURI()));
        }
    }


    @Bean
    /* package */ CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(
                List.of("Access-Control-Allow-Headers", "Access-Control-Allow-Origin", "Access-Control-Request-Method",
                        "Access-Control-Request-Headers", "Origin", "Cache-Control", "Content-Type", "Authorization"));
        configuration.setAllowedMethods(List.of("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH", "OPTIONS"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
