package net.catenax.prs.connector.consumer.service;

import com.github.javafaker.Faker;
import net.catenax.prs.client.model.PartId;
import net.catenax.prs.client.model.PartRelationship;
import net.catenax.prs.client.model.PartRelationshipsWithInfos;
import net.catenax.prs.connector.requests.PartsTreeRequest;
import net.catenax.prs.connector.requests.PartsTreeByObjectIdRequest;
import net.catenax.prs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.common.azure.BlobStoreApi;
import org.eclipse.dataspaceconnector.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.schema.azure.AzureBlobStoreSchema;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static net.catenax.prs.connector.constants.PrsConnectorConstants.DATA_REQUEST_PRS_DESTINATION_PATH;
import static net.catenax.prs.connector.constants.PrsConnectorConstants.DATA_REQUEST_PRS_REQUEST_PARAMETERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartsTreeRecursiveLogicTest {

    final RequestMother generate = new RequestMother();
    PartsTreeByObjectIdRequest request = generate.request().build();
    PartsTreeRequest partsTreeRequest = PartsTreeRequest.builder().byObjectIdRequest(request).build();
    PartId partId = toPartId(request);
    Faker faker = new Faker();
    Monitor monitor = new ConsoleMonitor();
    JsonUtil jsonUtil = new JsonUtil(monitor);
    String storageAccountName = faker.lorem().characters();
    String containerName = faker.lorem().word();
    String blobName = faker.lorem().word();
    String rootQueryConnectorAddress = faker.internet().url();
    DataRequestFactory.RequestContext.RequestContextBuilder requestContextBuilder = DataRequestFactory.RequestContext.builder()
            .queriedPartId(partId)
            .depth(request.getDepth())
            .requestTemplate(partsTreeRequest);
    PartsTreeRecursiveLogic sut;
    @Mock
    BlobStoreApi blobStoreApi;
    @Mock
    DataRequestFactory dataRequestFactory;
    @Mock
    PartsTreesAssembler assembler;
    @Mock
    DataRequest dataRequest;
    @Mock
    Stream<DataRequest> dataRequestStream;
    @Captor
    ArgumentCaptor<Stream<PartRelationshipsWithInfos>> partsTreesCaptor;
    @Captor
    ArgumentCaptor<Stream<PartId>> partIdsCaptor;
    @Captor
    ArgumentCaptor<DataRequestFactory.RequestContext> requestContextCaptor;

    @BeforeEach
    public void setUp() {
        sut = new PartsTreeRecursiveLogic(monitor, blobStoreApi, jsonUtil, dataRequestFactory, assembler);
    }

    @Test
    void createInitialPartsTreeRequest_WhenNoDataRequest_ReturnsEmptyStream() {
        // Arrange
        when(dataRequestFactory.createRequests(requestContextCaptor.capture(), partIdsCaptor.capture()))
                .thenReturn(Stream.empty());

        // Act
        var result = sut.createInitialPartsTreeRequest(partsTreeRequest);

        // Assert
        assertThat(result).isEmpty();
        assertThat(requestContextCaptor.getValue()).isEqualTo(requestContextBuilder.build());
        assertThat(partIdsCaptor.getValue()).containsExactly(partId);
    }

    @Test
    void createInitialPartsTreeRequest_WhenDataRequest_ReturnsStream() {
        // Arrange
        when(dataRequestFactory.createRequests(requestContextCaptor.capture(), partIdsCaptor.capture()))
                .thenReturn(Stream.of(dataRequest));

        // Act
        var result = sut.createInitialPartsTreeRequest(partsTreeRequest);

        // Assert
        assertThat(result).containsExactly(dataRequest);
        assertThat(requestContextCaptor.getValue()).isEqualTo(requestContextBuilder.build());
        assertThat(partIdsCaptor.getValue()).containsExactly(partId);
    }

    @Test
    void createSubsequentPartsTreeRequests() {
        // Arrange
        var transfer = transferProcess(blobName);
        var relationship = generate.relationship();
        var tree = generate.prsOutput().addRelationshipsItem(relationship);

        when(blobStoreApi.getBlob(storageAccountName, containerName, blobName)).thenReturn(serialize(tree));
        when(dataRequestFactory.createRequests(requestContextCaptor.capture(), partIdsCaptor.capture()))
                .thenReturn(dataRequestStream);

        // Act
        var result = sut.createSubsequentPartsTreeRequests(transfer, partsTreeRequest);

        // Assert
        assertThat(result).isSameAs(dataRequestStream);
        assertThat(requestContextCaptor.getValue()).isEqualTo(requestContextBuilder
                .previousUrlOrNull(rootQueryConnectorAddress)
                .queryResultRelationship(relationship)
                .build());
        assertThat(partIdsCaptor.getValue()).containsExactly(relationship.getChild());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void createSubsequentPartsTreeRequests_noRelationships(List<PartRelationship> partRelationships) {
        // Arrange
        var transfer = transferProcess(blobName);
        var tree = generatePrsOutput();
        tree.setRelationships(partRelationships);

        when(blobStoreApi.getBlob(storageAccountName, containerName, blobName)).thenReturn(serialize(tree));
        when(dataRequestFactory.createRequests(requestContextCaptor.capture(), partIdsCaptor.capture()))
                .thenReturn(dataRequestStream);

        // Act
        var result = sut.createSubsequentPartsTreeRequests(transfer, partsTreeRequest);

        // Assert
        assertThat(result).isSameAs(dataRequestStream);
        assertThat(requestContextCaptor.getValue()).isEqualTo(requestContextBuilder
                .previousUrlOrNull(rootQueryConnectorAddress)
                .build());
        assertThat(partIdsCaptor.getValue()).isEmpty();
    }

    @Test
    void assemblePartialPartTreeBlobs_WithNoInput() {
        // Arrange
        PartRelationshipsWithInfos prsOutput = generatePrsOutput();
        when(assembler.retrievePartsTrees(partsTreesCaptor.capture()))
                .thenReturn(prsOutput);

        // Act
        sut.assemblePartialPartTreeBlobs(List.of(), storageAccountName, containerName, blobName);

        // Assert
        assertThat(partsTreesCaptor.getValue()).isEmpty();
        verify(blobStoreApi).putBlob(storageAccountName, containerName, blobName, serialize(prsOutput));
    }

    private byte[] serialize(PartRelationshipsWithInfos prsOutput) {
        return jsonUtil.asString(prsOutput).getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void assemblePartialPartTreeBlobs_WithInput() {
        // Arrange
        var blob1 = faker.lorem().characters();
        var blob2 = faker.lorem().characters();
        var blob3 = faker.lorem().characters();
        var transfer1 = transferProcess(blob1);
        var transfer2 = transferProcess(blob2);
        var transfer3 = transferProcess(blob3);
        PartRelationshipsWithInfos prsOutput1 = generatePrsOutput();
        PartRelationshipsWithInfos prsOutput2 = generatePrsOutput();
        PartRelationshipsWithInfos prsOutput3 = generatePrsOutput();
        PartRelationshipsWithInfos prsOutput4 = generatePrsOutput();
        when(assembler.retrievePartsTrees(partsTreesCaptor.capture()))
                .thenReturn(prsOutput4);
        when(blobStoreApi.getBlob(storageAccountName, containerName, blob1))
                .thenReturn(serialize(prsOutput1));
        when(blobStoreApi.getBlob(storageAccountName, containerName, blob2))
                .thenReturn(serialize(prsOutput2));
        when(blobStoreApi.getBlob(storageAccountName, containerName, blob3))
                .thenReturn(serialize(prsOutput3));

        // Act
        sut.assemblePartialPartTreeBlobs(List.of(transfer1, transfer2, transfer3), storageAccountName, containerName, blobName);

        // Assert
        assertThat(partsTreesCaptor.getValue()).containsExactly(prsOutput1, prsOutput2, prsOutput3);
        verify(blobStoreApi).putBlob(storageAccountName, containerName, blobName, serialize(prsOutput4));
    }

    private TransferProcess transferProcess(String blobName) {
        return
                TransferProcess.Builder.newInstance()
                        .id(faker.lorem().characters())
                        .dataRequest(DataRequest.Builder.newInstance()
                                .connectorAddress(rootQueryConnectorAddress)
                                .dataDestination(DataAddress.Builder.newInstance()
                                        .type(AzureBlobStoreSchema.TYPE)
                                        .property(AzureBlobStoreSchema.ACCOUNT_NAME, storageAccountName)
                                        .property(AzureBlobStoreSchema.CONTAINER_NAME, containerName)
                                        .build())
                                .properties(Map.of(
                                        DATA_REQUEST_PRS_DESTINATION_PATH, blobName,
                                        DATA_REQUEST_PRS_REQUEST_PARAMETERS, jsonUtil.asString(request)
                                ))
                                .build())
                        .build();
    }

    private PartId toPartId(PartsTreeByObjectIdRequest partsTreeRequest) {
        var partId = new PartId();
        partId.setOneIDManufacturer(partsTreeRequest.getOneIDManufacturer());
        partId.setObjectIDManufacturer(partsTreeRequest.getObjectIDManufacturer());
        return partId;
    }

    private PartRelationshipsWithInfos generatePrsOutput() {
        return generate.prsOutput()
                .addRelationshipsItem(generate.relationship())
                .addPartInfosItem(generate.partInfo());
    }

}