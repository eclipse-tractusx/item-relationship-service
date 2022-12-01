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

    @Test
    void shouldParseJwtTokenAndFindViewIrsRole() {
        // given
        final Jwt jwt = jwt();

        // when
        final AbstractAuthenticationToken convert = new JwtAuthenticationConverter().convert(jwt);

        // then
        assertThat(convert.getAuthorities()).isNotNull();
        assertThat(convert.getAuthorities()).contains(new SimpleGrantedAuthority("view_irs"));
    }

    Jwt jwt() {
        final JSONArray irsRoles = new JSONArray();
        irsRoles.addAll(List.of("view_irs"));
        final Map<String, Object> irsResourceAccess = Map.of("Cl20-CX-IRS", new JSONObject(Map.of("roles", irsRoles)));
        final Map<String, Object> claims = Map.of("resource_access",  new JSONObject(irsResourceAccess), SUB, "sub", "clientId", "clientId");

        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(30), Map.of("alg", "none"),
                claims);
    }
}
