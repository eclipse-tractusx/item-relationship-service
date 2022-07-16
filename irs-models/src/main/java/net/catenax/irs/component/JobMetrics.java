//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Store jobs metrics data
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
public class JobMetrics {
    /**
     * Jobs processing per time unit (jobs/min, jobs/hour)
     */
    Integer jobProcessed;

    /**
     * Jobs in job store
     */
    Integer jobInJobStore;

    /**
     * Number of jobs with completed state since the start of IRS
     */
    Integer jobSuccessful;

    /**
     * Number of jobs with error state since the start of IRS
     */
    Integer jobFailed;

    /**
     * Number of jobs with cancel state since the start of IRS
     */
    Integer jobCancelled;

    /**
     * avg, median, min, max
     */
    Integer jobAverageProcessingTime;

    /**
     * Average -> Gauge (without DT registry or submodel calls)
     */
    Integer jobExecutionNetTime;

    /**
     * Running, Completed, Error, timeout
     */
    Integer jobPerStateSnapshot;

    /**
     * Jobs per State Aggregated (total, per hour, days, week)
     */
    Integer jobPerStateAggregate;

    /**
     * aggregated Exception count
     */
    Integer exception;

}
