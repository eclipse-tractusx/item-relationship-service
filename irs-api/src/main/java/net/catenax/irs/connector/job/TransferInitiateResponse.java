//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Response for initiated transfers
 */
@Value
@Builder
@AllArgsConstructor
public class TransferInitiateResponse {

    private final String transferId;
    private final ResponseStatus status;
}
