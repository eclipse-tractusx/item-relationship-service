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
package org.eclipse.tractusx.irs.edc.client;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

/**
 * Wrapper class for asynchrous execution of polling jobs.
 *
 * @param <T> the result type of the polling action
 */
@Builder
public class PollingJob<T> {

    private final Clock clock;
    private final ScheduledExecutorService scheduler;
    private Duration timeToLive;
    private Supplier<Optional<T>> action;
    private String description;
    private Duration pollInterval;

    public CompletableFuture<T> schedule() {
        enforceRequiredParameters();

        final CompletableFuture<T> completableFuture = new CompletableFuture<>();

        final Runnable actionToUse = () -> action.get().ifPresent(completableFuture::complete);

        final ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(
                wrapWithErrorHandler(wrapWithTimeout(actionToUse), completableFuture), 0, pollInterval.toMillis(),
                TimeUnit.MILLISECONDS);

        completableFuture.whenComplete((result, thrown) -> scheduledFuture.cancel(true));

        return completableFuture;

    }

    private Runnable wrapWithErrorHandler(final Runnable action, final CompletableFuture<T> completableFuture) {
        return RunnableDecorator.withErrorHandler(action, completableFuture::completeExceptionally);
    }

    private void enforceRequiredParameters() {
        Objects.requireNonNull(action, "An action must be supplied to the polling job.");
        Objects.requireNonNull(clock, "A clock must be supplied to the polling job.");
        Objects.requireNonNull(scheduler, "A scheduler must be supplied to the polling job.");
        pollInterval = Objects.requireNonNullElse(pollInterval, Duration.ofSeconds(1));

    }

    private Runnable wrapWithTimeout(final Runnable actionToUse) {
        if (timeToLive != null) {
            return RunnableDecorator.withTimeout(actionToUse, timeToLive, clock, StringUtils.trimToEmpty(description));
        }
        return actionToUse;
    }
}
