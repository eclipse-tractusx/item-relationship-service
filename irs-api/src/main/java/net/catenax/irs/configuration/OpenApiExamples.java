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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import net.catenax.irs.component.AsyncFetchedItems;
import net.catenax.irs.component.ChildItem;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobErrorDetails;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.JobParameter;
import net.catenax.irs.component.JobStatusResult;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.MeasurementUnit;
import net.catenax.irs.component.ProcessingError;
import net.catenax.irs.component.Quantity;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.Summary;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.Endpoint;
import net.catenax.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import net.catenax.irs.component.assetadministrationshell.LangString;
import net.catenax.irs.component.assetadministrationshell.ProtocolInformation;
import net.catenax.irs.component.assetadministrationshell.Reference;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.dtos.ErrorResponse;
import org.springframework.http.HttpStatus;

/**
 * Provides example objects for the OpenAPI documentation
 */
@SuppressWarnings({ "PMD.ExcessiveImports",
                    "PMD.TooManyMethods"
})
public class OpenApiExamples {
    private static final ZonedDateTime EXAMPLE_ZONED_DATETIME = ZonedDateTime.parse("2022-02-03T14:48:54.709Z");
    private static final String JOB_ID = "e5347c88-a921-11ec-b909-0242ac120002";
    private static final String GLOBAL_ASSET_ID = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0";
    private static final String JOB_HANDLE_ID_1 = "6c311d29-5753-46d4-b32c-19b918ea93b0";
    private static final int DEFAULT_DEPTH = 4;

    public void createExamples(final Components components) {
        components.addExamples("job-handle", toExample(createJobHandle(JOB_HANDLE_ID_1)));
        components.addExamples("error-response", toExample(ErrorResponse.builder()
                                                                        .withErrors(List.of("TimeoutException",
                                                                                "ParsingException"))
                                                                        .withMessage("Some errors occurred")
                                                                        .withStatusCode(
                                                                                HttpStatus.INTERNAL_SERVER_ERROR)
                                                                        .build()));
        components.addExamples("complete-job-result", createCompleteJobResult());
        components.addExamples("job-result-without-uncompleted-result-tree", createJobResultWithoutTree());
        components.addExamples("partial-job-result", createPartialJobResult());
        components.addExamples("canceled-job-result", createCanceledJobResult());
        components.addExamples("failed-job-result", createFailedJobResult());
        components.addExamples("complete-job-list-processing-state", createJobListProcessingState());
    }

    private Example createJobListProcessingState() {
        return toExample(List.of(JobStatusResult.builder().jobId(UUID.fromString(JOB_HANDLE_ID_1)).status(JobState.COMPLETED).build()));
    }

    private JobHandle createJobHandle(final String name) {
        return JobHandle.builder().jobId(UUID.fromString(name)).build();
    }

    private Example createFailedJobResult() {
        return toExample(Jobs.builder()
                             .job(Job.builder()
                                     .jobId(UUID.fromString(JOB_ID))
                                     .globalAssetId(createGAID(GLOBAL_ASSET_ID))
                                     .jobState(JobState.ERROR)
                                     .owner("")
                                     .createdOn(EXAMPLE_ZONED_DATETIME)
                                     .startedOn(EXAMPLE_ZONED_DATETIME)
                                     .lastModifiedOn(EXAMPLE_ZONED_DATETIME)
                                     .summary(createSummary())
                                     .jobParameter(createJobParameter())
                                     .exception(createJobException())
                                     .build())
                             .build());
    }

    private Example createPartialJobResult() {
        return toExample(Jobs.builder()
                             .job(Job.builder()
                                     .jobId(UUID.fromString(JOB_ID))
                                     .globalAssetId(createGAID(GLOBAL_ASSET_ID))
                                     .jobState(JobState.RUNNING)
                                     .owner("")
                                     .createdOn(EXAMPLE_ZONED_DATETIME)
                                     .startedOn(EXAMPLE_ZONED_DATETIME)
                                     .lastModifiedOn(EXAMPLE_ZONED_DATETIME)
                                     .jobCompleted(EXAMPLE_ZONED_DATETIME)
                                     .summary(createSummary())
                                     .jobParameter(createJobParameter())
                                     .exception(createJobException())
                                     .build())
                             .build());
    }

    private JobParameter createJobParameter() {
        return JobParameter.builder()
                           .bomLifecycle(BomLifecycle.AS_BUILT)
                           .depth(DEFAULT_DEPTH)
                           .aspects(List.of(AspectType.SERIAL_PART_TYPIZATION, AspectType.CONTACT_INFORMATION))
                           .direction(Direction.DOWNWARD)
                           .build();
    }

    private Summary createSummary() {
        return Summary.builder()
                      .asyncFetchedItems(AsyncFetchedItems.builder().completed(0).failed(0).running(0).build())
                      .build();
    }

    private JobErrorDetails createJobException() {
        return new JobErrorDetails("IrsTimeoutException", "Timeout while requesting Digital Registry",
                EXAMPLE_ZONED_DATETIME);
    }

    private Example createJobResultWithoutTree() {
        return createCompleteJobResult();
    }

    private Example createCompleteJobResult() {
        return toExample(Jobs.builder()
                             .job(Job.builder()
                                     .jobId(UUID.fromString(JOB_ID))
                                     .globalAssetId(createGAID(GLOBAL_ASSET_ID))
                                     .jobState(JobState.COMPLETED)
                                     .owner("")
                                     .createdOn(EXAMPLE_ZONED_DATETIME)
                                     .startedOn(EXAMPLE_ZONED_DATETIME)
                                     .lastModifiedOn(EXAMPLE_ZONED_DATETIME)
                                     .jobCompleted(EXAMPLE_ZONED_DATETIME)
                                     .owner("")
                                     .summary(createSummary())
                                     .jobParameter(createJobParameter())
                                     .exception(createJobException())
                                     .build())
                             .relationships(List.of(createRelationship()))
                             .shells(List.of(createShell()))
                             .tombstone(createTombstone())
                             .build());
    }

