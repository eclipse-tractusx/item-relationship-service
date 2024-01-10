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
package org.eclipse.tractusx.irs.configuration.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.SUB;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

class JwtAuthenticationConverterTest {

    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @BeforeEach
    void setUp() {
        final String resourceAccessClaim = "resource_access";
        final String irsResourceAccess = "Cl20-CX-IRS";
        final String roles = "roles";
        jwtAuthenticationConverter = new JwtAuthenticationConverter(new JwtGrantedAuthoritiesConverter(),
                new IrsTokenParser(resourceAccessClaim, irsResourceAccess, roles));
    }

    @Test
    void shouldParseJwtTokenAndFindViewIrsRole() {
        // given
        final Map<String, Object> irsResourceAccess = new LinkedTreeMap<>();
        final Map<String, Object> irsRoles = new LinkedTreeMap<>();
        irsRoles.put("roles", List.of("view_irs"));
        irsResourceAccess.put("Cl20-CX-IRS", irsRoles);
        final Jwt jwt = jwt(irsResourceAccess);

        // when
        final AbstractAuthenticationToken authenticationToken = jwtAuthenticationConverter.convert(jwt);

        // then
        assertThat(authenticationToken).isNotNull();
        assertThat(authenticationToken.getAuthorities()).isNotNull();
        assertThat(authenticationToken.getAuthorities()).contains(new SimpleGrantedAuthority(IrsRoles.VIEW_IRS));
    }

    @Test
    void shouldParseJwtTokenAndNotFindIrsRolesWhenWrongKey() {
        // given
        final Map<String, Object> irsResourceAccess = new LinkedTreeMap<>();
        final Map<String, Object> irsRoles = new LinkedTreeMap<>();
        irsRoles.put("roles", List.of());
        irsResourceAccess.put("Cl20-CX-IRS-WRONG-KEY", irsRoles);
        final Jwt jwt = jwt(irsResourceAccess);

        // when
        final AbstractAuthenticationToken authenticationToken = jwtAuthenticationConverter.convert(jwt);

        // then
        assertThat(authenticationToken).isNotNull();
        assertThat(authenticationToken.getAuthorities()).isNotNull();
        assertThat(authenticationToken.getAuthorities()).isEmpty();
    }

    @Test
    void shouldParseJwtTokenAndNotFindIrsRolesWhenWrongRolesKey() {
        // given
        final Map<String, Object> irsResourceAccess = new LinkedTreeMap<>();
        final Map<String, Object> irsRoles = new LinkedTreeMap<>();
        irsRoles.put("rolesWrong", List.of("view_irs"));
        irsResourceAccess.put("Cl20-CX-IRS-WRONG-KEY", irsRoles);
        final Jwt jwt = jwt(irsResourceAccess);

        // when
        final AbstractAuthenticationToken authenticationToken = jwtAuthenticationConverter.convert(jwt);

        // then
        assertThat(authenticationToken).isNotNull();
        assertThat(authenticationToken.getAuthorities()).isNotNull();
        assertThat(authenticationToken.getAuthorities()).isEmpty();
    }

    Jwt jwt(final Map<String, Object> irsResourceAccess) {
        final Map<String, Object> claims = new LinkedTreeMap<>();
        claims.putAll(Map.of("resource_access", irsResourceAccess, SUB, "sub", "clientId", "clientId"));

        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(30), Map.of("alg", "none"), claims);
    }
}