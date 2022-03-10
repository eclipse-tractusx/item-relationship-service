//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.brokerproxy.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import net.catenax.brokerproxy.BrokerProxyApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the springdoc OpenAPI generator.
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfiguration {
    /**
     * BrokerProxy configuration settings.
     */
    private final BrokerProxyConfiguration configuration;

    /**
     * Factory for generated Open API definition.
     *
     * @return Generated Open API configuration.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server()
                        .url(configuration.getApiUrl().toString())
                )
                .info(new Info()
                        .title("Catena-X Broker Proxy")
                        .version(BrokerProxyApplication.API_VERSION)
                        .description("API to send messages to message broker.")
                );
    }
}
