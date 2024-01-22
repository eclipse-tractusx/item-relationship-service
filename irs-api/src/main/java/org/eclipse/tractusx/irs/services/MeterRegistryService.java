/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.util.JobMetrics;
import org.springframework.stereotype.Service;

/**
 * Registering customer metrics for application
 */
@Service
@Slf4j
@SuppressWarnings("PMD.TooManyMethods")
public class MeterRegistryService {

    private static final String JOB_STATE_TAG = "jobstate";
    private static final String JOB_TIMER_TAG = "jobtimer";
    private static final String JOB_SNAPSHOT_TAG = "jobsnapshot";

    private final AtomicLong numbersOfJobsInJobStore = new AtomicLong();
    private final AtomicLong jobExecutionDuration = new AtomicLong();
    private final AtomicLong snapshotCompletedValue = new AtomicLong();
    private final AtomicLong snapshotRunningValue = new AtomicLong();
    private final AtomicLong snapshotFailedValue = new AtomicLong();
    private final AtomicLong snapshotCancelledValue = new AtomicLong();
    private final Map<String, Gauge> executionTimeMap = new ConcurrentHashMap<>();

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
                                    .jobSuccessSnapshot(Gauge.builder("jobs.snapshot.success", snapshotCompletedValue,
                                                                     AtomicLong::get)
                                                             .description("Snapshot of completed jobs")
                                                             .tags(JOB_SNAPSHOT_TAG, "job_completed_snapshot")
                                                             .register(meterRegistry))
                                    .jobRunningSnapshot(Gauge.builder("jobs.snapshot.running", snapshotRunningValue,
                                                                     AtomicLong::get)
                                                             .description("Snapshot of running jobs")
                                                             .tags(JOB_SNAPSHOT_TAG, "job_running_snapshot")
                                                             .register(meterRegistry))
                                    .jobFailedSnapshot(
                                            Gauge.builder("jobs.snapshot.failed", snapshotFailedValue, AtomicLong::get)
                                                 .description("Snapshot of failed jobs")
                                                 .tags(JOB_SNAPSHOT_TAG, "job_failed_snapshot")
                                                 .register(meterRegistry))
                                    .jobCancelledSnapshot(
                                            Gauge.builder("jobs.snapshot.cancelled", snapshotCancelledValue,
                                                         AtomicLong::get)
                                                 .description("Snapshot of cancelled jobs")
                                                 .tags(JOB_SNAPSHOT_TAG, "job_cancelled_snapshot")
                                                 .register(meterRegistry))
                                    .build();
    }

    public void incrementNumberOfCreatedJobs() {
        jobMetrics.getCounterCreatedJobs().increment();
    }

    public void incrementJobProcessed() {
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
                incrementJobProcessed();
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
        log.debug("Increment metric for {} state ", state);
    }

    public void setNumberOfJobsInJobStore(final Long size) {
        this.numbersOfJobsInJobStore.set(size);
        log.debug("Current size of Job in JobStore is {}", size);
    }

    public void setMeasuredMethodExecutionTime(final String tag, final long duration) {
        final Gauge gauge = executionTimeMap.computeIfAbsent(tag,
                key -> Gauge.builder("job.execution.time", jobExecutionDuration, AtomicLong::get)
                            .description("Measure time to execute a job")
                            .tag(JOB_TIMER_TAG, tag)
                            .register(meterRegistry));

        this.jobExecutionDuration.set(duration);
        this.jobMetrics = jobMetrics.toBuilder().jobExecutionTime(tag, gauge).build();
        log.debug("Execution time measured for {} is {}", tag, duration);

    }

    public void setStateSnapShot(final JobState state, final long value) {
        log.debug("Update State {} snapshot to {} ", state, value);
        switch (state) {
            case COMPLETED:
                snapshotCompletedValue.set(value);
                break;
            case RUNNING:
                snapshotRunningValue.set(value);
                break;
            case CANCELED:
                snapshotCancelledValue.set(value);
                break;
            case ERROR:
                snapshotFailedValue.set(value);
                break;
            default:
                log.debug("Unused State {} value {} ", state, value);
                break;
        }
    }

    public JobMetrics getJobMetric() {
        return jobMetrics;
    }
}
