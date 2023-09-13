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
package org.eclipse.tractusx.irs.common;

import java.util.HashMap;
import java.util.Map;

import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;

/**
 * Registering outbound connections metrics for the IRS
 */
@Getter
public class OutboundMeterRegistryService {

    private static final String ENDPOINT_SUBMODEL = "submodel";
    private static final String ENDPOINT_REGISTRY = "registry";
    private static final String TAG_ENDPOINT = "endpoint";
    private final Counter counterRetriesRegistry;

    private final Counter counterTimeoutsRegistry;
    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> counterTimeoutsSubmodel;
    private final Map<String, Counter> counterRetriesSubmodel;

    public OutboundMeterRegistryService(final MeterRegistry meterRegistry, final RetryRegistry retryRegistry) {
        this.counterRetriesRegistry = Counter.builder("http.requests.retries")
                                             .description("The total number of retries.")
                                             .tag(TAG_ENDPOINT, ENDPOINT_REGISTRY)
                                             .register(meterRegistry);
        this.counterTimeoutsRegistry = Counter.builder("http.requests.timeouts")
                                              .description("The total number of timeouts.")
                                              .tag(TAG_ENDPOINT, ENDPOINT_REGISTRY)
                                              .register(meterRegistry);
        this.meterRegistry = meterRegistry;

        counterTimeoutsSubmodel = new HashMap<>();
        counterRetriesSubmodel = new HashMap<>();

        retryRegistry.retry(ENDPOINT_REGISTRY).getEventPublisher().onRetry(event -> counterRetriesRegistry.increment());

        retryRegistry.getAllRetries()
                     .stream()
                     .filter(retry -> !ENDPOINT_REGISTRY.equals(retry.getName()))
                     .forEach(retry -> retry.getEventPublisher()
                                            .onRetry(event -> incrementSubmodelRetryCounter(retry.getName())));

    }

    public void incrementRegistryTimeoutCounter() {
        counterTimeoutsRegistry.increment();
    }

    public void incrementSubmodelTimeoutCounter(final String target) {
        final Counter counter = counterTimeoutsSubmodel.computeIfAbsent(target,
                key -> Counter.builder("http.requests.timeouts")
                              .tag("host", key)
                              .description("The total number of timeouts.")
                              .tag(TAG_ENDPOINT, ENDPOINT_SUBMODEL)
                              .register(meterRegistry));
        counter.increment();
    }

    public void incrementSubmodelRetryCounter(final String target) {
        final Counter counter = counterRetriesSubmodel.computeIfAbsent(target,
                key -> Counter.builder("http.requests.retries")
                              .tag("host", key)
                              .description("The total number of retries.")
                              .tag(TAG_ENDPOINT, ENDPOINT_SUBMODEL)
                              .register(meterRegistry));
        counter.increment();

    }
}
