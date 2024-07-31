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
package org.eclipse.tractusx.irs.common.util.concurrent;

import static java.util.concurrent.CompletableFuture.allOf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Helper class to find the relevant result from a list of futures.
 */
@Slf4j
public class ResultFinder {

    /**
     * Returns a new {@link CompletableFuture} which completes
     * when at least one of the given futures completes successfully or all fail.
     * The result from the fastest successful future is returned. The others are ignored.
     *
     * @param futures the futures
     * @param <T>     the return type
     * @return a {@link CompletableFuture} returning the fastest successful result or empty
     */
    public <T> CompletableFuture<T> getFastestResult(final List<CompletableFuture<T>> futures) {

        if (futures == null || futures.isEmpty()) {
            log.warn("Called getFastestResult with empty list of futures");
            return CompletableFuture.completedFuture(null);
        }

        log.debug("Trying to get fastest result from list of futures");

        // The purpose of this overall future is to track when the first data request is successful.
        // This way we do not need to wait for the others to complete.
        final CompletableFuture<T> overallFuture = new CompletableFuture<>();

        final List<Throwable> exceptions = new ArrayList<>();

        final var futuresList = futures.stream()
                                       .map(future -> future.exceptionally(collectingExceptionsAndThrow(exceptions))
                                                            .handle(completingOnFirstSuccessful(overallFuture)))
                                       .toList();

        allOf(toArray(futuresList)).whenComplete((value, ex) -> {

            log.debug("All of the futures completed");

            if (ex != null) {
                log.warn("All failed: " + System.lineSeparator()
                        + exceptions.stream()
                                    .map(ExceptionUtils::getStackTrace)
                                    .collect(Collectors.joining(System.lineSeparator())), ex);

                final CompletionExceptions noneSuccessful = new CompletionExceptions("None successful");
                for (final Throwable exception : exceptions) {
                    noneSuccessful.addSuppressed(exception);
                }

                overallFuture.completeExceptionally(noneSuccessful);

            } else {
                overallFuture.complete(null);
            }
        });

        return overallFuture;
    }

    private static <T> CompletableFuture<T>[] toArray(final List<CompletableFuture<T>> handledFutures) {
        return handledFutures.toArray(new CompletableFuture[0]);
    }

    private static <T> BiFunction<T, Throwable, Boolean> completingOnFirstSuccessful(
            final CompletableFuture<T> overallFuture) {

        return (value, throwable) -> {

            log.trace("value: '{}', throwable: {}", value, throwable);

            final boolean notFinishedByOtherFuture = !overallFuture.isDone();
            log.debug("notFinishedByOtherFuture {} ", notFinishedByOtherFuture);

            final boolean currentFutureSuccessful = throwable == null && value != null;

            if (notFinishedByOtherFuture && currentFutureSuccessful) {

                log.debug("First future that completed successfully");
                overallFuture.complete(value);
                return true;

            } else {
                if (throwable != null) {
                    log.warn("Exception occurred: " + throwable.getMessage(), throwable);
                    throw new CompletionException(throwable.getMessage(), throwable);
                }
                return false;
            }
        };
    }

    private static <T> Function<Throwable, T> collectingExceptionsAndThrow(final List<Throwable> exceptions) {
        return t -> {
            log.error("Exception occurred: " + t.getMessage(), t);
            exceptions.add(t);
            throw new CompletionException(t);
        };
    }

    /**
     * Helper exception that can hold multiple causes.
     */
    @Getter
    @ToString
    public static class CompletionExceptions extends CompletionException {

        public CompletionExceptions(final String msg) {
            super(msg);
        }

    }
}
