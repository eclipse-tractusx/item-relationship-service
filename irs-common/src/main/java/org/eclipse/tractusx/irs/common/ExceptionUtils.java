/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.common;

import java.util.Collection;

import io.github.resilience4j.core.functions.Either;

/**
 * Utilities for exception handling
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
        // private constructor, utility class
    }

    /**
     * Adds all exceptions from left side of the given Eithers to the exception.
     *
     * @param eithers   the {@link Either}s
     * @param exception the exception
     * @param <E>       the exception type (left-hand side of {@link Either})
     * @param <T>       the object type (right-hand side of {@link Either})
     */
    public static <E extends Exception, T> void addSuppressedExceptions(final Collection<Either<E, T>> eithers,
            final Exception exception) {
        for (final Either<E, T> either : eithers) {
            if (either.isLeft() && either.getLeft() != null) {
                exception.addSuppressed(either.getLeft());
            }
        }
    }
}
