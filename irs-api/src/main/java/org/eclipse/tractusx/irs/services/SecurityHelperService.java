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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Helper methods to retrieve data from Jwt token
 */
public final class SecurityHelperService {

    private static final String UNKNOWN = "Unknown";
    private static final String CLIENT_ID = "clientId";
    private static final String BPN = "bpn";

    public String getClientIdClaim() {
        return getClaimOrUnknown(CLIENT_ID);
    }

    public String getBpnClaim() {
        return getClaimOrUnknown(BPN);
    }

    public boolean isAdmin() {
        return getIrsRoles().contains(IrsRoles.ADMIN_IRS);
    }

    public String getClientIdForViewIrs() {
        if (getIrsRoles().contains(IrsRoles.VIEW_IRS)) {
            return getClientIdClaim();
        }
        return "";
    }

    private List<String> getIrsRoles() {
        final Authentication authentication = getAuthenticationFromSecurityContext();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getAuthorities()
                                         .stream()
                                         .map(GrantedAuthority::getAuthority)
                                         .toList();
        }
        return Collections.emptyList();
    }

    private String getClaimOrUnknown(final String claimName) {
        final Authentication authentication = getAuthenticationFromSecurityContext();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            final Jwt token = jwtAuthenticationToken.getToken();

            return Optional.ofNullable(token.getClaim(claimName)).map(Object::toString).orElse(UNKNOWN);
        }

        return UNKNOWN;
    }

    private Authentication getAuthenticationFromSecurityContext() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
