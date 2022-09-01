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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * ProtocolInformation
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
public class ProtocolInformation {

    @Schema(description = "Uniform resource identifier of endpoint.",
            example = "https://catena-x.net/vehicle/basedetails/", implementation = java.net.URI.class)
    private String endpointAddress;

    @Schema(description = "Protocol used to access the endpoint.", example = "HTTP or HTTPS",
            implementation = String.class)
    private String endpointProtocol;

    @Schema(description = "Protocol version.", example = "1.0", implementation = String.class)
    private String enpointProtocolVersion;
}
