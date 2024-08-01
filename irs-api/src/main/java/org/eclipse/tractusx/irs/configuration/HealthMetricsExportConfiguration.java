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
package org.eclipse.tractusx.irs.configuration;

import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Configuration;

/**
 * Exports the IRS health status to prometheus.
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class HealthMetricsExportConfiguration {

    /**
     * Constructor.
     *
     * @param registry       the metrics registry
     * @param healthEndpoint the IRS health endpoint
     */
    public HealthMetricsExportConfiguration(final MeterRegistry registry, final HealthEndpoint healthEndpoint) {
        registerIrsHealthMetrics(registry, healthEndpoint);
    }

    private void registerIrsHealthMetrics(final MeterRegistry registry, final HealthEndpoint irsHealthEndpoint) {

        final String metricName = "health-irs";
        log.debug("Registering metric '{}'", metricName);

        final ToDoubleFunction<? super HealthEndpoint> statusProvider = //
                healthEndpoint -> HealthStatusHelper.healthStatusToNumeric(getIrsStatus(healthEndpoint));

        Gauge.builder(metricName, irsHealthEndpoint, statusProvider)
             .description("The IRS health status.")
             .strongReference(true)
             .register(registry);
    }

    private Status getIrsStatus(final HealthEndpoint healthEndpoint) {
        final Status status = healthEndpoint.health().getStatus();
        log.debug("Health status for IRS is '{}'", status);
        return status;
    }

}
