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
import java.util.Optional;
import java.util.UUID;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.IrsApplication;
import net.catenax.irs.component.AsyncFetchedItems;
import net.catenax.irs.component.ChildItem;
import net.catenax.irs.component.Description;
import net.catenax.irs.component.EndPoint;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobException;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.JobHandleCollection;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.MeasurementUnit;
import net.catenax.irs.component.ProtocolInformation;
import net.catenax.irs.component.Quantity;
import net.catenax.irs.component.QueryParameter;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.SemanticId;
import net.catenax.irs.component.Shell;
import net.catenax.irs.component.Shells;
import net.catenax.irs.component.SubmodelDescriptor;
import net.catenax.irs.component.Summary;
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

    private static final Instant EXAMPLE_INSTANT = Instant.parse("2022-02-03T14:48:54.709Z");
    private static final String JOB_ID = "e5347c88-a921-11ec-b909-0242ac120002";
    private static final String GLOBAL_ASSET_ID = "6c311d29-5753-46d4-b32c-19b918ea93b0";
    private static final String JOB_HANDLE_ID_1 = GLOBAL_ASSET_ID;
    private static final int DEFAULT_DEPTH = 4;

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
            components.addExamples("job-handle", toExample(createJobHandle(GLOBAL_ASSET_ID)));
            components.addExamples("error-response", toExample(ErrorResponse.builder()
                                                                            .withErrors(List.of("TimeoutException",
                                                                                    "ParsingException"))
                                                                            .withMessage("Some errors occured")
                                                                            .withStatusCode(
                                                                                    HttpStatus.INTERNAL_SERVER_ERROR)
                                                                            .build()));
            components.addExamples("complete-job-result", createCompleteJobResult());
            components.addExamples("job-result-without-uncompleted-result-tree", createJobResultWithoutTree());
            components.addExamples("partial-job-result", createPartialJobResult());
            components.addExamples("canceled-job-result", createCanceledJobResult());
            components.addExamples("failed-job-result", createFailedJobResult());
            components.addExamples("complete-job-list-processing-state", createJobListProcessingState());

        };
    }

    private Example createJobListProcessingState() {
        return toExample(JobHandleCollection.builder()
                                            .jobHandleCollections(List.of(createJobHandle(JOB_HANDLE_ID_1),
                                                    createJobHandle("46cd8fb1-34c1-4426-9c16-84b913bcfd95")))
                                            .build());
    }

    private JobHandle createJobHandle(final String name) {
        return JobHandle.builder().jobId(UUID.fromString(name)).build();
    }

    private Example createFailedJobResult() {
        return toExample(Jobs.builder()
                             .job(Job.builder()
                                     .jobId(UUID.fromString(JOB_ID))
                                     .globalAssetId(
                                             GlobalAssetIdentification.builder().globalAssetId(GLOBAL_ASSET_ID).build())
                                     .jobState(JobState.ERROR)
                                     .createdOn(EXAMPLE_INSTANT)
                                     .lastModifiedOn(EXAMPLE_INSTANT)
                                     .requestUrl(getExampleRequestURL())
                                     .summary(createSummary())
                                     .queryParameter(createQueryParameter())
                                     .jobException(createJobException())
                                     .build())
                             .build());
    }

    private Example createPartialJobResult() {
        return toExample(Jobs.builder()
                             .job(Job.builder()
                                     .jobId(UUID.fromString(JOB_ID))
                                     .globalAssetId(
                                             GlobalAssetIdentification.builder().globalAssetId(GLOBAL_ASSET_ID).build())
                                     .jobState(JobState.IN_PROGRESS)
                                     .createdOn(EXAMPLE_INSTANT)
                                     .lastModifiedOn(EXAMPLE_INSTANT)
                                     .jobFinished(EXAMPLE_INSTANT)
                                     .requestUrl(getExampleRequestURL())
                                     .summary(createSummary())
                                     .queryParameter(createQueryParameter())
                                     .jobException(createJobException())
                                     .build())
                             .build());
    }

    private QueryParameter createQueryParameter() {
        return QueryParameter.builder()
                             .bomLifecycle(BomLifecycle.AS_BUILT)
                             .depth(DEFAULT_DEPTH)
                             .aspects(List.of(AspectType.SERIAL_PART_TYPIZATION))
                             .direction(Direction.DOWNWARD)
                             .build();
    }

    private Summary createSummary() {
        return Summary.builder()
                      .asyncFetchedItems(
                              AsyncFetchedItems.builder().complete(0).failed(0).running(0).lost(0).queue(0).build())
                      .build();
    }

    private JobException createJobException() {
        return new JobException("IrsTimeoutException", "Timeout while requesting Digital Registry", EXAMPLE_INSTANT);
    }

    private Example createJobResultWithoutTree() {
        return createCompleteJobResult();
    }

    private Example createCompleteJobResult() {
        return toExample(Jobs.builder()
                             .job(Job.builder()
                                     .jobId(UUID.fromString(JOB_ID))
                                     .globalAssetId(
                                             GlobalAssetIdentification.builder().globalAssetId(GLOBAL_ASSET_ID).build())
                                     .jobState(JobState.COMPLETED)
                                     .createdOn(EXAMPLE_INSTANT)
                                     .lastModifiedOn(EXAMPLE_INSTANT)
                                     .jobFinished(EXAMPLE_INSTANT)
                                     .requestUrl(getExampleRequestURL())
                                     .owner("")
                                     .summary(createSummary())
                                     .queryParameter(createQueryParameter())
                                     .build())
                             .relationships(List.of(Relationship.builder()
                                                                .catenaXId(GlobalAssetIdentification.builder()
                                                                                                    .globalAssetId(
                                                                                                            "d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447")
                                                                                                    .build())
                                                                .childItem(ChildItem.builder()
                                                                                    .quantity(Quantity.builder()
                                                                                                      .quantityNumber(1)
                                                                                                      .measurementUnit(
                                                                                                              MeasurementUnit.builder()
                                                                                                                             .dataTypeUri(
                                                                                                                                     "urn:bamm:io.openmanufacturing:meta-model:1.0.0#piece")
                                                                                                                             .lexicalValue(
                                                                                                                                     "piece")
                                                                                                                             .build())

                                                                                                      .build())
                                                                                    .childCatenaXId(
                                                                                            GlobalAssetIdentification.builder()
                                                                                                                     .globalAssetId(
                                                                                                                             "a45a2246-f6e1-42da-b47d-5c3b58ed62e9")
                                                                                                                     .build())
                                                                                    .lastModifiedOn(EXAMPLE_INSTANT)
                                                                                    .assembledOn(EXAMPLE_INSTANT)
                                                                                    .bomLifecycle(BomLifecycle.AS_BUILT)
                                                                                    .build())
                                                                .build()))
                             .shells(Optional.of(List.of(Shells.builder()
                                                               .shell(Shell.builder()
                                                                           .description(Description.builder()
                                                                                                   .language("en")
                                                                                                   .text("The shell for a vehicle")
                                                                                                   .build())
                                                                           .globalAssetId(
                                                                                   GlobalAssetIdentification.builder()
                                                                                                            .globalAssetId(
                                                                                                                    "a45a2246-f6e1-42da-b47d-5c3b58ed62e9")
                                                                                                            .build())
                                                                           .idShort("future concept x")
                                                                           .identification(
                                                                                   "882fc530-b69b-4707-95f6-5dbc5e9baaa8")
                                                                           .specificAssetId("engineserialid",
                                                                                   "12309481209312")
                                                                           .submodelDescriptor(
                                                                                   createBaseSubmodelDescriptor())
                                                                           .submodelDescriptor(
                                                                                   createPartSubmodelDescriptor())

                                                                           .build())

                                                               .build())))

                             .build());
    }

    private SubmodelDescriptor createBaseSubmodelDescriptor() {
        return SubmodelDescriptor.builder()
                                 .description(Description.builder()
                                                         .language("en")
                                                         .text("Provides base vehicle information")
                                                         .build())
                                 .idShort("vehicle base details")
                                 .identification("4a738a24-b7d8-4989-9cd6-387772f40565")
                                 .semanticId(SemanticId.builder().value("urn:bamm:com.catenax.vehicle:0.1.1").build())
                                 .endpoint(EndPoint.builder()
                                                   .interfaceType("HTTP")
                                                   .protocolInformation(ProtocolInformation.builder()
                                                                                           .endpointAddress(
                                                                                                   "https://catena-x.net/vehicle/basedetails/")
                                                                                           .endpointProtocol("HTTPS")
                                                                                           .enpointProtocolVersion(
                                                                                                   "1.0")
                                                                                           .build())
                                                   .build())
                                 .build();
    }

    private SubmodelDescriptor createPartSubmodelDescriptor() {
        return SubmodelDescriptor.builder()
                                 .description(Description.builder()
                                                         .language("en")
                                                         .text("Provides base vehicle information")
                                                         .build())
                                 .idShort("vehicle part details")
                                 .identification("dae4d249-6d66-4818-b576-bf52f3b9ae90")
                                 .semanticId(SemanticId.builder()
                                                       .value("urn:bamm:com.catenax.vehicle:0.1.1#PartDetails")
                                                       .build())
                                 .endpoint(EndPoint.builder()
                                                   .interfaceType("HTTP")
                                                   .protocolInformation(ProtocolInformation.builder()
                                                                                           .endpointAddress(
                                                                                                   "https://catena-x.net/vehicle/partdetails/")
                                                                                           .endpointProtocol("HTTPS")
                                                                                           .enpointProtocolVersion(
                                                                                                   "1.0")
                                                                                           .build())
                                                   .build())
                                 .build();
    }

    private Example createCanceledJobResult() {
        return toExample(Jobs.builder()
                             .job(Job.builder()
                                     .jobId(UUID.fromString(JOB_ID))
                                     .globalAssetId(
                                             GlobalAssetIdentification.builder().globalAssetId(GLOBAL_ASSET_ID).build())
                                     .jobState(JobState.CANCELED)
                                     .createdOn(EXAMPLE_INSTANT)
                                     .lastModifiedOn(EXAMPLE_INSTANT)
                                     .jobFinished(EXAMPLE_INSTANT)
                                     .requestUrl(getExampleRequestURL())
                                     .queryParameter(createQueryParameter())
                                     .jobException(createJobException())
                                     .build())
                             .build());
    }

    private URL getExampleRequestURL() {
        return toUrl("https://api.server.test/api/../");
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
