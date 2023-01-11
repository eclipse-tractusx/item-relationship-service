package org.eclipse.tractusx.irs.configuration.converter;

import static org.springframework.security.oauth2.jwt.JwtClaimNames.SUB;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;

import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

    @Test
    void shouldParseJwtTokenAndFindViewIrsRole() {
        // given
        final JsonArray irsRoles = new JsonArray();
        irsRoles.add("view_irs");
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("roles", irsRoles);
        final Map<String, Object> irsResourceAccess = Map.of("Cl20-CX-IRS", jsonObject);
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
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("roles", new JsonArray());

        final Map<String, Object> irsResourceAccess = Map.of("Cl20-CX-IRS-WRONG-KEY", jsonObject);
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
        final JsonArray irsRoles = new JsonArray();
        irsRoles.add("view_irs");
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("rolesWrong", irsRoles);
        final Map<String, Object> irsResourceAccess = Map.of("Cl20-CX-IRS", jsonObject);
        final Jwt jwt = jwt(irsResourceAccess);

        // when
        final AbstractAuthenticationToken authenticationToken = jwtAuthenticationConverter.convert(jwt);

        // then
        assertThat(authenticationToken).isNotNull();
        assertThat(authenticationToken.getAuthorities()).isNotNull();
        assertThat(authenticationToken.getAuthorities()).isEmpty();
    }

    Jwt jwt(final Map<String, Object> irsResourceAccess) {
        final JsonObject jsonObject = new JsonObject();
        irsResourceAccess.forEach((k,v) -> jsonObject.add(k, (JsonElement) v));
        final Map<String, Object> claims = Map.of("resource_access", jsonObject, SUB, "sub",
                "clientId", "clientId");

        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(30), Map.of("alg", "none"), claims);
    }
}
