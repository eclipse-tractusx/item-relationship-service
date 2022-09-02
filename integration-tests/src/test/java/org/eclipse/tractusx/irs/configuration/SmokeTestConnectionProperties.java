package org.eclipse.tractusx.irs.configuration;

import lombok.Data;

@Data
public class SmokeTestConnectionProperties {
    private String baseUri;
    private String accessToken;
}
