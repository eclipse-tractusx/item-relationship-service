/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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

import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Configuration;

/**
 * Exports the metrics from {@link DependenciesHealthIndicator} to prometheus.
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class IrsHealthMetricsExportConfiguration {

    private static final MetricDescriptor HEALTH_IRS = //
            new MetricDescriptor("health-irs", "The IRS health status.");

    private static final MetricDescriptor HEALTH_IRS_DEPENDENCY_OVERALL = //
            new MetricDescriptor("health-irs-dependency-overall",
                    "The overall health status of the IRS dependencies. See configuration under '%s'.".formatted(
                            DependenciesHealthConfiguration.MANAGEMENT_HEALTH_DEPENDENCIES));

    private static final MetricDescriptor HEALTH_IRS_DEPENDENCY = //
            new MetricDescriptor("health-irs-dependency",
                    "The health status of the IRS dependencies configured under '%s'.".formatted(
                            DependenciesHealthConfiguration.MANAGEMENT_HEALTH_DEPENDENCIES));
    private static final String HEALTH_IRS_DEPENDENCY_TAG_NAME = "name";

    /**
     * Constructor.
     *
     * @param registry                    the metrics registry
     * @param healthEndpoint              the IRS health endpoint
     * @param dependenciesHealthIndicator the health indicator for the IRS dependencies
     */
    public IrsHealthMetricsExportConfiguration(final MeterRegistry registry, final HealthEndpoint healthEndpoint,
            final DependenciesHealthIndicator dependenciesHealthIndicator) {

        registerIrsHealthMetrics(registry, healthEndpoint);
        registerIrsDependenciesOverallHealthMetric(registry, dependenciesHealthIndicator);
        registerIrsDependenciesMetrics(registry, dependenciesHealthIndicator);
    }

    private void registerIrsHealthMetrics(final MeterRegistry registry, final HealthEndpoint irsHealthEndpoint) {

        final MetricDescriptor metricDescriptor = HEALTH_IRS;
        log.debug("Registering metric '{}'", metricDescriptor.name);

        final ToDoubleFunction<? super HealthEndpoint> statusProvider = //
                healthEndpoint -> StatusHelper.toNumeric(getIrsStatus(healthEndpoint));

        Gauge.builder(metricDescriptor.name, irsHealthEndpoint, statusProvider)
             .description(metricDescriptor.description)
             .strongReference(true)
             .register(registry);
    }

    private void registerIrsDependenciesOverallHealthMetric(final MeterRegistry registry,
            final DependenciesHealthIndicator dependenciesHealthIndicator) {

        final MetricDescriptor metricDescriptor = HEALTH_IRS_DEPENDENCY_OVERALL;
        log.debug("Registering metric '{}'", metricDescriptor.name);

        final ToDoubleFunction<DependenciesHealthIndicator> statusProvider = //
                healthIndicator -> StatusHelper.toNumeric(getIrsDependenciesOverallStatus(healthIndicator));

        Gauge.builder(metricDescriptor.name, dependenciesHealthIndicator, statusProvider)
             .description(metricDescriptor.description())
             .strongReference(true)
             .register(registry);
    }

    private void registerIrsDependenciesMetrics(final MeterRegistry registry,
            final DependenciesHealthIndicator dependenciesHealthIndicator) {

        final Set<String> dependencyNames = getDependencyNamesFromConfiguration(dependenciesHealthIndicator);
        for (final String dependencyName : dependencyNames) {
            registerIrsDependencyHealthMetric(registry, dependenciesHealthIndicator, dependencyName);
        }
    }

    private Set<String> getDependencyNamesFromConfiguration(
            final DependenciesHealthIndicator dependenciesHealthIndicator) {

        final DependenciesHealthConfiguration config = dependenciesHealthIndicator.getConfig();
        return config.getUrls().keySet();
    }

    private void registerIrsDependencyHealthMetric(final MeterRegistry registry,
            final DependenciesHealthIndicator dependenciesHealthIndicator, final String dependencyName) {

        final MetricDescriptor metricDescriptor = HEALTH_IRS_DEPENDENCY;
        log.debug("Registering metric '{}' tag '{}'", metricDescriptor.name, dependencyName);

        final ToDoubleFunction<DependenciesHealthIndicator> statusProvider = //
                healthIndicator -> StatusHelper.toNumeric(getIrsDependencyStatus(healthIndicator, dependencyName));

        Gauge.builder(metricDescriptor.name, dependenciesHealthIndicator, statusProvider)
             .tag(HEALTH_IRS_DEPENDENCY_TAG_NAME, dependencyName)
             .description(metricDescriptor.description)
             .strongReference(true)
             .register(registry);
    }

    private Status getIrsStatus(final HealthEndpoint healthEndpoint) {
        final Status status = healthEndpoint.health().getStatus();
        log.debug("Health status for IRS is '{}'", status);
        return status;
    }

    private static Status getIrsDependenciesOverallStatus(final DependenciesHealthIndicator healthIndicator) {
        final Status status = healthIndicator.getHealth(false).getStatus();
        log.debug("Overall IRS dependency health status is '{}'", status);
        return status;
    }

    private Status getIrsDependencyStatus(final DependenciesHealthIndicator healthIndicator,
            final String dependencyName) {
        final Health health = healthIndicator.getHealth(true);
        final Map<String, Object> healthDetails = health.getDetails();
        final Status status = (Status) healthDetails.get(dependencyName);
        log.debug("Health status for IRS dependency '{}' is '{}'", dependencyName, status);
        return status;
    }

    /**
     * Utility class with status helper methods
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class StatusHelper {

        public static final int STATUS_UNKNOWN = 0;
        public static final int STATUS_DOWN = 1;
        public static final int STATUS_OUT_OF_SERVICE = 2;
        public static final int STATUS_UP = 3;

        /**
         * Converts health status for usage with Gauge.
         *
         * @param status the health status
         * @return the numeric representation of the health status
         */
        public static int toNumeric(final Status status) {

            // see Spring documentation - map health indicators to metrics:
            //     https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/
            //            #howto.actuator.map-health-indicators-to-metrics

            if (Status.UP.equals(status)) {
                return STATUS_UP;
            }
            if (Status.OUT_OF_SERVICE.equals(status)) {
                return STATUS_OUT_OF_SERVICE;
            }
            if (Status.DOWN.equals(status)) {
                return STATUS_DOWN;
            }

            return STATUS_UNKNOWN;
        }
    }

    private record MetricDescriptor(String name, String description) {
    }
}
