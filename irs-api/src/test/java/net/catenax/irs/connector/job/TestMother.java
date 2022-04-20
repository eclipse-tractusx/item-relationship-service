package net.catenax.irs.connector.job;

import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.javafaker.Faker;

/**
 * Base object mother class to create objects for testing.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
class TestMother {

    Faker faker = new Faker();

    MultiTransferJob job() {
        return job(faker.options().option(JobState.class));
    }

    MultiTransferJob job(JobState jobState) {
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

    DataRequest dataRequest() {
        return new DataRequest() {
        };
    }

    TransferInitiateResponse okResponse() {
        return response(ResponseStatus.OK);
    }

    TransferInitiateResponse response(ResponseStatus status) {
        return TransferInitiateResponse.builder()
                .transferId(UUID.randomUUID().toString())
                .status(status)
                .build();
    }

    TransferProcess transfer() {
        final String characters = faker.lorem().characters();
        return () -> characters;
    }

    public Stream<DataRequest> dataRequests(int count) {
        return IntStream.range(0, count).mapToObj(i -> dataRequest());
    }
}