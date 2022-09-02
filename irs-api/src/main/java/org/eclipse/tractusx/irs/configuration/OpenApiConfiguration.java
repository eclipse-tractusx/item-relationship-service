//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.configuration;

import java.util.List;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.irs.IrsApplication;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the springdoc OpenAPI generator.
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfiguration {

    /**
     * IRS configuration settings.
     */
    private final IrsConfiguration irsConfiguration;

    /**
     * Factory for generated Open API definition.
     *
     * @return Generated Open API configuration.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().addServersItem(new Server().url(irsConfiguration.getApiUrl().toString()))
                            .addSecurityItem(new SecurityRequirement().addList("oAuth2", List.of("read", "write")))
                            .info(new Info().title("IRS API")
                                            .version(IrsApplication.API_VERSION)
                                            .description(
                                                    "The API of the Item Relationship Service (IRS) for retrieving item graphs along the value chain of CATENA-X partners."));
    }

    /**
     * Generates example values in Swagger
     *
     * @return the customiser
     */
    @Bean
    public OpenApiCustomiser customiser() {
        return openApi -> {
            final Components components = openApi.getComponents();
            components.addSecuritySchemes("OAuth2", new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
                                                                        .flows(new OAuthFlows().clientCredentials(
                                                                                new OAuthFlow().tokenUrl("https://centralidp.demo.catena-x.net/auth/realms/CX-Central/protocol/openid-connect/token"))));
            new OpenApiExamples().createExamples(components);
        };
    }

}
