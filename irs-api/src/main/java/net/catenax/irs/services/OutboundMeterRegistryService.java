//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.services;

import java.util.HashMap;
import java.util.Map;

import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

/**
 * Registering outbound connections metrics for the IRS
 */
@Service
public class OutboundMeterRegistryService {

    private static final String ENDPOINT_SUBMODEL = "submodel";
    private static final String ENDPOINT_REGISTRY = "registry";
    private static final String TAG_ENDPOINT = "endpoint";
    private final Counter counterRetriesRegistry;

    private final Counter counterTimeoutsRegistry;
    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> counterTimeoutsSubmodel;

    /* package */ OutboundMeterRegistryService(final MeterRegistry meterRegistry, final RetryRegistry retryRegistry) {
        this.counterRetriesRegistry = Counter.builder("http.requests.retries")
                                             .description("The total number of retries.")
                                             .tag(TAG_ENDPOINT, ENDPOINT_REGISTRY)
                                             .register(meterRegistry);
        this.counterTimeoutsRegistry = Counter.builder("http.requests.timeouts")
                                              .description("The total number of timeouts.")
                                              .tag(TAG_ENDPOINT, ENDPOINT_REGISTRY)
                                              .register(meterRegistry);
        this.meterRegistry = meterRegistry;

        counterTimeoutsSubmodel = new HashMap<>();

        retryRegistry.retry(ENDPOINT_REGISTRY).getEventPublisher().onRetry(event -> counterRetriesRegistry.increment());

        retryRegistry.getAllRetries()
                     .filter(retry -> !retry.getName().equals(ENDPOINT_REGISTRY))
                     .forEach(retry -> retry.getEventPublisher()
                                            .onRetry(event -> incrementSubmodelRetryCounter(retry.getName())));

    }

    public void incrementRegistryTimeoutCounter() {
        counterTimeoutsRegistry.increment();
    }

    public void incrementSubmodelTimeoutCounter(final String target) {
        final Counter counter = counterTimeoutsSubmodel.computeIfAbsent(target,
                key -> Counter.builder("http.requests.timeouts")
                              .tag("host", key)
                              .description("The total number of timeouts.")
                              .tag(TAG_ENDPOINT, ENDPOINT_SUBMODEL)
                              .register(meterRegistry));
        counter.increment();
    }

    public void incrementSubmodelRetryCounter(final String target) {
        final Counter counter = counterTimeoutsSubmodel.computeIfAbsent(target,
                key -> Counter.builder("http.requests.retries")
                              .tag("host", key)
                              .description("The total number of retries.")
                              .tag(TAG_ENDPOINT, ENDPOINT_SUBMODEL)
                              .register(meterRegistry));
        counter.increment();

    }

}
