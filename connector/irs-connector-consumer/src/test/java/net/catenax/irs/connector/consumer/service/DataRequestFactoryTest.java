package net.catenax.irs.connector.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import net.catenax.irs.component.ChildItem;
import net.catenax.irs.connector.consumer.configuration.ConsumerConfiguration;
import net.catenax.irs.connector.consumer.registry.StubRegistryClient;
import net.catenax.irs.connector.requests.JobsTreeByCatenaXIdRequest;
import net.catenax.irs.connector.requests.JobsTreeRequest;
import net.catenax.irs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.catenax.irs.connector.constants.IrsConnectorConstants.*;
import static net.catenax.irs.connector.consumer.service.DataRequestFactory.PARTIAL_PARTS_TREE_BLOB_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Disabled
@ExtendWith(MockitoExtension.class)
class DataRequestFactoryTest {

    static final ObjectMapper MAPPER = new ObjectMapper();
    Faker faker = new Faker();
    int depth = faker.number().numberBetween(5, 10);
    RequestMother generate = new RequestMother();
    JobsTreeByCatenaXIdRequest.JobsTreeByCatenaXIdRequestBuilder irsRequest = generate.request()
            .depth(depth);
    JobsTreeRequest jobsTreeRequest = JobsTreeRequest.builder()
            .byObjectIdRequest(irsRequest.build())
            .build();
    ChildItem childItem = generate.child();
    ChildItem parentItem = generate.child();

    Monitor monitor = new ConsoleMonitor();
    String storageAccountName = faker.lorem().characters();
    String connectorAddress = faker.internet().url();
    ConsumerConfiguration configuration = ConsumerConfiguration.builder()
            .storageAccountName(storageAccountName)
            .build();
    DataRequestFactory sut;
    DataRequestFactory.RequestContext.RequestContextBuilder requestContextBuilder = DataRequestFactory.RequestContext.builder()
            .requestTemplate(jobsTreeRequest)
            .depth(depth);
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
        when(registryClient.getUrl(childItem)).thenReturn(Optional.ofNullable(connectorAddress));

        assertThat(sut.createRequests(requestContextBuilder.previousUrlOrNull(connectorAddress).build(), childItem)).isEmpty();
    }

    @Test
    void createRequests_WhenConnectorUrlDifferentFromPrevious_ReturnsDataRequest() throws Exception {
        // Arrange
        when(registryClient.getUrl(childItem))
                .thenReturn(Optional.of(connectorAddress));

        JobsTreeByCatenaXIdRequest expectedIrsRequest = jobsTreeRequest.getByObjectIdRequest()
                .toBuilder()
                .childCatenaXId(childItem.getChildCatenaXId())
                .lastModifiedOn(LocalDateTime.ofInstant(childItem.getLastModifiedOn(), ZoneId.of("Europe/London")))
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
                        .type("dummyType")
                        .build())
                .properties(Map.of(DATA_REQUEST_IRS_REQUEST_PARAMETERS, serializedIrsRequest, DATA_REQUEST_IRS_DESTINATION_PATH, PARTIAL_PARTS_TREE_BLOB_NAME
                ))
                .managedResources(true)
                .build();

        // Act
        var requests = sut.createRequests(requestContextBuilder
                        .relationships(Set.of(generate.relationship(parentItem, childItem)))
                .build(), childItem)
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
                .relationships(Set.of(generate.relationship(parentItem, childItem)));

        // Assert
        when(registryClient.getUrl(childItem))
                .thenReturn(Optional.of(connectorAddress));

        // Act
        assertThat(singleRequestProducedWithIrsRequest().getDepth()).isEqualTo(depth - 1);
    }

    @Test
    void createRequests_AdjustsDepth_By2() throws Exception {
        // Arrange
        ChildItem childItem1 = generate.child();
        String previousUrl = faker.internet().url();
        requestContextBuilder
                .previousUrlOrNull(previousUrl)
                .relationships(Set.of(
                        generate.relationship(parentItem, childItem),
                        generate.relationship(childItem1, childItem)
                ));

        when(registryClient.getUrl(childItem))
                .thenReturn(Optional.of(connectorAddress));

        // Act
        assertThat(singleRequestProducedWithIrsRequest())
                .usingRecursiveComparison()
                .isEqualTo(irsRequest
                        .childCatenaXId(childItem.getChildCatenaXId())
                        .depth(depth - 2)
                        .build());
    }

    @Test
    void createRequests_WhenDepthExhausted_ReturnsEmpty() {
        // Arrange
        requestContextBuilder
                .relationships(Set.of(generate.relationship(childItem, parentItem)))
                .depth(1);

        // Act
        var requests = sut.createRequests(requestContextBuilder.build(), childItem)
                .collect(Collectors.toList());

        // Assert
        assertThat(requests).isEmpty();
    }

    private JobsTreeByCatenaXIdRequest singleRequestProducedWithIrsRequest() throws JsonProcessingException {
        var requests = sut.createRequests(requestContextBuilder.build(), childItem)
                .collect(Collectors.toList());
        assertThat(requests).singleElement();
        var request = requests.get(0);
        var newIrsRequest = request.getProperties().get(DATA_REQUEST_IRS_REQUEST_PARAMETERS);
        return MAPPER.readValue(newIrsRequest, JobsTreeByCatenaXIdRequest.class);
    }
}
