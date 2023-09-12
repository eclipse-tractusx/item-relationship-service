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
package org.eclipse.tractusx.irs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.SUB;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuthorizationServiceTest {

    @Test
    void shouldReturnTrueWhenTokenBpnIsEqualToAllowedBpn() {
        // given
        final String BPN = "BPNL00000003CRHK";
        final Map<String, Object> claims = Map.of(SUB, "sub", "clientId", "clientId", "bpn", BPN);
        thereIsJwtAuthenticationWithClaims(claims);
        final AuthorizationService authorizationService = new AuthorizationService(BPN);

        // when
        final Boolean isBpnAllowed = authorizationService.verifyBpn();

        // then
        assertThat(isBpnAllowed).isEqualTo(Boolean.TRUE);
    }

    @Test
    void shouldReturnFalseWhenTokenBpnIsDifferentThanAllowedBpn() {
        // given
        final String claimBPN = "BPNL00000003CRHK";
        final String configurationBPN = "BPNL00000003CML1";
        final Map<String, Object> claims = Map.of(SUB, "sub", "clientId", "clientId", "bpn", claimBPN);
        thereIsJwtAuthenticationWithClaims(claims);
        final AuthorizationService authorizationService = new AuthorizationService(configurationBPN);

        // when
        final Boolean isBpnAllowed = authorizationService.verifyBpn();

        // then
        assertThat(isBpnAllowed).isEqualTo(Boolean.FALSE);
    }

    @Test
    void shouldReturnFalseWhenNotAllowedBpnConfigured() {
        // given
        final String emptyConfigurationBPN = "";
        final AuthorizationService authorizationService = new AuthorizationService(emptyConfigurationBPN);

        // when
        final Boolean isBpnAllowed = authorizationService.verifyBpn();

        // then
        assertThat(isBpnAllowed).isEqualTo(Boolean.FALSE);
    }

    @Test
    void shouldReturnFalseTokenBpnIsMissing() {
        // given
        final String configurationBPN = "BPNL00000003CML1";
        final Map<String, Object> claims = Map.of(SUB, "sub", "clientId", "clientId");
        thereIsJwtAuthenticationWithClaims(claims);
        final AuthorizationService authorizationService = new AuthorizationService(configurationBPN);

        // when
        final Boolean isBpnAllowed = authorizationService.verifyBpn();

        // then
        assertThat(isBpnAllowed).isEqualTo(Boolean.FALSE);
    }

    private void thereIsJwtAuthenticationWithClaims(final Map<String, Object> claims) {
        final JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt(claims));
        SecurityContext securityContext = mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        SecurityContextHolder.setContext(securityContext);
    }

    Jwt jwt(final Map<String, Object> claims) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(30), Map.of("alg", "none"), claims);
    }

}
