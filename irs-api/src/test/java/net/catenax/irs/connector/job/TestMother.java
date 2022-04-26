package net.catenax.irs.connector.job;

import static net.catenax.irs.dtos.IrsCommonConstants.ROOT_ITEM_ID_KEY;

import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.javafaker.Faker;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.enums.JobState;

/**
 * Base object mother class to create objects for testing.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
class TestMother {

    Faker faker = new Faker();

    Job fakeJob(JobState state) {
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

    MultiTransferJob job() {
        return job(faker.options().option(JobState.class));
    }

    MultiTransferJob job(JobState jobState) {
        return MultiTransferJob.builder()
                               .job(fakeJob(jobState))
                               .jobData(Map.of(ROOT_ITEM_ID_KEY, faker.lorem().characters(),
                                   faker.lorem().characters(), faker.lorem().characters()))
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
        return TransferInitiateResponse.builder().transferId(UUID.randomUUID().toString()).status(status).build();
    }

    TransferProcess transfer() {
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
}