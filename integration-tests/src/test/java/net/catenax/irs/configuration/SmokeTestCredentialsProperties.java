package net.catenax.irs.configuration;

import lombok.Data;

@Data
public class SmokeTestCredentialsProperties {
    private String clientId;
    private String clientSecret;
    private String authorizationGrantType;
}
