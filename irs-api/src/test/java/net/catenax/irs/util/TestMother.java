package net.catenax.irs.util;

import static net.catenax.irs.controllers.IrsAppConstants.UUID_SIZE;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.DataRequest;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferInitiateResponse;
import net.catenax.irs.connector.job.TransferProcess;
import net.catenax.irs.dto.JobParameter;
import net.catenax.irs.services.MeterRegistryService;
import net.datafaker.Faker;

/**
 * Base object mother class to create objects for testing.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
public class TestMother {

    private static final String AS_BUILT = "AsBuilt";

    Faker faker = new Faker();

    public static RegisterJob registerJobWithoutDepthAndAspect() {
        return registerJobWithDepthAndAspect(null, null);
    }

    public static RegisterJob registerJobWithoutDepth() {
        return registerJobWithDepthAndAspect(null, List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP));
    }

    public static RegisterJob registerJobWithDepthAndAspect(final Integer depth, final List<AspectType> aspectTypes) {
        return registerJobWithGlobalAssetIdAndDepth("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6", depth, aspectTypes,
                false);
    }

    public static RegisterJob registerJobWithDepthAndAspectAndCollectAspects(final Integer depth,
            final List<AspectType> aspectTypes) {
        return registerJobWithGlobalAssetIdAndDepth("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6", depth, aspectTypes,
                true);
    }

    public static RegisterJob registerJobWithGlobalAssetIdAndDepth(final String globalAssetId, final Integer depth,
            final List<AspectType> aspectTypes, final boolean collectAspects) {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setGlobalAssetId(globalAssetId);
        registerJob.setDepth(depth);
        registerJob.setAspects(aspectTypes);
        registerJob.setCollectAspects(collectAspects);

        return registerJob;
    }

    public static JobParameter jobParameter() {
        return JobParameter.builder()
                           .rootItemId("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6")
                           .treeDepth(0)
                           .bomLifecycle("AsBuilt")
                           .aspectTypes(List.of(AspectType.SERIAL_PART_TYPIZATION.toString(),
                                   AspectType.ASSEMBLY_PART_RELATIONSHIP.toString()))
                           .build();
    }

    public static JobParameter jobParameterFilter() {
        return JobParameter.builder()
                           .rootItemId("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6")
                           .treeDepth(0)
                           .bomLifecycle("AsRequired")
                           .aspectTypes(List.of(AspectType.MATERIAL_FOR_RECYCLING.toString()))
                           .build();
    }

    public static JobParameter jobParameterEmptyFilter() {
        return JobParameter.builder()
                           .rootItemId("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6")
                           .treeDepth(0)
                           .bomLifecycle("AsRequired")
                           .aspectTypes(List.of())
                           .build();
    }

    public static MeterRegistryService simpleMeterRegistryService() {
        return new MeterRegistryService(new SimpleMeterRegistry());
    }

    public AASTransferProcess aasTransferProcess() {
        return new AASTransferProcess(faker.lorem().characters(UUID_SIZE), faker.number().numberBetween(1, 100));
    }

    public Job fakeJob(JobState state) {
        return Job.builder()
                  .jobId(UUID.randomUUID())
                  .globalAssetId(GlobalAssetIdentification.of(UUID.randomUUID().toString()))
                  .jobState(state)
                  .createdOn(ZonedDateTime.now(ZoneId.of("UTC")))
                  .owner(faker.lorem().characters())
                  .lastModifiedOn(ZonedDateTime.now(ZoneId.of("UTC")))
                  .build();
    }

    public MultiTransferJob job() {
        return job(faker.options().option(JobState.class));
    }

    public MultiTransferJob job(JobState jobState) {
        return MultiTransferJob.builder()
                               .job(fakeJob(jobState))
                               .jobParameter(jobParameter())
                               .jobParameter(jobParameter())
                               .build();
    }

    public DataRequest dataRequest() {
        return new DataRequest() {
        };
    }

    public TransferInitiateResponse okResponse() {
        return response(ResponseStatus.OK);
    }

    public TransferInitiateResponse response(ResponseStatus status) {
        return TransferInitiateResponse.builder().transferId(UUID.randomUUID().toString()).status(status).build();
    }

    public TransferProcess transfer() {
        final String characters = faker.lorem().characters();
        return () -> characters;
    }

    public Stream<DataRequest> dataRequests(int count) {
        return IntStream.range(0, count).mapToObj(i -> dataRequest());
    }

}