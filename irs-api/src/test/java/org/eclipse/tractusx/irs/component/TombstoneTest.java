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
package org.eclipse.tractusx.irs.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import io.github.resilience4j.retry.RetryRegistry;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.junit.jupiter.api.Test;

class TombstoneTest {

    Tombstone tombstone;

    @Test
    void fromTombstoneTest() {
        // arrange
        String catenaXId = "5e3e9060-ba73-4d5d-a6c8-dfd5123f4d99";
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Some funny error occur");
        String endPointUrl = "http://localhost/dummy/interfaceinformation/urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        ProcessingError processingError = ProcessingError.builder()
                                                         .withProcessStep(ProcessStep.SUBMODEL_REQUEST)
                                                         .withRetryCounter(RetryRegistry.ofDefaults()
                                                                                        .getDefaultConfig()
                                                                                        .getMaxAttempts())
                                                         .withLastAttempt(ZonedDateTime.now(ZoneOffset.UTC))
                                                         .withErrorDetail("Some funny error occur")
                                                         .build();

        Tombstone expectedTombstone = Tombstone.builder()
                                               .catenaXId(catenaXId)
                                               .endpointURL(endPointUrl)
                                               .processingError(processingError)
                                               .build();

        //act
        tombstone = Tombstone.from(catenaXId, endPointUrl, illegalArgumentException,
                RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts(), ProcessStep.SUBMODEL_REQUEST);

        // assert
        assertThat(tombstone).isNotNull();
        assertThat(tombstone.getProcessingError().getErrorDetail()).isEqualTo(processingError.getErrorDetail());
        assertThat(tombstone.getProcessingError().getRetryCounter()).isEqualTo(processingError.getRetryCounter());
        assertThat(zonedDateTimeExcerpt(tombstone.getProcessingError().getLastAttempt())).isEqualTo(
                zonedDateTimeExcerpt(processingError.getLastAttempt()));
        assertThat(tombstone.getCatenaXId()).isEqualTo(expectedTombstone.getCatenaXId());
        assertThat(tombstone.getEndpointURL()).isEqualTo(expectedTombstone.getEndpointURL());
        assertThat(tombstone.getProcessingError().getRetryCounter()).isEqualTo(
                expectedTombstone.getProcessingError().getRetryCounter());
    }

    @Test
    void shouldUseSuppressedExceptionWhenPresent() {
        // arrange
        final String mainExceptionMessage = "Exception occurred.";
        final Exception exception = new Exception(mainExceptionMessage);
        final String suppressedExceptionMessage = "Suppressed Exception which occurred deeper.";
        exception.addSuppressed(new Exception(suppressedExceptionMessage));
        Throwable[] suppressed = exception.getSuppressed();

        // act
        final Tombstone from = Tombstone.from("testId", "testUrl", exception, suppressed, 1,
                ProcessStep.DIGITAL_TWIN_REQUEST);

        // assert
        assertThat(from.getProcessingError().getErrorDetail()).isEqualTo(exception.getMessage());
        assertThat(from.getProcessingError().getRootCauses()).contains(suppressedExceptionMessage);
    }

    @Test
    void shouldUseDeepSuppressedExceptionWhenPresent() {
        // arrange
        final Exception exception = new Exception("Exception occurred.");

        final Exception rootCause = new Exception("Wrapper exception to the root cause");
        rootCause.addSuppressed(new Exception("Root cause of the exception"));

        final Exception suppressedWrapperException = new Exception("Suppressed Exception which was added through Futures.", rootCause);
        exception.addSuppressed(suppressedWrapperException);

        Throwable[] suppressed = exception.getSuppressed();

        // act
        final Tombstone from = Tombstone.from("testId", "testUrl", exception, suppressed, 1,
                ProcessStep.DIGITAL_TWIN_REQUEST);

        // assert
        assertThat(from.getProcessingError().getErrorDetail()).isEqualTo(exception.getMessage());
        assertThat(from.getProcessingError().getRootCauses()).contains("Root cause of the exception");
    }

    @Test
    void shouldUseExceptionMessageWhenSuppressedExceptionNotPresent() {
        // arrange
        final String mainExceptionMessage = "Exception occurred.";
        final Exception exception = new Exception(mainExceptionMessage);
        Throwable[] suppressed = exception.getSuppressed();

        // act
        final Tombstone from = Tombstone.from("testId", "testUrl", exception, suppressed, 1,
                ProcessStep.DIGITAL_TWIN_REQUEST);

        // assert
        assertThat(from.getProcessingError().getErrorDetail()).isEqualTo(exception.getMessage());
        assertThat(from.getProcessingError().getRootCauses()).isEmpty();
    }

    private String zonedDateTimeExcerpt(ZonedDateTime dateTime) {
        return new StringBuilder().append(dateTime.getYear())
                                  .append("-")
                                  .append(dateTime.getMonth())
                                  .append("-")
                                  .append(dateTime.getDayOfMonth())
                                  .append("T")
                                  .append(dateTime.getHour())
                                  .append(":")
                                  .append(dateTime.getMinute())
                                  .append(":")
                                  .append(dateTime.getSecond())
                                  .toString();
    }

}
