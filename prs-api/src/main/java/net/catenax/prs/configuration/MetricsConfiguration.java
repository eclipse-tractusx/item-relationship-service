//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans for capturing application custom metrics.
 */
@Configuration
@RequiredArgsConstructor
public class MetricsConfiguration {
    /**
     * {@link Qualifier} value to inject {@link #messageAge()} meter.
     */
    public static final String MESSAGE_AGE = "message_age";

    /**
     * {@link Qualifier} value to inject {@link #processingTime()} meter.
     */
    public static final String MESSAGE_PROCESSING_TIME = "message_processing_time";

    /**
     * Registry for publishing custom metrics.
     */
    private final MeterRegistry registry;

    /**
     * A custom meter recording the age of received messages, i.e. the duration between the time the
     * message was published to Kafka and the time at which it is received.
     *
     * @return Guaranteed to never return {@literal null}.
     */
    @Bean
    @Qualifier(MESSAGE_AGE)
    public Timer messageAge() {
        return Timer
                .builder("message_age")
                .description("Age of received messages")
                .publishPercentileHistogram()
                .register(registry);
    }

    /**
     * A custom meter recording the time taken to process received messages.
     *
     * @return Guaranteed to never return {@literal null}.
     */
    @Bean
    @Qualifier(MESSAGE_PROCESSING_TIME)
    public Timer processingTime() {
        return Timer
                .builder("message_processing_time")
                .description("Time to process messages")
                .publishPercentileHistogram()
                .register(registry);
    }
}
