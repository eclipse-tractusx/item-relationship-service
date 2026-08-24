/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.config;

import java.time.Duration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.eclipse.tractusx.irs.recursive.util.RecursivePatternStore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the recursive IRS module.
 *
 * <p>Configure per instance in atlas.yml / belfast.yml / ceres.yml / delta.yml
 * under the {@code irs.recursive} prefix.</p>
 */
@Data
@Validated
@ConfigurationProperties(prefix = "irs.recursive")
public class RecursiveProperties {

    private static final int DEFAULT_JOB_TTL_MINUTES = 30;
    private static final int MAX_JOB_TTL_HOURS = 2;
    private static final int CHILD_RESPONSE_SAFETY_BUFFER_SECONDS = 60;
    private static final int TIMEOUT_CHECK_INTERVAL_SECONDS = 30;

    /**
     * BPNL of the local IRS instance (e.g. BPNL0000ATLS0001 for Atlas).
     */
    @NotBlank(message = "must be configured because the recursive IRS module is always active")
    @Pattern(regexp = RecursivePatternStore.BPNL_STRING,
             message = "must be a BPNL with prefix BPNL and 12 alphanumeric characters")
    private String localBpnl = "";

    /**
     * Deadline and timeout settings for recursive jobs.
     */
    @Valid
    private Timeout timeout = new Timeout();

    /**
     * Timeout settings for recursive jobs and partner response handling.
     */
    @Data
    public static class Timeout {

        /**
         * Default TTL for root requests when no TTL is provided.
         */
        private Duration defaultJobTtl = Duration.ofMinutes(DEFAULT_JOB_TTL_MINUTES);

        /**
         * Upper bound for accepted root request TTLs.
         */
        private Duration maxJobTtl = Duration.ofHours(MAX_JOB_TTL_HOURS);

        /**
         * Buffer subtracted before forwarding the deadline to a child IRS.
         */
        private Duration childResponseSafetyBuffer = Duration.ofSeconds(CHILD_RESPONSE_SAFETY_BUFFER_SECONDS);

        /**
         * How often the timeout monitor checks waiting recursive jobs.
         */
        private Duration timeoutCheckInterval = Duration.ofSeconds(TIMEOUT_CHECK_INTERVAL_SECONDS);
    }

}
