//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.brokerProxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import net.catenax.brokerproxy.BrokerProxyApplication;
import net.catenax.brokerproxy.configuration.BrokerProxyConfiguration;
import net.catenax.prs.testing.BaseDtoMother;
import net.catenax.prs.testing.DtoMother;
import net.catenax.prs.testing.UpdateRequestMother;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.AbstractJavaTypeMapper;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@Tag("IntegrationTests")
@Import(BrokerProxyIntegrationTestBase.KafkaTestContainersConfiguration.class)
@SpringBootTest(classes = {BrokerProxyApplication.class}, webEnvironment = RANDOM_PORT)
@DirtiesContext
abstract class BrokerProxyIntegrationTestBase {

    private static final String KAFKA_TEST_CONTAINER_IMAGE = "confluentinc/cp-kafka:5.4.3";
    private static final String KAFKA_AUTO_OFFSET_RESET_CONFIG = "earliest";
    protected static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Empty string as a constant.
     */
    protected static final String EMPTY = "";
    /**
     * This will match any of these whitespaces
     * e.g. space (_), the tab (\t), the new line (\n) and the carriage return (\r).
     */
    protected static final String WHITESPACE_REGEX = "\\s";

    static {
        /*
          jackson-datatype-jsr310 module is needed to support java.time.Instant.
         */
        objectMapper.registerModule(new JavaTimeModule());
    }

    private static KafkaContainer kafka;

    @LocalServerPort
    private int port;

    protected static final Faker faker = new Faker();

    /**
     * Object Mother to generate broker proxy response data for testing.
     */
    protected final BrokerProxyResponseMother generateResponse = new BrokerProxyResponseMother();
    /**
     * Object Mother to generate update api DTO data for testing.
     */
    protected static final UpdateRequestMother generate = new UpdateRequestMother();
    /**
     * Base Object Mother to generate core DTO data for testing.
     */
    protected static final BaseDtoMother generateBaseDto = new BaseDtoMother();
    /**
     * Object Mother to generate core DTO data for testing.
     */
    protected static final DtoMother generateDto = new DtoMother();
    /**
     * Broker proxy api configuration settings.
     */
    @Autowired
    protected BrokerProxyConfiguration configuration;

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
     * Subscribe to a kafka topic.
     * @param topic Kafka topic name.
     * @return Instance of created Kafka consumer. {@link KafkaConsumer}
     */
    protected KafkaConsumer<String, String> subscribe(final String topic) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfigs());
        consumer.subscribe(List.of(topic));
        return consumer;
    }

    /**
     * Provides invalid effect time test data.
     * @return Invalid effect time as {@link Stream} of {@link Arguments}.
     */
    protected static Stream<Arguments> provideInvalidEffectTime() {
        return Stream.of(
                Arguments.of(null, "effectTime:must not be null"),
                Arguments.of(faker.date().future(faker.number().randomDigitNotZero(), TimeUnit.DAYS).toInstant()
                        , "effectTime:must be a past date")
        );
    }

    /**
     * Kafka consumer configuration.
     * @return see {@link Map}
     */
    private Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KAFKA_AUTO_OFFSET_RESET_CONFIG);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return props;
    }

    /**
     * Kafka test configuration is needed to use kafka test container within {@link net.catenax.brokerproxy.services.MessageProducerService}
     */
    @TestConfiguration
    static class KafkaTestContainersConfiguration {

        @Bean
        public ProducerFactory<String, Object> producerFactory() {
            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return new DefaultKafkaProducerFactory<>(configProps);
        }

        @Bean
        public KafkaTemplate<String, Object> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }

    }

    protected <T, E> boolean hasExpectedBrokerEvent(T request, Class<E> valueType) throws Exception {
        var consumer = subscribe(configuration.getKafkaTopic());
        Instant afterTenSeconds = Instant.now().plusSeconds(10);
        boolean isEventMatched = false;
        while (Instant.now().isBefore(afterTenSeconds)) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {

                if (record.value() != null && Arrays.equals(record.headers().lastHeader(AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME).value(), valueType.getCanonicalName().getBytes(StandardCharsets.UTF_8))) {
                    E event = objectMapper.readValue(record.value(), valueType);

                    if (event.equals(request)) {
                        isEventMatched = true;
                        break;
                    }
                }
            }
        }

        consumer.close();

        return isEventMatched;
    }
}
