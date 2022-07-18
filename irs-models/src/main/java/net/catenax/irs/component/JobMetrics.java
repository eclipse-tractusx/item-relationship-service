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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
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
    Counter jobProcessed;

    /**
     * Jobs in job store
     */
    Counter jobInJobStore;

    /**
     * Number of jobs with completed state since the start of IRS
     */
    Counter jobSuccessful;

    /**
     * Number of jobs with error state since the start of IRS
     */
    Counter jobFailed;

    /**
     * Number of jobs with cancel state since the start of IRS
     */
    Counter jobCancelled;

    /**
     * Average -> Gauge (without DT registry or submodel calls)
     */
    Gauge jobExecutionNetTime;

    /**
     * Running, Completed, Error, timeout
     */
    Counter jobPerStateSnapshot;

    /**
     * aggregated Exception count
     */
    Counter exception;

}
