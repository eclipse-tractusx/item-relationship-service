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

import java.util.List;

import lombok.Data;

/**
 *
 */
@Data
class Descriptor {

    /**
     *
     */
    private List<Endpoint> endpoints;

}
