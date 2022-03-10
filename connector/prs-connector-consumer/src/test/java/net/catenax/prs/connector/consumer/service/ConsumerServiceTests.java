package net.catenax.prs.connector.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import net.catenax.prs.connector.consumer.configuration.ConsumerConfiguration;
import net.catenax.prs.connector.job.JobInitiateResponse;
import net.catenax.prs.connector.job.JobOrchestrator;
import net.catenax.prs.connector.job.JobState;
import net.catenax.prs.connector.job.JobStore;
import net.catenax.prs.connector.job.MultiTransferJob;
import net.catenax.prs.connector.requests.PartsTreeRequest;
import net.catenax.prs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.common.azure.BlobStoreApi;
import org.eclipse.dataspaceconnector.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.transfer.response.ResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static net.catenax.prs.connector.consumer.service.ConsumerService.CONTAINER_NAME_KEY;
import static net.catenax.prs.connector.consumer.service.ConsumerService.DESTINATION_PATH_KEY;
import static net.catenax.prs.connector.consumer.service.ConsumerService.PARTS_REQUEST_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConsumerServiceTests {

    static final ObjectMapper MAPPER = new ObjectMapper();
    static final TemporalAmount SAS_TOKEN_VALIDITY = Duration.ofHours(1);
    private final RequestMother generate = new RequestMother();
    private final PartsTreeRequest partsTreeRequest = generate.partsTreeRequest();
    Faker faker = new Faker();
    String jobId = UUID.randomUUID().toString();
    MultiTransferJob job = MultiTransferJob.builder().build();
    String accountName = faker.lorem().word();
    String containerName = faker.lorem().word();
    String blobName = faker.lorem().word();
    String sasToken = faker.lorem().characters(10);
    @Mock
    JobStore jobStore;
    @Mock
    JobOrchestrator jobOrchestrator;
    @Mock
    BlobStoreApi blobStoreApi;
    Monitor monitor = new ConsoleMonitor();
    ConsumerConfiguration configuration = ConsumerConfiguration.builder().storageAccountName(accountName).build();
    ConsumerService service;
    @Captor
    ArgumentCaptor<Map<String, String>> jobDataCaptor;
    @Captor
    ArgumentCaptor<OffsetDateTime> offsetCaptor;

    /**
     * Provides incomplete job data with its corresponding error message
     *
     * @return Incomplete job data with error messages {@link Stream} of {@link Arguments}.
     */
    private static Stream<Arguments> provideIncompleteJobData() {
        return Stream.of(
                Arguments.of(Map.of(CONTAINER_NAME_KEY, "containerName"), "Missing destinationPath in jobData"),
                Arguments.of(Map.of(DESTINATION_PATH_KEY, "destinationPath"), "Missing containerName in jobData")
        );
    }

    @BeforeEach
    public void setUp() {
        service = new ConsumerService(monitor, new JsonUtil(monitor), jobStore, jobOrchestrator, blobStoreApi, configuration);
    }

    @Test
    public void getStatus_WhenProcessNotInStore_ReturnsEmpty() {
        // Act
        var response = service.getStatus(jobId);
        // Assert
        assertThat(response).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = JobState.class, names = {"COMPLETED", "ERROR"}, mode = EXCLUDE)
    public void getStatus_WhenProcessInStore_ReturnsState(JobState state) {
        // Arrange
        job = job.toBuilder().state(state).build();
        when(jobStore.find(jobId)).thenReturn(Optional.of(job));
        // Act
        var response = service.getStatus(jobId);
        // Assert
        assertThat(response).isNotEmpty();
        assertThat(response.get())
                .usingRecursiveComparison()
                .isEqualTo(StatusResponse.builder()
                        .status(job.getState())
                        .build());
    }

    @Test
    public void getStatus_WhenCompleted_ReturnsSasUrl() {
        // Arrange
        job = job.toBuilder()
                .state(JobState.COMPLETED)
                .jobDatum(CONTAINER_NAME_KEY, containerName)
                .jobDatum(DESTINATION_PATH_KEY, blobName)
                .build();
        when(jobStore.find(jobId)).thenReturn(Optional.of(job));
        when(blobStoreApi.createContainerSasToken(eq(accountName), eq(containerName), eq("r"), offsetCaptor.capture()))
                .thenReturn(sasToken);
        OffsetDateTime before = OffsetDateTime.now();

        // Act
        var response = service.getStatus(jobId);
        OffsetDateTime after = OffsetDateTime.now();
        // Assert
        assertThat(response).isNotEmpty();
        assertThat(response.get())
                .usingRecursiveComparison()
                .isEqualTo(StatusResponse.builder()
                        .status(job.getState())
                        .sasToken(format("https://%s.blob.core.windows.net/%s/%s?%s",
                                accountName,
                                containerName,
                                blobName,
                                sasToken
                        ))
                        .build());
        assertThat(offsetCaptor.getValue()).isBetween(
                before.plus(SAS_TOKEN_VALIDITY),
                after.plus(SAS_TOKEN_VALIDITY));
    }

    @ParameterizedTest
    @MethodSource("provideIncompleteJobData")
    public void getStatus_WhenCompletedAndJobDataMissing_Throws(Map<String, String> jobData, String errorMessage) {
        // Arrange
        job = job.toBuilder().jobData(jobData).state(JobState.COMPLETED).build();
        when(jobStore.find(jobId)).thenReturn(Optional.of(job));
        // Act
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> service.getStatus(jobId))
                .withMessage(errorMessage);
    }

    @Test
    public void retrievePartsTree_WhenPartsTreeRequestValid_ReturnsProcessId() throws JsonProcessingException {
        // Arrange
        String serializedRequest = MAPPER.writeValueAsString(partsTreeRequest);

        when(jobOrchestrator.startJob(any(Map.class)))
                .thenReturn(okResponse());

        // Act
        var response = service.retrievePartsTree(partsTreeRequest);
        // Assert
        assertThat(response).isNotNull();
        // Verify that startJob got called with correct job parameters.
        verify(jobOrchestrator).startJob(jobDataCaptor.capture());

        var randomContainerName = jobDataCaptor.getValue().get(CONTAINER_NAME_KEY);
        var randomDestinationPath = jobDataCaptor.getValue().get(DESTINATION_PATH_KEY);
        assertThat(randomContainerName).isNotEmpty();
        assertThat(randomDestinationPath).isNotEmpty();
        assertThat(jobDataCaptor.getValue())
                .isEqualTo(Map.of(
                        PARTS_REQUEST_KEY, serializedRequest,
                        CONTAINER_NAME_KEY, randomContainerName,
                        DESTINATION_PATH_KEY, randomDestinationPath
                ));
    }

    private JobInitiateResponse okResponse() {
        return JobInitiateResponse.builder()
                .jobId(UUID.randomUUID().toString())
                .status(ResponseStatus.OK)
                .build();
    }
}
