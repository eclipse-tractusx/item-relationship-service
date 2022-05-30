package net.catenax.irs.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class PropertyConfigurer {

    @Bean
    @ConfigurationProperties(prefix = "connection")
    public ClientProperties baseUri() {
        return new ClientProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.keycloak")
    public ClientProperties authorizationGrantType() {
        return new ClientProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.keycloak")
    public ClientProperties clientId() {
        return new ClientProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.keycloak")
    public ClientProperties clientSecret() {
        return new ClientProperties();
    }
}
