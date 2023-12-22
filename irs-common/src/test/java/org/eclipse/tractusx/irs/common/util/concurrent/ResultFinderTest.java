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

package org.eclipse.tractusx.irs.common.util.concurrent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Test for {@link ResultFinder}
 */
class ResultFinderTest {

    final ResultFinder sut = new ResultFinder();

    @NullAndEmptySource
    @ParameterizedTest
    void withNullOrEmptyInputList(final List<CompletableFuture<String>> list)
            throws ExecutionException, InterruptedException {
        final var result = sut.getFastestResult(list).get();
        assertThat(result).isNull();
    }

    @Test
    void withOneSuccessfulCompletableFuture() throws ExecutionException, InterruptedException {
        final var futures = List.of(supplyAsync(() -> "ok"));
        final String result = sut.getFastestResult(futures).get();
        assertThat(result).isEqualTo("ok");
    }

    @Test
    void withOnlySuccessfulAndOtherCompletableFuturesFailing() throws ExecutionException, InterruptedException {

        // given
        final List<CompletableFuture<String>> futures = List.of( //
                supplyAsync(() -> {
                    throw new RuntimeException("failing");
                }), //
                supplyAsync(() -> "ok"), supplyAsync(() -> {
                    throw new RuntimeException("failing");
                }));

        // when
        final String result = sut.getFastestResult(futures).get();

        // then
        assertThat(result).isEqualTo("ok");
    }

    @Test
    void shouldReturnFastestSuccessful() throws ExecutionException, InterruptedException {

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
            throw new RuntimeException(e);
        }
    }

}