//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.enums.NodeType;

/**
 * Tombstone with information about request failure
 */
@Getter
@Builder
@Jacksonized
public class Tombstone {
    private static final NodeType NODE_TYPE = NodeType.TOMBSTONE;
    private final String catenaXId;
    private final String endpointURL;
    private final ProcessingError processingError;

    public static Tombstone from(final String catenaXId, final String endpointURL, final Exception exception,
            final int retryCount) {
        final ProcessingError processingError = ProcessingError.builder()
                                                               .withRetryCounter(retryCount)
                                                               .withLastAttempt(ZonedDateTime.now(ZoneOffset.UTC))
                                                               .withErrorDetail(exception.getMessage())
                                                               .build();
        return Tombstone.builder()
                        .endpointURL(endpointURL)
                        .catenaXId(catenaXId)
                        .processingError(processingError)
                        .build();
    }
}
