package net.catenax.prs.connector.consumer.registry;

import com.github.javafaker.Faker;
import net.catenax.prs.connector.consumer.configuration.PartitionDeploymentsConfig;
import net.catenax.prs.connector.consumer.configuration.PartitionsConfig;
import net.catenax.prs.connector.consumer.service.RequestMother;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class StubRegistryClientTest {

    RequestMother generate = new RequestMother();
    PartitionsConfig partitions = new PartitionsConfig();
    PartitionDeploymentsConfig attributes = new PartitionDeploymentsConfig();
    Faker faker = new Faker();
    PartitionsConfig.PartitionConfig partition1 = generatePartitionConfig();
    PartitionsConfig.PartitionConfig partition2 = generatePartitionConfig();
    PartitionDeploymentsConfig.PartitionAttributeCollection attributeColl = new PartitionDeploymentsConfig.PartitionAttributeCollection();
    PartitionDeploymentsConfig.PartitionAttribute value = new PartitionDeploymentsConfig.PartitionAttribute();

    @Test
    void constructor_WhenNoPartitions_Throws() {
        assertThatExceptionOfType(EdcConfigurationException.class)
                .isThrownBy(() -> new StubRegistryClient(partitions, attributes))
                .withMessage("No partitions defined");
    }

    @Test
    void constructor_WhenMissingAttributesEntry_Throws() {
        partitions.setPartitions(List.of(partition1, partition2));
        value.setValue(randomString());
        attributeColl.put("connector_url", value);
        attributes.put(partition1.getKey(), attributeColl);
        assertThatExceptionOfType(EdcConfigurationException.class)
                .isThrownBy(() -> new StubRegistryClient(partitions, attributes))
                .withMessage("Missing entry in partition attributes file: " + partition2.getKey());
    }

    @Test
    void constructor_WhenMissingApiUrl_Throws() {
        partitions.setPartitions(List.of(partition1, partition2));
        value.setValue(randomString());
        attributeColl.put("apI_url", value); // intentional wrong casing
        attributes.put(partition1.getKey(), attributeColl);
        attributes.put(partition2.getKey(), attributeColl);
        assertThatExceptionOfType(EdcConfigurationException.class)
                .isThrownBy(() -> new StubRegistryClient(partitions, attributes))
                .withMessage("Missing connector_url key in partition attributes file for " + partition1.getKey());
    }

    @Test
    void getUrl_WhenValid_Returns() {
        var partId = generate.partId();
        partition2.setOneIDs(List.of(randomString(), partId.getOneIDManufacturer()));
        partitions.setPartitions(List.of(partition1, partition2));
        var url = randomString();
        value.setValue(url);
        attributeColl.put("connector_url", value);
        attributes.put(partition1.getKey(), attributeColl);
        attributes.put(partition2.getKey(), attributeColl);
        var client = new StubRegistryClient(partitions, attributes);
        assertThat(client.getUrl(partId))
                .contains(url);
    }

    @Test
    void getUrl_WhenUnknownOneId_ReturnsEmpty() {
        var partId = generate.partId();
        partition2.setOneIDs(List.of(randomString(), partId.getOneIDManufacturer()));
        partitions.setPartitions(List.of(partition1, partition2));
        var url = randomString();
        value.setValue(url);
        attributeColl.put("connector_url", value);
        attributes.put(partition1.getKey(), attributeColl);
        attributes.put(partition2.getKey(), attributeColl);
        var client = new StubRegistryClient(partitions, attributes);
        assertThat(client.getUrl(generate.partId()))
                .isEmpty();
    }

    private PartitionsConfig.PartitionConfig generatePartitionConfig() {
        var partition1 = new PartitionsConfig.PartitionConfig();
        partition1.setKey(randomString());
        partition1.setOneIDs(List.of(randomString(), randomString()));
        return partition1;
    }

    private String randomString() {
        return faker.letterify("????????????????????");
    }
}