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

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

/**
 * Test for {@link ResultFinder}
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("ResultFinder")
class ResultFinderTest {

    final ResultFinder sut = new ResultFinder();

    @NullAndEmptySource
    @ParameterizedTest
    void withNullOrEmptyInputList_nullShouldBeReturned(final List<CompletableFuture<String>> list)
            throws ExecutionException, InterruptedException {
        final var result = sut.getFastestResult(list).get();
        assertThat(result).isNull();
    }

    @Test
    void withOneSuccessfulCompletableFuture_theSuccessfulResultShouldBeReturned()
            throws ExecutionException, InterruptedException {
        final var futures = List.of(supplyAsync(() -> "ok"));
        final String result = sut.getFastestResult(futures).get();
        assertThat(result).isEqualTo("ok");
    }

    @Test
    void withOnlyOneSuccessful_andOtherCompletableFuturesFailing_theSuccessfulResultShouldBeReturned()
            throws ExecutionException, InterruptedException {

        // given
        final List<CompletableFuture<String>> futures = List.of( //
                supplyAsync(() -> {
                    throw new RuntimeException("failing");
                }), //
                supplyAsync(() -> "ok"), //
                supplyAsync(() -> {
                    throw new RuntimeException("failing");
                }));

        // when
        final String result = sut.getFastestResult(futures).get();

        // then
        assertThat(result).isEqualTo("ok");
    }

    @Test
    void withAllCompletableFuturesFailing_itShouldThrow() {

        // given
        final List<CompletableFuture<String>> futures = List.of( //
                futureThrowAfterMillis(5000, () -> new RuntimeException("failing 1")), //
                supplyAsync(() -> {
                    throw new RuntimeException("failing 2");
                }), //
                futureThrowAfterMillis(1000, () -> new RuntimeException("failing 3")));

        // when
        final ThrowingCallable call = () -> sut.getFastestResult(futures).get();

        // then
        assertThatThrownBy(call).isInstanceOf(ExecutionException.class)
                                .extracting(Throwable::getCause)
                                .isInstanceOf(ResultFinder.CompletionExceptions.class)
                                .extracting(collectedFailures -> (ResultFinder.CompletionExceptions) collectedFailures)
                                .extracting(ResultFinder.CompletionExceptions::getSuppressed)
                                .describedAs("should have collected all exceptions")
                                .satisfies(causes -> assertThat(Arrays.stream(causes)
                                                                      .map(Throwable::getMessage)
                                                                      .toList()).containsExactlyInAnyOrder(
                                        "java.lang.RuntimeException: failing 1",
                                        "java.lang.RuntimeException: failing 2",
                                        "java.lang.RuntimeException: failing 3"));

    }

    @Test
    void withMultipleSuccessfulCompletableFutures_theFastestSuccessfulResultShouldBeReturned()
            throws ExecutionException, InterruptedException {

        // given
        final List<CompletableFuture<String>> futures = List.of( //
                futureThrowAfterMillis(200, () -> new RuntimeException("slower failing")), //
                futureReturnAfterMillis(1000, () -> "slowest success"), //
                futureReturnAfterMillis(300, () -> "fastest success"), //
                futureThrowAfterMillis(0, () -> new RuntimeException("failing immediately")) //
        );

        // when
        final String result = sut.getFastestResult(futures).get();

        // then
        assertThat(result).isEqualTo("fastest success");
    }

    private static CompletableFuture<String> futureThrowAfterMillis(final int sleepMillis,
            final Supplier<RuntimeException> exceptionSupplier) {
        return supplyAsync(() -> {
            sleep(sleepMillis);
            throw exceptionSupplier.get();
        });
    }

    private static <T> CompletableFuture<T> futureReturnAfterMillis(final int sleepMillis,
            final Supplier<T> resultSupplier) {
        return supplyAsync(() -> {
            sleep(sleepMillis);
            return resultSupplier.get();
        });
    }

    private static void sleep(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

}