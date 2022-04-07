package net.catenax.irs.connector.consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import net.catenax.irs.connector.consumer.configuration.ConsumerConfiguration;
import net.catenax.irs.connector.job.JobState;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.connector.requests.JobsTreeRequest;
import net.catenax.irs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.catenax.irs.connector.consumer.service.ConsumerService.CONTAINER_NAME_KEY;
import static net.catenax.irs.connector.consumer.service.ConsumerService.DESTINATION_PATH_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class JobsTreeRecursiveJobHandlerTest {

    static final ObjectMapper MAPPER = new ObjectMapper();
    private final RequestMother generate = new RequestMother();
    private final JobsTreeRequest partsTreeRequest = generate.jobsTreeRequest();
    Faker faker = new Faker();
    Monitor monitor = new ConsoleMonitor();
    String storageAccountName = faker.lorem().characters();
    String containerName = faker.lorem().word();
    String blobName = faker.lorem().word();
    ConsumerConfiguration configuration = ConsumerConfiguration.builder()
            .storageAccountName(storageAccountName)
            .build();
    JobsTreeRecursiveJobHandler sut;
    MultiTransferJob job = MultiTransferJob.builder()
            .jobId(faker.lorem().characters())
            .state(faker.options().option(JobState.class))
            .jobDatum(CONTAINER_NAME_KEY, containerName)
            .jobDatum(DESTINATION_PATH_KEY, blobName)
            .build();
    @Mock
    private JobsTreeRecursiveLogic logic;
    @Mock
    private Stream<DataRequest> streamOfDataRequests;

    @BeforeEach
    public void setUp() throws Exception {
        sut = new JobsTreeRecursiveJobHandler(monitor, configuration, new JsonUtil(monitor), logic);

        var serializedPartsTreeRequest = MAPPER.writeValueAsString(partsTreeRequest);
        job = job.toBuilder().jobData(Map.of(ConsumerService.PARTS_REQUEST_KEY, serializedPartsTreeRequest)).build();
    }

    @Test
    void initiate() {
        // Arrange
        when(logic.createInitialPartsTreeRequest(partsTreeRequest))
                .thenReturn(streamOfDataRequests);

        // Act
        var result = sut.initiate(job);

        // Assert
        assertThat(result).isSameAs(streamOfDataRequests);
    }

    @Test
    void recurse() {
        // Arrange
        var transfer = generate.transferProcess();
        when(logic.createSubsequentPartsTreeRequests(transfer, partsTreeRequest))
                .thenReturn(streamOfDataRequests);

        // Act
        var result = sut.recurse(job, transfer);

        // Assert
        assertThat(result).isSameAs(streamOfDataRequests);
    }

    @Test
    void complete() {
        // Arrange
        var transfers = IntStream.range(0, faker.number().numberBetween(0, 3))
                .mapToObj(n -> generate.transferProcess())
                .collect(Collectors.toList());

        job = job.toBuilder().completedTransfers(transfers).build();

        // Act
        sut.complete(job);

        // Assert
        verify(logic).assemblePartialPartTreeBlobs(transfers, blobName);
    }
}
