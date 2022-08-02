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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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
    private static final String JOB_TIMER_TAG = "jobtimer";

    private final AtomicLong numbersOfJobsInJobStore = new AtomicLong();
    private final AtomicLong jobExecutionDuration = new AtomicLong();
    private final AtomicLong valueJobStateSnapShot = new AtomicLong();
    private final Map<String, Gauge> executionTimeMap = new ConcurrentHashMap<>();
    private final Map<String, Gauge> stateSnapshotMap = new ConcurrentHashMap<>();

    private JobMetrics jobMetrics;
    private final MeterRegistry meterRegistry;

    public MeterRegistryService(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.jobMetrics = JobMetrics.builder()
                                    .counterCreatedJobs(Counter.builder("jobs.created")
                                                               .description("Number of jobs ever created")
                                                               .register(meterRegistry))
                                    .jobProcessed(Counter.builder("jobs.processed")
                                                         .description("Number of jobs processed")
                                                         .tags(JOB_STATE_TAG, "processed")
                                                         .register(meterRegistry))
                                    .jobInJobStore(
                                            Gauge.builder("jobs.jobstore", numbersOfJobsInJobStore, AtomicLong::get)
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

    public void setNumberOfJobsInJobStore(final Long size) {
        this.numbersOfJobsInJobStore.set(size);
        log.info("Current size of Job in JobStore is {}", size);
    }

    public void setMeasuredMethodExecutionTime(final String tag, final long duration) {
        final Gauge gauge = executionTimeMap.computeIfAbsent(tag,
                key -> Gauge.builder("job.execution.time", jobExecutionDuration, AtomicLong::get)
                            .description("Measure time to execute a job")
                            .tag(JOB_TIMER_TAG, tag)
                            .register(meterRegistry));

        this.jobExecutionDuration.set(duration);
        this.jobMetrics = jobMetrics.toBuilder().jobExecutionTime(tag, gauge).build();
        log.info("Execution time measured for {} is {}", tag, duration);

    }

    public void setStateSnapShot(final String tag, final long value) {
        final Gauge gauge = stateSnapshotMap.computeIfAbsent(tag,
                key -> Gauge.builder("job.state.snapshot", valueJobStateSnapShot, AtomicLong::get)
                            .description("Snapshot of job states")
                            .tag(JOB_STATE_TAG, tag)
                            .register(meterRegistry));
        this.valueJobStateSnapShot.set(value);
        this.jobMetrics = jobMetrics.toBuilder().jobStateSnapShot(tag, gauge).build();
        log.info("Update State {} snapshot to {} ", tag, value);
    }

    public JobMetrics getJobMetric() {
        return jobMetrics;
    }
}
