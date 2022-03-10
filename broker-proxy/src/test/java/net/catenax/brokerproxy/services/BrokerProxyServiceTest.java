package net.catenax.brokerproxy.services;

import com.github.javafaker.Faker;
import io.micrometer.core.instrument.DistributionSummary;
import net.catenax.brokerproxy.exceptions.MessageProducerFailedException;
import net.catenax.prs.dtos.events.PartAspectsUpdateRequest;
import net.catenax.prs.dtos.events.PartAttributeUpdateRequest;
import net.catenax.prs.dtos.events.PartRelationshipUpdate;
import net.catenax.prs.dtos.events.PartRelationshipsUpdateRequest;
import net.catenax.prs.testing.UpdateRequestMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BrokerProxyServiceTest {

    @Mock
    MessageProducerService producerService;

    @Mock
    DistributionSummary uploadedBomSize;

    @InjectMocks
    BrokerProxyService sut;

    Faker faker = new Faker();
    UpdateRequestMother generate = new UpdateRequestMother();
    PartRelationshipsUpdateRequest partRelationshipUpdateRequest = generate.partRelationshipUpdateList();
    PartAspectsUpdateRequest partAspectUpdateRequest = generate.partAspectUpdate();
    PartAttributeUpdateRequest partAttributeUpdateRequest = generate.partAttributeUpdate();

    @Test
    void send_PartRelationshipUpdateList_sendsMessageToBroker() {

        // Act
        sut.send(partRelationshipUpdateRequest);

        // Assert
        verify(producerService).send(argThat(this::isExpectedBrokerMessageForRelationshipUpdate));
    }

    @Test
    void send_PartRelationshipUpdateList_onProducerException_Throws() {

        // Arrange
        doThrow(new MessageProducerFailedException(new InterruptedException()))
                .when(producerService).send(any());

        // Act
        assertThatExceptionOfType(MessageProducerFailedException.class).isThrownBy(() ->
                sut.send(partRelationshipUpdateRequest));
    }

    @Test
    void send_PartRelationshipUpdateList_recordsBomSize() {
        // Arrange
        final var bomSize = faker.number().numberBetween(0, 3);
        final var dto = generate.partRelationshipUpdateList(
                IntStream.range(0, bomSize)
                        .mapToObj(i -> generate.partRelationshipUpdate())
                        .toArray(PartRelationshipUpdate[]::new));

        // Act
        sut.send(dto);

        // Assert
        verify(uploadedBomSize).record(bomSize);
    }


    @Test
    void send_PartAspectUpdate_sendsMessage() {

        // Act
        sut.send(partAspectUpdateRequest);

        // Assert
        verify(producerService).send(argThat(this::isExpectedBrokerMessageForAspectUpdate));
    }

    @Test
    void send_PartAttributeUpdate_sendsMessage() {

        // Act
        sut.send(partAttributeUpdateRequest);

        // Assert
        verify(producerService).send(argThat(this::isExpectedBrokerMessageForAttributeUpdate));
    }

    private boolean isExpectedBrokerMessageForRelationshipUpdate(PartRelationshipsUpdateRequest event) {
        assertThat(event.getRelationships()).isNotEmpty();
        var eventData = event.getRelationships().get(0);
        assertThat(eventData.getRelationship()).isEqualTo(partRelationshipUpdateRequest.getRelationships().get(0).getRelationship());
        assertThat(eventData.getEffectTime()).isEqualTo(partRelationshipUpdateRequest.getRelationships().get(0).getEffectTime());
        assertThat(eventData.getStage()).isEqualTo(partRelationshipUpdateRequest.getRelationships().get(0).getStage());
        assertThat(eventData.isRemove()).isFalse();
        return true;
    }

    private boolean isExpectedBrokerMessageForAspectUpdate(PartAspectsUpdateRequest event) {
        assertThat(event.getAspects()).isEqualTo(partAspectUpdateRequest.getAspects());
        assertThat(event.getPart()).isEqualTo(partAspectUpdateRequest.getPart());
        assertThat(event.getEffectTime()).isEqualTo(partAspectUpdateRequest.getEffectTime());
        assertThat(event.isRemove()).isFalse();
        return true;
    }

    private boolean isExpectedBrokerMessageForAttributeUpdate(PartAttributeUpdateRequest event) {
        assertThat(event.getPart()).isEqualTo(partAttributeUpdateRequest.getPart());
        assertThat(event.getEffectTime()).isEqualTo(partAttributeUpdateRequest.getEffectTime());
        assertThat(event.getName()).isEqualTo(partAttributeUpdateRequest.getName());
        assertThat(event.getValue()).isEqualTo(partAttributeUpdateRequest.getValue());
        return true;
    }
}