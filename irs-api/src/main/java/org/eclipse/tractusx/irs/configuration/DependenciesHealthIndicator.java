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
package org.eclipse.tractusx.irs.configuration;

import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.NO_ERROR_REST_TEMPLATE;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * External Dependencies health indicator for Spring actuator
 */
@Component
@Slf4j
@ConditionalOnEnabledHealthIndicator("dependencies")
public class DependenciesHealthIndicator implements HealthIndicator {

    private final DependenciesHealthConfiguration dependenciesHealthConfiguration;
    private final RestTemplate restTemplate;

    public DependenciesHealthIndicator(@Qualifier(NO_ERROR_REST_TEMPLATE) final RestTemplate noErrorRestTemplate,
            final DependenciesHealthConfiguration dependenciesHealthConfiguration) {
        this.dependenciesHealthConfiguration = dependenciesHealthConfiguration;
        this.restTemplate = noErrorRestTemplate;
    }

    public DependenciesHealthConfiguration getConfig() {
        return dependenciesHealthConfiguration;
    }

    @Override
    public Health health() {
        final Map<String, Status> details = details();
        return Health.status(globalStatus(details.values()))
                     .withDetails(details)
                     .build();
    }

    private Status globalStatus(final Collection<Status> statuses) {
        final boolean allDependenciesAreUp = statuses.stream().allMatch(status -> status.equals(Status.UP));
        return allDependenciesAreUp ? Status.UP : Status.DOWN;
    }

    private Map<String, Status> details() {
        return dependenciesHealthConfiguration.getUrls()
                                              .entrySet()
                                              .stream()
                                              .map(dependency -> {
                                                  final String dependencyName = dependency.getKey();
                                                  try {
                                                      final String dependencyHealthUrl = dependency.getValue();
                                                      final ResponseEntity<Void> health = restTemplate.getForEntity(
                                                              dependencyHealthUrl, Void.class);
                                                      log.info("Health endpoint URL for {} dependency pinged with status {}.",
                                                              dependencyName, health.getStatusCode());
                                                      return new ExternalServiceHealthStatus(dependencyName, health.getStatusCode());
                                                  } catch (final ResourceAccessException resourceAccessException) {
                                                      log.warn("Health endpoint URL for {} dependency is not reachable.", dependencyName);
                                                      return new ExternalServiceHealthStatus(dependencyName, Status.UNKNOWN);
                                                  }
                                              }).collect(Collectors.toMap(ExternalServiceHealthStatus::getName, ExternalServiceHealthStatus::getStatus));
    }

    /**
     * External Service Status DTO
     */
    @Value
    @RequiredArgsConstructor
    private static final class ExternalServiceHealthStatus {
        private final String name;
        private final Status status;

        private ExternalServiceHealthStatus(final String name, final HttpStatusCode httpStatusCode) {
            this.name = name;
            this.status = httpStatusCode.is2xxSuccessful() ? Status.UP : Status.DOWN;
        }
    }

}
