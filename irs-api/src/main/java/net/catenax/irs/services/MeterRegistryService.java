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
import net.catenax.irs.component.JobMetrics;
import org.springframework.stereotype.Service;

/**
 * Registering customer metrics for application
 */
@Service
public class MeterRegistryService {

    private final Counter counterCreatedJobs;

    private final JobMetrics jobMetrics;

    /* package */ MeterRegistryService(final MeterRegistry meterRegistry) {
        this.counterCreatedJobs = Counter.builder("jobs.created")
                                         .description("The number of jobs ever created")
                                         .register(meterRegistry);

        this.jobMetrics = JobMetrics.builder()
                                    .jobProcessed(Counter.builder("jobs.processed")
                                                         .description("The number jobs processed")
                                                         .register(meterRegistry))
                                    .jobInJobStore(Counter.builder("jobs.jobstore")
                                                          .description("The number jobs in jobstore")
                                                          .register(meterRegistry))
                                    .jobSuccessful(Counter.builder("jobs.jobstate.sucessful")
                                                          .description("The number jobs successful")
                                                          .register(meterRegistry))
                                    .jobFailed(Counter.builder("jobs.jobstate.failed")
                                                      .description("The number jobs failed")
                                                      .register(meterRegistry))
                                    .jobCancelled(Counter.builder("jobs.jobstate.cancelled")
                                                         .description("The number jobs cancelled")
                                                         .register(meterRegistry))
                                    .jobCancelled(Counter.builder("jobs.jobstate.cancelled")
                                                         .description("The number jobs cancelled")
                                                         .register(meterRegistry))
                                    .exception(Counter.builder("jobs.exception")
                                                      .description("The number exceptions in jobs")
                                                      .register(meterRegistry))
                                    .build();
    }

    /* package */ void incrementNumberOfCreatedJobs() {
        counterCreatedJobs.increment();
    }

    public void incrementJobsProcessed() {
        jobMetrics.getJobProcessed().increment();
    }

    public void incrementJobInJobStore() {
        jobMetrics.getJobInJobStore().increment();
    }

    public void incrementJobSuccessful() {
        jobMetrics.getJobSuccessful().increment();
    }

    public void incrementJobFailed(int count) {
        jobMetrics.getJobFailed().increment((Double.valueOf(String.valueOf(count))));
    }

    public void incrementJobCancelled() {
        jobMetrics.getJobInJobStore().increment();
    }

    public void incremenJobSnapShot() {
        jobMetrics.getJobPerStateSnapshot().increment();
    }

    public void incremenException() {
        jobMetrics.getException().increment();
    }
}
