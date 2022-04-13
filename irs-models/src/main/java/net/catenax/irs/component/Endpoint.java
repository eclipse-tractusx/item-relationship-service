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
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Communication endpoint
 */
@Schema(description = "Communication endpoint.")
@Value
@Builder
@Jacksonized
@ExcludeFromCodeCoverageGeneratedReport
public class Endpoint {

    @Schema(description = "Communication interface type.", example = "HTTP", implementation = String.class, defaultValue = "HTTP")
    private String interfaceType;

    @Schema(description = "Information to the interface used.", implementation = ProtocolInformation.class)
    private ProtocolInformation protocolInformation;

}
