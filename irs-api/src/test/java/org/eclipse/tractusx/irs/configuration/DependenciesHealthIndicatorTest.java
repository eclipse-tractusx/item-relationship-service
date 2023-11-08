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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

class DependenciesHealthIndicatorTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final DependenciesHealthConfiguration dependenciesHealthConfiguration = mock(DependenciesHealthConfiguration.class);

    @Test
    void shouldReturnStatusUpWhenAllExternalServicesAreUp() {
        // given
        final DependenciesHealthIndicator dependenciesHealthIndicator = new DependenciesHealthIndicator(restTemplate, dependenciesHealthConfiguration);
        final Map<String, String> externalServicesHealthUrls = externalServicesHealthUrls();
        when(dependenciesHealthConfiguration.getUrls()).thenReturn(externalServicesHealthUrls);
        when(restTemplate.getForEntity(anyString(), eq(Void.class))).thenReturn(ResponseEntity.ok().build());

        // when
        final Health health = dependenciesHealthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).hasSameSizeAs(externalServicesHealthUrls);
        verify(restTemplate, times(externalServicesHealthUrls.size())).getForEntity(anyString(), eq(Void.class));
    }

    @Test
    void shouldReturnStatusDownWhenAnyExternalServiceIsNotReachable() {
        // given
        final DependenciesHealthIndicator dependenciesHealthIndicator = new DependenciesHealthIndicator(restTemplate, dependenciesHealthConfiguration);
        final Map<String, String> externalServicesHealthUrls = externalServicesHealthUrls();
        when(dependenciesHealthConfiguration.getUrls()).thenReturn(externalServicesHealthUrls);
        when(restTemplate.getForEntity(anyString(), eq(Void.class))).thenThrow(new ResourceAccessException("Not reachable"))
                                                                    .thenReturn(ResponseEntity.ok().build());

        // when
        final Health health = dependenciesHealthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).hasSameSizeAs(externalServicesHealthUrls);
        verify(restTemplate, times(externalServicesHealthUrls.size())).getForEntity(anyString(), eq(Void.class));
    }

    @Test
    void shouldReturnStatusDownWhenAnyExternalServiceIsDown() {
        // given
        final DependenciesHealthIndicator dependenciesHealthIndicator = new DependenciesHealthIndicator(restTemplate, dependenciesHealthConfiguration);
        final Map<String, String> externalServicesHealthUrls = externalServicesHealthUrls();
        when(dependenciesHealthConfiguration.getUrls()).thenReturn(externalServicesHealthUrls);
        when(restTemplate.getForEntity(anyString(), eq(Void.class))).thenReturn(ResponseEntity.notFound().build())
                                                                    .thenReturn(ResponseEntity.ok().build());

        // when
        final Health health = dependenciesHealthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).hasSameSizeAs(externalServicesHealthUrls);
        verify(restTemplate, times(externalServicesHealthUrls.size())).getForEntity(anyString(), eq(Void.class));
    }

    @NotNull
    private static Map<String, String> externalServicesHealthUrls() {
        return Map.of(
                "service_one", "http://service_one/health",
                "service_two", "http://service_two/health",
                "service_three", "http://service_three/health");
    }
}
