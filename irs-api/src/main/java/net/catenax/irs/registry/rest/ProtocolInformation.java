//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.registry.rest;

import lombok.Data;

/**
 *
 */
@Data
class ProtocolInformation {

    /**
     *
     */
    private String endpointAddress;
    /**
     *
     */
    private String endpointProtocol;
    /**
     *
     */
    private String endpointProtocolVersion;
    /**
     *
     */
    private String subprotocol;
    /**
     *
     */
    private String subprotocolBody;
    /**
     *
     */
    private String subprotocolBodyEncoding;

}
