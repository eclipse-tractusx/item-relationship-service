//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.util;

import java.util.Map;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 * Store jobs metrics data
 */
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class JobMetrics {
    /**
     * Jobs processing per time unit (jobs/min, jobs/hour)
     */
    private Counter jobProcessed;

    /**
     * Jobs in job store
     */
    private Gauge jobInJobStore;

    /**
     * Number of job create within a time frame
     */
    private final Counter counterCreatedJobs;

    /**
     * Number of jobs with completed state since the start of IRS
     */
    private Counter jobSuccessful;

    /**
     * Number of jobs with error state since the start of IRS
     */
    private Counter jobFailed;

    /**
     * Number of jobs with cancel state since the start of IRS
     */
    private Counter jobCancelled;

    /**
     * Average -> Gauge (without DT registry or submodel calls)
     */
    private Gauge jobExecutionNetTime;

    /**
     * Running, Completed, Error, timeout
     */
    private Counter jobRunning;

    /**
     * aggregated Exception count
     */
    private Counter exception;

    /**
     * take snapshot of successful job
     */
    private Gauge jobSuccessSnapshot;

    /**
     * take snapshot of failed job
     */
    private Gauge jobFailedSnapshot;

    /**
     * take snapshot of running job
     */
    private Gauge jobRunningSnapshot;

    /**
     * take snapshot of cancelled job
     */
    private Gauge jobCancelledSnapshot;

    /**
     * Record measured execution time
     */
    @Singular
    private Map<String, Gauge> jobExecutionTimes;

}
