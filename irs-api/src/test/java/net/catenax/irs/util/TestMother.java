package net.catenax.irs.util;

import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.javafaker.Faker;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.connector.job.DataRequest;
import net.catenax.irs.connector.job.JobState;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferInitiateResponse;
import net.catenax.irs.connector.job.TransferProcess;

/**
 * Base object mother class to create objects for testing.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
public class TestMother {

    Faker faker = new Faker();

    public MultiTransferJob job() {
        return job(faker.options().option(JobState.class));
    }

    public MultiTransferJob job(JobState jobState) {
        return MultiTransferJob.builder()
                .jobId(faker.lorem().characters(36))
                .jobData(Map.of(
                        faker.lorem().characters(),
                        faker.lorem().characters(),
                        faker.lorem().characters(),
                        faker.lorem().characters()
                ))
                .state(jobState)
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
        return TransferInitiateResponse.builder()
                .transferId(UUID.randomUUID().toString())
                .status(status)
                .build();
    }

    public TransferProcess transfer() {
        final String characters = faker.lorem().characters();
        return () -> characters;
    }

    public Stream<DataRequest> dataRequests(int count) {
        return IntStream.range(0, count).mapToObj(i -> dataRequest());
    }

    public static RegisterJob registerJobWithoutDepth() {
        return registerJobWithDepth(null);
    }

    public static RegisterJob registerJobWithDepth(final Integer depth) {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setGlobalAssetId("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6");
        registerJob.setDepth(depth);
        return registerJob;
    }
}