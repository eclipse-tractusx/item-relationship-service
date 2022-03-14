package net.catenax.irs.connector.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import net.catenax.irs.client.model.PartId;
import net.catenax.irs.connector.consumer.configuration.ConsumerConfiguration;
import net.catenax.irs.connector.consumer.registry.StubRegistryClient;
import net.catenax.irs.connector.requests.PartsTreeRequest;
import net.catenax.irs.connector.requests.PartsTreeByObjectIdRequest;
import net.catenax.irs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.schema.azure.AzureBlobStoreSchema;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.catenax.irs.connector.constants.IrsConnectorConstants.DATA_REQUEST_IRS_DESTINATION_PATH;
import static net.catenax.irs.connector.constants.IrsConnectorConstants.DATA_REQUEST_IRS_REQUEST_PARAMETERS;
import static net.catenax.irs.connector.constants.IrsConnectorConstants.IRS_REQUEST_ASSET_ID;
import static net.catenax.irs.connector.constants.IrsConnectorConstants.IRS_REQUEST_POLICY_ID;
import static net.catenax.irs.connector.consumer.service.DataRequestFactory.PARTIAL_PARTS_TREE_BLOB_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataRequestFactoryTest {

    static final ObjectMapper MAPPER = new ObjectMapper();
    Faker faker = new Faker();
    int depth = faker.number().numberBetween(5, 10);
    RequestMother generate = new RequestMother();
    PartsTreeByObjectIdRequest.PartsTreeByObjectIdRequestBuilder irsRequest = generate.request()
            .depth(depth);
    PartsTreeRequest partsTreeRequest = PartsTreeRequest.builder()
            .byObjectIdRequest(irsRequest.build())
            .build();
    PartId rootPartId = generate.partId();
    PartId partId = generate.partId();
    Monitor monitor = new ConsoleMonitor();
    String storageAccountName = faker.lorem().characters();
    String connectorAddress = faker.internet().url();
    ConsumerConfiguration configuration = ConsumerConfiguration.builder()
            .storageAccountName(storageAccountName)
            .build();
    DataRequestFactory sut;
    DataRequestFactory.RequestContext.RequestContextBuilder requestContextBuilder = DataRequestFactory.RequestContext.builder()
            .requestTemplate(partsTreeRequest)
            .depth(depth)
            .queriedPartId(rootPartId);
    @Mock
    private StubRegistryClient registryClient;

    @BeforeEach
    public void setUp() {
        sut = new DataRequestFactory(monitor, configuration, new JsonUtil(monitor), registryClient);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"www.connector.com"})
    void createRequests_WhenConnectorUrlSameAsPrevious_ReturnsEmpty(String connectorAddress) {
        when(registryClient.getUrl(partId)).thenReturn(Optional.ofNullable(connectorAddress));

        assertThat(sut.createRequests(requestContextBuilder.previousUrlOrNull(connectorAddress).build(), Stream.of(partId))).isEmpty();
    }

    @Test
    void createRequests_WhenConnectorUrlDifferentFromPrevious_ReturnsDataRequest() throws Exception {
        // Arrange
        when(registryClient.getUrl(partId))
                .thenReturn(Optional.of(connectorAddress));

        PartsTreeByObjectIdRequest expectedIrsRequest = partsTreeRequest.getByObjectIdRequest()
                .toBuilder()
                .oneIDManufacturer(partId.getOneIDManufacturer())
                .objectIDManufacturer(partId.getObjectIDManufacturer())
                .build();

        String serializedIrsRequest = MAPPER.writeValueAsString(expectedIrsRequest);
        var expectedRequest = DataRequest.Builder.newInstance()
                .id(faker.lorem().characters())
                .connectorAddress(connectorAddress)
                .processId(null)
                .protocol("ids-rest")
                .connectorId("consumer")
                .dataEntry(DataEntry.Builder.newInstance()
                        .id(IRS_REQUEST_ASSET_ID)
                        .policyId(IRS_REQUEST_POLICY_ID)
                        .build())
                .dataDestination(DataAddress.Builder.newInstance()
                        .type(AzureBlobStoreSchema.TYPE)
                        .property(AzureBlobStoreSchema.ACCOUNT_NAME, configuration.getStorageAccountName())
                        .build())
                .properties(Map.of(DATA_REQUEST_IRS_REQUEST_PARAMETERS, serializedIrsRequest, DATA_REQUEST_IRS_DESTINATION_PATH, PARTIAL_PARTS_TREE_BLOB_NAME
                ))
                .managedResources(true)
                .build();

        // Act
        var requests = sut.createRequests(requestContextBuilder
                .queryResultRelationships(Set.of(generate.relationship(rootPartId, partId)))
                .build(), Stream.of(partId))
                .collect(Collectors.toList());

        // Assert
        assertThat(requests)
                .singleElement()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedRequest);
    }

    @Test
    void createRequests_AdjustsDepth_By1() throws Exception {
        // Arrange
        String previousUrl = faker.internet().url();
        requestContextBuilder
                .previousUrlOrNull(previousUrl)
                .queryResultRelationships(Set.of(generate.relationship(rootPartId, partId)));

        // Assert
        when(registryClient.getUrl(partId))
                .thenReturn(Optional.of(connectorAddress));

        // Act
        assertThat(singleRequestProducedWithIrsRequest().getDepth()).isEqualTo(depth - 1);
    }

    @Test
    void createRequests_AdjustsDepth_By2() throws Exception {
        // Arrange
        PartId partId1 = generate.partId();
        String previousUrl = faker.internet().url();
        requestContextBuilder
                .previousUrlOrNull(previousUrl)
                .queryResultRelationships(Set.of(
                        generate.relationship(rootPartId, partId1),
                        generate.relationship(partId1, partId)
                ));

        when(registryClient.getUrl(partId))
                .thenReturn(Optional.of(connectorAddress));

        // Act
        assertThat(singleRequestProducedWithIrsRequest())
                .usingRecursiveComparison()
                .isEqualTo(irsRequest
                        .oneIDManufacturer(partId.getOneIDManufacturer())
                        .objectIDManufacturer(partId.getObjectIDManufacturer())
                        .depth(depth - 2)
                        .build());
    }

    @Test
    void createRequests_WhenDepthExhausted_ReturnsEmpty() {
        // Arrange
        requestContextBuilder
                .queryResultRelationships(Set.of(generate.relationship(rootPartId, partId)))
                .depth(1);

        // Act
        var requests = sut.createRequests(requestContextBuilder.build(), Stream.of(partId))
                .collect(Collectors.toList());

        // Assert
        assertThat(requests).isEmpty();
    }

    private PartsTreeByObjectIdRequest singleRequestProducedWithIrsRequest() throws JsonProcessingException {
        var requests = sut.createRequests(requestContextBuilder.build(), Stream.of(partId))
                .collect(Collectors.toList());
        assertThat(requests).singleElement();
        var request = requests.get(0);
        var newIrsRequest = request.getProperties().get(DATA_REQUEST_IRS_REQUEST_PARAMETERS);
        return MAPPER.readValue(newIrsRequest, PartsTreeByObjectIdRequest.class);
    }
}
