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
    private final Counter counterRetriesSubmodel;

    private final Counter counterTimeoutsRegistry;
    private final Counter counterTimeoutsSubmodel;

    /* package */ OutboundMeterRegistryService(final MeterRegistry meterRegistry, final RetryRegistry retryRegistry) {
        this.counterRetriesRegistry = Counter.builder("http.requests.retries")
                                             .description("The total number of retries.")
                                             .tag(TAG_ENDPOINT, ENDPOINT_REGISTRY)
                                             .register(meterRegistry);
        this.counterRetriesSubmodel = Counter.builder("http.requests.retries")
                                             .description("The total number of retries.")
                                             .tag(TAG_ENDPOINT, ENDPOINT_SUBMODEL)
                                             .register(meterRegistry);
        this.counterTimeoutsRegistry = Counter.builder("http.requests.timeouts")
                                              .description("The total number of timeouts.")
                                              .tag(TAG_ENDPOINT, ENDPOINT_REGISTRY)
                                              .register(meterRegistry);
        this.counterTimeoutsSubmodel = Counter.builder("http.requests.timeouts")
                                              .description("The total number of timeouts.")
                                              .tag(TAG_ENDPOINT, ENDPOINT_SUBMODEL)
                                              .register(meterRegistry);

        retryRegistry.retry(ENDPOINT_REGISTRY).getEventPublisher().onRetry(event -> counterRetriesRegistry.increment());

        retryRegistry.retry(ENDPOINT_SUBMODEL).getEventPublisher().onRetry(event -> counterRetriesSubmodel.increment());

    }

    public void incrementRegistryTimeoutCounter() {
        counterTimeoutsRegistry.increment();
    }

    public void incrementSubmodelTimeoutCounter() {
        counterTimeoutsSubmodel.increment();
    }
}
