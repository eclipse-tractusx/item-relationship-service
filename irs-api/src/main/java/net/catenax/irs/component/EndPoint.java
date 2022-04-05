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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Communication endpoint
 */
@Schema(description = "Communication endpoint")
@Value
@Builder
@Jacksonized
public class EndPoint {

    @Schema(description = "Communication interface type", example = "Http", implementation = String.class)
    private String interfaceType;

    @Schema(description = "Informaiton to the interface used", implementation = ProtocolInformation.class)
    private ProtocolInformation protocolInformation;

}
