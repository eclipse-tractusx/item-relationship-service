//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.registry.domain;

import lombok.Data;

/**
 * ProtocolInformation
 */
@Data
class ProtocolInformation {

    /**
     * endpointAddress
     */
    private String endpointAddress;
    /**
     * endpointProtocol
     */
    private String endpointProtocol;
    /**
     * endpointProtocolVersion
     */
    private String endpointProtocolVersion;
    /**
     * subprotocol
     */
    private String subprotocol;
    /**
     * subprotocolBody
     */
    private String subprotocolBody;
    /**
     * subprotocolBodyEncoding
     */
    private String subprotocolBodyEncoding;

}
