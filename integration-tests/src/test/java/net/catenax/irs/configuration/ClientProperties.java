package net.catenax.irs.configuration;

import lombok.Data;

@Data
public class ClientProperties {
    private String baseuri;
    private String authorizationGrantType;
    private String clientId;
    private String clientSecret;
}
