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
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.util.JobMetrics;
import org.springframework.stereotype.Service;

/**
 * Registering customer metrics for application
 */
@Service
public class MeterRegistryService {

    private final Counter counterCreatedJobs;

    private final JobMetrics jobMetrics;

    private final Integer lastJobInJobStoreCount = 0;

    public MeterRegistryService(final MeterRegistry meterRegistry) {
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
                                    .jobRunning(Counter.builder("jobs.jobstate.running")
                                                       .description("The number jobs running")
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

    public void incrementJobInJobStore(final Double count) {
        jobMetrics.getJobInJobStore().increment(count);
    }

    public void incrementJobSuccessful() {
        jobMetrics.getJobSuccessful().increment();
    }

    public void incrementJobFailed() {
        jobMetrics.getJobFailed().increment();
    }

    public void incrementJobCancelled() {
        jobMetrics.getJobCancelled().increment();
    }

    public void incrementJobRunning() {
        jobMetrics.getJobRunning().increment();
    }

    public void incremenException() {
        jobMetrics.getException().increment();
    }

    public void recordJobStateMetric(final JobState state) {
        switch (state) {
            case COMPLETED:
                incrementJobSuccessful();
                break;
            case TRANSFERS_FINISHED:
                incrementJobsProcessed();
                break;
            case ERROR:
                incrementJobFailed();
                break;
            case CANCELED:
                incrementJobCancelled();
                break;
            case RUNNING:
                incrementJobRunning();
                break;
            default:
        }
    }

    public void setJobsInJobStore(final Integer count) {
        incrementJobInJobStore(Double.valueOf(count));
    }

    public JobMetrics getJobMetric() {
        return jobMetrics;
    }
}
