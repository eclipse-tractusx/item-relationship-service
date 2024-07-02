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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import io.github.resilience4j.core.functions.Either;
import org.junit.jupiter.api.Test;

/**
 * Test for class {@link ExceptionUtils}
 */
class ExceptionUtilsTest {

    @Test
    void addSuppressedExceptionsTest() {

        // ARRANGE
        final List<Either<Exception, String>> eithers = new ArrayList<>();
        eithers.add(Either.left(new RuntimeException("Some runtime exception")));
        eithers.add(Either.right("Some string"));
        eithers.add(Either.left(new IllegalArgumentException("Another exception")));
        eithers.add(Either.left(null));

        final Exception mainException = new Exception("Main exception");

        // ACT
        ExceptionUtils.addSuppressedExceptions(eithers, mainException);

        // ASSERT
        final Throwable[] suppressedExceptions = mainException.getSuppressed();
        assertEquals(2, suppressedExceptions.length); // Expecting two suppressed exceptions
        assertEquals("Some runtime exception", suppressedExceptions[0].getMessage());
        assertEquals("Another exception", suppressedExceptions[1].getMessage());
    }
}