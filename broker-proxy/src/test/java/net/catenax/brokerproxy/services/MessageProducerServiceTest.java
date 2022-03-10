package net.catenax.brokerproxy.services;

import com.github.javafaker.Faker;
import net.catenax.brokerproxy.configuration.BrokerProxyConfiguration;
import net.catenax.brokerproxy.exceptions.MessageProducerFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.scheduling.annotation.AsyncResult;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageProducerServiceTest {

    @Mock
    KafkaOperations<String, Object> kafka;

    @Mock
    BrokerProxyConfiguration configuration;

    @InjectMocks
    MessageProducerService sut;

    Object message = new Object();
    Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        when(configuration.getKafkaTopic()).thenReturn(faker.lorem().word());
        when(kafka.send(any(), any())).thenReturn(AsyncResult.forValue(null));
    }

    @Test
    void sendPartRelationshipUpdateList_sendsMessageToBroker() {

        // Act
        sut.send(message);

        // Assert
        verify(kafka).send(configuration.getKafkaTopic(), message);
    }

    @Test
    void send_onKafkaFailure_Throws() {
        // Arrange
        verifyExceptionThrownOnFailure(new ExecutionException(new IOException()));
    }

    @Test
    void send_onInterrupted_Throws() {
        // Arrange
        verifyExceptionThrownOnFailure(new InterruptedException());
    }

    private void verifyExceptionThrownOnFailure(Throwable exception) {
        //Arrange
        when(kafka.send(any(), any())).thenReturn(AsyncResult.forExecutionException(exception));

        // Act
        assertThatExceptionOfType(MessageProducerFailedException.class).isThrownBy(() ->
                sut.send(message));
    }
}
