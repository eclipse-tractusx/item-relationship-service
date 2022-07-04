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

import java.time.ZonedDateTime;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 *
 */
@Data
@Jacksonized
class ChildData {

    /**
     * createdOn
     */
    private ZonedDateTime assembledOn;

    /**
     * quantity
     */
    private Quantity quantity;

    /**
     * lastModifiedOn
     */
    private ZonedDateTime lastModifiedOn;

    /**
     * lifecycleContext
     */
    private LifecycleContextCharacteristic lifecycleContext;

    /**
     * childCatenaXId
     */
    private String childCatenaXId;

}
