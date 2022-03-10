package net.catenax.prs.connector.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import net.catenax.prs.client.ApiException;
import net.catenax.prs.client.api.PartsRelationshipServiceApi;
import net.catenax.prs.client.model.PartInfo;
import net.catenax.prs.client.model.PartRelationshipsWithInfos;
import net.catenax.prs.connector.requests.PartsTreeByObjectIdRequest;
import org.eclipse.dataspaceconnector.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowInitiateResponse;
import org.eclipse.dataspaceconnector.spi.transfer.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.catenax.prs.connector.constants.PrsConnectorConstants.DATA_REQUEST_PRS_DESTINATION_PATH;
import static net.catenax.prs.connector.constants.PrsConnectorConstants.DATA_REQUEST_PRS_REQUEST_PARAMETERS;
import static net.catenax.prs.connector.constants.PrsConnectorConstants.PRS_REQUEST_ASSET_ID;
import static net.catenax.prs.connector.constants.PrsConnectorConstants.PRS_REQUEST_POLICY_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartsRelationshipServiceApiToFileFlowControllerTest {

    @Spy
    Monitor monitor = new ConsoleMonitor();

    @Mock
    PartsRelationshipServiceApi client;

    @Mock
    BlobStorageClient blobStorageClient;

    @Mock
    Vault vault;

    @InjectMocks
    PartsRelationshipServiceApiToFileFlowController sut;

    Faker faker = new Faker();

    static final ObjectMapper MAPPER = new ObjectMapper();

    @ParameterizedTest
    @CsvSource({
            "AzureStorage,true",
            "azurestorage,true",
            "AzureStorag,false",
            "dummy,false"
    })
    void canHandle(String type, boolean expected) {
        var dataRequest = DataRequest.Builder.newInstance()
                .dataDestination(DataAddress.Builder.newInstance()
                        .type(type)
                        .build())
                .build();
        assertThat(sut.canHandle(dataRequest)).isEqualTo(expected);
    }

    @Test
    void initiateFlow() throws Exception {
        // Arrange
        PartsTreeByObjectIdRequest request = generateApiRequest();
        PartRelationshipsWithInfos response = generateApiResponse();
        String serializedRequest = MAPPER.writeValueAsString(request);
        String serializedResponse = MAPPER.writeValueAsString(response);
        String destinationPath = faker.lorem().word();
        String keyName = faker.lorem().word();

        DataRequest dataRequest = generateDataRequest(serializedRequest, destinationPath, keyName);

        whenApiCalledWith(request).thenReturn(response);

        // Act
        DataFlowInitiateResponse dataFlowInitiateResponse = sut.initiateFlow(dataRequest);

        //Assert
        assertThat(dataFlowInitiateResponse)
                .usingRecursiveComparison()
                .isEqualTo(DataFlowInitiateResponse.OK);

        verify(blobStorageClient).writeToBlob(dataRequest.getDataDestination(), destinationPath, serializedResponse);
    }

    @Test
    void initiateFlow_WhenInvalidRequestPayload_Fail() throws Exception {
        // Arrange
        var dataRequest = generateDataRequest("{" + MAPPER.writeValueAsString(generateApiRequest()), faker.lorem().word(), faker.lorem().word());

        // Act
        DataFlowInitiateResponse response = sut.initiateFlow(dataRequest);

        // Assert
        assertThat(response)
            .hasFieldOrPropertyWithValue("status", ResponseStatus.FATAL_ERROR)
            .satisfies(c -> assertThat(c.getError()).startsWith("Error deserializing"));
    }

    @Test
    void initiateFlow_WhenApiCallFails_Fail() throws Exception {
        // Arrange
        PartsTreeByObjectIdRequest request = generateApiRequest();
        String apiMessage = faker.lorem().sentence();
        DataRequest dataRequest = generateDataRequest(MAPPER.writeValueAsString(request), faker.lorem().word(), faker.lorem().word());

        whenApiCalledWith(request).thenThrow(new ApiException(apiMessage));

        // Act
        DataFlowInitiateResponse response = sut.initiateFlow(dataRequest);

        // Assert
        assertThat(response)
                .hasFieldOrPropertyWithValue("status", ResponseStatus.FATAL_ERROR)
                .hasFieldOrPropertyWithValue("error", "Error with API call: " + apiMessage);
    }

    @Test
    void initiateFlow_WhenApiCallReturnsInvalidPayload_Fail() throws Exception {
        // Arrange
        PartsTreeByObjectIdRequest request = generateApiRequest();
        DataRequest dataRequest = generateDataRequest(MAPPER.writeValueAsString(request), faker.lorem().word(), faker.lorem().word());

        whenApiCalledWith(request).thenReturn(mock(PartRelationshipsWithInfos.class));

        // Act
        DataFlowInitiateResponse response = sut.initiateFlow(dataRequest);

        // Assert
        assertThat(response)
                .hasFieldOrPropertyWithValue("status", ResponseStatus.FATAL_ERROR)
                .satisfies(c -> assertThat(c.getError()).startsWith("Error serializing API response:"));
    }

    @Test
    void initiateFlow_WhenWriteBlobError_Fail() throws Exception {
        // Arrange
        PartsTreeByObjectIdRequest request = generateApiRequest();
        PartRelationshipsWithInfos response = generateApiResponse();
        String serializedRequest = MAPPER.writeValueAsString(request);
        String serializedResponse = MAPPER.writeValueAsString(response);
        String destinationPath = faker.lorem().word();
        String keyName = faker.lorem().word();
        String message = faker.lorem().sentence();

        DataRequest dataRequest = generateDataRequest(serializedRequest, destinationPath, keyName);

        whenApiCalledWith(request).thenReturn(response);
        doThrow(new EdcException(message)).when(blobStorageClient).writeToBlob(dataRequest.getDataDestination(), destinationPath, serializedResponse);

        // Act
        DataFlowInitiateResponse dataFlowInitiateResponse = sut.initiateFlow(dataRequest);

        //Assert
        assertThat(dataFlowInitiateResponse)
                .hasFieldOrPropertyWithValue("status", ResponseStatus.FATAL_ERROR)
                .satisfies(c -> assertThat(c.getError()).isEqualTo("Data transfer to Azure Blob Storage failed"));
    }

    private DataRequest generateDataRequest(String requestParameters, String destinationPath, String keyName) {
        return DataRequest.Builder.newInstance()
                .id(UUID.randomUUID().toString()) // This is not relevant, thus can be random.
                .protocol("ids-rest") // Must be ids-rest.
                .connectorId("consumer")
                .dataEntry(DataEntry.Builder.newInstance()
                        .id(PRS_REQUEST_ASSET_ID)
                        .policyId(PRS_REQUEST_POLICY_ID)
                        .build())
                .dataDestination(DataAddress.Builder.newInstance()
                        .type("AzureStorage") // The provider uses this to select the correct DataFlowController.
                        .keyName(keyName)
                        .build())
                .properties(Map.of(
                        DATA_REQUEST_PRS_REQUEST_PARAMETERS, requestParameters,
                        DATA_REQUEST_PRS_DESTINATION_PATH, destinationPath
                ))
                .managedResources(true) // We do not need any provisioning.
                .build();
    }

    private PartsTreeByObjectIdRequest generateApiRequest() {
        return PartsTreeByObjectIdRequest.builder()
                .oneIDManufacturer(faker.lorem().characters())
                .objectIDManufacturer(faker.lorem().characters())
                .view(faker.lorem().word())
                .build();
    }

    private PartRelationshipsWithInfos generateApiResponse() {
        PartInfo partInfo = new PartInfo();
        partInfo.setPartTypeName(faker.lorem().word());
        var response = new PartRelationshipsWithInfos();
        response.setPartInfos(List.of(partInfo));
        return response;
    }

    private OngoingStubbing<PartRelationshipsWithInfos> whenApiCalledWith(PartsTreeByObjectIdRequest request) throws ApiException {
        return when(client.getPartsTreeByOneIdAndObjectId(request.getOneIDManufacturer(), request.getObjectIDManufacturer(),
                request.getView(), request.getAspect(), request.getDepth()));
    }

}