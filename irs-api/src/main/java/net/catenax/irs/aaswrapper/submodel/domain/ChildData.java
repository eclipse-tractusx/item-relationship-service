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

import java.time.LocalDateTime;

import lombok.Data;

/**
 *
 */
@Data
class ChildData {

    /**
     * createdOn
     */
    private LocalDateTime createdOn;

    /**
     * quantity
     */
    private Quantity quantity;

    /**
     * lastModifiedOn
     */
    private LocalDateTime lastModifiedOn;

    /**
     * lifecycleContext
     */
    private LifecycleContextCharacteristic lifecycleContext;

    /**
     * childCatenaXId
     */
    private String childCatenaXId;

}
