//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.services;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Helper methods to retrieve data from Jwt token
 */
public final class SecurityHelperService {

    private static final String UNKNOWN = "Unknown";

    private SecurityHelperService() { }

    public static String getClientIdClaim() {
        return getClaimOrUnknown("clientId");
    }

    private static String getClaimOrUnknown(final String claimName) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken) {
            final Jwt token = ((JwtAuthenticationToken) authentication).getToken();

            return Optional
                    .ofNullable(token.getClaim(claimName))
                    .map(Object::toString)
                    .orElse(UNKNOWN);
        }

        return UNKNOWN;
    }

}
