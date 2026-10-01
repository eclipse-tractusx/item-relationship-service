/********************************************************************************
 * Copyright (c) 2022 ZF Friedrichshafen AG
 * Copyright (c) 2022 ISTOS GmbH
 * Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2022 BOSCH AG
 * Copyright (c) 2026 Volkswagen AG
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.configuration;

import java.util.Arrays;
import java.util.List;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.irs.IrsApplication;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the springdoc OpenAPI generator.
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfiguration {

    private static final String RECURSIVE_BOM_LIFECYCLE_DESCRIPTION =
            "BOM lifecycle used for recursive PURIS traversal. PURIS supports only asPlanned.";
    private static final String RECURSIVE_JOB_REQUEST_SCHEMA = "RecursiveJobRequest";
    private static final String RECURSIVE_NOTIFICATION_CONTENT_SCHEMA = "Content";
    private static final String RECURSIVE_JOB_PARAMETER_SCHEMA = "RecursiveJobParameter";
    private static final String RECURSIVE_JOB_RESULT_SCHEMA = "RecursiveJobResult";
    private static final String RECURSIVE_ASPECT_ITEM_SCHEMA = "RecursiveAspectItem";
    private static final String RECURSIVE_TOMBSTONE_SCHEMA = "RecursiveTombstone";
    private static final String USE_CASE_PROPERTY = "useCase";
    private static final String BOM_LIFECYCLE_PROPERTY = "bomLifecycle";
    private static final String ASPECTS_PROPERTY = "aspects";

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
                            .addSecurityItem(new SecurityRequirement().addList("api_key"))
                            .info(new Info().title("IRS API")
                                            .version(IrsApplication.API_VERSION)
                                            .description(
                                                    "The API of the Item Relationship Service (IRS) for retrieving item graphs along the value chain of CATENA-X partners."));
    }

    /**
     * Generates example values in Swagger
     *
     * @return the customizer
     */
    @Bean
    public OpenApiCustomizer customizer() {
        return openApi -> {
            final Components components = openApi.getComponents();
            components.addSecuritySchemes("api_key", new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                    .description("Api Key access")
                    .in(SecurityScheme.In.HEADER)
                    .name("X-API-KEY")
            );
            openApi.getComponents().getSchemas().values().forEach(s -> s.setAdditionalProperties(false));
            final Schema<?> recursivePayload = schemaProperty(components, RECURSIVE_ASPECT_ITEM_SCHEMA, "items");
            if (recursivePayload != null) {
                recursivePayload.setAdditionalProperties(true);
            }
            final Schema<?> recursiveNotificationHeader = components.getSchemas().get("Header");
            if (recursiveNotificationHeader != null) {
                recursiveNotificationHeader.setAdditionalProperties(true);
            }
            applyRecursivePolicySchemas(components);

            new OpenApiExamples().createExamples(components);
        };
    }

    private static void applyRecursivePolicySchemas(final Components components) {
        final List<String> useCases = Arrays.stream(RecursiveUseCase.values()).map(Enum::name).toList();
        final List<String> lifecycles = Arrays.stream(BomLifecycle.values())
                .filter(lifecycle -> Arrays.stream(RecursiveUseCase.values())
                        .anyMatch(useCase -> useCase.getAllowedBomLifecycles().contains(lifecycle)))
                .map(BomLifecycle::toString)
                .toList();
        final List<String> aspects = Arrays.stream(RecursiveUseCase.values())
                .flatMap(useCase -> useCase.getAllowedAspects().stream())
                .distinct()
                .map(RecursiveAspect::getSemanticId)
                .toList();

        setPropertyEnum(components, RECURSIVE_JOB_REQUEST_SCHEMA, USE_CASE_PROPERTY, useCases);
        setPropertyEnum(components, RECURSIVE_JOB_REQUEST_SCHEMA, BOM_LIFECYCLE_PROPERTY, lifecycles);
        setPropertyDescription(components, RECURSIVE_JOB_REQUEST_SCHEMA, BOM_LIFECYCLE_PROPERTY,
                RECURSIVE_BOM_LIFECYCLE_DESCRIPTION);
        setArrayItemEnum(components, RECURSIVE_JOB_REQUEST_SCHEMA, ASPECTS_PROPERTY, aspects);
        setPropertyEnum(components, RECURSIVE_NOTIFICATION_CONTENT_SCHEMA, BOM_LIFECYCLE_PROPERTY, lifecycles);
        setPropertyDescription(components, RECURSIVE_NOTIFICATION_CONTENT_SCHEMA, BOM_LIFECYCLE_PROPERTY,
                RECURSIVE_BOM_LIFECYCLE_DESCRIPTION);
        setArrayItemEnum(components, RECURSIVE_NOTIFICATION_CONTENT_SCHEMA, ASPECTS_PROPERTY, aspects);
        setArrayItemEnum(components, RECURSIVE_JOB_PARAMETER_SCHEMA, ASPECTS_PROPERTY, aspects);
        setPropertyEnum(components, RECURSIVE_JOB_PARAMETER_SCHEMA, BOM_LIFECYCLE_PROPERTY, lifecycles);
        setPropertyDescription(components, RECURSIVE_JOB_PARAMETER_SCHEMA, BOM_LIFECYCLE_PROPERTY,
                RECURSIVE_BOM_LIFECYCLE_DESCRIPTION);
        setPropertyEnum(components, RECURSIVE_JOB_RESULT_SCHEMA, USE_CASE_PROPERTY, useCases);
        setPropertyEnum(components, RECURSIVE_JOB_RESULT_SCHEMA, BOM_LIFECYCLE_PROPERTY, lifecycles);
        setPropertyDescription(components, RECURSIVE_JOB_RESULT_SCHEMA, BOM_LIFECYCLE_PROPERTY,
                RECURSIVE_BOM_LIFECYCLE_DESCRIPTION);
        setArrayItemEnum(components, RECURSIVE_JOB_RESULT_SCHEMA, "requestedAspects", aspects);
        setPropertyEnum(components, RECURSIVE_ASPECT_ITEM_SCHEMA, "aspect", aspects);
        setArrayItemEnum(components, RECURSIVE_TOMBSTONE_SCHEMA, ASPECTS_PROPERTY, aspects);
    }

    private static void setPropertyEnum(final Components components, final String schemaName,
            final String propertyName, final List<String> values) {
        setEnum(schemaProperty(components, schemaName, propertyName), values);
    }

    private static void setPropertyDescription(final Components components, final String schemaName,
            final String propertyName, final String description) {
        final Schema<?> property = schemaProperty(components, schemaName, propertyName);
        if (property != null) {
            property.setDescription(description);
        }
    }

    private static void setArrayItemEnum(final Components components, final String schemaName,
            final String propertyName, final List<String> values) {
        final Schema<?> property = schemaProperty(components, schemaName, propertyName);
        if (property != null) {
            setEnum(property.getItems(), values);
        }
    }

    private static Schema<?> schemaProperty(final Components components, final String schemaName,
            final String propertyName) {
        final Schema<?> schema = components.getSchemas().get(schemaName);
        return schema == null || schema.getProperties() == null ? null : schema.getProperties().get(propertyName);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void setEnum(final Schema<?> schema, final List<String> values) {
        if (schema != null) {
            ((Schema) schema).setEnum(values);
        }
    }

}
