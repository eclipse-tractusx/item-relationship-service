package net.catenax.prs.connector.job;

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.spi.transfer.TransferInitiateResponse;
import org.eclipse.dataspaceconnector.spi.transfer.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;

import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
                .jobId(faker.lorem().characters())
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
        return DataRequest.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .connectorAddress(faker.internet().url())
                .protocol("ids-rest")
                .connectorId("consumer")
                .dataEntry(DataEntry.Builder.newInstance()
                        .id("prs-request")
                        .policyId("use-eu")
                        .build())
                .dataDestination(DataAddress.Builder.newInstance()
                        .type(faker.lorem().word())
                        .property("account", faker.lorem().word())
                        .build())
                .properties(Map.of(
                        faker.lorem().word(), faker.lorem().word()
                ))
                .managedResources(true)
                .build();
    }

    TransferInitiateResponse okResponse() {
        return response(ResponseStatus.OK);
    }

    TransferInitiateResponse response(ResponseStatus status) {
        return TransferInitiateResponse.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .status(status)
                .build();
    }

    TransferProcess transfer() {
        return TransferProcess.Builder.newInstance()
                .id(faker.lorem().characters())
                .build();
    }

    public Stream<DataRequest> dataRequests(int count) {
        return IntStream.range(0, count).mapToObj(i -> dataRequest());
    }
}