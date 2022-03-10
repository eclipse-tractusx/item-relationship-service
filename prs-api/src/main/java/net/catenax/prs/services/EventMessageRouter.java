//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.services;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.prs.dtos.events.PartRelationshipsUpdateRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;

import static net.catenax.prs.configuration.MetricsConfiguration.MESSAGE_AGE;
import static net.catenax.prs.configuration.MetricsConfiguration.MESSAGE_PROCESSING_TIME;

/**
 * Kafka message consumer service, routing event messages by payload type.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@KafkaListener(topics = "${prs.kafkaTopic}")
public class EventMessageRouter {

    /**
     * Service for processing {@link PartRelationshipsUpdateRequest}s.
     */
    private final PartRelationshipUpdateProcessor updateProcessor;

    /**
     * A custom metric recording the age of received messages, i.e.
     * the duration between the time the message was published to
     * Kafka and the time at which it is received.
     */
    @Qualifier(MESSAGE_AGE)
    private final Timer messageAge;

    /**
     * A custom metric recording the time taken to process received
     * messages.
     */
    @Qualifier(MESSAGE_PROCESSING_TIME)
    private final Timer processingTime;

    /**
     * Route {@link PartRelationshipsUpdateRequest}s to processor.
     *
     * @param payload Payload from broker.
     * @param timestamp Timestamp of the record.
     */
    @KafkaHandler
    public void route(final @Payload @Valid PartRelationshipsUpdateRequest payload, final @Header(KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp) {
        log.info("PartRelationshipUpdateRequest event received.");
        processingTime.record(() -> updateProcessor.process(payload, recordTimestamp(timestamp)));
        log.info("Event processed.");
    }

    private Instant recordTimestamp(final Long timestamp) {
        final var instant = Instant.ofEpochMilli(timestamp);
        messageAge.record(Duration.between(instant, Instant.now()));
        return instant;
    }
}
