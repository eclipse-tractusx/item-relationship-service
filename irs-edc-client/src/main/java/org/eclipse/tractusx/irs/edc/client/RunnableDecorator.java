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
package org.eclipse.tractusx.irs.edc.client;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.tractusx.irs.edc.client.exceptions.TimeoutException;

/**
 * Decorates any runnable with additional features.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RunnableDecorator {

    /**
     * Creates a new runnable which enforces a timeout for the embedded action.
     * If the action takes longer than the specified duration, it throws a TimeoutException.
     *
     * @param action         the Runnable to wrap with a timeout
     * @param ttl            the time-to-live; after the duration has passed, a
     *                       TimeoutException will be thrown.
     * @param clock          the clock to use to calculate timeouts
     * @param timeoutMessage the message to include in the TimeoutException
     * @return the wrapped Runnable
     */
    public static Runnable withTimeout(final Runnable action, final Duration ttl, final Clock clock,
            final String timeoutMessage) {
        final LocalTime startTime = LocalTime.now(clock);
        return () -> {
            final LocalTime now = LocalTime.now(clock);
            if (startTime.plus(ttl).isBefore(now)) {
                throw new TimeoutException("'" + timeoutMessage + "' timed out after " + ttl);
            }
            action.run();
        };
    }

    /**
     * Creates a new runnable which catches all exceptions occuring during the action execution and
     * sends them to the specified error handler.
     *
     * @param action  the Runnable to wrap
     * @param onError an error handler for this Runnable
     * @return the wrapped Runnable
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public static Runnable withErrorHandler(final Runnable action, final Consumer<Exception> onError) {
        return () -> {
            try {
                action.run();
            } catch (Exception e) {
                onError.accept(e);
            }
        };
    }

}
