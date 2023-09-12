/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import org.eclipse.tractusx.ess.service.NotificationSummary;
import org.eclipse.tractusx.irs.component.AsyncFetchedItems;
import org.eclipse.tractusx.irs.component.BatchResponse;
import org.eclipse.tractusx.irs.component.BatchOrderResponse;
import org.eclipse.tractusx.irs.component.Bpn;
import org.eclipse.tractusx.irs.component.FetchedItems;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobErrorDetails;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.JobStatusResult;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.MeasurementUnit;
import org.eclipse.tractusx.irs.component.PageResult;
import org.eclipse.tractusx.irs.component.ProcessingError;
import org.eclipse.tractusx.irs.component.Quantity;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.Summary;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.assetadministrationshell.LangString;
import org.eclipse.tractusx.irs.component.assetadministrationshell.ProtocolInformation;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SemanticId;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;
import org.eclipse.tractusx.irs.dtos.ErrorResponse;
import org.eclipse.tractusx.irs.semanticshub.AspectModel;
import org.eclipse.tractusx.irs.semanticshub.AspectModels;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.beans.support.PagedListHolder;
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

    private static final UUID UUID_ID = UUID.fromString("f253718e-a270-4367-901b-9d50d9bd8462");
    private static final String GLOBAL_ASSET_ID = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0";
    private static final String SUBMODEL_IDENTIFICATION = "urn:uuid:fc784d2a-5506-4e61-8e34-21600f8cdeff";
    private static final String JOB_HANDLE_ID_1 = "6c311d29-5753-46d4-b32c-19b918ea93b0";
    private static final String EXAMPLE_BPN = "BPNL00000003AAXX";
    private static final String SUPPLY_CHAIN_IMPACTED_ASPECT_TYPE = "supply_chain_impacted";
    private static final String SUPPLY_CHAIN_IMPACTED_KEY = "supplyChainImpacted";
    private static final String SUPPLY_CHAIN_IMPACTER_RESULT = "YES";
    private static final int FETCHED_ITEMS_SIZE = 3;
    private static final int NO_RUNNING_OR_FAILED_ITEMS = 0;
    private static final int SENT_NOTIFICATIONS_SIZE = 6;

    public void createExamples(final Components components) {
        components.addExamples("job-handle", toExample(createJobHandle(JOB_HANDLE_ID_1)));
        components.addExamples("error-response-400", toExample(ErrorResponse.builder()
                                                                            .withMessages(
                                                                                    List.of("BadRequestException"))
                                                                            .withError("Bad request")
                                                                            .withStatusCode(HttpStatus.BAD_REQUEST)
                                                                            .build()));
        components.addExamples("error-response-401", toExample(ErrorResponse.builder()
                                                                            .withMessages(
                                                                                    List.of("UnauthorizedException"))
                                                                            .withError("Unauthorized")
                                                                            .withStatusCode(HttpStatus.UNAUTHORIZED)
                                                                            .build()));
        components.addExamples("error-response-403", toExample(ErrorResponse.builder()
                                                                            .withMessages(List.of("ForbiddenException"))
                                                                            .withError("Forbidden")
                                                                            .withStatusCode(HttpStatus.FORBIDDEN)
                                                                            .build()));
        components.addExamples("error-response-404", toExample(ErrorResponse.builder()
                                                                            .withMessages(List.of("NotFoundException"))
                                                                            .withError("Not found")
                                                                            .withStatusCode(HttpStatus.NOT_FOUND)
                                                                            .build()));
        components.addExamples("complete-job-result", createCompleteJobResult());
        components.addExamples("complete-ess-job-result", createCompleteEssJobResult());
        components.addExamples("complete-order-result", createCompleteOrderResult());
        components.addExamples("complete-batch-result", createCompleteBatchResult());
        components.addExamples("job-result-without-uncompleted-result-tree", createJobResultWithoutTree());
        components.addExamples("partial-job-result", createPartialJobResult());
        components.addExamples("canceled-job-result", createCanceledJobResult());
        components.addExamples("failed-job-result", createFailedJobResult());
        components.addExamples("canceled-job-response", createCanceledJobResponse());
        components.addExamples("complete-job-list-processing-state", createJobListProcessingState());
        components.addExamples("aspect-models-list", createAspectModelsResult());
    }

    private Example createAspectModelsResult() {
        final AspectModel assemblyPartRelationship = AspectModel.builder()
                                                                .name("SingleLevelBomAsBuilt")
                                                                .urn("urn:bamm:io.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt")
                                                                .version("1.0.0")
                                                                .status("RELEASED")
                                                                .type("BAMM")
                                                                .build();
        final AspectModel serialPart = AspectModel.builder()
                                                            .name("SerialPart")
                                                            .urn("urn:bamm:io.catenax.serial_part:1.0.0#SerialPart")
                                                            .version("1.0.0")
                                                            .status("RELEASED")
                                                            .type("BAMM")
                                                            .build();

        return toExample(AspectModels.builder()
                                     .lastUpdated("2023-02-13T08:18:11.990659500Z")
                                     .models(List.of(assemblyPartRelationship, serialPart))
                                     .build());
    }

    private Example createJobListProcessingState() {
        return toExample(new PageResult(new PagedListHolder<>(List.of(JobStatusResult.builder()
                                                                                     .id(UUID.fromString(
                                                                                             JOB_HANDLE_ID_1))
                                                                                     .state(JobState.COMPLETED)
                                                                                     .startedOn(EXAMPLE_ZONED_DATETIME)
                                                                                     .completedOn(
                                                                                             EXAMPLE_ZONED_DATETIME)
                                                                                     .build()))));
    }

    private JobHandle createJobHandle(final String name) {
        return JobHandle.builder().id(UUID.fromString(name)).build();
    }

    private Example createFailedJobResult() {
        return toExample(Jobs.builder()
                             .job(Job.builder()
                                     .id(UUID.fromString(JOB_ID))
                                     .globalAssetId(createGAID(GLOBAL_ASSET_ID))
                                     .state(JobState.ERROR)
                                     .owner("")
                                     .createdOn(EXAMPLE_ZONED_DATETIME)
                                     .startedOn(EXAMPLE_ZONED_DATETIME)
                                     .lastModifiedOn(EXAMPLE_ZONED_DATETIME)
                                     .summary(createSummary())
                                     .parameter(createJobParameter())
                                     .exception(createJobException())
                                     .build())
                             .build());
    }

    private Example createPartialJobResult() {
        return toExample(Jobs.builder()
                             .job(Job.builder()
                                     .id(UUID.fromString(JOB_ID))
                                     .globalAssetId(createGAID(GLOBAL_ASSET_ID))
                                     .state(JobState.RUNNING)
                                     .owner("")
                                     .createdOn(EXAMPLE_ZONED_DATETIME)
                                     .startedOn(EXAMPLE_ZONED_DATETIME)
                                     .lastModifiedOn(EXAMPLE_ZONED_DATETIME)
                                     .completedOn(EXAMPLE_ZONED_DATETIME)
                                     .summary(createSummary())
                                     .parameter(createJobParameter())
                                     .exception(createJobException())
                                     .build())
                             .build());
    }

    private JobParameter createJobParameter() {
        return JobParameter.builder()
                           .bomLifecycle(BomLifecycle.AS_BUILT)
                           .depth(1)
                           .aspects(List.of(AspectType.SERIAL_PART.toString(), AspectType.ADDRESS_ASPECT.toString()))
                           .direction(Direction.DOWNWARD)
                           .collectAspects(false)
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
                                     .id(UUID.fromString(JOB_ID))
                                     .globalAssetId(createGAID(GLOBAL_ASSET_ID))
                                     .state(JobState.COMPLETED)
                                     .owner("")
                                     .createdOn(EXAMPLE_ZONED_DATETIME)
                                     .startedOn(EXAMPLE_ZONED_DATETIME)
                                     .lastModifiedOn(EXAMPLE_ZONED_DATETIME)
                                     .completedOn(EXAMPLE_ZONED_DATETIME)
                                     .owner("")
                                     .summary(createSummary())
                                     .parameter(createJobParameter())
                                     .exception(createJobException())
                                     .build())
                             .relationships(List.of(createRelationship()))
                             .shells(List.of(createShell()))
                             .tombstone(createTombstone())
                             .submodel(createSubmodel())
                             .bpn(Bpn.withManufacturerId("BPNL00000003AYRE").updateManufacturerName("OEM A"))
                             .build());
    }

    private Example createCompleteEssJobResult() {
        final Jobs essJobsJobs = Jobs.builder()
                                     .job(Job.builder()
                                             .id(UUID.fromString(JOB_ID))
                                             .globalAssetId(createGAID(GLOBAL_ASSET_ID))
                                             .state(JobState.COMPLETED)
                                             .owner("")
                                             .createdOn(EXAMPLE_ZONED_DATETIME)
                                             .startedOn(EXAMPLE_ZONED_DATETIME)
                                             .lastModifiedOn(EXAMPLE_ZONED_DATETIME)
                                             .completedOn(EXAMPLE_ZONED_DATETIME)
                                             .owner("")
                                             .summary(createSummary())
                                             .parameter(createJobParameter())
                                             .exception(createJobException())
                                             .build())
                                     .relationships(List.of(createRelationship()))
                                     .shells(List.of(createShell()))
                                     .tombstone(createTombstone())
                                     .submodel(createEssSubmodel())
                                     .bpn(Bpn.withManufacturerId(EXAMPLE_BPN).updateManufacturerName("AB CD"))
                                     .build();
        final NotificationSummary newSummary = new NotificationSummary(
                AsyncFetchedItems.builder().running(NO_RUNNING_OR_FAILED_ITEMS).completed(FETCHED_ITEMS_SIZE).failed(NO_RUNNING_OR_FAILED_ITEMS).build(),
                FetchedItems.builder().completed(FETCHED_ITEMS_SIZE).failed(NO_RUNNING_OR_FAILED_ITEMS).build(),
                SENT_NOTIFICATIONS_SIZE, SENT_NOTIFICATIONS_SIZE);
        final Job job = essJobsJobs.getJob().toBuilder().summary(newSummary).build();
        return toExample(essJobsJobs.toBuilder().job(job).build());
    }

    private Submodel createEssSubmodel() {
        return Submodel.builder()
                       .aspectType(SUPPLY_CHAIN_IMPACTED_ASPECT_TYPE)
                       .identification(SUBMODEL_IDENTIFICATION)
                       .payload(Map.of(SUPPLY_CHAIN_IMPACTED_KEY, SUPPLY_CHAIN_IMPACTER_RESULT))
                       .build();
    }

    private Example createCompleteOrderResult() {
        return toExample(BatchOrderResponse.builder()
                                           .orderId(UUID_ID)
                                           .state(ProcessingState.COMPLETED)
                                           .batchChecksum(1)
                                           .batches(List.of(BatchOrderResponse.BatchResponse.builder()
                                                                                            .batchId(UUID_ID)
                                                                                            .batchNumber(1)
                                                                                            .jobsInBatchChecksum(1)
                                                                                            .batchUrl(
                                                                                                    "https://../irs/orders/"
                                                                                                            + UUID_ID
                                                                                                            + "/batches/"
                                                                                                            + UUID_ID)
                                                                                            .batchProcessingState(
                                                                                                    ProcessingState.PARTIAL)
                                                                                            .build()))
                                           .build());
    }

    private Example createCompleteBatchResult() {
        return toExample(BatchResponse.builder()
                                      .batchId(UUID_ID)
                                      .orderId(UUID_ID)
                                      .batchNumber(1)
                                      .batchTotal(1)
                                      .totalJobs(1)
                                      .startedOn(EXAMPLE_ZONED_DATETIME)
                                      .completedOn(EXAMPLE_ZONED_DATETIME)
                                      .jobs(List.of(JobStatusResult.builder()
                                                                   .id(UUID.fromString(JOB_HANDLE_ID_1))
                                                                   .state(JobState.COMPLETED)
                                                                   .startedOn(EXAMPLE_ZONED_DATETIME)
                                                                   .completedOn(EXAMPLE_ZONED_DATETIME)
                                                                   .build()))
                                      .jobsInBatchChecksum(1)
                                      .batchProcessingState(ProcessingState.COMPLETED)
                                      .build());
    }

    private Example createCanceledJobResponse() {
        return toExample(Job.builder()
                            .id(UUID.fromString(JOB_HANDLE_ID_1))
                            .globalAssetId(createGAID(GLOBAL_ASSET_ID))
                            .state(JobState.CANCELED)
                            .lastModifiedOn(EXAMPLE_ZONED_DATETIME)
                            .startedOn(EXAMPLE_ZONED_DATETIME)
                            .completedOn(EXAMPLE_ZONED_DATETIME)
                            .build());
    }

    private Submodel createSubmodel() {
        return Submodel.builder()
                       .aspectType("urn:bamm:io.catenax.single_level_bom_as_built:1.0.0")
                       .identification(SUBMODEL_IDENTIFICATION)
                       .payload(createAssemblyPartRelationshipPayloadMap())
                       .build();
    }

    private Map<String, Object> createAssemblyPartRelationshipPayloadMap() {
        final String assemblyPartRelationshipPayload =
                "{\"catenaXId\": \"urn:uuid:d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447\", "
                        + "\"childItems\": [ { \"createdOn\": \"2022-02-03T14:48:54.709Z\", \"catenaXId\": \"urn:uuid:d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447\", "
                        + "\"lastModifiedOn\": \"2022-02-03T14:48:54.709Z\", \"lifecycleContext\": \"AsBuilt\", \"quantity\": "
                        + "{\"measurementUnit\": {\"datatypeURI\": \"urn:bamm:io.openmanufacturing:meta-model:1.0.0#piece\",\"lexicalValue\": \"piece\"},\"quantityNumber\": 1}}]}";

        return new JsonUtil().fromString(assemblyPartRelationshipPayload, Map.class);
    }

    private Tombstone createTombstone() {
        return Tombstone.builder()
                        .catenaXId(createGAID(GLOBAL_ASSET_ID).getGlobalAssetId())
                        .endpointURL("https://catena-x.net/vehicle/partdetails/")
                        .processingError(ProcessingError.builder()
                                                        .withProcessStep(ProcessStep.SCHEMA_VALIDATION)
                                                        .withErrorDetail("Details to reason of failure")
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
                                                 .globalAssetId("urn:uuid:a45a2246-f6e1-42da-b47d-5c3b58ed62e9")
                                                 .idShort("future concept x")
                                                 .id("882fc530-b69b-4707-95f6-5dbc5e9baaa8")
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                                                 .name("engineserialid")
                                                                                                 .value("12309481209312")
                                                                                                 .build()))
                                                 .submodelDescriptors(List.of(createBaseSubmodelDescriptor(),
                                                         createPartSubmodelDescriptor()))
                                                 .build();
    }

    private Relationship createRelationship() {
        return Relationship.builder()
                           .catenaXId(createGAID("d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447"))
                           .linkedItem(LinkedItem.builder()
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
        return GlobalAssetIdentification.of(prefixedId);
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
                                 .id("4a738a24-b7d8-4989-9cd6-387772f40565")
                                 .semanticId(Reference.builder()
                                                      .keys(List.of(SemanticId.builder()
                                                                              .type("Submodel")
                                                                              .value("urn:bamm:com.catenax.vehicle:0.1.1")
                                                                              .build()))
                                                      .type("ModelReference")
                                                      .build())
                                 .endpoints(List.of(createEndpoint("https://catena-x.net/vehicle/basedetails/")))
                                 .build();
    }

    private Endpoint createEndpoint(final String endpointAddress) {
        return Endpoint.builder()
                       .interfaceInformation("HTTP")
                       .protocolInformation(ProtocolInformation.builder()
                                                               .href(endpointAddress)
                                                               .endpointProtocol("HTTPS")
                                                               .endpointProtocolVersion(List.of("1.0"))
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
                                 .id("dae4d249-6d66-4818-b576-bf52f3b9ae90")
                                 .semanticId(Reference.builder()
                                                      .keys(List.of(SemanticId.builder()
                                                                              .type("Submodel")
                                                                              .value("urn:bamm:com.catenax.vehicle:0.1.1#PartDetails")
                                                                              .build()))
                                                      .type("ModelReference")
                                                      .build())
                                 .endpoints(List.of(createEndpoint("https://catena-x.net/vehicle/partdetails/")))
                                 .build();
    }

    private Example createCanceledJobResult() {
        return toExample(Jobs.builder()
                             .job(Job.builder()
                                     .id(UUID.fromString(JOB_ID))
                                     .globalAssetId(createGAID(GLOBAL_ASSET_ID))
                                     .state(JobState.CANCELED)
                                     .owner("")
                                     .createdOn(EXAMPLE_ZONED_DATETIME)
                                     .startedOn(EXAMPLE_ZONED_DATETIME)
                                     .lastModifiedOn(EXAMPLE_ZONED_DATETIME)
                                     .completedOn(EXAMPLE_ZONED_DATETIME)
                                     .parameter(createJobParameter())
                                     .exception(createJobException())
                                     .summary(createSummary())
                                     .build())
                             .build());
    }

    private Example toExample(final Object value) {
        return new Example().value(value);
    }
}
