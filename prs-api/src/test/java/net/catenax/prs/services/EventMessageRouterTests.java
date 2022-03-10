package net.catenax.prs.services;

import com.github.javafaker.Faker;
import io.micrometer.core.instrument.Timer;
import net.catenax.prs.dtos.events.PartRelationshipsUpdateRequest;
import net.catenax.prs.testing.UpdateRequestMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static java.util.concurrent.TimeUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventMessageRouterTests {

    @Mock
    PartRelationshipUpdateProcessor updateProcessor;

    @Mock
    Timer messageAge;

    @Mock
    Timer processingTime;

    // @InjectMocks is not used because of ambiguous Timer parameter
    EventMessageRouter sut;

    @Captor
    ArgumentCaptor<Runnable> callbackCaptor;

    @Captor
    ArgumentCaptor<Duration> durationCaptor;

    Faker faker = new Faker();
    UpdateRequestMother generate = new UpdateRequestMother();
    PartRelationshipsUpdateRequest relationshipUpdate = generate.partRelationshipUpdateList();
    Instant timestamp = faker.date().past(10, DAYS).toInstant();

    @BeforeEach
    void setUp() {
        sut = new EventMessageRouter(updateProcessor, messageAge, processingTime);
    }

    @Test
    void consumePartRelationshipUpdateEvent() {
        // Act
        sut.route(relationshipUpdate, timestamp.toEpochMilli());

        // Assert
        verify(processingTime).record(callbackCaptor.capture());
        callbackCaptor.getValue().run();

        verify(updateProcessor).process(relationshipUpdate, timestamp);
    }

    @Test
    void consumePartRelationshipUpdateEvent_ProducesMetrics() {
        // Act
        sut.route(relationshipUpdate, timestamp.toEpochMilli());

        // Assert
        verify(processingTime).record(callbackCaptor.capture());

        var before = Instant.now();
        callbackCaptor.getValue().run();
        var after = Instant.now();

        verify(messageAge).record(durationCaptor.capture());

        assertThat(durationCaptor.getValue()).isBetween(
                Duration.between(timestamp, before),
                Duration.between(timestamp, after));
    }
}
