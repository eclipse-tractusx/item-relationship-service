package net.catenax.irs.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import net.catenax.irs.component.AsyncFetchedItems;
import net.catenax.irs.component.ChildItem;
import net.catenax.irs.component.Description;
import net.catenax.irs.component.Endpoint;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobErrorDetails;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.MeasurementUnit;
import net.catenax.irs.component.ProtocolInformation;
import net.catenax.irs.component.Quantity;
import net.catenax.irs.component.QueryParameter;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.SemanticId;
import net.catenax.irs.component.Shell;
import net.catenax.irs.component.SubmodelDescriptor;
import net.catenax.irs.component.Summary;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;
import net.catenax.irs.component.enums.JobState;

public class JobsHelper {

    private static final int DEFAULT_DEPTH = 3;
    Instant instant = Instant.now();

    public Job createJob(String jobId, String globalAssetId, JobState state) {
        return Job.builder()
                  .jobId(UUID.fromString(jobId))
                  .globalAssetId(globalAssetId)
                  .jobState(state)
                  .createdOn(Instant.now())
                  .lastModifiedOn(Instant.now())
                  .exception(null)
                  .startedOn(Instant.now())
                  .build();
    }

    public Jobs createCompleteJobResult(String jobId, String globalAssetId) {

        return Jobs.builder()
                   .job(Job.builder()
                           .jobId(UUID.fromString(jobId))
                           .globalAssetId(globalAssetId)
                           .jobState(JobState.COMPLETED)
                           .owner("")
                           .createdOn(instant)
                           .startedOn(instant)
                           .lastModifiedOn(instant)
                           .jobCompleted(instant)
                           .requestUrl(getJobRequestURL())
                           .owner("")
                           .summary(createSummary())
                           .queryParameter(createQueryParameter())
                           .exception(createJobException())
                           .build())
                   .relationships(List.of(createRelationship()))
                   .shells(List.of(createShell()))
                   .build();
    }

    public Jobs createPartialJobResult(String jobId, String globalAssetId) {
        return Jobs.builder()
                   .job(Job.builder()
                           .jobId(UUID.fromString(jobId))
                           .globalAssetId(globalAssetId)
                           .jobState(JobState.RUNNING)
                           .owner("")
                           .createdOn(instant)
                           .startedOn(instant)
                           .lastModifiedOn(instant)
                           .jobCompleted(instant)
                           .requestUrl(getJobRequestURL())
                           .summary(createSummary())
                           .queryParameter(createQueryParameter())
                           .exception(createJobException())
                           .build())
                   .build();
    }

    public JobHandle createJobHandle(final String name) {
        return JobHandle.builder().jobId(UUID.fromString(name)).build();
    }

    public QueryParameter createQueryParameter() {
        return QueryParameter.builder()
                             .bomLifecycle(BomLifecycle.AS_BUILT)
                             .depth(DEFAULT_DEPTH)
                             .aspects(List.of(AspectType.SERIAL_PART_TYPIZATION, AspectType.CONTACT))
                             .direction(Direction.DOWNWARD)
                             .build();
    }

    public Summary createSummary() {
        return Summary.builder()
                      .asyncFetchedItems(AsyncFetchedItems.builder().complete(0).failed(0).running(0).queue(0).build())
                      .build();
    }

    public JobErrorDetails createJobException() {
        return new JobErrorDetails("IrsTimeoutException", "Timeout while requesting Digital Registry", instant);
    }

    public Shell createShell() {
        return Shell.builder()
                    .description(Description.builder().language("en").text("The shell for a vehicle").build())
                    .globalAssetId(createGAID("a45a2246-f6e1-42da-b47d-5c3b58ed62e9"))
                    .idShort("future concept x")
                    .identification("882fc530-b69b-4707-95f6-5dbc5e9baaa8")
                    .specificAssetId("engineserialid", "12309481209312")
                    .submodelDescriptor(createBaseSubmodelDescriptor())
                    .submodelDescriptor(createPartSubmodelDescriptor())
                    .build();
    }

    public Relationship createRelationship() {
        return Relationship.builder()
                           .catenaXId(createGAID("d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447"))
                           .childItem(ChildItem.builder()
                                               .quantity(createQuantity())
                                               .childCatenaXId(createGAID("a45a2246-f6e1-42da-b47d-5c3b58ed62e9"))
                                               .lastModifiedOn(instant)
                                               .assembledOn(instant)
                                               .lifecycleContext(BomLifecycle.AS_BUILT)
                                               .build())
                           .build();
    }

    public Quantity createQuantity() {
        return Quantity.builder()
                       .quantityNumber(1)
                       .measurementUnit(MeasurementUnit.builder()
                                                       .datatypeURI(
                                                               "urn:bamm:io.openmanufacturing:meta-model:1.0.0#piece")
                                                       .lexicalValue("piece")
                                                       .build())

                       .build();
    }

    public GlobalAssetIdentification createGAID(final String globalAssetId) {
        final String prefixedId = globalAssetId.startsWith("urn:uuid:") ? globalAssetId : "urn:uuid:" + globalAssetId;
        return GlobalAssetIdentification.builder().globalAssetId(prefixedId).build();
    }

    public SubmodelDescriptor createBaseSubmodelDescriptor() {
        return SubmodelDescriptor.builder()
                                 .description(Description.builder()
                                                         .language("en")
                                                         .text("Provides base vehicle information")
                                                         .build())
                                 .idShort("vehicle base details")
                                 .identification("4a738a24-b7d8-4989-9cd6-387772f40565")
                                 .semanticId(SemanticId.builder().value("urn:bamm:com.catenax.vehicle:0.1.1").build())
                                 .endpoint(createEndpoint("https://catena-x.net/vehicle/basedetails/"))
                                 .build();
    }

    public Endpoint createEndpoint(final String endpointAddress) {
        return Endpoint.builder()
                       .interfaceType("HTTP")
                       .protocolInformation(ProtocolInformation.builder()
                                                               .endpointAddress(endpointAddress)
                                                               .endpointProtocol("HTTPS")
                                                               .enpointProtocolVersion("1.0")
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
                                 .endpoint(createEndpoint("https://catena-x.net/vehicle/partdetails/"))
                                 .build();
    }

    private URL getJobRequestURL() {
        return toUrl("https://api.server.test/api/../");
    }

    private URL toUrl(final String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Cannot create URL " + urlString, e);
        }
    }

    private Jobs createJobResultWithoutTree(String jobId, String globalAssetId) {
        return createCompleteJobResult(jobId, globalAssetId);
    }

}
