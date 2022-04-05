package net.catenax.irs.connector.consumer.service;

import com.github.javafaker.Faker;
import net.catenax.irs.client.model.PartId;
import net.catenax.irs.client.model.PartRelationship;
import net.catenax.irs.client.model.PartRelationshipsWithInfos;
import net.catenax.irs.connector.consumer.persistence.BlobPersistence;
import net.catenax.irs.connector.consumer.persistence.BlobPersistenceException;
import net.catenax.irs.connector.requests.PartsTreeRequest;
import net.catenax.irs.connector.requests.PartsTreeByObjectIdRequest;
import net.catenax.irs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.monitor.ConsoleMonitor;
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

import static net.catenax.irs.connector.constants.IrsConnectorConstants.DATA_REQUEST_IRS_DESTINATION_PATH;
import static net.catenax.irs.connector.constants.IrsConnectorConstants.DATA_REQUEST_IRS_REQUEST_PARAMETERS;
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
    BlobPersistence blobStoreApi;
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
    void createSubsequentPartsTreeRequests() throws BlobPersistenceException {
        // Arrange
        var transfer = transferProcess(blobName);
        var relationship = generate.relationship();
        var tree = generate.irsOutput().addRelationshipsItem(relationship);

        when(blobStoreApi.getBlob(blobName)).thenReturn(serialize(tree));
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
    void createSubsequentPartsTreeRequests_noRelationships(List<PartRelationship> partRelationships)
          throws BlobPersistenceException {
        // Arrange
        var transfer = transferProcess(blobName);
        var tree = generateIrsOutput();
        tree.setRelationships(partRelationships);

        when(blobStoreApi.getBlob(blobName)).thenReturn(serialize(tree));
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
    void assemblePartialPartTreeBlobs_WithNoInput() throws BlobPersistenceException {
        // Arrange
        PartRelationshipsWithInfos irsOutput = generateIrsOutput();
        when(assembler.retrievePartsTrees(partsTreesCaptor.capture()))
                .thenReturn(irsOutput);

        // Act
        sut.assemblePartialPartTreeBlobs(List.of(), blobName);

        // Assert
        assertThat(partsTreesCaptor.getValue()).isEmpty();
        verify(blobStoreApi).putBlob(blobName, serialize(irsOutput));
    }

    private byte[] serialize(PartRelationshipsWithInfos irsOutput) {
        return jsonUtil.asString(irsOutput).getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void assemblePartialPartTreeBlobs_WithInput() throws BlobPersistenceException {
        // Arrange
        var blob1 = faker.lorem().characters();
        var blob2 = faker.lorem().characters();
        var blob3 = faker.lorem().characters();
        var transfer1 = transferProcess(blob1);
        var transfer2 = transferProcess(blob2);
        var transfer3 = transferProcess(blob3);
        PartRelationshipsWithInfos irsOutput1 = generateIrsOutput();
        PartRelationshipsWithInfos irsOutput2 = generateIrsOutput();
        PartRelationshipsWithInfos irsOutput3 = generateIrsOutput();
        PartRelationshipsWithInfos irsOutput4 = generateIrsOutput();
        when(assembler.retrievePartsTrees(partsTreesCaptor.capture()))
                .thenReturn(irsOutput4);
        when(blobStoreApi.getBlob( blob1))
                .thenReturn(serialize(irsOutput1));
        when(blobStoreApi.getBlob(blob2))
                .thenReturn(serialize(irsOutput2));
        when(blobStoreApi.getBlob(blob3))
                .thenReturn(serialize(irsOutput3));

        // Act
        sut.assemblePartialPartTreeBlobs(List.of(transfer1, transfer2, transfer3), blobName);

        // Assert
        assertThat(partsTreesCaptor.getValue()).containsExactly(irsOutput1, irsOutput2, irsOutput3);
        verify(blobStoreApi).putBlob(blobName, serialize(irsOutput4));
    }

    private TransferProcess transferProcess(String blobName) {
        return
                TransferProcess.Builder.newInstance()
                        .id(faker.lorem().characters())
                        .dataRequest(DataRequest.Builder.newInstance()
                                .connectorAddress(rootQueryConnectorAddress)
                                .dataDestination(DataAddress.Builder.newInstance().build())
                                .properties(Map.of(DATA_REQUEST_IRS_DESTINATION_PATH, blobName,
                                      DATA_REQUEST_IRS_REQUEST_PARAMETERS, jsonUtil.asString(request)
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

    private PartRelationshipsWithInfos generateIrsOutput() {
        return generate.irsOutput()
                .addRelationshipsItem(generate.relationship())
                .addPartInfosItem(generate.partInfo());
    }

}