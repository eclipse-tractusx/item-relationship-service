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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Parsing JWT - retrieving resource_access claim with IRS roles.
 */
@Slf4j
@AllArgsConstructor
public class IrsTokenParser {

    private String resourceAccessClaim;
    private String irsResourceAccess;
    private String roles;

    /**
     * Parsing JWT - retrieving resource_access claim with IRS roles.
     *
     * @param jwt source
     * @return set of roles from token
     */
    public Set<SimpleGrantedAuthority> extractIrsRolesFromToken(final Jwt jwt) {
        return Optional.ofNullable(jwt.getClaim(resourceAccessClaim))
                       .map(LinkedTreeMap.class::cast)
                       .map(accesses -> accesses.get(irsResourceAccess))
                       .map(LinkedTreeMap.class::cast)
                       .map(irsAccesses -> irsAccesses.get(roles))
                       .map(irsRoles -> ((List<String>) irsRoles).stream()
                                                                 .map(SimpleGrantedAuthority::new)
                                                                 .collect(Collectors.toSet()))
                       .orElse(Collections.emptySet());
    }
}