    private Tombstone createTombstone() {
        return Tombstone.builder()
                        .catenaXId(createGAID(GLOBAL_ASSET_ID).getGlobalAssetId())
                        .endpointURL("https://catena-x.net/vehicle/partdetails/")
                        .processingError(ProcessingError.builder()
                                                        .withErrorDetail("Details to reason of Failure")
                                                        .withLastAttempt(EXAMPLE_ZONED_DATETIME)
                                                        .withRetryCounter(0)
                                                        .build())
                        .build();
    }

    private AssetAdministrationShellDescriptor createShell() {
        return AssetAdministrationShellDescriptor.builder()
                                                 .description(List.of(LangString.builder()
                                                                                .language("en")
                                                                                .text("The shell for a vehicle")
                                                                                .build()))
                                                 .globalAssetId(Reference.builder()
                                                                         .value(List.of(
                                                                                 "urn:uuid:a45a2246-f6e1-42da-b47d-5c3b58ed62e9"))
                                                                         .build())
                                                 .idShort("future concept x")
                                                 .identification("882fc530-b69b-4707-95f6-5dbc5e9baaa8")
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                                                 .key("engineserialid")
                                                                                                 .value("12309481209312")
                                                                                                 .build()))
                                                 .submodelDescriptors(List.of(createBaseSubmodelDescriptor(),
                                                         createPartSubmodelDescriptor()))
                                                 .build();
    }

    private Relationship createRelationship() {
        return Relationship.builder()
                           .catenaXId(createGAID("d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447"))
                           .childItem(ChildItem.builder()
                                               .quantity(createQuantity())
                                               .childCatenaXId(createGAID("a45a2246-f6e1-42da-b47d-5c3b58ed62e9"))
                                               .lastModifiedOn(EXAMPLE_ZONED_DATETIME)
                                               .assembledOn(EXAMPLE_ZONED_DATETIME)
                                               .lifecycleContext(BomLifecycle.AS_BUILT)
                                               .build())
                           .build();
    }

    private GlobalAssetIdentification createGAID(final String globalAssetId) {
        final String prefixedId = globalAssetId.startsWith("urn:uuid:") ? globalAssetId : "urn:uuid:" + globalAssetId;
        return GlobalAssetIdentification.builder().globalAssetId(prefixedId).build();
    }

    private Quantity createQuantity() {
        return Quantity.builder()
                       .quantityNumber(1d)
                       .measurementUnit(MeasurementUnit.builder()
                                                       .datatypeURI(
                                                               "urn:bamm:io.openmanufacturing:meta-model:1.0.0#piece")
                                                       .lexicalValue("piece")
                                                       .build())

                       .build();
    }

    private SubmodelDescriptor createBaseSubmodelDescriptor() {
        return SubmodelDescriptor.builder()
                                 .description(List.of(LangString.builder()
                                                                .language("en")
                                                                .text("Provides base vehicle information")
                                                                .build()))
                                 .idShort("vehicle base details")
                                 .identification("4a738a24-b7d8-4989-9cd6-387772f40565")
                                 .semanticId(Reference.builder()
                                                      .value(List.of("urn:bamm:com.catenax.vehicle:0.1.1"))
                                                      .build())
                                 .endpoints(List.of(createEndpoint("https://catena-x.net/vehicle/basedetails/")))
                                 .build();
    }

    private Endpoint createEndpoint(final String endpointAddress) {
        return Endpoint.builder()
                       .interfaceInformation("HTTP")
                       .protocolInformation(ProtocolInformation.builder()
                                                               .endpointAddress(endpointAddress)
                                                               .endpointProtocol("HTTPS")
                                                               .endpointProtocolVersion("1.0")
                                                               .build())
                       .build();
    }

    private SubmodelDescriptor createPartSubmodelDescriptor() {
        return SubmodelDescriptor.builder()
                                 .description(List.of(LangString.builder()
                                                                .language("en")
                                                                .text("Provides base vehicle information")
                                                                .build()))
                                 .idShort("vehicle part details")
                                 .identification("dae4d249-6d66-4818-b576-bf52f3b9ae90")
                                 .semanticId(Reference.builder()
                                                      .value(List.of("urn:bamm:com.catenax.vehicle:0.1.1#PartDetails"))
                                                      .build())
                                 .endpoints(List.of(createEndpoint("https://catena-x.net/vehicle/partdetails/")))
                                 .build();
    }

    private Example createCanceledJobResult() {
        return toExample(Jobs.builder()
                             .job(Job.builder()
                                     .jobId(UUID.fromString(JOB_ID))
                                     .globalAssetId(createGAID(GLOBAL_ASSET_ID))
                                     .jobState(JobState.CANCELED)
                                     .owner("")
                                     .createdOn(EXAMPLE_ZONED_DATETIME)
                                     .startedOn(EXAMPLE_ZONED_DATETIME)
                                     .lastModifiedOn(EXAMPLE_ZONED_DATETIME)
                                     .jobCompleted(EXAMPLE_ZONED_DATETIME)
                                     .jobParameter(createJobParameter())
                                     .jobParameter(createJobParameter())
                                     .exception(createJobException())
                                     .summary(createSummary())
                                     .build())
                             .build());
    }

    private Example toExample(final Object value) {
        return new Example().value(value);
    }
}
