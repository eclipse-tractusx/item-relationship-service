package org.eclipse.tractusx.irs.configuration.converter;

import static org.springframework.security.oauth2.jwt.JwtClaimNames.SUB;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

    @Test
    void shouldParseJwtTokenAndFindViewIrsRole() {
        // given
        final JSONArray irsRoles = new JSONArray();
        irsRoles.addAll(List.of("view_irs"));
        final Map<String, Object> irsResourceAccess = Map.of("Cl20-CX-IRS", new JSONObject(Map.of("roles", irsRoles)));
        final Jwt jwt = jwt(irsResourceAccess);

        // when
        final AbstractAuthenticationToken authenticationToken = jwtAuthenticationConverter.convert(jwt);

        // then
        assertThat(authenticationToken).isNotNull();
        assertThat(authenticationToken.getAuthorities()).isNotNull();
        assertThat(authenticationToken.getAuthorities()).contains(new SimpleGrantedAuthority("view_irs"));
    }

    @Test
    void shouldParseJwtTokenAndNotFindIrsRolesWhenWrongKey() {
        // given
        final Map<String, Object> irsResourceAccess = Map.of("Cl20-CX-IRS-WRONG-KEY", new JSONObject(Map.of("roles", new JSONArray())));
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
        final JSONArray irsRoles = new JSONArray();
        irsRoles.addAll(List.of("view_irs"));
        final Map<String, Object> irsResourceAccess = Map.of("Cl20-CX-IRS", new JSONObject(Map.of("rolesWrong", irsRoles)));
        final Jwt jwt = jwt(irsResourceAccess);

        // when
        final AbstractAuthenticationToken authenticationToken = jwtAuthenticationConverter.convert(jwt);

        // then
        assertThat(authenticationToken).isNotNull();
        assertThat(authenticationToken.getAuthorities()).isNotNull();
        assertThat(authenticationToken.getAuthorities()).isEmpty();
    }

    Jwt jwt(final Map<String, Object> irsResourceAccess) {
        final Map<String, Object> claims = Map.of("resource_access",  new JSONObject(irsResourceAccess), SUB, "sub", "clientId", "clientId");

        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(30), Map.of("alg", "none"),
                claims);
    }
}
