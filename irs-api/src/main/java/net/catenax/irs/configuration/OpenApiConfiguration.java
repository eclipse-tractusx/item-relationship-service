//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.IrsApplication;
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
                            .info(new Info().title("IRS API")
                                            .version(IrsApplication.API_VERSION)
                                            .description(
                                                    "API to retrieve parts tree information. See <a href=\"https://confluence.catena-x.net/display/CXM/PRS+Environments+and+Test+Data\">this page</a> for more information on test data available in this environment."));
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
            new OpenApiExamples().createExamples(components);
        };
    }

}
