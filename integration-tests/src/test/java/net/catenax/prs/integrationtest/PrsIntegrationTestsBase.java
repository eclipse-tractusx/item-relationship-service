//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.integrationtest;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import net.catenax.prs.PrsApplication;
import net.catenax.prs.configuration.PrsConfiguration;
import net.catenax.prs.dtos.events.PartRelationshipsUpdateRequest;
import net.catenax.prs.entities.PartIdEntityPart;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.catenax.prs.testing.TestUtil.DATABASE_TESTCONTAINER;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Tag("IntegrationTests")
@TestPropertySource(properties = DATABASE_TESTCONTAINER)
@Import(PrsIntegrationTestsBase.KafkaTestContainersConfiguration.class)
@SpringBootTest(classes = {PrsApplication.class}, webEnvironment = RANDOM_PORT)
@DirtiesContext
public class PrsIntegrationTestsBase {

    /**
     * PRS Query path.
     */
    protected static final String PATH = "/api/v0.1/parts/{oneIDManufacturer}/{objectIDManufacturer}/partsTree";

    /**
     * PRS Path parameter name for specifying the part One Id.
     *
     * @see PartIdEntityPart#getOneIDManufacturer()
     */
    protected static final String ONE_ID_MANUFACTURER = "oneIDManufacturer";

    /**
     * PRS Path parameter name for specifying the part Object Id.
     *
     * @see PartIdEntityPart#getObjectIDManufacturer()
     */
    protected static final String OBJECT_ID_MANUFACTURER = "objectIDManufacturer";

    /**
     * PRS Query parameter name for selecting the query view.
     */
    protected static final String VIEW = "view";

    /**
     * Docker container image used to run Kafka Test Container.
     */
    private static final String KAFKA_TEST_CONTAINER_IMAGE = "confluentinc/cp-kafka:5.4.3";

    protected static final Faker faker = new Faker();

    /**
     * The first partition number for a Kafka topic. Partition 0 always exists, regardless
     * of the number of partitions in the topic.
     */
    private static final int FIRST_PARTITION = 0;

    /**
     * Value used to represent an empty key for a Kafka message.
     */
    private static final Object EMPTY_KEY = null;

    private static KafkaContainer kafka;

    @LocalServerPort
    private int port;

    protected final PartsTreeApiResponseMother expected = new PartsTreeApiResponseMother();

    /**
     * PRS configuration settings.
     */
    @Autowired
    protected PrsConfiguration configuration;

    @Autowired
    private KafkaOperations<Object, Object> kafkaOperations;

    @BeforeAll
    public static void initKafkaTestContainer() {
        kafka = new KafkaContainer(DockerImageName.parse(KAFKA_TEST_CONTAINER_IMAGE));
        kafka.start();
    }

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }

    @AfterAll
    public static void stopKafkaTestContainer() {
        kafka.stop();
    }

    /**
     * Publish update event to given kafka topic. Publish all events in a single
     * partition to ensure sequential processing, enabling test cases for
     * dead-lettering and duplicate processing.
     *
     * @param event Update event to be published.
     */
    protected void publishUpdateEvent(Object event) throws Exception {
        kafkaOperations.send(configuration.getKafkaTopic(), FIRST_PARTITION, EMPTY_KEY, event).get();
    }

    /**
     * Kafka test configuration is needed to use kafka test container
     */
    @TestConfiguration
    static class KafkaTestContainersConfiguration {

        @Bean
        public ConsumerFactory<Object, Object> consumerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, String.format("%s-%s", getClass().getName(), UUID.randomUUID()));
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
            props.put(JsonDeserializer.TRUSTED_PACKAGES, PartRelationshipsUpdateRequest.class.getPackageName());
            return new DefaultKafkaConsumerFactory<>(props);
        }

        @Bean
        public ProducerFactory<Object, Object> producerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            props.put(ProducerConfig.RETRIES_CONFIG, 3);
            return new DefaultKafkaProducerFactory<>(props);
        }
    }
}
