//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.brokerproxy.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.brokerproxy.configuration.BrokerProxyConfiguration;
import net.catenax.brokerproxy.exceptions.MessageProducerFailedException;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

/**
 * Kafka message producer service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducerService {

    /**
     * Object allowing to send messages to the Kafka broker.
     */
    private final KafkaOperations<String, Object> kafka;

    /**
     * Kafka configuration.
     */
    private final BrokerProxyConfiguration configuration;

    /**
     * Send a message to the broker on a specific topic.
     *
     * @param message message to send.
     * @throws MessageProducerFailedException if message could not be delivered to the broker.
     */
    public void send(final Object message) {
        final var send = kafka.send(configuration.getKafkaTopic(), message);
        try {
            send.get();
            log.info("Sent message to broker");
        } catch (InterruptedException | ExecutionException e) {
            throw new MessageProducerFailedException(e);
        }
    }
}
