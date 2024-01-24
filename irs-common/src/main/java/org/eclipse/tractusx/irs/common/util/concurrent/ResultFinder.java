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

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

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
            log.warn("#214@getFastestResult#0 Called getFastestResult with empty list of futures");
            return CompletableFuture.completedFuture(null);
        }

        log.info("#214@getFastestResult#1 Trying to get fastest result from list of futures");

        final CompletableFuture<T> fastestResultPromise = new CompletableFuture<>();

        final List<Throwable> exceptions = new ArrayList<>();

        final var futuresList = futures.stream()
                                       .map(future -> future.exceptionally(collectingExceptionsAndThrow(exceptions))
                                                            .handle(completingOnFirstSuccessful(fastestResultPromise)))
                                       .toList();

        log.info("#214@getFastestResult#2");

        allOf(toArray(futuresList)).whenComplete((value, ex) -> {

            log.info("#214@getFastestResult#3 List of futures completed");

            if (ex != null) {
                log.error("g#214@etFastestResult#4 Exception occurred: " + ex.getMessage(), ex);
                fastestResultPromise.completeExceptionally(new CompletionExceptions(exceptions));
            } else if (fastestResultPromise.isDone()) {
                log.info("#214@getFastestResult#5 Fastest result already found, ignoring the others");
            } else {
                log.info("#214@getFastestResult#6 Completing");
                fastestResultPromise.complete(null);
            }
        });

        return fastestResultPromise;
    }

    private static <T> CompletableFuture<T>[] toArray(final List<CompletableFuture<T>> handledFutures) {
        return handledFutures.toArray(new CompletableFuture[0]);
    }

    private static <T> BiFunction<T, Throwable, Boolean> completingOnFirstSuccessful(
            final CompletableFuture<T> resultPromise) {

        return (value, throwable) -> {

            log.info("#214@completingOnFirstSuccessful#1 value: {}, throwable: {}", value, throwable);

            final boolean notFinishedByOtherFuture = !resultPromise.isDone();
            log.info("#214@completingOnFirstSuccessful#2 notFinishedByOtherFuture {} ", notFinishedByOtherFuture);

            final boolean currentFutureSuccessful = throwable == null && value != null;

            if (notFinishedByOtherFuture && currentFutureSuccessful) {

                // first future that completes successfully completes the overall future
                log.info("#214@completingOnFirstSuccessful#3 First future that completed successfully");
                resultPromise.complete(value);
                return true;

            } else {
                if (throwable != null) {
                    log.warn("#214@completingOnFirstSuccessful#4 Exception occurred: " + throwable.getMessage(),
                            throwable);
                    throw new CompletionException(throwable.getMessage(), throwable);
                } else {
                    log.info("#214@completingOnFirstSuccessful#5 log just for debugging");
                }
                return false;
            }
        };
    }

    private static <T> Function<Throwable, T> collectingExceptionsAndThrow(final List<Throwable> exceptions) {
        return t -> {
            log.error("collectingExceptionsAndThrow -- Exception occurred: " + t.getMessage(), t);
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

        private final List<Throwable> causes;

        public CompletionExceptions(final List<Throwable> causes) {
            super("All failing, use getCauses() for details");
            this.causes = causes;
        }

    }
}