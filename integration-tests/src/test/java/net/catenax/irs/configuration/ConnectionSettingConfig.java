package net.catenax.irs.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix="connection")
public class ConnectionSettingConfig {

    private String baseuri;

    public String getBaseuri() {
        System.out.println("ConnectionSettingConfig baseuri: " + baseuri);
        return baseuri;
    }

    public void setBaseuri(final String baseuri) {
        this.baseuri = baseuri;
    }
}
