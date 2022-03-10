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

import lombok.Data;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 * Configuration settings for retrying incoming event processing.
 */
@Data
public class EventProcessingRetryConfiguration {
    /**
     * Default value for the maximum number of retries.
     */
    private static final int DEFAULT_MAX_RETRIES = 10;

    /**
     * Maximum number of retries.
     */
    private int maxRetries = DEFAULT_MAX_RETRIES;

    /**
     * Initial exponential back-off retry interval in milliseconds.
     */
    private long initialIntervalMilliseconds = ExponentialBackOffWithMaxRetries.DEFAULT_INITIAL_INTERVAL;

    /**
     * Exponential back-off retry multiplier (e.g. 1.5 increases the interval by 50%).
     */
    private double multiplier = ExponentialBackOffWithMaxRetries.DEFAULT_MULTIPLIER;

    /**
     * Exponential back-off retry * maximum time.
     */
    private long maxIntervalMilliseconds = ExponentialBackOffWithMaxRetries.DEFAULT_MAX_INTERVAL;
}
