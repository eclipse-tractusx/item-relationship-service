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
package org.eclipse.tractusx.irs.component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.enums.NodeType;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;

/**
 * Tombstone with information about request failure
 */
@Getter
@Builder
@Jacksonized
@Schema(description = "Tombstone with information about request failure")
public class Tombstone {
    private static final NodeType NODE_TYPE = NodeType.TOMBSTONE;
    public static final int CATENA_X_ID_LENGTH = 45;

    @Schema(description = "CATENA-X global asset id in the format urn:uuid:uuid4.",
            example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0", minLength = CATENA_X_ID_LENGTH,
            maxLength = CATENA_X_ID_LENGTH,
            pattern = "^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private final String catenaXId;
    private final String endpointURL;
    private final String businessPartnerNumber;
    private final ProcessingError processingError;
    private final Map<String, Object> policy;

    public static Tombstone from(final String catenaXId, final String endpointURL, final Exception exception,
            final int retryCount, final ProcessStep processStep) {
        return from(catenaXId, endpointURL, exception.getMessage(), retryCount, processStep);
    }

    public static Tombstone from(final String catenaXId, final String endpointURL, final Exception exception,
            final int retryCount, final ProcessStep processStep, final String businessPartnerNumber,
            final Map<String, Object> policy) {

        return Tombstone.builder()
                        .endpointURL(endpointURL)
                        .catenaXId(catenaXId)
                        .processingError(withProcessingError(processStep, retryCount, exception.getMessage()))
                        .businessPartnerNumber(businessPartnerNumber)
                        .policy(policy)
                        .build();
    }

    public static Tombstone from(final String catenaXId, final String endpointURL, final String errorDetails,
            final int retryCount, final ProcessStep processStep) {

        return Tombstone.builder()
                        .endpointURL(endpointURL)
                        .catenaXId(catenaXId)
                        .processingError(withProcessingError(processStep, retryCount, errorDetails))
                        .build();
    }

    public static Tombstone from(final String globalAssetId, final String endpointURL, final Throwable exception,
            final Throwable[] suppressed, final int retryCount, final ProcessStep processStep) {
        final ProcessingError processingError =
                        withProcessingError(processStep, retryCount, exception.getMessage(), suppressed);
        //                hasSuppressedExceptions(exception)
        //                        ? withProcessingError(processStep, retryCount, exception.getMessage(), suppressed)
        //                        : withProcessingError(processStep, retryCount, exception.getMessage());
        return Tombstone.builder()
                        .endpointURL(endpointURL)
                        .catenaXId(globalAssetId)
                        .processingError(processingError)
                        .build();
    }

    private static ProcessingError withProcessingError(final ProcessStep processStep, final int retryCount,
            final String message, final Throwable... suppressed) {
        final List<String> rootCauses = Arrays.stream(suppressed).flatMap(Tombstone::getErrorMessages).toList();

        return ProcessingError.builder()
                              .withProcessStep(processStep)
                              .withRetryCounter(retryCount)
                              .withLastAttempt(ZonedDateTime.now(ZoneOffset.UTC))
                              .withErrorDetail(message)
                              .withRootCauses(rootCauses)
                              .build();
    }

    private static Stream<String> getErrorMessages(final Throwable throwable) {
        final Throwable cause = throwable.getCause();
        if (cause != null && hasSuppressedExceptions(cause)) {
            return Arrays.stream(throwable.getCause().getSuppressed()).map(Throwable::getMessage);
        }
        return Stream.of(throwable.getMessage());
    }

    private static ProcessingError withProcessingError(final ProcessStep processStep, final int retryCount,
            final String exception) {
        return ProcessingError.builder()
                              .withProcessStep(processStep)
                              .withRetryCounter(retryCount)
                              .withLastAttempt(ZonedDateTime.now(ZoneOffset.UTC))
                              .withErrorDetail(exception)
                              .build();
    }

    private static boolean hasSuppressedExceptions(final Throwable exception) {
        return exception.getSuppressed().length > 0;
    }
}
