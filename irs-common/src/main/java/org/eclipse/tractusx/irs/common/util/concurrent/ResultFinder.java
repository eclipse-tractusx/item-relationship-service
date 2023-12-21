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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Helper class to find the relevant result from a list of futures.
 */
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
    public <T> CompletableFuture<Optional<T>> getFastestResult(final List<CompletableFuture<T>> futures) {

        if (futures == null || futures.isEmpty()) {
            return CompletableFuture.supplyAsync(Optional::empty);
        }

        final CompletableFuture<Optional<T>> resultPromise = new CompletableFuture<>();

        final var handledFutures = futures.stream() //
                                          .map(future -> future.handle((value, throwable) -> {
                                              if (!resultPromise.isDone() && throwable == null && value != null) {
                                                  resultPromise.complete(Optional.of(value));
                                                  return true;
                                              } else {
                                                  return false;
                                              }
                                          })).toList();

        CompletableFuture.allOf(handledFutures.toArray(new CompletableFuture[0])).thenRun(() -> {
            if (!resultPromise.isDone()) {
                resultPromise.complete(Optional.empty());
            }
        });

        return resultPromise;
    }

}
