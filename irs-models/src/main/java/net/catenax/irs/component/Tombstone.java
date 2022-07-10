//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.component.enums.NodeType;

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

    public static Tombstone from(final String catenaXId, final String endpointURL, final Exception exception) {
        final ProcessingError processingError = ProcessingError.builder()
                                                               .withRetryCounter(0)
                                                               .withLastAttempt(ZonedDateTime.now(ZoneOffset.UTC))
                                                               .withErrorDetail(exception.getMessage())
                                                               .build();
        return Tombstone.builder().endpointURL(endpointURL).catenaXId(catenaXId).processingError(processingError).build();
    }
}
