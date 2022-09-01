//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.component.assetadministrationshell;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Endpoint
 */
@Data
@Builder
@Jacksonized
public class Endpoint {

    /**
     * interfaceInformation
     */
    @JsonProperty("interface")
    private String interfaceInformation;
    /**
     * protocolInformation
     */
    private ProtocolInformation protocolInformation;

}
