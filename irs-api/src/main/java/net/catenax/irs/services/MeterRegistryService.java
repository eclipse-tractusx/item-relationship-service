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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

/**
 * Registering customer metrics for application
 */
@Service
public class MeterRegistryService {

    private final MeterRegistry meterRegistry;

    private final Counter counterCreatedJobs;

    public MeterRegistryService(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.counterCreatedJobs = Counter.builder("jobs.created")
               .description("The number of jobs ever created")
               .register(meterRegistry);
    }

    void incrementNumberOfCreatedJobs() {
        counterCreatedJobs.increment();
    }
}
