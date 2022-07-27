package net.catenax.irs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.SUB;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SecurityHelperServiceTest {

    private final String CLIENT_ID = "sa-cl6-cx-2";

    final SecurityHelperService securityHelperService = new SecurityHelperService();

    @Test
    void shouldReturnUnknownWhenNoAuthentication() {
        // when
        final String clientIdClaim = securityHelperService.getClientIdClaim();

        // then
        assertThat(clientIdClaim).isEqualTo("Unknown");
    }

    @Test
    void shouldReturnClientIdClaimWhenJwtAuthentication() {
        // given
        thereIsJwtAuthentication();

        // when
        final String clientIdClaim = securityHelperService.getClientIdClaim();

        // then
        assertThat(clientIdClaim).isEqualTo(CLIENT_ID);
    }

    private void thereIsJwtAuthentication() {
        final JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt());
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        SecurityContextHolder.setContext(securityContext);
    }

    Jwt jwt() {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(30), Map.of("alg", "none"),
                Map.of(SUB, CLIENT_ID, "clientId", CLIENT_ID));
    }

}
