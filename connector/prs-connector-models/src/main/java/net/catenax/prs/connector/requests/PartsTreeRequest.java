//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * JSON payload for a connector request to assemble a composite Parts Tree.
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
public class PartsTreeRequest {
    /**
     * Parts Tree API Request.
     */
    @Valid
    @NotNull
    private PartsTreeByObjectIdRequest byObjectIdRequest;
}
