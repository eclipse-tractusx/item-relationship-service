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
package org.eclipse.tractusx.irs.common.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.SUB;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SecurityHelperServiceTest {

    private final String CLIENT_ID = "sa-cl6-cx-2";
    private final String BPN = "BPNL00000003CRHK";

    final SecurityHelperService securityHelperService = new SecurityHelperService();

    @Test
    void shouldReturnUnknownWhenNoAuthentication() {
        // given
        SecurityContextHolder.setContext(Mockito.mock(SecurityContext.class));

        // when
        final String clientIdClaim = securityHelperService.getClientIdClaim();

        // then
        assertThat(clientIdClaim).isEqualTo("Unknown");
    }

    @Test
    void shouldReturnClientIdClaimWhenJwtAuthentication() {
        // given
        thereIsJwtAuthentication(IrsRoles.VIEW_IRS);

        // when
        final String clientIdClaim = securityHelperService.getClientIdClaim();

        // then
        assertThat(clientIdClaim).isEqualTo(CLIENT_ID);
    }

    @Test
    void shouldReturnBpnClaimWhenJwtAuthentication() {
        // given
        thereIsJwtAuthentication(IrsRoles.VIEW_IRS);

        // when
        final String bpnClaim = securityHelperService.getBpnClaim();

        // then
        assertThat(bpnClaim).isEqualTo(BPN);
    }

    @Test
    void shouldReturnClientIdWhenJwtAuthenticationAndViewIrsRole() {
        // given
        thereIsJwtAuthentication(IrsRoles.VIEW_IRS);

        // when
        final String bpnClaim = securityHelperService.getClientIdForViewIrs();

        // then
        assertThat(bpnClaim).isEqualTo(CLIENT_ID);
    }

    @Test
    void shouldReturnTrueWhenAdminRolePresentInToken() {
        // given
        thereIsJwtAuthentication(IrsRoles.ADMIN_IRS);

        // when
        final Boolean isAdmin = securityHelperService.isAdmin();

        // then
        assertThat(isAdmin).isTrue();
    }

    private void thereIsJwtAuthentication(final String irsRole) {
        final JwtAuthenticationToken jwtAuthenticationToken = mock(JwtAuthenticationToken.class);
        final Jwt token = mock(Jwt.class);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority(irsRole)));
        when(jwtAuthenticationToken.getToken()).thenReturn(token);
        when(token.getClaim("clientId")).thenReturn(CLIENT_ID);
        when(token.getClaim("bpn")).thenReturn(BPN);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        SecurityContextHolder.setContext(securityContext);
    }

    Jwt jwt() {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(30), Map.of("alg", "none"),
                Map.of(SUB, CLIENT_ID, "clientId", CLIENT_ID, "bpn", BPN));
    }

}
