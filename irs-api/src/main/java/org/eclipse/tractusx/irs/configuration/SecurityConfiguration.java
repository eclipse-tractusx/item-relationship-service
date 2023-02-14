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

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.time.Duration;
import java.util.List;

import org.eclipse.tractusx.irs.common.ApiConstants;
import org.eclipse.tractusx.irs.configuration.converter.JwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security config bean
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    private static final String[] WHITELIST  = {
        "/actuator/health",
        "/actuator/health/readiness",
        "/actuator/health/liveness",
        "/actuator/prometheus",
        "/api/swagger-ui/**",
        "/api/api-docs",
        "/api/api-docs.yaml",
        "/api/api-docs/swagger-config",
        "/" + ApiConstants.API_PREFIX_INTERNAL + "/endpoint-data-reference",
        "/ess/mock/notification/receive"
    };
    private static final long HSTS_MAX_AGE_DAYS = 365;

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Bean
    /* package */ SecurityFilterChain securityFilterChain(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity.httpBasic().disable();
        httpSecurity.formLogin().disable();
        httpSecurity.csrf().disable();
        httpSecurity.logout().disable();
        httpSecurity.cors();

        httpSecurity.headers()
                    .httpStrictTransportSecurity()
                    .maxAgeInSeconds(Duration.ofDays(HSTS_MAX_AGE_DAYS).toSeconds())
                    .includeSubDomains(true)
                    .preload(true)
                    .requestMatcher(AnyRequestMatcher.INSTANCE);

        httpSecurity.headers().xssProtection().xssProtectionEnabled(true).block(true);

        httpSecurity.headers().frameOptions().sameOrigin();

        httpSecurity
            .sessionManagement()
            .sessionCreationPolicy(STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(WHITELIST)
            .permitAll()
            .antMatchers("/**")
            .authenticated()
            .and()
            .oauth2ResourceServer(oauth2ResourceServer ->
                oauth2ResourceServer.jwt().jwtAuthenticationConverter(new JwtAuthenticationConverter())
            )
            .oauth2Client();

        return httpSecurity.build();
    }

    @Bean
    /* package */ CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("Access-Control-Allow-Headers", "Access-Control-Allow-Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Origin", "Cache-Control", "Content-Type", "Authorization"));
        configuration.setAllowedMethods(List.of("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH", "OPTIONS"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
