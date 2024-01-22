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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.HealthEndpoint;

@ExtendWith(MockitoExtension.class)
class HealthMetricsExportConfigurationTest {

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Mock
    private HealthEndpoint healthEndpointMock;

    @Test
    void shouldRegisterHealthMetricsWithPrometheus() {

        // ACT
        new HealthMetricsExportConfiguration(meterRegistry, healthEndpointMock);

        // ASSERT
        final List<Meter.Id> meterIds = meterRegistry.getMeters()
                                                     .stream()
                                                     .map(Meter::getId)
                                                     .collect(Collectors.toList());

        assertThat(meterIds).describedAs("should have registered the IRS health metric") //
                            .hasSize(1) //
                            .contains(new Meter.Id("health-irs", Tags.empty(), null, null, Meter.Type.GAUGE));
    }

}
