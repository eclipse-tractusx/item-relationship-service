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
package org.eclipse.tractusx.irs.configuration.converter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * JWT Converter
 */
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private final IrsTokenParser irsTokenParser = new IrsTokenParser();

    @Override
    public AbstractAuthenticationToken convert(final @NotNull Jwt source) {
        final Collection<GrantedAuthority> authorities =
            Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(source).stream(),
                irsTokenParser.extractIrsRolesFromToken(source).stream()
            ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(source, authorities);
    }

    /**
     * Parsing JWT - retrieving resource_access claim with IRS roles.
     */
    /* package */ static class IrsTokenParser {

        private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
        private static final String IRS_RESOURCE_ACCESS = "Cl20-CX-IRS";
        private static final String ROLES = "roles";

        /**
         * Parsing JWT - retrieving resource_access claim with IRS roles.
         * @param jwt source
         * @return list of roles from token
         */
        public Collection<GrantedAuthority> extractIrsRolesFromToken(final Jwt jwt) {
            final Map<String, Object> resourceAccessClaim = Optional.ofNullable(jwt.getClaimAsMap(RESOURCE_ACCESS_CLAIM))
                                                                    .orElse(Map.of());

            if (resourceAccessClaim.containsKey(IRS_RESOURCE_ACCESS)) {
                final Map<String, List<String>> irsResourceAccess = (Map<String, List<String>>) resourceAccessClaim.get(IRS_RESOURCE_ACCESS);

                return irsResourceAccess.get(ROLES)
                                        .stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList());
            }
            
            return Collections.emptyList();
        }

    }


}

