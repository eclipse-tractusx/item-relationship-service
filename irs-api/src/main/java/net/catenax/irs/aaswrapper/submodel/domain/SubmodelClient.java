//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.web.bind.annotation.GetMapping;

/**
 * Submodel Rest Client
 */
interface SubmodelClient {

    /**
     * @return Returns the Submodel
     */
    @GetMapping(value = "/submodel", consumes = APPLICATION_JSON_VALUE)
    Object getSubmodel();

}
