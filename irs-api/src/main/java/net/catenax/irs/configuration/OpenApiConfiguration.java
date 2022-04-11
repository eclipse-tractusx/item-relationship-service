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

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.IrsApplication;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobException;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.QueryParameter;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.dtos.ErrorResponse;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * Configuration for the springdoc OpenAPI generator.
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfiguration {

    private static final String EXAMPLE_INSTANT = "2022-02-03T14:48:54.709Z";
    public static final int DEFAULT_DEPTH = 4;

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
            components.addExamples("job-handle", toExample(
                    JobHandle.builder().jobId(UUID.fromString("6c311d29-5753-46d4-b32c-19b918ea93b0")).build()));
            components.addExamples("error-response", toExample(ErrorResponse.builder()
                                                                            .withErrors(List.of("TimeoutException",
                                                                                    "ParsingException"))
                                                                            .withMessage("Some errors occured")
                                                                            .withStatusCode(
                                                                                    HttpStatus.INTERNAL_SERVER_ERROR)
                                                                            .build()));
            components.addExamples("complete-job-result", toExample(Jobs.builder().build())); // TODO
            components.addExamples("job-result-without-uncompleted-result-tree",
                    toExample(Jobs.builder().build())); // TODO
            components.addExamples("partial-job-result", toExample(Jobs.builder().build())); // TODO
            components.addExamples("canceled-job-result", createCanceledJobResult());
            components.addExamples("failed-job-result", toExample(Jobs.builder().build())); // TODO
            components.addExamples("complete-job-list-processing-state", toExample(Jobs.builder().build())); // TODO

        };
    }

    private Example createCanceledJobResult() {
        return toExample(Jobs.builder()
                             .job(Job.builder()
                                     .jobId(UUID.fromString("e5347c88-a921-11ec-b909-0242ac120002"))
                                     .globalAssetId(GlobalAssetIdentification.builder()
                                                                             .globalAssetId(
                                                                                     "6c311d29-5753-46d4-b32c-19b918ea93b0")
                                                                             .build())
                                     .jobState(JobState.CANCELED)
                                     .createdOn(Instant.parse(EXAMPLE_INSTANT))
                                     .lastModifiedOn(Instant.parse(EXAMPLE_INSTANT))
                                     .jobFinished(Instant.parse(EXAMPLE_INSTANT))
                                     .requestUrl(toUrl("https://api.server.test/api/../"))
                                     .queryParameter(QueryParameter.builder()
                                                                   .bomLifecycle(BomLifecycle.AS_BUILT)
                                                                   .aspects(List.of(AspectType.SERIAL_PART_TYPIZATION,
                                                                           AspectType.CONTACT))
                                                                   .depth(DEFAULT_DEPTH)
                                                                   .direction(Direction.DOWNWARD)
                                                                   .build())
                                     .jobException(new JobException("IrsTimeoutException",
                                             "Timeout while requesting Digital Registry",
                                             Instant.parse(EXAMPLE_INSTANT)))
                                     .build())
                             .build());
    }

    private URL toUrl(final String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Cannot create URL " + urlString, e);
        }
    }

    private Example toExample(final Object value) {
        return new Example().value(value);
    }
}
