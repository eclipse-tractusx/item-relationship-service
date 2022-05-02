package net.catenax.irs.util;

import static net.catenax.irs.controllers.IrsApiConstants.GLOBAL_ASSET_ID_SIZE;
import static net.catenax.irs.controllers.IrsApiConstants.UUID_SIZE;
import static net.catenax.irs.dtos.IrsCommonConstants.LIFE_CYCLE_CONTEXT;
import static net.catenax.irs.dtos.IrsCommonConstants.ROOT_ITEM_ID_KEY;

import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.javafaker.Faker;
import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.DataRequest;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferInitiateResponse;
import net.catenax.irs.connector.job.TransferProcess;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;

/**
 * Base object mother class to create objects for testing.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
public class TestMother {

    private static final String AS_BUILT = "AsBuilt";

    Faker faker = new Faker();

    public AssemblyPartRelationshipDTO assemblyPartRelationshipDTO() {
        final ChildDataDTO childPart = ChildDataDTO.builder()
                                                   .childCatenaXId(faker.lorem().characters(GLOBAL_ASSET_ID_SIZE))
                                                   .lifecycleContext(AS_BUILT)
                                                   .build();
        final Set<ChildDataDTO> childParts = Set.of(childPart);
        return AssemblyPartRelationshipDTO.builder()
                                          .childParts(childParts)
                                          .catenaXId(faker.lorem().characters(GLOBAL_ASSET_ID_SIZE))
                                          .build();
    }

    public AASTransferProcess aasTransferProcess() {
        return new AASTransferProcess(faker.lorem().characters(UUID_SIZE), faker.number().numberBetween(1, 100));
    }

    public Job fakeJob(JobState state) {
        return Job.builder()
                  .jobId(UUID.randomUUID())
                  .globalAssetId(
                          GlobalAssetIdentification.builder().globalAssetId(UUID.randomUUID().toString()).build())
                  .jobState(state)
                  .createdOn(Instant.now())
                  .owner(faker.lorem().characters())
                  .lastModifiedOn(Instant.now())
                  .requestUrl(fakeURL())
                  .build();
    }

    public MultiTransferJob job() {
        return job(faker.options().option(JobState.class));
    }

    public MultiTransferJob job(JobState jobState) {
        return MultiTransferJob.builder()
                               .job(fakeJob(jobState))
                               .jobData(Map.of(ROOT_ITEM_ID_KEY, faker.lorem().characters(), faker.lorem().characters(),
                                       faker.lorem().characters()))
                               .jobData(Map.of(ROOT_ITEM_ID_KEY, faker.lorem().characters(),
                                       faker.lorem().characters(), faker.lorem().characters(), LIFE_CYCLE_CONTEXT,
                                       AS_BUILT))
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

    private URL fakeURL() {
        try {
            return new URL("http://localhost:8888/fake/url");
        } catch (Exception e) {
            return null;
        }
    }

    public static RegisterJob registerJobWithoutDepth() {
        return registerJobWithDepth(null);
    }

    public static RegisterJob registerJobWithDepth(final Integer depth) {
        return registerJobWithGlobalAssetIdAndDepth("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6", depth);
    }

    public static RegisterJob registerJobWithGlobalAssetIdAndDepth(final String globalAssetId, final Integer depth) {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setGlobalAssetId(globalAssetId);
        registerJob.setDepth(depth);
        return registerJob;
    }

}