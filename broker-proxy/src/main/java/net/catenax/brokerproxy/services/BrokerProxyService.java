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

import io.micrometer.core.instrument.DistributionSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.brokerproxy.exceptions.MessageProducerFailedException;
import net.catenax.prs.dtos.events.PartAspectsUpdateRequest;
import net.catenax.prs.dtos.events.PartAttributeUpdateRequest;
import net.catenax.prs.dtos.events.PartRelationshipsUpdateRequest;
import org.springframework.stereotype.Service;

/**
 * Broker proxy service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrokerProxyService {

    /**
     * Kafka message producer service.
     */
    private final MessageProducerService producerService;

    /**
     * A custom metric recording the number of items
     * in uploaded {@link PartRelationshipsUpdateRequest} messages.
     */
    private final DistributionSummary uploadedBomSize;

    /**
     * Send a {@link PartRelationshipsUpdateRequest} to the broker.
     *
     * @param updateRelationships message to send.
     * @throws MessageProducerFailedException if message could not be delivered to the broker.
     */
    public void send(final PartRelationshipsUpdateRequest updateRelationships) {
        uploadedBomSize.record(updateRelationships.getRelationships().size());

        log.info("Sending PartRelationshipUpdateList to broker");
        producerService.send(updateRelationships);
        log.info("Sent PartRelationshipUpdateList to broker");
    }

    /**
     * Send a {@link PartAspectsUpdateRequest} to the broker.
     *
     * @param updateAspect message to send.
     * @throws MessageProducerFailedException if message could not be delivered to the broker.
     */
    public void send(final PartAspectsUpdateRequest updateAspect) {
        log.info("Sending PartAspectUpdate to broker");
        producerService.send(updateAspect);
        log.info("Sent PartAspectUpdate to broker");
    }

    /**
     * Send a {@link PartAttributeUpdateRequest} to the broker.
     *
     * @param updateAttribute message to send.
     * @throws MessageProducerFailedException if message could not be delivered to the broker.
     */
    public void send(final PartAttributeUpdateRequest updateAttribute) {
        log.info("Sending PartAttributeUpdate to broker");
        producerService.send(updateAttribute);
        log.info("Sent PartAttributeUpdate to broker");
    }
}
