/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
