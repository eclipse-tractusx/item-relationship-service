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

import java.util.ArrayList;
import java.util.List;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.util.JobMetrics;
import org.springframework.stereotype.Service;

/**
 * Registering customer metrics for application
 */
@Service
@Slf4j
public class MeterRegistryService {

    private static final String JOB_STATE_TAG = "jobstate";

    private final List<Integer> numbersOfJobsInJobStore = new ArrayList<>();

    private final JobMetrics jobMetrics;

    public MeterRegistryService(final MeterRegistry meterRegistry) {
        this.jobMetrics = JobMetrics.builder()
                                    .counterCreatedJobs(Counter.builder("jobs.created")
                                                               .description("Number of jobs ever created")
                                                               .register(meterRegistry))
                                    .jobProcessed(Counter.builder("jobs.processed")
                                                         .description("Number of jobs processed")
                                                         .tags(JOB_STATE_TAG, "processed")
                                                         .register(meterRegistry))
                                    .jobInJobStore(Gauge.builder("jobs.jobstore", numbersOfJobsInJobStore, List::size)
                                                        .description("Number of jobs in jobstore")
                                                        .tags(JOB_STATE_TAG, "jobs_in_store")
                                                        .register(meterRegistry))
                                    .jobSuccessful(Counter.builder("jobs.jobstate.sucessful")
                                                          .description("Number of successful jobs")
                                                          .tags(JOB_STATE_TAG, "successful")
                                                          .register(meterRegistry))
                                    .jobFailed(Counter.builder("jobs.jobstate.failed")
                                                      .description("Number of failed jobs")
                                                      .tags(JOB_STATE_TAG, "failed")
                                                      .register(meterRegistry))
                                    .jobCancelled(Counter.builder("jobs.jobstate.cancelled")
                                                         .description("Number of cancelled jobs")
                                                         .tags(JOB_STATE_TAG, "cancelled")
                                                         .register(meterRegistry))
                                    .jobRunning(Counter.builder("jobs.jobstate.running")
                                                       .description("Number of running jobs")
                                                       .tags(JOB_STATE_TAG, "running")
                                                       .register(meterRegistry))
                                    .exception(Counter.builder("jobs.exception")
                                                      .description("Number of jobs exceptions")
                                                      .tags(JOB_STATE_TAG, "exception")
                                                      .register(meterRegistry))
                                    .build();
    }

    public void incrementNumberOfCreatedJobs() {
        jobMetrics.getCounterCreatedJobs().increment();
    }

    public void incrementJobsProcessed() {
        jobMetrics.getJobProcessed().increment();
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

    public void incrementException() {
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
        log.info("Increment metric for {} state ", state);
    }

    public void incrementNumberOfJobsInJobStore() {
        numbersOfJobsInJobStore.add(1);
    }

    public void decrementNumberOfJobsInJobStore() {
        if (!numbersOfJobsInJobStore.isEmpty()) {
            numbersOfJobsInJobStore.remove(0);
        }
    }

    public JobMetrics getJobMetric() {
        return jobMetrics;
    }
}
