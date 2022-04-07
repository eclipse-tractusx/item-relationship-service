//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.connector.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * JSON payload for a connector request to assemble a composite Parts Tree.
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
@ExcludeFromCodeCoverageGeneratedReport
public class JobsTreeRequest {
    /**
     * Parts Tree API Request.
     */
    @Valid
    @NotNull
    private JobsTreeByCatenaXIdRequest byObjectIdRequest;
}

