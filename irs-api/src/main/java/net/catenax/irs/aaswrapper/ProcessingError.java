//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Processing Error Data Class
 */
@Getter
@AllArgsConstructor
@ToString
public class ProcessingError {
    private final Exception exception;
    private final String errorDetail;
    private final String endpointURL;
    private final int retryCounter;
    private final Instant lastAttempt;

}
