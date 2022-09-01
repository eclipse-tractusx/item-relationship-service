package org.eclipse.tractusx.irs.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties()
public class SmokeTestConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "connection")
    public SmokeTestConnectionProperties connection() {
        return new SmokeTestConnectionProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.keycloak")
    public SmokeTestCredentialsProperties credentials() {
        return new SmokeTestCredentialsProperties();
    }

}
